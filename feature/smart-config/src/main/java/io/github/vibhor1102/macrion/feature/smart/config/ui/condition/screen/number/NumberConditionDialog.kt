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
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.number

import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.vibhor1102.macrion.core.common.navigation.getTutorialNavigator

import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.Tip
import io.github.vibhor1102.macrion.core.domain.model.counter.CounterOperationValue
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.DialogNavigationButton
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setButtonEnabledState
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setDescription
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setError
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setLabel
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnClickListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnTextChangedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnValueChangedFromUserListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setSliderRange
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setSliderValue
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setText
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setTitle
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setValueLabelState
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogConfigConditionNumberBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.bindings.counter.setSelectedOperator
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.bindings.counter.setValueInfo
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.bindings.counter.setup
import io.github.vibhor1102.macrion.core.ui.bindings.dropdown.setItems
import io.github.vibhor1102.macrion.core.ui.bindings.dropdown.setSelectedItem
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showDeleteConditionsWithAssociatedActionsDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.condition.allNumberFormatDropdownItems
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.model.counter.allCounterComparisonOperatorDropdownItems
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.OnConditionConfigCompleteListener
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.areaselector.ConditionAreaSelectorMenu
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.image.MAX_THRESHOLD
import io.github.vibhor1102.macrion.feature.smart.config.ui.counter.selection.CounterSelectionDialog

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.roundToInt
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType

class NumberConditionDialog(
    private val listener: OnConditionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.NUMBER_CONDITION.name

    /** The view model for this dialog. */
    private val viewModel: NumberConditionViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { numberConditionViewModel() },
    )

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigConditionNumberBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigConditionNumberBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_condition_config)

                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onConfirmClicked()
                        super.back()
                    }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onDeleteClicked() }
                }
            }

            fieldEditName.apply {
                setLabel(R.string.generic_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldEditName.textField)

            editValueLayout.apply {
                setup(
                    dropdownItems = allCounterComparisonOperatorDropdownItems(),
                    onOperatorSelected = viewModel::setComparisonOperator,
                    onChangeTypeClicked = viewModel::setOperandType,
                    onStaticValueChangedListener = { newValue ->
                        viewModel.setOperationValue(CounterOperationValue.Number(newValue))
                    },
                    onOpenCounterSelectionClicked = {
                        showCounterSelectionDialog { counterSelected ->
                            viewModel.setOperationValue(CounterOperationValue.Counter(counterSelected))
                        }
                    },
                    onItemBound = { item, view ->
                        viewModel.monitorDropdownItem(item, view)
                    }
                )
                hideSoftInputOnFocusLoss(staticValueLayout.textField)
            }

            fieldNumberFormat.setItems(
                label = context.getString(R.string.field_number_condition_number_format_label),
                items = allNumberFormatDropdownItems(),
                onItemSelected = viewModel::setNumberFormat,
            )

            fieldSelectArea.apply {
                setTitle(context.getString(R.string.generic_detection_area_title))
                setOnClickListener { showDetectionAreaSelector() }
            }

            fieldSliderThreshold.apply {
                setTitle(context.getString(R.string.generic_condition_threshold_title))
                setValueLabelState(isEnabled = true, prefix = "%")
                setSliderRange(0f, MAX_THRESHOLD)
                setOnValueChangedFromUserListener { value -> viewModel.setThreshold(value.roundToInt()) }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingCondition.collect(::onConditionEditingStateChanged) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::updateUi) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.monitorSaveButtonView(viewBinding.layoutTopBar.buttonSave)
        viewModel.monitorDetectionAreaField(viewBinding.fieldSelectArea.root)
        viewModel.monitorOperatorField(viewBinding.editValueLayout.operatorField.root)
        viewModel.monitorValueToDetectField(viewBinding.editValueLayout.staticValueLayout.textField)
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopViewMonitoring()
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                listener.onDismissClicked()
                super.back()
            }
            return
        }

        listener.onDismissClicked()
        super.back()
    }

    private fun updateUi(uiState: NumberConditionUiState?) {
        if (uiState == null) return

        viewBinding.apply {
            layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, uiState.canBeSaved)
            fieldEditName.setText(uiState.name)
            fieldEditName.setError(uiState.nameError)

            editValueLayout.setSelectedOperator(uiState.selectorOperatorDropdownItem)
            editValueLayout.setValueInfo(uiState.operandValue)
            effectDesc.text = uiState.conditionEffectDesc

            fieldNumberFormat.setSelectedItem(uiState.numberFormatDropdownItem)
            fieldSelectArea.setDescription(uiState.detectionAreaDescription)
            fieldSelectArea.setError(uiState.detectionAreaError)
            fieldSliderThreshold.setSliderValue(uiState.detectionThreshold.toFloat())
        }
    }

    private fun onDeleteClicked() {
        if (viewModel.isConditionRelatedToClick()) {
            context.showDeleteConditionsWithAssociatedActionsDialog { onConfirmDelete() }
            return
        }

        onConfirmDelete()
    }

    private fun onConfirmDelete() {
        listener.onDeleteClicked()
        super.back()
    }

    private fun onConditionEditingStateChanged(isEditing: Boolean) {
        if (!isEditing) {
            Log.e(TAG, "Closing ConditionDialog because there is no condition edited")
            finish()
        }
    }

    private fun showCounterSelectionDialog(onCounterSelected: (String) -> Unit) {
        overlayManager.navigateTo(
            context = context,
            newOverlay = CounterSelectionDialog(onCounterSelected),
            hideCurrent = true,
        )
    }

    private fun showDetectionAreaSelector() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = ConditionAreaSelectorMenu(
                onHelpClicked = { context.getTutorialNavigator().showTipDialog(context, Tip.NUMBER_DETECTION_AREA) },
                onAreaSelected = viewModel::setDetectionArea,
            ),
            hideCurrent = true,
        )
    }
}

private const val TAG = "NumberConditionDialog"
