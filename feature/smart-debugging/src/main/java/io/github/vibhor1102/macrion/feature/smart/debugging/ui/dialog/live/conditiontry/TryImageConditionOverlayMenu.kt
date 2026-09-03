/* Copyright (C) 2025 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.live.conditiontry

import android.content.res.ColorStateList
import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.slider.Slider
import io.github.vibhor1102.macrion.core.base.isStopScenarioKey
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.menu.OverlayMenu
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.domain.model.scenario.Scenario
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.live.createDebugOverlayMenu
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.live.uistate.ScreenConditionResultUiState
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.view.DebugOverlayView
import kotlinx.coroutines.launch

class TryImageConditionOverlayMenu(
    private val scenario: Scenario,
    private val imageCondition: ScreenCondition,
    private val onNewThresholdSelected: (Int) -> Unit,
) : OverlayMenu() {
    private val viewModel: TryImageConditionViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { tryImageConditionViewModel() },
    )
    private var result by mutableStateOf<ScreenConditionResultUiState?>(null)
    private var thresholdText by mutableStateOf("")

    override fun onCreateMenu(layoutInflater: LayoutInflater): ViewGroup =
        createDebugOverlayMenu(context, contentWidthDp = 287, contentHeightDp = 152) { ResultPanel() }
    override fun onCreateOverlayView(): View = DebugOverlayView(context)

    override fun onStart() {
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { viewModel.displayResults.collect { state ->
                result = state
                (screenOverlayView as? DebugOverlayView)?.setResults(state?.let(::listOf) ?: emptyList())
            } }
            launch { viewModel.thresholdText.collect { thresholdText = it } }
        } }
        viewModel.startTry(context, scenario, imageCondition)
    }
    override fun onStop() { viewModel.stopTry(); onNewThresholdSelected(viewModel.getSelectedThreshold()) }
    override fun getWindowMaximumSize(backgroundView: ViewGroup): Size =
        super.getWindowMaximumSize(backgroundView).let { Size(
            it.width + context.resources.getDimensionPixelSize(R.dimen.overlay_debug_text_width), it.height,
        ) }
    override fun onMenuItemClicked(viewId: Int) {
        if (viewId == R.id.btn_back) { viewModel.stopTry(); back() }
    }
    override fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        if (!keyEvent.isStopScenarioKey()) return false
        if (keyEvent.action == KeyEvent.ACTION_DOWN) { viewModel.stopTry(); back() }
        return true
    }

    @Composable private fun ResultPanel() {
        val textColor = colorResource(R.color.textTitle)
        val controlColor = colorResource(R.color.overlayMenuButtons)
        Column(Modifier.width(287.dp).height(152.dp).padding(start = 8.dp, end = 4.dp, top = 12.dp, bottom = 8.dp)) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                ResultValue(context.getString(R.string.overlay_title_results), result?.resultText.orEmpty(), Modifier.weight(1f))
                Column(Modifier.fillMaxHeight().width(1.dp).background(controlColor)) {}
                ResultValue(context.getString(R.string.overlay_title_threshold), thresholdText, Modifier.weight(1f))
            }
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                factory = { ctx -> Slider(ctx).apply {
                    valueFrom = MIN_THRESHOLD
                    valueTo = MAX_THRESHOLD
                    stepSize = 1f
                    value = imageCondition.threshold.toFloat()
                    thumbHeight = (32 * resources.displayMetrics.density).toInt()
                    trackHeight = (12 * resources.displayMetrics.density).toInt()
                    trackTintList = ColorStateList.valueOf(controlColor.toArgb())
                    thumbTintList = ColorStateList.valueOf(controlColor.toArgb())
                    addOnChangeListener { _, sliderValue, fromUser ->
                        if (fromUser) viewModel.setThreshold(sliderValue.toInt())
                    }
                } },
            )
        }
    }

    @Composable private fun ResultValue(title: String, value: String, modifier: Modifier) {
        val color = colorResource(R.color.textTitle)
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = color, maxLines = 1, textAlign = TextAlign.Center)
            Text(value, color = color, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}
