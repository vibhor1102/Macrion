/* Copyright (C) 2026 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.timeline.filter.events

import android.view.ViewGroup
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportDialogTopBar
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecycler
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecyclerViews
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.timeline.filter.DebugReportTimelineFilter

class FilteredEventsSelectorDialog(
    private val eventsFilter: DebugReportTimelineFilter.Events,
    private val onFilteredIdsChanged: (DebugReportTimelineFilter.Events) -> Unit,
) : OverlayDialog(R.style.AppTheme) {
    private val viewModel: FilteredEventsSelectorViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { filteredEventsSelectorViewModel() },
    )
    private val adapter = FilteredEventsSelectorAdapter(viewModel::setFilteredState)
    private var listViews: ReportRecyclerViews? = null

    override fun onCreateView(): ViewGroup {
        viewModel.setEventFilter(eventsFilter)
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MacrionTheme { this@FilteredEventsSelectorDialog.Content() } }
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit

    @Composable private fun Content() {
        val items = viewModel.eventsItems.collectAsStateWithLifecycle(initialValue = emptyList()).value
        LaunchedEffect(items) {
            listViews?.fastScroller?.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            adapter.submitList(items) { listViews?.fastScroller?.refresh() }
        }
        Surface(Modifier.fillMaxSize().heightIn(min = 600.dp)) {
            Column {
                ReportDialogTopBar(
                    title = "",
                    onDismiss = ::back,
                    onSave = {
                        onFilteredIdsChanged(viewModel.getFilter())
                        back()
                    },
                )
                ReportRecycler(
                    contentDescriptionRes = R.string.content_desc_filtered_events_fast_scroller,
                    modifier = Modifier.weight(1f),
                    onCreated = { views ->
                        listViews = views
                        views.recyclerView.adapter = adapter
                        views.fastScroller.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                    },
                )
            }
        }
    }
}
