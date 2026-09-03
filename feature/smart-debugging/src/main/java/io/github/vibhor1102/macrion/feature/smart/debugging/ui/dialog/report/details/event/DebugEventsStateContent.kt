/* Copyright (C) 2025 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.event

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.viewModels
import io.github.vibhor1102.macrion.core.smart.debugging.domain.model.report.DebugReportEventOccurrence
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportLoadableList
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecyclerViews
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.event.adapter.EventStateAdapter

class DebugEventsStateContent(
    appContext: Context,
    private val scenarioId: Long,
    private val eventOccurrence: DebugReportEventOccurrence,
) : NavBarDialogContent(appContext) {
    private val viewModel: DebugEventsStateContentViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { debugEventStateContentViewModel() },
    )
    private val adapter = EventStateAdapter()
    private var listViews: ReportRecyclerViews? = null

    override fun onCreateView(container: ViewGroup): ViewGroup {
        viewModel.setOccurrence(scenarioId, eventOccurrence)
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { MacrionTheme { this@DebugEventsStateContent.Content() } }
        }
    }
    override fun onViewCreated() = Unit

    @Composable private fun Content() {
        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        val items = (state as? DebugEventsStateContentUiState.Available)?.eventsState
            ?: if (state == DebugEventsStateContentUiState.Empty) emptyList() else null
        LaunchedEffect(items) {
            if (items != null) adapter.submitList(items) { listViews?.fastScroller?.refresh() }
        }
        ReportLoadableList(items, R.string.content_desc_event_occurrence_fast_scroller) { views ->
            listViews = views
            views.recyclerView.adapter = adapter
        }
    }
}
