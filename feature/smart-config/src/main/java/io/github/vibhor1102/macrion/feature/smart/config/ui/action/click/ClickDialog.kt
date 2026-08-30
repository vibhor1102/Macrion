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
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.click

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.graphics.toPoint
import androidx.core.graphics.toPointF
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.common.actions.GESTURE_DURATION_MAX_VALUE
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.PositionSelectorMenu
import io.github.vibhor1102.macrion.core.domain.model.action.Click
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.MultiStateButtonConfig
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.DialogNavigationButton
import io.github.vibhor1102.macrion.core.ui.bindings.dialogs.setButtonEnabledState
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setLabel
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnTextChangedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setText
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setError
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setButtonConfig
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setChecked
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setDescription
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setEnabled
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setIconBitmap
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setImageDrawable
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnCheckedListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setOnClickListener
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setTitle
import io.github.vibhor1102.macrion.core.ui.bindings.fields.setupDescriptions
import io.github.vibhor1102.macrion.core.ui.utils.MinMaxInputFilter
import io.github.vibhor1102.macrion.core.ui.views.itembrief.renderers.ClickDescription
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.databinding.DialogConfigActionClickBinding
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.OnActionConfigCompleteListener
import io.github.vibhor1102.macrion.feature.smart.config.ui.action.click.offset.ClickOffsetDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.selection.ScreenConditionSelectionDialog

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.monitoring.MonitoredOverlayType

class ClickDialog(
    private val listener: OnActionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    override fun tutorialMonitoringTag(): String = MonitoredOverlayType.CLICK.name

    /** The view model for this dialog. */
    private val viewModel: ClickViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { clickViewModel() },
    )

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionClickBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionClickBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_click)

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
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            fieldPressDuration.apply {
                textField.filters = arrayOf(MinMaxInputFilter(1, GESTURE_DURATION_MAX_VALUE.toInt()))
                setLabel(R.string.input_field_label_click_press_duration)
                setOnTextChangedListener {
                    viewModel.setPressDuration(if (it.isNotEmpty()) it.toString().toLong() else null)
                }
            }
            hideSoftInputOnFocusLoss(fieldPressDuration.textField)

            fieldClickType.apply {
                setTitle(context.getString(R.string.field_click_type_title))
                setupDescriptions(
                    listOf(
                        context.getString(R.string.field_click_type_desc_on_position),
                        context.getString(R.string.field_click_type_desc_on_condition),
                    )
                )
                setButtonConfig(
                    MultiStateButtonConfig(
                        icons = listOf(R.drawable.ic_click_on_condition, R.drawable.ic_condition),
                        singleSelection = true,
                        selectionRequired = true,
                    )
                )
                setOnCheckedListener { checkedId ->
                    viewModel.setClickOnCondition(
                        if (checkedId == 0) Click.PositionType.USER_SELECTED
                        else Click.PositionType.ON_DETECTED_CONDITION
                    )
                }
            }

            fieldClickOffset.apply {
                setTitle(context.getString(R.string.field_click_offset_title))
                setOnClickListener { showClickOffsetDialog() }
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
                launch { viewModel.uiState.collect(::updateUi) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.monitorViews(
            onConditionTypeView = viewBinding.fieldClickType.multiStateButton.buttonMiddle,
            selectPositionFieldView = viewBinding.fieldClickSelection.root,
            saveButton = viewBinding.layoutTopBar.buttonSave,
        )
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

    private fun onSaveButtonClicked() {
        viewModel.saveLastConfig()
        listener.onConfirmClicked()
        super.back()
    }

    private fun onDeleteButtonClicked() {
        listener.onDeleteClicked()
        super.back()
    }

    private fun updateUi(state: ClickUiState?) {
        state ?: return

        viewBinding.apply {
            layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, state.canBeSaved)
            fieldName.setText(state.name)
            fieldName.setError(state.nameError)
            fieldPressDuration.setText(state.pressDuration, InputType.TYPE_CLASS_NUMBER)
            fieldPressDuration.setError(state.pressDurationError)
        }

        updateClickPositionUiState(state.positionState)
    }

    private fun updateClickPositionUiState(state: ClickPositionUiState?) {
        state ?: return

        viewBinding.fieldClickType.apply {
            val checkIndex = if (state.positionType == Click.PositionType.USER_SELECTED) 0 else 1
            setChecked(checkIndex)
            setDescription(checkIndex)

            root.isVisible = state.isTypeFieldVisible
            viewBinding.dividerClickTypeBottom.isVisible = state.isTypeFieldVisible
        }

        viewBinding.fieldClickSelection.apply {
            setTitle(state.selectorTitle)
            setDescription(state.selectorDescription)
            setEnabled(state.isSelectorEnabled)
            setError(state.isSelectorInError)

            when (val visualization = state.selectorVisualization) {
                is Drawable -> setImageDrawable(visualization)
                is Bitmap -> setIconBitmap(visualization)
                else -> setImageDrawable(null)
            }

            when (state.positionType) {
                Click.PositionType.USER_SELECTED ->
                    setOnClickListener { debounceUserInteraction { showPositionSelector() } }
                Click.PositionType.ON_DETECTED_CONDITION ->
                    setOnClickListener { debounceUserInteraction { showConditionSelector() } }
            }
        }

        viewBinding.fieldClickOffset.apply {
            setEnabled(state.isClickOffsetEnabled)
            setDescription(state.clickOffsetDescription)

            root.isVisible = state.isClickOffsetVisible
            viewBinding.dividerClickOffset.isVisible = state.isClickOffsetVisible
        }
    }

    private fun showPositionSelector() {
        viewModel.getEditedClick()?.let { click ->
            overlayManager.navigateTo(
                context = context,
                newOverlay = PositionSelectorMenu(
                    tutorialMonitoringTag = MonitoredOverlayType.CLICK_POSITION.name,
                    itemBriefDescription = ClickDescription(
                        position = click.position?.toPointF(),
                        pressDurationMs = click.pressDuration ?: 1L,
                    ),
                    onConfirm = { description ->
                        (description as ClickDescription).position?.let {
                            viewModel.setPosition(it.toPoint())
                        }
                    },
                ),
                hideCurrent = true,
            )
        }
    }

    private fun showConditionSelector() =
        overlayManager.navigateTo(
            context = context,
            newOverlay = ScreenConditionSelectionDialog(
                conditionList = viewModel.uiState.value?.availableConditions ?: emptyList(),
                onConditionSelected = viewModel::setConditionToBeClicked,
            ),
            hideCurrent = false,
        )

    private fun showClickOffsetDialog() =
        overlayManager.navigateTo(
            context = context,
            newOverlay = ClickOffsetDialog(),
            hideCurrent = false,
        )

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing ClickDialog because there is no action edited")
            finish()
        }
    }
}

private const val TAG = "ClickDialog"
