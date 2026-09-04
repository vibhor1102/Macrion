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
package io.github.vibhor1102.macrion.feature.smart.config.ui.scenario.triggerevents

import android.view.ViewGroup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.domain.model.event.TriggerEvent
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.event.UiTriggerEvent
import io.github.vibhor1102.macrion.feature.smart.config.ui.scenario.common.EventListRow

/**
 * Adapter displaying a list of trigger events.
 * @param itemClickedListener listener called when the user clicks on an item.
 */
class TriggerEventListAdapter(
    private val itemClickedListener: (TriggerEvent) -> Unit,
) : ListAdapter<UiTriggerEvent, TriggerEventViewHolder>(TriggerEventDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriggerEventViewHolder =
        TriggerEventViewHolder(parent)

    override fun onBindViewHolder(holder: TriggerEventViewHolder, position: Int) {
        holder.bindEvent(getItem(position), itemClickedListener)
    }
}

/** DiffUtil Callback comparing two ActionItem when updating the [TriggerEventListAdapter] list. */
object TriggerEventDiffUtilCallback: DiffUtil.ItemCallback<UiTriggerEvent>() {
    override fun areItemsTheSame(oldItem: UiTriggerEvent, newItem: UiTriggerEvent): Boolean =
        oldItem.event.id == newItem.event.id

    override fun areContentsTheSame(oldItem: UiTriggerEvent, newItem: UiTriggerEvent): Boolean =
        oldItem == newItem
}

/**
 * View holder displaying a click in the [TriggerEventListAdapter].
 * @param holderViewBinding the view binding for this item.
 */
class TriggerEventViewHolder(
    parent: ViewGroup,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {
    private var itemState by mutableStateOf<UiTriggerEvent?>(null)
    private var clickListener: ((TriggerEvent) -> Unit)? = null

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    itemState?.let { item ->
                        EventListRow(
                            name = item.name,
                            conditionsCount = item.conditionsCountText,
                            actionsCount = item.actionsCountText,
                            enabledTextRes = item.enabledOnStartTextRes,
                            enabledIconRes = item.enabledOnStartIconRes,
                            conditionIconRes = R.drawable.ic_trigger_condition,
                            actionsInError = item.haveError,
                            showReorderHandle = false,
                            onClick = { clickListener?.invoke(item.event) },
                        )
                    }
                }
            }
        }
    }

    /**
     * Bind this view holder to an event.
     *
     * @param item the item providing the binding data.
     * @param itemClickedListener listener called when an event is clicked.
     */
    fun bindEvent(item: UiTriggerEvent, itemClickedListener: (TriggerEvent) -> Unit) {
        clickListener = itemClickedListener
        itemState = item
    }
}
