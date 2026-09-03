/*
 * Copyright (C) 2026 Vibhor Goel
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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.ConditionPerformanceEntry
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.formatAverageDuration
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.formatCount
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.formatPercentage
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.conditions.formatTotalDuration
import kotlinx.coroutines.Job

internal sealed interface ConditionPerformanceListItem {
    data class Condition(val entry: ConditionPerformanceEntry) : ConditionPerformanceListItem
    data object Footer : ConditionPerformanceListItem
}

internal class ConditionPerformanceAdapter(
    private val bitmapProvider: (ScreenCondition.Image, (Bitmap?) -> Unit) -> Job,
) : ListAdapter<ConditionPerformanceListItem, RecyclerView.ViewHolder>(DiffCallback) {

    fun submitEntries(entries: List<ConditionPerformanceEntry>, commitCallback: (() -> Unit)? = null) {
        submitList(entries.map(ConditionPerformanceListItem::Condition) + ConditionPerformanceListItem.Footer, commitCallback)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ConditionPerformanceListItem.Condition -> VIEW_TYPE_CONDITION
        ConditionPerformanceListItem.Footer -> VIEW_TYPE_FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_CONDITION -> ConditionViewHolder(parent, bitmapProvider)
            VIEW_TYPE_FOOTER -> FooterViewHolder(parent)
            else -> error("Unknown condition performance view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ConditionViewHolder) {
            holder.bind((getItem(position) as ConditionPerformanceListItem.Condition).entry)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ConditionViewHolder) holder.unbind()
        super.onViewRecycled(holder)
    }
}

private class ConditionViewHolder(
    parent: ViewGroup,
    private val bitmapProvider: (ScreenCondition.Image, (Bitmap?) -> Unit) -> Job,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {

    private var bitmapLoadingJob: Job? = null
    private var rowState by mutableStateOf<ConditionPerformanceRowState?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { MacrionTheme { rowState?.let { ConditionPerformanceRow(it) } } }
        }
    }

    fun bind(entry: ConditionPerformanceEntry) {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        val context = itemView.context
        val totalTime = context.getString(
            R.string.item_condition_performance_total_time,
            formatTotalDuration(entry.totalDurationNs),
        )
        val fulfilledCount = formatCount(entry.fulfilledCount)
        val checkCount = formatCount(entry.checkCount)
        val fulfilled = context.getString(
            R.string.item_condition_performance_fulfilled,
            fulfilledCount,
            context.resources.getQuantityString(R.plurals.item_condition_performance_time, entry.fulfilledCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
            checkCount,
            context.resources.getQuantityString(R.plurals.item_condition_performance_check, entry.checkCount.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
        )
        val average = formatAverageDuration(entry.totalDurationNs, entry.checkCount)?.let { value ->
            context.getString(R.string.item_condition_performance_average, value)
        } ?: context.getString(R.string.item_condition_performance_average_unavailable)
        rowState = ConditionPerformanceRowState(
            entry = entry,
            totalTime = totalTime,
            fulfilled = fulfilled,
            average = average,
            percentage = formatPercentage(entry.totalDurationNs, entry.totalMeasuredDurationNs),
        )

        val condition = entry.condition
        if (condition is ScreenCondition.Image) {
            bitmapLoadingJob = bitmapProvider(condition) { bitmap ->
                if (rowState?.entry?.condition?.id == condition.id) {
                    rowState = rowState?.copy(bitmap = bitmap, bitmapFailed = bitmap == null)
                }
            }
        }
    }

    fun unbind() {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
    }
}

private class FooterViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(ComposeView(parent.context).apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    setContent { MacrionTheme { ConditionPerformanceFooter() } }
})

private const val VIEW_TYPE_CONDITION = 0
private const val VIEW_TYPE_FOOTER = 1

private object DiffCallback : DiffUtil.ItemCallback<ConditionPerformanceListItem>() {
    override fun areItemsTheSame(
        oldItem: ConditionPerformanceListItem,
        newItem: ConditionPerformanceListItem,
    ): Boolean = when {
        oldItem is ConditionPerformanceListItem.Condition && newItem is ConditionPerformanceListItem.Condition ->
            oldItem.entry.condition.id == newItem.entry.condition.id
        oldItem is ConditionPerformanceListItem.Footer && newItem is ConditionPerformanceListItem.Footer -> true
        else -> false
    }

    override fun areContentsTheSame(
        oldItem: ConditionPerformanceListItem,
        newItem: ConditionPerformanceListItem,
    ): Boolean = oldItem == newItem
}
