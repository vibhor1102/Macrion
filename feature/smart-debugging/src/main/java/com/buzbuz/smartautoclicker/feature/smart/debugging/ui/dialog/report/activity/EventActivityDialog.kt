/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.feature.smart.debugging.R
import com.buzbuz.smartautoclicker.feature.smart.debugging.databinding.DialogEventActivityBinding
import com.buzbuz.smartautoclicker.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.activity.adapter.EventActivityAdapter
import com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortOption
import com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.sort.DebugReportSortPopup
import com.buzbuz.smartautoclicker.feature.smart.debugging.utils.captureScrollPosition
import com.buzbuz.smartautoclicker.feature.smart.debugging.utils.restoreScrollPosition

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class EventActivityDialog : OverlayDialog(R.style.AppTheme) {

    private val viewModel: EventActivityViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { eventActivityViewModel() },
    )

    private lateinit var binding: DialogEventActivityBinding
    private val adapter = EventActivityAdapter()
    private var sortPopup: DebugReportSortPopup<EventActivitySort>? = null

    override fun onCreateView(): ViewGroup {
        binding = DialogEventActivityBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_overlay_title_event_activity)
                buttonDelete.visibility = View.GONE
                buttonSave.visibility = View.GONE
                buttonDismiss.setDebouncedOnClickListener { back() }
            }

            list.adapter = adapter
            fastScroller.attachToRecyclerView(list)

            floatingActionButtons.apply {
                secondary.visibility = View.GONE
                primaryBadge.visibility = View.GONE
                primary.setImageResource(R.drawable.ic_sort)
                primary.contentDescription = context.getString(R.string.content_desc_event_activity_sort)
                primary.setOnClickListener { openSortDialog() }
            }
        }
        return binding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::updateUiState)
            }
        }
    }

    override fun onStop() {
        sortPopup?.dismiss()
        sortPopup = null
        super.onStop()
    }

    private fun updateUiState(uiState: EventActivityUiState) {
        when (uiState) {
            EventActivityUiState.Loading -> showLoading()
            EventActivityUiState.NotAvailable -> back()
            EventActivityUiState.Empty -> showEmpty()
            is EventActivityUiState.Available -> showActivity(uiState)
        }
    }

    private fun showLoading() = binding.apply {
        loading.visibility = View.VISIBLE
        empty.visibility = View.GONE
        list.visibility = View.GONE
        fastScroller.visibility = View.GONE
        floatingActionButtons.root.visibility = View.GONE
    }

    private fun showEmpty() = binding.apply {
        loading.visibility = View.GONE
        empty.visibility = View.VISIBLE
        list.visibility = View.GONE
        fastScroller.visibility = View.GONE
        floatingActionButtons.root.visibility = View.GONE
    }

    private fun showActivity(uiState: EventActivityUiState.Available) = binding.apply {
        loading.visibility = View.GONE
        empty.visibility = View.GONE
        list.visibility = View.VISIBLE
        fastScroller.visibility = View.VISIBLE
        floatingActionButtons.root.visibility = View.VISIBLE
        val previousScrollPosition = list.captureScrollPosition()
        adapter.submitList(uiState.items) {
            list.restoreScrollPosition(previousScrollPosition)
            list.postOnAnimation {
                list.restoreScrollPosition(previousScrollPosition)
                fastScroller.refresh()
            }
        }
    }

    private fun openSortDialog() {
        val selectedSort = viewModel.getSort()
        sortPopup?.dismiss()
        sortPopup = DebugReportSortPopup(
            anchor = binding.floatingActionButtons.primary,
            options = EventActivitySort.entries.map { sort ->
                DebugReportSortOption(
                    value = sort,
                    titleRes = when (sort) {
                        EventActivitySort.SCENARIO_ORDER -> R.string.event_activity_sort_scenario_order
                        EventActivitySort.MOST_FREQUENT -> R.string.event_activity_sort_most_frequent
                        EventActivitySort.FIRST_EXECUTION -> R.string.event_activity_sort_first_execution
                    },
                    selected = sort == selectedSort,
                )
            },
            onSelected = viewModel::setSort,
        ).also { it.show() }
    }
}
