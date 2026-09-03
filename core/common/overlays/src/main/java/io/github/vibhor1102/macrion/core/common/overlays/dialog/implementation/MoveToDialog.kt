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
package io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation

import android.view.KeyEvent

import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import io.github.vibhor1102.macrion.core.common.overlays.R
import io.github.vibhor1102.macrion.core.common.overlays.base.BaseOverlay
import io.github.vibhor1102.macrion.core.common.overlays.manager.OverlayManager
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.utils.getDynamicColorsContext
import io.github.vibhor1102.macrion.core.ui.R as UiR

import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MoveToDialog(
    @StyleRes theme: Int,
    private val defaultValue: Int,
    private val itemCount: Int,
    private val onValueSelected: ((Int) -> Unit),
) : BaseOverlay(theme, recreateOnRotation = true) {

    private var currentValue by mutableStateOf(defaultValue.toString())

    /** Tells if the dialog is visible. */
    private var isShown = false
    private var dialog: AlertDialog? = null

    override fun onCreate() {
        val content = ComposeView(context).apply {
            setContent {
                MacrionTheme {
                    MoveToPositionField(
                        value = currentValue,
                        itemCount = itemCount,
                        onValueChanged = { value ->
                            currentValue = value
                            updatePositiveButtonState()
                        },
                    )
                }
            }
        }

        dialog = MaterialAlertDialogBuilder(context.getDynamicColorsContext(R.style.AppTheme))
            .setTitle(R.string.dialog_move_to_title)
            .setView(content)
            .setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    this@MoveToDialog.back()
                    true
                } else {
                    false
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ -> validateCurrentValueAndClose() }
            .setNegativeButton(android.R.string.cancel) { _, _ -> back() }
            .setOnDismissListener {
                dialog = null
                destroy()
            }
            .create()

        dialog?.window?.setType(OverlayManager.OVERLAY_WINDOW_TYPE)
    }

    override fun onStart() {
        if (isShown) return

        isShown = true
        dialog?.show()
        updatePositiveButtonState()
    }

    override fun onStop() {
        if (!isShown) return

        dialog?.hide()
        isShown = false
    }

    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
    }

    private fun updatePositiveButtonState() {
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled =
            currentValue.toEditedValue() != null
    }

    private fun validateCurrentValueAndClose() {
        currentValue.toEditedValue()?.let { value ->
            onValueSelected(value)
            back()
        }
    }

    private fun String.toEditedValue(): Int? =
        try {
            toInt()
        } catch (nfEx: NumberFormatException) {
            null
        }
}

@Composable
private fun MoveToPositionField(
    value: String,
    itemCount: Int,
    onValueChanged: (String) -> Unit,
) {
    val focusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        androidx.compose.runtime.withFrameNanos { }
        keyboardController?.show()
    }

    OutlinedTextField(
        value = value,
        onValueChange = { candidate ->
            if (candidate.isEmpty() || candidate.toIntOrNull()?.let { it in 1..itemCount } == true) {
                onValueChanged(candidate)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .focusRequester(focusRequester),
        label = { Text(stringResource(R.string.dialog_move_to_position_label)) },
        placeholder = { Text("Max: $itemCount") },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChanged("") }) {
                    Icon(painterResource(UiR.drawable.ic_cancel), contentDescription = null)
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
    )
}
