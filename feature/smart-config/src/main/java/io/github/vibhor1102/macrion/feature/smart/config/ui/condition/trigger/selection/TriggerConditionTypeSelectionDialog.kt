/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.selection

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose.TutorialClickAnchor

class TriggerConditionTypeSelectionDialog(
    private val choices: List<TriggerConditionTypeChoice>,
    private val onChoiceSelectedListener: (TriggerConditionTypeChoice) -> Unit,
    private val onCancelledListener: (() -> Unit)? = null,
) : OverlayDialog(R.style.AppTheme) {
    private val viewModel: TriggerConditionTypeSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java, creator = { triggerConditionTypeSelectionViewModel() })
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.TRIGGER_CONDITION_TYPE_SELECTION.name

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@TriggerConditionTypeSelectionDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun onStop() { viewModel.stopAllViewsMonitoring(); super.onStop() }

    @Composable private fun Content() {
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = ::cancel) { Icon(painterResource(R.drawable.ic_cancel), null) }
                    Text(stringResource(R.string.dialog_title_trigger_condition_type), Modifier.weight(1f).padding(8.dp),
                        style = MaterialTheme.typography.titleLarge)
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(choices, key = { it::class.simpleName.orEmpty() }) { choice ->
                        Box(Modifier.fillMaxWidth()) {
                            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    choice.iconId?.let { Icon(painterResource(it), null, Modifier.size(32.dp)) }
                                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                                        Text(stringResource(choice.title), style = MaterialTheme.typography.titleSmall)
                                        choice.description?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                }
                            }
                            TutorialClickAnchor(
                                onViewChanged = { view -> if (view != null) viewModel.monitorTriggerConditionTypeView(choice, view)
                                    else viewModel.stopTriggerConditionTypeViewMonitoring(choice) },
                                onClick = { back(); onChoiceSelectedListener(choice) },
                            )
                        }
                    }
                }
            }
        }
    }
    private fun cancel() { onCancelledListener?.invoke(); back() }
}
