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
package io.github.vibhor1102.macrion.feature.tutorial.ui.dialogs

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

import io.github.vibhor1102.macrion.core.common.tutorial.domain.TutorialRepository
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.Tip
import io.github.vibhor1102.macrion.core.ui.utils.getDynamicColorsContext
import io.github.vibhor1102.macrion.feature.tutorial.R
import io.github.vibhor1102.macrion.feature.tutorial.databinding.DialogStopWithVolumeDownBinding

import com.google.android.material.dialog.MaterialAlertDialogBuilder


internal fun Context.createStopWithVolumeDownTutorialDialog(
    tutorialRepository: TutorialRepository,
    onDismissed: (() -> Unit)?,
): AlertDialog {

    val dialogContext = getDynamicColorsContext(R.style.AppTheme)
    val dialogViewBinding = DialogStopWithVolumeDownBinding.inflate(LayoutInflater.from(dialogContext))
    val dialog = MaterialAlertDialogBuilder(dialogContext)
        .setView(dialogViewBinding.root)
        .setOnDismissListener {
            val dontShowAgain = dialogViewBinding.buttonDontShowAgain.isChecked
            if (dontShowAgain) tutorialRepository.dontShowTipAgain(Tip.STOP_WITH_VOLUME_DOWN)

            onDismissed?.invoke()
        }
        .create()

    dialogViewBinding.apply {
        textDontShowAgain.setOnClickListener {
            buttonDontShowAgain.isChecked = !buttonDontShowAgain.isChecked
        }
        buttonUnderstood.setOnClickListener {
            dialog.dismiss()
        }
    }

    return dialog
}