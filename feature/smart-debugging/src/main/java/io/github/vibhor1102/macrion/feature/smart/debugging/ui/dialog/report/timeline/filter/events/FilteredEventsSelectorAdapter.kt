/*
 * Copyright (C) 2026 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.timeline.filter.events

import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

class FilteredEventsSelectorAdapter(
    private val onItemClicked: (Long, Boolean) -> Unit,
) : ListAdapter<FilteredEventsSelectorItem, FilteredEventsSelectorItemViewHolder>(FilteredEventsSelectorItemDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilteredEventsSelectorItemViewHolder =
        FilteredEventsSelectorItemViewHolder(
            parent = parent,
            onItemClicked = onItemClicked,
        )

    override fun onBindViewHolder(holder: FilteredEventsSelectorItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private object FilteredEventsSelectorItemDiffUtilCallback: DiffUtil.ItemCallback<FilteredEventsSelectorItem>() {
    override fun areItemsTheSame(
        oldItem: FilteredEventsSelectorItem,
        newItem: FilteredEventsSelectorItem,
    ): Boolean = oldItem.eventId == newItem.eventId

    override fun areContentsTheSame(
        oldItem: FilteredEventsSelectorItem,
        newItem: FilteredEventsSelectorItem,
    ): Boolean = oldItem == newItem
}

class FilteredEventsSelectorItemViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (id: Long, state: Boolean) -> Unit,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {
    private var itemState by mutableStateOf<FilteredEventsSelectorItem?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    itemState?.let { item ->
                        androidx.compose.foundation.layout.Column(
                            Modifier.fillMaxWidth().clickable {
                                onItemClicked(item.eventId, !item.eventState)
                            }.padding(horizontal = 16.dp),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.eventName,
                                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Checkbox(
                                    checked = item.eventState,
                                    onCheckedChange = { onItemClicked(item.eventId, !item.eventState) },
                                )
                            }
                            HorizontalDivider(Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }

    fun bind(item: FilteredEventsSelectorItem) {
        itemState = item
    }
}
