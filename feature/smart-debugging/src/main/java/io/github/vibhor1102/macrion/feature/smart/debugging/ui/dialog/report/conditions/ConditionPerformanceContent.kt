/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.viewModels
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportEmptyMessage
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportLoading
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecycler
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.ReportRecyclerViews
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.adapter.ConditionPerformanceAdapter
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortOption
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortPopup
import io.github.vibhor1102.macrion.feature.smart.debugging.utils.captureScrollPosition
import io.github.vibhor1102.macrion.feature.smart.debugging.utils.restoreScrollPosition

class ConditionPerformanceContent(appContext: Context) : NavBarDialogContent(appContext) {
    private val viewModel: ConditionPerformanceViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { conditionPerformanceViewModel() },
    )
    private val adapter = ConditionPerformanceAdapter { condition, callback ->
        viewModel.getConditionBitmap(condition, callback)
    }
    private var listViews: ReportRecyclerViews? = null
    private var sortPopup: DebugReportSortPopup<ConditionPerformanceSort>? = null

    override fun floatingActionButtonsAreAvailable() = true
    override fun primaryFloatingActionButtonIcon() = R.drawable.ic_sort
    override fun onCreateView(container: ViewGroup): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { Content() } }
    }
    override fun onViewCreated() = Unit
    override fun onStart() {
        dialogController.floatingActionButtons.primary.contentDescription =
            context.getString(R.string.content_desc_condition_performance_sort)
    }
    override fun onStop() { sortPopup?.dismiss(); sortPopup = null }

    @Composable private fun Content() {
        val state = viewModel.uiState.collectAsStateWithLifecycle().value
        val overlayColor = colorResource(R.color.overlayViewPrimary)
        LaunchedEffect(state) {
            dialogController.floatingActionButtons.root.visibility =
                if (state is ConditionPerformanceUiState.Available) View.VISIBLE else View.GONE
            if (state is ConditionPerformanceUiState.Available) {
                val previous = listViews?.recyclerView?.captureScrollPosition()
                adapter.submitEntries(state.entries) {
                    previous?.let { listViews?.recyclerView?.restoreScrollPosition(it) }
                    listViews?.fastScroller?.refresh()
                }
            } else if (state is ConditionPerformanceUiState.NotAvailable) adapter.submitList(emptyList())
        }
        when (state) {
            ConditionPerformanceUiState.Loading -> ReportLoading(overlayColor)
            ConditionPerformanceUiState.NotAvailable -> ReportEmptyMessage(
                context.getString(R.string.title_condition_performance_unavailable),
                contentColor = overlayColor,
            )
            is ConditionPerformanceUiState.Available -> ReportRecycler(
                R.string.content_desc_condition_performance_fast_scroller,
                Modifier.fillMaxSize(),
                bottomPaddingDp = 88,
                onCreated = { views -> listViews = views; views.recyclerView.adapter = adapter },
            )
        }
    }

    override fun onPrimaryFloatingActionButtonClicked() {
        val selected = viewModel.getSort()
        sortPopup?.dismiss()
        sortPopup = DebugReportSortPopup(
            dialogController.floatingActionButtons.primary,
            ConditionPerformanceSort.entries.map { sort -> DebugReportSortOption(
                sort,
                when (sort) {
                    ConditionPerformanceSort.TOTAL_TIME -> R.string.condition_performance_sort_total_time
                    ConditionPerformanceSort.AVERAGE_PER_CHECK -> R.string.condition_performance_sort_average
                    ConditionPerformanceSort.CHECKS -> R.string.condition_performance_sort_checks
                    ConditionPerformanceSort.SCENARIO_ORDER -> R.string.condition_performance_sort_scenario_order
                },
                sort == selected,
            ) },
            viewModel::setSort,
        ).also { it.show() }
    }
}
