/* Copyright (C) 2025 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.scenarios.list.adapter

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.core.settings.domain.model.ScenarioSortType
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.scenarios.list.model.ScenarioListUiState

class SortViewHolder(
    parent: ViewGroup,
    private val onSortTypeClicked: (ScenarioSortType) -> Unit,
    private val onSmartChipClicked: (Boolean) -> Unit,
    private val onDumbChipClicked: (Boolean) -> Unit,
    private val onSortOrderClicked: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {
    private var config by mutableStateOf<ScenarioListUiState.Item.SortItem?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent { MacrionTheme { config?.let { SortControls(it) } } }
        }
    }

    fun onBind(value: ScenarioListUiState.Item.SortItem) { config = value }
    fun onUnbind() = Unit

    @Composable
    private fun SortControls(state: ScenarioListUiState.Item.SortItem) {
        val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (landscape) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrderingButtons(state, Modifier.weight(1f))
                FilterControls(state)
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OrderingButtons(state, Modifier.fillMaxWidth())
                FilterControls(state)
            }
        }
    }

    @Composable
    private fun OrderingButtons(state: ScenarioListUiState.Item.SortItem, modifier: Modifier) {
        Row(modifier, horizontalArrangement = Arrangement.Center) {
            OrderingButton(
                R.drawable.ic_sort_name,
                R.string.item_scenario_filter_by_name,
                state.sortType == ScenarioSortType.NAME,
                { onSortTypeClicked(ScenarioSortType.NAME) },
            )
            OrderingButton(
                R.drawable.ic_sort_recent,
                R.string.item_scenario_filter_by_recent,
                state.sortType == ScenarioSortType.RECENT,
                { onSortTypeClicked(ScenarioSortType.RECENT) },
            )
            OrderingButton(
                R.drawable.ic_most_used,
                R.string.item_scenario_filter_by_most_used,
                state.sortType == ScenarioSortType.MOST_USED,
                { onSortTypeClicked(ScenarioSortType.MOST_USED) },
            )
        }
    }

    @Composable
    private fun OrderingButton(icon: Int, label: Int, selected: Boolean, onClick: () -> Unit) {
        OutlinedButton(
            onClick = onClick,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
        ) {
            Icon(painterResource(icon), null, Modifier.size(18.dp))
            Text(stringResource(label), Modifier.padding(start = 6.dp), maxLines = 1)
        }
    }

    @Composable
    private fun FilterControls(state: ScenarioListUiState.Item.SortItem) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.smartVisible,
                onClick = { onSmartChipClicked(!state.smartVisible) },
                label = { Text(stringResource(R.string.item_title_smart_scenario)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_smart), null, Modifier.size(18.dp)) },
            )
            FilterChip(
                selected = state.dumbVisible,
                onClick = { onDumbChipClicked(!state.dumbVisible) },
                label = { Text(stringResource(R.string.item_title_dumb_scenario)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_dumb), null, Modifier.size(18.dp)) },
            )
            IconToggleButton(
                checked = state.changeOrderChecked,
                onCheckedChange = onSortOrderClicked,
            ) {
                Icon(painterResource(R.drawable.ic_sort_order), null)
            }
        }
    }
}
