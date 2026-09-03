/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportDialogTopBar
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportEmptyMessage
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportLoading
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecycler
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecyclerViews
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity.adapter.EventActivityAdapter
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortOption
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortPopup
import io.github.vibhor1102.macrion.feature.smart.debugging.utils.captureScrollPosition
import io.github.vibhor1102.macrion.feature.smart.debugging.utils.restoreScrollPosition

class EventActivityDialog : OverlayDialog(R.style.AppTheme) {
    private val viewModel: EventActivityViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { eventActivityViewModel() },
    )
    private val adapter = EventActivityAdapter()
    private var listViews: ReportRecyclerViews? = null
    private var sortButton: FloatingActionButton? = null
    private var sortPopup: DebugReportSortPopup<EventActivitySort>? = null

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@EventActivityDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun onStop() { sortPopup?.dismiss(); sortPopup = null; super.onStop() }

    @Composable private fun Content() {
        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        LaunchedEffect(state) {
            if (state == EventActivityUiState.NotAvailable) back()
            if (state is EventActivityUiState.Available) {
                val previous = listViews?.recyclerView?.captureScrollPosition()
                adapter.submitList(state.items) {
                    previous?.let { listViews?.recyclerView?.restoreScrollPosition(it) }
                    listViews?.fastScroller?.refresh()
                }
            }
        }
        Surface(Modifier.fillMaxSize().heightIn(min = 600.dp)) {
            Column {
                ReportDialogTopBar(context.getString(R.string.dialog_overlay_title_event_activity), ::back)
                Box(Modifier.weight(1f)) {
                    when (state) {
                        EventActivityUiState.Loading -> ReportLoading()
                        EventActivityUiState.Empty -> ReportEmptyMessage(
                            context.getString(R.string.title_event_activity_empty),
                            context.getString(R.string.desc_event_activity_empty),
                        )
                        is EventActivityUiState.Available -> {
                            ReportRecycler(
                                contentDescriptionRes = R.string.content_desc_event_activity_fast_scroller,
                                modifier = Modifier.fillMaxSize(),
                                bottomPaddingDp = 88,
                                onCreated = { views ->
                                    listViews = views
                                    views.recyclerView.adapter = adapter
                                },
                            )
                            AndroidView(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                                factory = { ctx -> FloatingActionButton(ctx).apply {
                                    setImageResource(R.drawable.ic_sort)
                                    contentDescription = ctx.getString(R.string.content_desc_event_activity_sort)
                                    setOnClickListener { openSortDialog() }
                                    sortButton = this
                                } },
                            )
                        }
                        EventActivityUiState.NotAvailable -> Unit
                    }
                }
            }
        }
    }

    private fun openSortDialog() {
        val anchor = sortButton ?: return
        val selected = viewModel.getSort()
        sortPopup?.dismiss()
        sortPopup = DebugReportSortPopup(
            anchor,
            EventActivitySort.entries.map { sort -> DebugReportSortOption(
                sort,
                when (sort) {
                    EventActivitySort.SCENARIO_ORDER -> R.string.event_activity_sort_scenario_order
                    EventActivitySort.MOST_FREQUENT -> R.string.event_activity_sort_most_frequent
                    EventActivitySort.FIRST_EXECUTION -> R.string.event_activity_sort_first_execution
                },
                sort == selected,
            ) },
            viewModel::setSort,
        ).also { it.show() }
    }
}
