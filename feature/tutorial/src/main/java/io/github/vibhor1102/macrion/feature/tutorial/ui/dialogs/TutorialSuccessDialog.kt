/*
 * Copyright (C) 2026 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.tutorial.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.vibhor1102.macrion.core.ui.compose.MacrionDialogSurface
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.utils.getDynamicColorsContext
import io.github.vibhor1102.macrion.feature.tutorial.R

internal fun Context.createTutorialSuccessDialog(onClose: () -> Unit): AlertDialog {
    val dialogContext = getDynamicColorsContext(R.style.AppTheme)
    lateinit var dialog: AlertDialog
    val content = ComposeView(dialogContext).apply {
        setContent {
            MacrionTheme {
                MacrionDialogSurface {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_title_tutorial_completed),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        HorizontalDivider(Modifier.padding(bottom = 12.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_tutorial_completed),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp).size(64.dp),
                        )
                        Text(
                            text = stringResource(R.string.message_tutorial_completed),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = {
                                dialog.dismiss()
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth().padding(start = 32.dp, top = 16.dp, end = 32.dp),
                        ) {
                            Text(stringResource(R.string.button_tutorial_completed_close))
                        }
                        OutlinedButton(
                            onClick = { dialog.dismiss() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                        ) {
                            Text(stringResource(R.string.button_tutorial_completed_keep_playing))
                        }
                    }
                }
            }
        }
    }
    dialog = MaterialAlertDialogBuilder(dialogContext).setView(content).create()
    return dialog
}
