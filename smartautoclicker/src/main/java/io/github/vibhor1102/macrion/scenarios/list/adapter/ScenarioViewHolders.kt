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

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.scenarios.list.model.ScenarioListUiState
import io.github.vibhor1102.macrion.core.base.extensions.setLeftCompoundDrawable
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.dumb.domain.model.DumbScenario
import io.github.vibhor1102.macrion.databinding.ItemDumbScenarioBinding
import io.github.vibhor1102.macrion.databinding.ItemEmptyScenarioBinding
import io.github.vibhor1102.macrion.databinding.ItemSmartScenarioBinding
import io.github.vibhor1102.macrion.scenarios.list.model.getTimeSinceString
import kotlinx.coroutines.Job

import java.util.Locale

class EmptyScenarioHolder(
    private val viewBinding: ItemEmptyScenarioBinding,
    private val launchScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Empty) -> Unit),
    private val deleteScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Empty) -> Unit),
): RecyclerView.ViewHolder(viewBinding.root) {

    fun onBind(scenarioItem: ScenarioListUiState.Item.ScenarioItem.Empty) = viewBinding.apply {
        scenarioName.text = scenarioItem.displayName
        scenarioName.setLeftCompoundDrawable(
            if (scenarioItem.scenario is DumbScenario) R.drawable.ic_dumb
            else R.drawable.ic_smart
        )

        buttonStart.setOnClickListener { launchScenarioListener(scenarioItem) }
        buttonDelete.setOnClickListener { deleteScenarioListener(scenarioItem) }
    }
}

/** ViewHolder for the [ScenarioAdapter]. */
class DumbScenarioViewHolder(
    private val viewBinding: ItemDumbScenarioBinding,
    private val launchScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val expandCollapseListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val exportClickListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val copyClickedListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val deleteScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun onBind(scenarioItem: ScenarioListUiState.Item.ScenarioItem.Valid.Dumb) = viewBinding.apply {
        scenarioName.text = scenarioItem.displayName

        if (scenarioItem.showExportCheckbox) {
            buttonExpandCollapse.visibility = View.INVISIBLE
            buttonExpandCollapse.isEnabled = false
            buttonExport.apply {
                visibility = View.VISIBLE
                isChecked = scenarioItem.checkedForExport
            }
            topDivider.visibility = View.GONE
            root.setOnClickListener { exportClickListener(scenarioItem) }

        } else {
            buttonExpandCollapse.visibility = View.VISIBLE
            buttonExpandCollapse.isEnabled = true
            buttonExport.visibility = View.GONE
            topDivider.visibility = View.VISIBLE
            root.setOnClickListener { launchScenarioListener(scenarioItem) }
        }

        if (!scenarioItem.showExportCheckbox && scenarioItem.expanded) {
            scenarioDetails.visibility = View.VISIBLE
            buttonExpandCollapse.setIconResource(R.drawable.ic_chevron_up)

            executionCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.startCount)
            lastExecution.text = root.context.getTimeSinceString(scenarioItem.lastStartTimestamp)
            clickCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.clickCount)
            swipeCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.swipeCount)
            pauseCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.pauseCount)

            repeatLimit.text = scenarioItem.repeatText
            durationLimit.text = scenarioItem.maxDurationText
        } else {
            buttonExpandCollapse.setIconResource(R.drawable.ic_chevron_down)
            scenarioDetails.visibility = View.GONE
        }

        buttonCopy.setOnClickListener { copyClickedListener(scenarioItem) }
        buttonExpandCollapse.setOnClickListener { expandCollapseListener(scenarioItem) }
        buttonDelete.setOnClickListener { deleteScenarioListener(scenarioItem) }
        buttonExport.setOnClickListener { exportClickListener(scenarioItem) }
    }
}

/** ViewHolder for the [ScenarioAdapter]. */
class SmartScenarioViewHolder(
    private val viewBinding: ItemSmartScenarioBinding,
    bitmapProvider: (ScreenCondition.Image, onBitmapLoaded: (Bitmap?) -> Unit) -> Job?,
    private val launchScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val expandCollapseListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val exportClickListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val copyClickedListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
    private val deleteScenarioListener: ((ScenarioListUiState.Item.ScenarioItem.Valid) -> Unit),
) : RecyclerView.ViewHolder(viewBinding.root) {

    private val eventsAdapter = ScenarioEventsAdapter(bitmapProvider)

    init {
        viewBinding.listEvent.adapter = eventsAdapter
    }

    fun onBind(scenarioItem: ScenarioListUiState.Item.ScenarioItem.Valid.Smart) = viewBinding.apply {
        scenarioName.text = scenarioItem.displayName

        if (scenarioItem.showExportCheckbox) {
            buttonExpandCollapse.visibility = View.INVISIBLE
            buttonExpandCollapse.isEnabled = false
            buttonExport.apply {
                visibility = View.VISIBLE
                isChecked = scenarioItem.checkedForExport
            }
            topDivider.visibility = View.GONE
            root.setOnClickListener { exportClickListener(scenarioItem) }
        } else {
            buttonExpandCollapse.visibility = View.VISIBLE
            buttonExpandCollapse.isEnabled = true
            buttonExport.visibility = View.GONE
            topDivider.visibility = View.VISIBLE
            root.setOnClickListener { launchScenarioListener(scenarioItem) }
        }

        if (!scenarioItem.showExportCheckbox && scenarioItem.expanded) {
            scenarioDetails.visibility = View.VISIBLE
            buttonExpandCollapse.setIconResource(R.drawable.ic_chevron_up)

            executionCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.startCount)
            lastExecution.text = root.context.getTimeSinceString(scenarioItem.lastStartTimestamp)
            detectionQuality.text = String.format(Locale.getDefault(), "%d", scenarioItem.detectionQuality)
            triggerEventCount.text = String.format(Locale.getDefault(), "%d", scenarioItem.triggerEventCount)

            eventsAdapter.submitList(scenarioItem.eventsItems)
            if (scenarioItem.eventsItems.isEmpty()) {
                listEvent.visibility = View.GONE
                noImageEvents.visibility = View.VISIBLE
            } else {
                listEvent.visibility = View.VISIBLE
                noImageEvents.visibility = View.GONE
            }
        } else {
            buttonExpandCollapse.setIconResource(R.drawable.ic_chevron_down)
            scenarioDetails.visibility = View.GONE
        }

        buttonCopy.setOnClickListener { copyClickedListener(scenarioItem) }
        buttonExpandCollapse.setOnClickListener { expandCollapseListener(scenarioItem) }
        buttonDelete.setOnClickListener { deleteScenarioListener(scenarioItem) }
        buttonExport.setOnClickListener { exportClickListener(scenarioItem) }
    }
}
