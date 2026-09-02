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
package io.github.vibhor1102.macrion.feature.smart.config.ui.event

import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme


internal class EventChildrenCardsAdapter(
    private val itemClickedListener: (index: Int) -> Unit,
) : ListAdapter<EventChildrenItem, EventChildCardViewHolder>(CardIconResDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventChildCardViewHolder =
        EventChildCardViewHolder(parent, itemClickedListener)

    override fun onBindViewHolder(holder: EventChildCardViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

internal class EventChildCardViewHolder (
    parent: ViewGroup,
    private val itemClickedListener: (index: Int) -> Unit,
): ViewHolder(ComposeView(parent.context)) {
    private var itemState by mutableStateOf<EventChildrenItem?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { MacrionTheme { itemState?.let { EventChildCard(it) } } }
        }
    }

    fun onBind(item: EventChildrenItem) {
        itemState = item
    }

    @Composable
    private fun EventChildCard(item: EventChildrenItem) {
        Box(Modifier.width(56.dp).height(64.dp).padding(horizontal = 4.dp, vertical = 8.dp)) {
            Surface(
                Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    .clickable {
                        val position = bindingAdapterPosition
                        if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) itemClickedListener(position)
                    },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AndroidView(
                        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
                        update = { it.setImageResource(item.iconRes) },
                        modifier = Modifier.size(29.dp),
                    )
                    if (item.isInError) Box(
                        Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 6.dp).size(6.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                    )
                }
            }
        }
    }
}

internal object CardIconResDiffUtilCallback: DiffUtil.ItemCallback<EventChildrenItem>() {
    override fun areItemsTheSame(oldItem: EventChildrenItem, newItem: EventChildrenItem): Boolean =
        oldItem.iconRes == newItem.iconRes
    override fun areContentsTheSame(oldItem: EventChildrenItem, newItem: EventChildrenItem): Boolean =
        oldItem == newItem
}

data class EventChildrenItem(
    @field:DrawableRes val iconRes: Int,
    val isInError: Boolean,
)
