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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.counter.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalContext
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportComposeViewHolder
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportNameValueRow
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.counter.CounterStateItem
import java.math.BigDecimal

class CounterStateViewHolder(parent: ViewGroup) : ReportComposeViewHolder<CounterStateItem>(
    parent = parent,
    content = { item ->
        ReportNameValueRow(item.counterName, item.toValueDisplayText(LocalContext.current))
    },
) {

    fun bind(item: CounterStateItem) {
        bindComposeItem(item)
    }

}

private fun CounterStateItem.toValueDisplayText(context: Context): String  {
    val oldValue = oldCounterValue
    return if (oldValue == null) {
        context.getString(
            R.string.item_counter_state_value_same,
            currentCounterValue.toNaturalDisplayString(),
        )
    } else {
        context.getString(
            R.string.item_counter_state_value_changed,
            oldValue.toNaturalDisplayString(),
            currentCounterValue.toNaturalDisplayString(),
        )
    }
}

private fun Double.toNaturalDisplayString(): String {
    if (!isFinite()) return toString()
    return BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
}
