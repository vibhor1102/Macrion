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
package io.github.vibhor1102.macrion.feature.smart.config.ui.scenario.imageevents

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.viewModels
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.domain.model.event.ScreenEvent
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.ui.event.EventDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.copy.event.EventCopyDialog
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.event.UiImageEvent

import kotlinx.coroutines.launch

class ImageEventListContent(appContext: Context) : NavBarDialogContent(appContext) {

    /** View model for this content. */
    private val viewModel: ImageEventListViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { imageEventListViewModel() },
    )

    /** TouchHelper applied to [eventAdapter] allowing to drag and drop the items. */
    private val itemTouchHelper = ItemTouchHelper(ImageEventReorderTouchHelper())

    /** Adapter for the list of events. */
    private lateinit var eventAdapter: ImageEventListAdapter

    override fun floatingActionButtonsAreAvailable(): Boolean = true

    override fun onCreateView(container: ViewGroup): ViewGroup {
        eventAdapter = ImageEventListAdapter(
            itemClickedListener = ::onEventItemClicked,
            itemReorderListener = viewModel::updateEventsPriority,
            itemViewBound = ::onEventItemBound,
        )

        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MacrionTheme { this@ImageEventListContent.Content() } }
        }
    }

    override fun onViewCreated() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.copyButtonIsVisible.collect(::updateCopyButtonVisibility) }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopViewMonitoring()
    }

    override fun onPrimaryFloatingActionButtonClicked() {
        debounceUserInteraction {
            showEventConfigDialog(viewModel.createNewEvent(context))
        }
    }

    override fun onSecondaryFloatingActionButtonClicked() {
        debounceUserInteraction {
            showEventCopyDialog()
        }
    }

    private fun onEventItemClicked(event: ScreenEvent) {
        debounceUserInteraction {
            showEventConfigDialog(event)
        }
    }

    private fun onEventItemBound(index: Int, eventItemView: View?) {
        if (index > 3) return

        if (eventItemView != null) viewModel.monitorEventView(index, eventItemView)
        else viewModel.stopEventViewMonitoring(index)
    }

    @Composable private fun Content() {
        val items = viewModel.eventsItems.collectAsStateWithLifecycle(null).value
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            when {
                items == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                items.isEmpty() -> EmptyState(R.string.message_empty_screen_event_title, R.string.message_empty_screen_event_desc)
                else -> AndroidView(factory = { ctx -> RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = eventAdapter
                    addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                    itemTouchHelper.attachToRecyclerView(this)
                } }, update = { eventAdapter.submitList(items) }, modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Composable private fun EmptyState(title: Int, description: Int) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(context.getString(title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(context.getString(description), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    private fun updateCopyButtonVisibility(isVisible: Boolean) {
        dialogController.floatingActionButtons.secondary.apply {
            if (isVisible) show() else hide()
        }
    }

    /** Opens the dialog allowing the user to copy an event. */
    private fun showEventCopyDialog() {
        dialogController.overlayManager.navigateTo(
            context = context,
            newOverlay = EventCopyDialog(
                requestTriggerEvents = false,
                onEventsSelected = { events ->
                    if (events.size != 1) return@EventCopyDialog
                    (events[0] as? ScreenEvent)?.let { screenEvent ->  showEventConfigDialog(screenEvent) }
                },
            ),
        )
    }

    /** Opens the dialog allowing the user to add a new event. */
    private fun showEventConfigDialog(item: ScreenEvent) {
        viewModel.startEventEdition(item)

        dialogController.overlayManager.navigateTo(
            context = context,
            newOverlay = EventDialog(
                onConfigComplete = viewModel::saveEventEdition,
                onDelete = viewModel::deleteEditedEvent,
                onDismiss = viewModel::dismissEditedEvent,
            ),
            hideCurrent = true,
        )
    }
}
