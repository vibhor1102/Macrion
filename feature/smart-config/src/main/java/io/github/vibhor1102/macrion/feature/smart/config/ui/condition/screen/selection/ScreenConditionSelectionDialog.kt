/* Copyright (C) 2026 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.selection

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.TutorialClickAnchor
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toEffectDescription
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toNaturalDisplayString
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.UiScreenCondition

class ScreenConditionSelectionDialog(
    private val conditionList: List<UiScreenCondition>,
    private val onConditionSelected: (ScreenCondition) -> Unit,
) : OverlayDialog(R.style.ScenarioConfigTheme) {
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.SCREEN_CONDITION_SELECTION.name
    private val viewModel: ScreenConditionSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { screenConditionSelectionViewModel() },
    )

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@ScreenConditionSelectionDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun onStop() { viewModel.stopViewMonitoring(); super.onStop() }

    @Composable private fun Content() {
        Surface(Modifier.fillMaxWidth().heightIn(max = 640.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = ::back) { Icon(painterResource(R.drawable.ic_cancel), null) }
                    Text(context.getString(R.string.dialog_title_condition_selection), Modifier.weight(1f).padding(8.dp),
                        style = MaterialTheme.typography.titleLarge)
                }
                if (conditionList.isEmpty()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(context.getString(R.string.message_empty_screen_condition_list_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else LazyVerticalGrid(
                    columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    itemsIndexed(conditionList, key = { _, item -> item.condition.id.toString() }) { index, item ->
                        Box(Modifier.fillMaxWidth()) {
                            ConditionCard(item) { onConditionSelected(item.condition); back() }
                            if (index == 0) TutorialClickAnchor(
                                onViewChanged = { view -> if (view != null) viewModel.monitorFirstConditionItemView(view)
                                    else viewModel.stopViewMonitoring() },
                                onClick = { onConditionSelected(item.condition); back() },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable private fun ConditionCard(item: UiScreenCondition, onClick: () -> Unit) {
        Card(Modifier.fillMaxWidth().padding(6.dp).clickable(onClick = onClick), shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.height(100.dp)) {
                ConditionPreview(item, Modifier.fillMaxWidth().height(55.dp))
                Column(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(item.name, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(item.shouldBeVisibleIconRes), null, Modifier.size(14.dp))
                        if (item.condition is ScreenCondition.Image) Icon(painterResource(item.detectionTypeIconRes), null, Modifier.size(14.dp))
                        Text(item.thresholdText, style = MaterialTheme.typography.bodySmall,
                            color = if (item.haveError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    @Composable private fun ConditionPreview(item: UiScreenCondition, modifier: Modifier) {
        val condition = item.condition
        var bitmap by remember(condition.id) { mutableStateOf<Bitmap?>(null) }
        DisposableEffect(condition.id) {
            val job = if (condition is ScreenCondition.Image) viewModel.getConditionBitmap(condition) { bitmap = it } else null
            onDispose { job?.cancel() }
        }
        Box(modifier.clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            when (condition) {
                is ScreenCondition.Color -> Box(Modifier.size(40.dp).background(Color(condition.color), CircleShape))
                is ScreenCondition.Image -> bitmap?.let {
                    Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } ?: Icon(painterResource(if (item.haveError) R.drawable.ic_cancel else item.iconRes), null, Modifier.size(40.dp))
                is ScreenCondition.Number -> Text(condition.comparisonOperation.toEffectDescription(context,
                    operand = condition.counterValue.toNaturalDisplayString()), Modifier.padding(6.dp),
                    style = MaterialTheme.typography.bodySmall, maxLines = 2)
                is ScreenCondition.Text -> Text(condition.text, Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
