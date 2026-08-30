/*
 * Copyright (C) 2026 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.selection

import android.view.View

import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.MultiChoiceDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint

class TriggerConditionTypeSelectionDialog(
    choices: List<TriggerConditionTypeChoice>,
    onChoiceSelectedListener: (TriggerConditionTypeChoice) -> Unit,
    onCancelledListener: (() -> Unit)? = null,
) : MultiChoiceDialog<TriggerConditionTypeChoice>(
    theme = R.style.AppTheme,
    dialogTitleText = R.string.dialog_title_trigger_condition_type,
    choices = choices,
    onChoiceSelected = onChoiceSelectedListener,
    onCanceled = onCancelledListener,
) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.TRIGGER_CONDITION_TYPE_SELECTION.name

    private val viewModel: TriggerConditionTypeSelectionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { triggerConditionTypeSelectionViewModel() },
    )

    override fun onStop() {
        super.onStop()
        viewModel.stopAllViewsMonitoring()
    }

    override fun onChoiceViewBound(choice: TriggerConditionTypeChoice, view: View?) {
        if (view != null) viewModel.monitorTriggerConditionTypeView(choice, view)
        else viewModel.stopTriggerConditionTypeViewMonitoring(choice)
    }
}
