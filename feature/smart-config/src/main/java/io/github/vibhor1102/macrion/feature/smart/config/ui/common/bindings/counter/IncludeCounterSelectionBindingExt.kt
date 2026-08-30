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
package io.github.vibhor1102.macrion.feature.smart.config.ui.common.bindings.counter

import io.github.vibhor1102.macrion.core.base.extensions.setLeftCompoundDrawable
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.IncludeCounterSelectionBinding
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toNaturalDisplayString
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.counter.UiStaticOrCounterSelection

import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors

fun IncludeCounterSelectionBinding.setOnClickListener(listener: () -> Unit) {
    root.setOnClickListener { listener() }
}

fun IncludeCounterSelectionBinding.setCounter(uiState: UiStaticOrCounterSelection.CounterValue) {
    if (uiState.counter?.counterName.isNullOrEmpty()) {
        title.setLeftCompoundDrawable(R.drawable.ic_badge_error)
        description.setTextColor(MaterialColors.getColor(root, AppCompatR.attr.colorError))
        title.setText(R.string.field_counter_selection_title_empty)
        description.setText(R.string.field_counter_selection_desc_empty)
    } else {
        title.text = uiState.counter.counterName
        title.setLeftCompoundDrawable(null)
        description.text = root.context.getString(
            R.string.field_counter_selection_desc,
            uiState.counter.defaultValue.toNaturalDisplayString(maxFractionDigits = 2),
        )
        description.setTextColor(MaterialColors.getColor(root, MaterialR.attr.colorOnSurfaceVariant))
    }
}
