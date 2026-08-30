/*
 * Copyright (C) 2024 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.selection.TriggerConditionTypeSelectionDialog
import io.github.vibhor1102.macrion.core.domain.model.condition.TriggerCondition
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.DialogNavigationButton
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setButtonVisibility
import io.github.vibhor1102.macrion.core.ui.bindings.lists.setEmptyText
import io.github.vibhor1102.macrion.core.ui.bindings.lists.updateState
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogTriggerConditionsBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.UiTriggerCondition
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.OnConditionConfigCompleteListener
import io.github.vibhor1102.macrion.feature.smart.config.ui.copy.condition.ConditionCopyDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.broadcast.BroadcastReceivedConditionDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.counter.CounterReachedConditionDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.timer.TimerReachedConditionDialog

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.trigger.selection.allTriggerConditionChoices


class TriggerConditionListDialog : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.TRIGGER_CONDITION_LIST.name

    private val viewModel: TriggerConditionListViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { triggerConditionsViewModel() },
    )

    private lateinit var viewBinding: DialogTriggerConditionsBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogTriggerConditionsBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                setButtonVisibility(DialogNavigationButton.SAVE, View.GONE)
                setButtonVisibility(DialogNavigationButton.DELETE, View.GONE)
                dialogTitle.setText(R.string.dialog_title_trigger_event)

                buttonDismiss.setDebouncedOnClickListener { back() }
            }

            buttonNew.setDebouncedOnClickListener { showTriggerConditionTypeSelectionDialog() }
            buttonCopy.setDebouncedOnClickListener { showCopyDialog() }

            layoutLoadableList.apply {
                setEmptyText(
                    id = R.string.message_empty_trigger_condition_list_title,
                    secondaryId = R.string.message_empty_trigger_condition_list_desc,
                )

                list.apply {
                    adapter = TriggerConditionAdapter(::showTriggerConditionDialog)
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.canCopyCondition.collect(::updateCopyButton) }
                launch { viewModel.configuredTriggerConditions.collect(::updateConditionList) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.monitorViews(
            createConditionButton = viewBinding.buttonNew,
            closeButton = viewBinding.layoutTopBar.buttonDismiss,
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopViewMonitoring()
    }

    private fun updateCopyButton(visible: Boolean) {
        viewBinding.buttonCopy.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateConditionList(newItems: List<UiTriggerCondition>?) {
        viewBinding.layoutLoadableList.apply {
            updateState(newItems)
            (list.adapter as ListAdapter<UiTriggerCondition, RecyclerView.ViewHolder>).submitList(newItems)
        }
    }

    private fun showTriggerConditionTypeSelectionDialog() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = TriggerConditionTypeSelectionDialog(
                choices = allTriggerConditionChoices(),
                onChoiceSelectedListener = { choice ->
                    showTriggerConditionDialog(viewModel.createNewTriggerCondition(context, choice))
                },
            ),
            hideCurrent = false,
        )
    }

    private fun showCopyDialog() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = ConditionCopyDialog(
                onConditionsCopied = { conditionsSelected ->
                    if (conditionsSelected.size != 1) return@ConditionCopyDialog
                    (conditionsSelected[0] as? TriggerCondition)?.let { condition ->
                        showTriggerConditionDialog(condition)
                    }
                },
                requestTriggerConditions = true,
            ),
        )
    }

    private fun showTriggerConditionDialog(condition: TriggerCondition) {
        viewModel.startConditionEdition(condition)

        val conditionConfigDialogListener: OnConditionConfigCompleteListener by lazy {
            object : OnConditionConfigCompleteListener {
                override fun onConfirmClicked() { viewModel.upsertEditedCondition() }
                override fun onDeleteClicked() { viewModel.removeEditedCondition() }
                override fun onDismissClicked() { viewModel.dismissEditedCondition() }
            }
        }

        val configOverlay = when (condition) {
            is TriggerCondition.OnBroadcastReceived ->
                BroadcastReceivedConditionDialog(conditionConfigDialogListener)
            is TriggerCondition.OnCounterCountReached ->
                CounterReachedConditionDialog(conditionConfigDialogListener)
            is TriggerCondition.OnTimerReached ->
                TimerReachedConditionDialog(conditionConfigDialogListener)
        }

        overlayManager.navigateTo(
            context = context,
            newOverlay = configOverlay,
            hideCurrent = true,
        )
    }
}
