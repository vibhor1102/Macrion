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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity.adapter

import android.view.ViewGroup
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity.EventActivityListItem
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity.EventActivityType
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportActivityRow
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportComposeViewHolder
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportSectionHeader


class EventActivityAdapter : ListAdapter<EventActivityListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is EventActivityListItem.Header -> VIEW_TYPE_HEADER
        is EventActivityListItem.Event -> VIEW_TYPE_EVENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(parent)
            VIEW_TYPE_EVENT -> EventViewHolder(parent)
            else -> error("Unknown Event Activity view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as EventActivityListItem.Header)
            is EventViewHolder -> holder.bind(getItem(position) as EventActivityListItem.Event)
        }
    }
}

private class HeaderViewHolder(parent: ViewGroup) : ReportComposeViewHolder<EventActivityListItem.Header>(
    parent = parent,
    content = { item ->
        val title = when (item.type) {
            EventActivityType.SCREEN -> R.string.item_event_activity_screen_events
            EventActivityType.TRIGGER -> R.string.item_event_activity_trigger_events
        }
        val icon = when (item.type) {
            EventActivityType.SCREEN -> R.drawable.ic_screen_event
            EventActivityType.TRIGGER -> R.drawable.ic_trigger_event
        }
        ReportSectionHeader(stringResource(title), icon)
    },
) {

    fun bind(item: EventActivityListItem.Header) {
        bindComposeItem(item)
    }
}

private class EventViewHolder(parent: ViewGroup) : ReportComposeViewHolder<EventActivityListItem.Event>(
    parent = parent,
    content = { item ->
        val activity = item.activity
        ReportActivityRow(
            name = activity.name,
            count = LocalContext.current.getString(
                R.string.item_event_activity_occurrence_count,
                activity.occurrenceCount,
            ),
            reached = activity.occurrenceCount != 0,
        )
    },
) {

    fun bind(item: EventActivityListItem.Event) {
        bindComposeItem(item)
    }
}

private object DiffCallback : DiffUtil.ItemCallback<EventActivityListItem>() {
    override fun areItemsTheSame(oldItem: EventActivityListItem, newItem: EventActivityListItem): Boolean =
        when {
            oldItem is EventActivityListItem.Header && newItem is EventActivityListItem.Header ->
                oldItem.type == newItem.type
            oldItem is EventActivityListItem.Event && newItem is EventActivityListItem.Event ->
                oldItem.activity.key == newItem.activity.key
            else -> false
        }

    override fun areContentsTheSame(oldItem: EventActivityListItem, newItem: EventActivityListItem): Boolean =
        oldItem == newItem
}

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_EVENT = 1
