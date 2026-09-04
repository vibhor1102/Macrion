/* Copyright (C) 2024 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.selection

import android.view.View
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

class ActionTypeSelectionDialog(
    private val choices: List<ActionTypeChoice>,
    private val onChoiceSelectedListener: (ActionTypeChoice) -> Unit,
    private val onCancelledListener: (() -> Unit)? = null,
) : OverlayDialog(R.style.ScenarioConfigTheme) {
    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.ACTION_TYPE_SELECTION.name
    private val viewModel: ActionTypeSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { actionTypeSelectionViewModel() },
    )

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme {
            TutorialChoiceList(
                title = R.string.dialog_title_action_type, choices = choices, onDismiss = ::cancel,
                onChoiceSelected = { back(); onChoiceSelectedListener(it) },
                onChoiceViewChanged = ::monitorChoice,
            )
        } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun onStop() {
        viewModel.stopViewCreateClickMonitoring()
        viewModel.stopViewToggleEventMonitoring()
        viewModel.stopViewCounterMonitoring()
        super.onStop()
    }

    private fun monitorChoice(choice: ActionTypeChoice, view: View?) {
        when (choice) {
            ActionTypeChoice.Click -> if (view != null) viewModel.monitorCreateClickView(view)
                else viewModel.stopViewCreateClickMonitoring()
            ActionTypeChoice.ToggleEvent -> if (view != null) viewModel.monitorCreateToggleEventView(view)
                else viewModel.stopViewToggleEventMonitoring()
            ActionTypeChoice.ChangeCounter -> if (view != null) viewModel.monitorCreateCounterView(view)
                else viewModel.stopViewCounterMonitoring()
            else -> Unit
        }
    }
    private fun cancel() { onCancelledListener?.invoke(); back() }
}
