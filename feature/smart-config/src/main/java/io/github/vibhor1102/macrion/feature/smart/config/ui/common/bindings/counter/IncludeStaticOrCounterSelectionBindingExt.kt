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

import android.text.InputType
import android.view.View

import io.github.vibhor1102.macrion.core.ui.bindings.buttons.MultiStateButtonConfig
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.setChecked
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.setOnCheckedListener
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.setup
import io.github.vibhor1102.macrion.core.ui.bindings.dropdown.setItems
import io.github.vibhor1102.macrion.core.ui.bindings.dropdown.setSelectedItem
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnTextChangedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setText
import io.github.vibhor1102.macrion.core.ui.utils.NumberInputFilter
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.IncludeStaticOrCounterSelectionBinding
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.formatters.toNaturalDisplayString
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.counter.UiOperandType
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.counter.UiCounterOperatorDropdownItem
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.counter.UiStaticOrCounterSelection


fun IncludeStaticOrCounterSelectionBinding.setup(
    dropdownItems: List<UiCounterOperatorDropdownItem>,
    onOperatorSelected: (UiCounterOperatorDropdownItem) -> Unit,
    onChangeTypeClicked: (UiOperandType) -> Unit,
    onStaticValueChangedListener: (Double) -> Unit,
    onOpenCounterSelectionClicked: () -> Unit,
    onItemBound: ((UiCounterOperatorDropdownItem, View?) -> Unit)? = null,
) {
    operatorField.setItems(
        label = root.context.getString(R.string.dropdown_comparison_operator_label),
        items = dropdownItems,
        onItemSelected = onOperatorSelected,
        onItemBound = onItemBound,
    )

    valueTypeMultiStateButton.apply {
        setup(
            MultiStateButtonConfig(
                icons = listOf(R.drawable.ic_numbers, R.drawable.ic_change_counter),
                singleSelection = true,
                selectionRequired = true,
            )
        )

        setOnCheckedListener { checkedId ->
            onChangeTypeClicked(
                when (checkedId) {
                    0 -> UiOperandType.STATIC
                    1 -> UiOperandType.COUNTER
                    else -> return@setOnCheckedListener
                }
            )
        }
    }

    staticValueLayout.apply {
        textField.filters = arrayOf(NumberInputFilter(type = Double::class))
        staticValueLayout.textLayout.setHint(R.string.field_counter_operation_value_label)

        setOnTextChangedListener {
            try {
                onStaticValueChangedListener(textField.text.toString().toDouble())
            } catch (_: NumberFormatException) { }
        }
    }

    counterValueLayout.setOnClickListener { onOpenCounterSelectionClicked() }
}

fun IncludeStaticOrCounterSelectionBinding.setSelectedOperator(item: UiCounterOperatorDropdownItem) {
    operatorField.setSelectedItem(item)
}

fun IncludeStaticOrCounterSelectionBinding.setValueInfo(uiState: UiStaticOrCounterSelection) {
    when (uiState) {
        is UiStaticOrCounterSelection.CounterValue -> {
            valueTypeMultiStateButton.setChecked(1)

            staticValueLayout.root.visibility = View.GONE
            counterValueLayout.root.visibility = View.VISIBLE

            counterValueLayout.setCounter(uiState)
        }

        is UiStaticOrCounterSelection.StaticValue -> {
            valueTypeMultiStateButton.setChecked(0)

            staticValueLayout.root.visibility = View.VISIBLE
            counterValueLayout.root.visibility = View.GONE

            staticValueLayout.setText(
                uiState.value.toNaturalDisplayString(),
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
            )
        }
    }
}
