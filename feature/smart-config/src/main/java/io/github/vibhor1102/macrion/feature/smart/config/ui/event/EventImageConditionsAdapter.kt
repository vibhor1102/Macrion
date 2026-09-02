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

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.databinding.IncludeScreenConditionCardBinding
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.bindings.bind
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.UiScreenCondition

import kotlinx.coroutines.Job


internal class EventImageConditionsAdapter(
    private val itemClickedListener: (index: Int) -> Unit,
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
) : ListAdapter<UiScreenCondition, EventImageConditionViewHolder>(ImageConditionDiffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventImageConditionViewHolder =
        EventImageConditionViewHolder(
            parent,
            bitmapProvider,
            itemClickedListener,
        )

    override fun onBindViewHolder(holder: EventImageConditionViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onViewRecycled(holder: EventImageConditionViewHolder) {
        holder.onUnbind()
    }
}

internal class EventImageConditionViewHolder (
    parent: ViewGroup,
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
    private val itemClickedListener: (index: Int) -> Unit,
): ViewHolder(ComposeView(parent.context)) {
    private var itemState by mutableStateOf<UiScreenCondition?>(null)

    /** Job for the loading of the condition bitmap. Null until bound. */
    private var bitmapLoadingJob: Job? = null

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    Box(Modifier.size(108.dp).padding(horizontal = 4.dp, vertical = 4.dp)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxSize(),
                            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp),
                        ) {
                            AndroidView(
                                factory = { context ->
                                    IncludeScreenConditionCardBinding.inflate(
                                        LayoutInflater.from(context),
                                        null,
                                        false,
                                    ).root
                                },
                                update = { root ->
                                    val condition = itemState ?: return@AndroidView
                                    bitmapLoadingJob?.cancel()
                                    bitmapLoadingJob = IncludeScreenConditionCardBinding.bind(root).bind(
                                        condition,
                                        bitmapProvider,
                                    ) {
                                        val position = bindingAdapterPosition
                                        if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                            itemClickedListener(position)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }

    fun onBind(uiCondition: UiScreenCondition) {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        itemState = uiCondition
    }

    fun onUnbind() {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        itemState = null
    }
}

internal object ImageConditionDiffUtilCallback: DiffUtil.ItemCallback<UiScreenCondition>() {
    override fun areItemsTheSame(oldItem: UiScreenCondition, newItem: UiScreenCondition): Boolean =
        oldItem.condition.id == newItem.condition.id
    override fun areContentsTheSame(oldItem: UiScreenCondition, newItem: UiScreenCondition): Boolean =
        oldItem == newItem
}
