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
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.toggleevent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.domain.model.action.toggleevent.EventToggle
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.DialogNavigationButton
import io.github.vibhor1102.macrion.core.ui.bindings.lists.newDividerWithoutHeader
import io.github.vibhor1102.macrion.core.ui.bindings.lists.setEmptyText
import io.github.vibhor1102.macrion.core.ui.bindings.lists.updateState
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.domain.model.action.ToggleEvent
import io.github.vibhor1102.macrion.core.domain.model.event.Event
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setButtonVisibility
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogConfigEventsToggleBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType

class EventTogglesDialog(
    private val toggleEventAction: ToggleEvent,
    private val scenarioEvents: List<Event>,
    private val onConfirmClicked: (List<EventToggle>) -> Unit,
    private val onDismissed: (() -> Unit)? = null,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.EVENT_TOGGLES.name

    /** The view model for this dialog. */
    private val viewModel: EventTogglesViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { eventTogglesViewModel() },
    )

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigEventsToggleBinding

    private lateinit var eventToggleAdapter: EventToggleAdapter

    override fun onCreateView(): ViewGroup {
        viewModel.setDialogArgs(toggleEventAction, scenarioEvents)

        viewBinding = DialogConfigEventsToggleBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_events_toggle)
                setButtonVisibility(DialogNavigationButton.SAVE, View.VISIBLE)
                setButtonVisibility(DialogNavigationButton.DELETE, View.GONE)

                buttonSave.setDebouncedOnClickListener {
                    onConfirmClicked(viewModel.getEditedEventToggleList())
                    back()
                }
                buttonDismiss.setDebouncedOnClickListener {
                    back()
                }
            }

            eventToggleAdapter = EventToggleAdapter(onEventToggleStateChanged = viewModel::changeEventToggleState)

            layoutLoadableList.apply {
                setEmptyText(R.string.message_empty_screen_event_title)

                list.apply {
                    addItemDecoration(newDividerWithoutHeader(context))
                    adapter = eventToggleAdapter
                }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.currentItems.collect(::updateToggleList) }
            }
        }
    }

    override fun onDestroy() {
        onDismissed?.invoke()
        super.onDestroy()
    }

    private fun updateToggleList(toggleList: List<EventTogglesListItem>) {
        viewBinding.layoutLoadableList.updateState(toggleList)
        eventToggleAdapter.submitList(toggleList)
    }
}
