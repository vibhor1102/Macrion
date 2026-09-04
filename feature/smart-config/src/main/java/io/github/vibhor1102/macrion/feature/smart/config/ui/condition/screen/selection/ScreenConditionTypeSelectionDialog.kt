/* Copyright (C) 2024 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.selection

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.TutorialChoiceList

class ScreenConditionTypeSelectionDialog(
    private val choices: List<ScreenConditionTypeChoice>,
    private val onChoiceSelectedListener: (ScreenConditionTypeChoice) -> Unit,
    private val onCancelledListener: (() -> Unit)? = null,
) : OverlayDialog(R.style.AppTheme) {
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.SCREEN_CONDITION_TYPE_SELECTION.name
    private val viewModel: ScreenConditionTypeSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { screenConditionTypeSelectionViewModel() },
    )

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme {
            TutorialChoiceList(
                title = R.string.dialog_title_screen_condition_type, choices = choices, onDismiss = ::cancel,
                onChoiceSelected = { back(); onChoiceSelectedListener(it) },
                onChoiceViewChanged = { choice, view ->
                    if (view != null) viewModel.monitorScreenConditionTypeView(choice, view)
                    else viewModel.stopScreenConditionTypeViewMonitoring(choice)
                },
            )
        } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun onStop() { viewModel.stopAllViewsMonitoring(); super.onStop() }
    private fun cancel() { onCancelledListener?.invoke(); back() }
}
