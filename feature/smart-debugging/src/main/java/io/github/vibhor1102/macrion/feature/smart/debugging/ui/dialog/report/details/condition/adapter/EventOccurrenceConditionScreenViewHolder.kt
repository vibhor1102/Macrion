/*
 * Copyright (C) 2025 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.EventOccurrenceItem

import kotlinx.coroutines.Job


class EventOccurrenceConditionScreenViewHolder(
    parent: ViewGroup,
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {

    /** Job for the loading of the condition bitmap. Null until bound. */
    private var bitmapLoadingJob: Job? = null

    private var rowState by mutableStateOf<ScreenConditionResultState?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { MacrionTheme { rowState?.let { ScreenConditionResultRow(it) } } }
        }
    }

    fun bind(item: EventOccurrenceItem.Screen) {
        bitmapLoadingJob?.cancel()
        bitmapLoadingJob = null
        rowState = ScreenConditionResultState(item)
        val condition = item.condition
        if (condition is ScreenCondition.Image) {
            bitmapLoadingJob = bitmapProvider(condition) { bitmap ->
                if (rowState?.item?.id == item.id) {
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
