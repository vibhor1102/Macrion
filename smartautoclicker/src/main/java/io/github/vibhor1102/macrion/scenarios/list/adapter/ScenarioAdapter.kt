/*
 * Copyright (C) 2024 Kevin Buzeau
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
package io.github.vibhor1102.macrion.scenarios.list.adapter

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.scenarios.list.model.ScenarioListUiState
import io.github.vibhor1102.macrion.core.settings.domain.model.ScenarioSortType

import kotlinx.coroutines.Job

/**
 * Adapter for the display of the click scenarios created by the user into a RecyclerView.
 *
 * @param launchScenarioListener listener upon the click on a scenario.
 * @param exportClickListener listener upon the export button of a scenario.
 * @param deleteScenarioListener listener upon the delete button of a scenario.
 */
class ScenarioAdapter(
    private val bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
    private val launchScenarioListener: ((ScenarioListUiState.Item.ScenarioItem) -> Unit),
    private val expandCollapseListener: ((ScenarioListUiState.Item.ScenarioItem) -> Unit),
    private val exportClickListener: ((ScenarioListUiState.Item.ScenarioItem) -> Unit),
    private val copyClickedListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val deleteScenarioListener: ((ScenarioListUiState.Item.ScenarioItem) -> Unit),
    private val onSortTypeClicked: (ScenarioSortType) -> Unit,
    private val onSmartChipClicked: (Boolean) -> Unit,
    private val onDumbChipClicked: (Boolean) -> Unit,
    private val onSortOrderClicked: (Boolean) -> Unit,
) : ListAdapter<ScenarioListUiState.Item, RecyclerView.ViewHolder>(ScenarioDiffUtilCallback) {

    private companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_DUMB = 1
        const val TYPE_SMART = 2
        const val TYPE_SORT = 3
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ScenarioListUiState.Item.ScenarioItem.Empty -> TYPE_EMPTY
            is ScenarioListUiState.Item.ScenarioItem.Valid.Dumb -> TYPE_DUMB
            is ScenarioListUiState.Item.ScenarioItem.Valid.Smart -> TYPE_SMART
            is ScenarioListUiState.Item.SortItem -> TYPE_SORT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_EMPTY -> EmptyScenarioHolder(
                parent = parent,
                launch = launchScenarioListener,
                delete = deleteScenarioListener,
            )

            TYPE_DUMB -> DumbScenarioViewHolder(
                parent = parent,
                launch = launchScenarioListener,
                expand = expandCollapseListener,
                export = exportClickListener,
                copy = copyClickedListener,
                delete = deleteScenarioListener,
            )

            TYPE_SMART -> SmartScenarioViewHolder(
                parent = parent,
                bitmapProvider = bitmapProvider,
                launch = launchScenarioListener,
                expand = expandCollapseListener,
                export = exportClickListener,
                copy = copyClickedListener,
                delete = deleteScenarioListener,
            )

            TYPE_SORT -> SortViewHolder(
                parent = parent,
                onSortTypeClicked = onSortTypeClicked,
                onSmartChipClicked = onSmartChipClicked,
                onDumbChipClicked = onDumbChipClicked,
                onSortOrderClicked = onSortOrderClicked,
            )

            else -> throw IllegalArgumentException("Unsupported view type !")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = holder is SortViewHolder || holder is EmptyScenarioHolder
        }

        when (holder) {
            is EmptyScenarioHolder -> holder.onBind(getItem(position) as ScenarioListUiState.Item.ScenarioItem.Empty)
            is DumbScenarioViewHolder -> holder.onBind(getItem(position) as ScenarioListUiState.Item.ScenarioItem.Valid.Dumb)
            is SmartScenarioViewHolder -> holder.onBind(getItem(position) as ScenarioListUiState.Item.ScenarioItem.Valid.Smart)
            is SortViewHolder -> holder.onBind(getItem(position) as ScenarioListUiState.Item.SortItem)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is SortViewHolder) holder.onUnbind()
        super.onViewRecycled(holder)
    }
}

/** DiffUtil Callback comparing two ScenarioItem when updating the [ScenarioAdapter] list. */
object ScenarioDiffUtilCallback: DiffUtil.ItemCallback<ScenarioListUiState.Item>() {
    override fun areItemsTheSame(oldItem: ScenarioListUiState.Item, newItem: ScenarioListUiState.Item): Boolean =
        when {
            oldItem is ScenarioListUiState.Item.ScenarioItem.Empty.Dumb && newItem is ScenarioListUiState.Item.ScenarioItem.Empty.Dumb ->
                oldItem.scenario.id == newItem.scenario.id
            oldItem is ScenarioListUiState.Item.ScenarioItem.Empty.Smart && newItem is ScenarioListUiState.Item.ScenarioItem.Empty.Smart ->
                oldItem.scenario.id == newItem.scenario.id
            oldItem is ScenarioListUiState.Item.ScenarioItem.Valid.Dumb && newItem is ScenarioListUiState.Item.ScenarioItem.Valid.Dumb ->
                oldItem.scenario.id == newItem.scenario.id
            oldItem is ScenarioListUiState.Item.ScenarioItem.Valid.Smart && newItem is ScenarioListUiState.Item.ScenarioItem.Valid.Smart ->
                oldItem.scenario.id == newItem.scenario.id
            oldItem is ScenarioListUiState.Item.SortItem && newItem is ScenarioListUiState.Item.SortItem -> true
            else -> false
        }

    override fun areContentsTheSame(oldItem: ScenarioListUiState.Item, newItem: ScenarioListUiState.Item): Boolean =
        if (oldItem is ScenarioListUiState.Item.SortItem && newItem is ScenarioListUiState.Item.SortItem) true
        else oldItem == newItem
}
