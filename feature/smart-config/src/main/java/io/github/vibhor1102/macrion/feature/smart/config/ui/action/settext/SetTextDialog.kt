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
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.settext

import android.os.Build
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.vibhor1102.macrion.core.common.actions.text.appendCounterReference

import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.DialogNavigationButton
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setButtonEnabledState
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setChecked
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setDescription
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setLabel
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnCheckboxClickedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnClickListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnTextChangedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setText
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setTextValue
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setTitle
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setup
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setupDescriptions
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogConfigActionSetTextBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.OnActionConfigCompleteListener
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.counter.selection.CounterSelectionDialog
import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType


class SetTextDialog(
    private val listener: OnActionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.SET_TEXT.name

    /** The view model for this dialog. */
    private val viewModel: SetTextViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { setTextViewModel() },
    )

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionSetTextBinding


    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionSetTextBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_set_text_action)

                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onSaveButtonClicked() }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onDeleteButtonClicked() }
                }
            }

            fieldName.apply {
                setLabel(R.string.generic_name)
                textField.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(
                    context.resources.getInteger(R.integer.name_max_length)
                ))
                setOnTextChangedListener { viewModel.setName(it.toString()) }
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            fieldTextToWrite.apply {
                setup(
                    label = R.string.field_input_set_text_text_to_write_title,
                    icon = R.drawable.ic_append_counter,
                    disableInputWithCheckbox = false,
                )
                textField.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(
                    context.resources.getInteger(R.integer.set_text_action_max_length)
                ))
                setOnTextChangedListener { viewModel.setTextToWrite(it.toString()) }
                setOnCheckboxClickedListener {
                    showCounterSelectionDialog { selectedCounter ->
                        val textToWrite = textField.text.toString().appendCounterReference(
                            counterName = selectedCounter,
                            atIndex = textField.selectionEnd,
                        )

                        viewModel.setTextToWrite(textToWrite)
                        setTextValue(value = textToWrite, force = true)
                    }
                }
            }
            hideSoftInputOnFocusLoss(fieldTextToWrite.textField)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                toggleValidateTextCard.visibility = View.GONE
            } else {
                fieldValidateText.apply {
                    setTitle(context.resources.getString(R.string.field_set_text_validate_title))
                    setupDescriptions(
                        listOf(
                            context.getString(R.string.field_set_text_validate_desc_disabled),
                            context.getString(R.string.field_set_text_validate_desc_enabled),
                        )
                    )
                    setOnClickListener(viewModel::toggleValidateInput)
                }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::onUiStateUpdated) }
            }
        }
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

    private fun onSaveButtonClicked() {
        listener.onConfirmClicked()
        super.back()
    }

    private fun onDeleteButtonClicked() {
        listener.onDeleteClicked()
        super.back()
    }

    private fun onUiStateUpdated(uiState: SetTextUiState?) {
        uiState ?: return

        viewBinding.apply {
            layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, uiState.canBeSaved)

            fieldName.setText(uiState.name)
            fieldTextToWrite.setTextValue(uiState.textToWrite)
            viewBinding.fieldValidateText.apply {
                setChecked(uiState.validateInput)
                setDescription(if (uiState.validateInput) 1 else 0)
            }
        }
    }

    private fun showCounterSelectionDialog(onCounterSelected: (String) -> Unit) {
        overlayManager.navigateTo(
            context = context,
            newOverlay = CounterSelectionDialog(onCounterSelected),
            hideCurrent = true,
        )
    }

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing SystemAction Dialog because there is no action edited")
            finish()
        }
    }
}

private const val TAG = "SystemActionDialog"
