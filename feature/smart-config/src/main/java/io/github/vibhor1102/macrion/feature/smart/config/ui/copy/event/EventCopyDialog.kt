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
package io.github.vibhor1102.macrion.feature.smart.config.ui.copy.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.ui.bindings.lists.updateState
import io.github.vibhor1102.macrion.core.domain.model.event.Event
import io.github.vibhor1102.macrion.core.ui.bindings.lists.newDividerWithoutHeader
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.CopyDialog
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.copy.fix.event.FixEventsCopyDialog

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType

class EventCopyDialog(
    private val requestTriggerEvents: Boolean,
    private val onEventsSelected: (List<Event>) -> Unit,
) : CopyDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.EVENT_COPY.name

    /** View model for this content. */
    private val viewModel: EventCopyViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { eventCopyModel() },
    )
    /** Adapter displaying the list of events. */
    private lateinit var eventCopyAdapter: EventCopyAdapter

    override val titleRes: Int = R.string.dialog_overlay_title_copy_from
    override val searchHintRes: Int = R.string.search_view_hint_event_copy
    override val emptyRes: Int = R.string.message_empty_copy

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        viewModel.setCopyListType(requestTriggerEvents)
        eventCopyAdapter = EventCopyAdapter(
            onEventSelected = { item -> debounceUserInteraction { onEventClicked(item) } },
            onEventCheckboxClicked = { item -> viewModel.toggleCheckedForCopy(item)}
        )

        viewBinding.layoutLoadableList.list.apply {
            addItemDecoration(newDividerWithoutHeader(context))
            adapter = eventCopyAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateEventList)
            }
        }
    }

    private fun updateEventList(newItems: List<EventCopyItem>?) {
        viewBinding.layoutLoadableList.updateState(newItems)
        eventCopyAdapter.submitList(newItems)
        viewBinding.layoutTopBar.buttonCopy.isEnabled = !newItems.isNullOrEmpty()
    }

    override fun onSearchQueryChanged(newText: String?) {
        viewModel.updateSearchQuery(newText)
    }

    private fun onEventClicked(event: Event) {
        viewModel.toggleCheckedForCopy(event)
    }

    override fun onCopyClicked() {
        val copyEvents = viewModel.getEventsCopy()
        if (viewModel.eventsCopyShouldWarnUser(copyEvents)) {
            showCopyFixDialog(copyEvents)
        } else {
            notifySelectionAndDestroy(copyEvents)
        }
    }

    private fun showCopyFixDialog(eventsToCopy: List<Event>) {
        overlayManager.navigateTo(
            context = context,
            hideCurrent = false,
            newOverlay = FixEventsCopyDialog(
                eventsToCopy = eventsToCopy,
                onFixConfirmed = ::notifySelectionAndDestroy,
            )
        )
    }

    private fun notifySelectionAndDestroy(eventsToCopy: List<Event>) {
        viewModel.saveCopyEvents(eventsToCopy)
        back()
        onEventsSelected(eventsToCopy)
    }
}
