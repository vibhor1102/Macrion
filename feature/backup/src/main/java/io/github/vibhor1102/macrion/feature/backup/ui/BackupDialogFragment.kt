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
package io.github.vibhor1102.macrion.feature.backup.ui

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.github.vibhor1102.macrion.core.ui.compose.MacrionDialogSurface
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

import io.github.vibhor1102.macrion.feature.backup.R

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/** Fragment displaying the state of a backup (import or export). */
@AndroidEntryPoint
class BackupDialogFragment : DialogFragment() {

    companion object {

        /** Tag for backup dialog fragment. */
        const val FRAGMENT_TAG_BACKUP_DIALOG = "BackupDialog"
        /** Key for this fragment argument. Tells if the backup is an import (true) or export (false). */
        private const val FRAGMENT_ARG_KEY_IS_IMPORT = ":backup:fragment_args_key_is_import"
        /** Key for this fragment argument. Contains the list of scenario identifier to export (LongArray). */
        private const val FRAGMENT_ARG_KEY_SCENARIO_LIST = ":backup:fragment_args_key_scenario_list"
        /** Key for this fragment argument. Contains the list of dumb scenario identifier to export (LongArray). */
        private const val FRAGMENT_ARG_KEY_DUMB_SCENARIO_LIST = ":backup:fragment_args_key_dumb_scenario_list"

        /**
         * Creates a new instance of this fragment.
         * @param isImport true for an import, false for an export.
         * @param exportSmartScenarios the list of scenario identifier to be exported. Ignored for import.
         * @param exportDumbScenarios the list of dumb scenario identifier to be exported. Ignored for import.
         * @return the new fragment.
         */
        fun newInstance(
            isImport: Boolean,
            exportSmartScenarios: Collection<Long>? = null,
            exportDumbScenarios: Collection<Long>? = null,
        ) : BackupDialogFragment {
            return BackupDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(FRAGMENT_ARG_KEY_IS_IMPORT, isImport)
                    exportSmartScenarios?.let {
                        putLongArray(FRAGMENT_ARG_KEY_SCENARIO_LIST, it.toLongArray())
                    }
                    exportDumbScenarios?.let {
                        putLongArray(FRAGMENT_ARG_KEY_DUMB_SCENARIO_LIST, it.toLongArray())
                    }
                }
            }
        }
    }

    /** The view model containing the backup state. */
    private val backupViewModel: BackupViewModel by viewModels()
    /** The result launcher for the file picker activity. Provides the uri for the backup file. */
    private lateinit var backupActivityResult: ActivityResultLauncher<Intent>

    /** Fragment argument. True for import, false for export. */
    private val isImport: Boolean by lazy { arguments?.getBoolean(FRAGMENT_ARG_KEY_IS_IMPORT)?: false }
    /** Fragment argument, export only. The list of scenario identifier to be exported. */
    private val exportSmartScenarios: List<Long> by lazy {
        arguments?.getLongArray(FRAGMENT_ARG_KEY_SCENARIO_LIST)?.toList() ?: emptyList()
    }
    /** Fragment argument, export only. The list of dumb scenario identifier to be exported. */
    private val exportDumbScenarios: List<Long> by lazy {
        arguments?.getLongArray(FRAGMENT_ARG_KEY_DUMB_SCENARIO_LIST)?.toList() ?: emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backupViewModel.initialize(requireContext(), isImport)

        backupActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    backupViewModel.startBackup(requireContext(), uri, isImport, exportDumbScenarios, exportSmartScenarios)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    MacrionDialogSurface {
                        BackupDialogContent(
                            title = getString(
                                if (isImport) R.string.dialog_title_import_backup
                                else R.string.dialog_title_create_backup
                            ),
                            stateFlow = backupViewModel.backupState,
                            onFileSelection = ::onFileSelection,
                            onKlickrCompatibleChanged = {
                                backupViewModel.setKlickrCompatibleExport(requireContext(), it)
                            },
                            onConfirm = ::onConfirm,
                            onCancel = ::dismiss,
                        )
                    }
                }
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(content)
            .setCancelable(false)
            .create()
    }

    private fun onFileSelection(state: BackupDialogUiState) {
        if (state.requiresCompatibilityPreparation) {
            backupViewModel.prepareKlickrCompatibleExport(
                requireContext(),
                exportDumbScenarios,
                exportSmartScenarios,
            )
        } else {
            launchDocumentPickerOrShowError()
        }
    }

    private fun onConfirm(state: BackupDialogUiState) {
        if (state.compatibilityReviewReady) launchDocumentPickerOrShowError()
        else dismiss()
    }

    private fun launchDocumentPickerOrShowError() {
        if (!launchDocumentPicker()) {
            Toast.makeText(context, R.string.message_backup_error_no_zip_app, Toast.LENGTH_LONG).show()
        }
    }

    private fun launchDocumentPicker(): Boolean =
        try {
            backupActivityResult.launch(
                if (isImport) backupViewModel.createBackupRestorationFileSelectionIntent()
                else backupViewModel.createBackupFileCreationIntent()
            )
            true
        } catch (anfex: ActivityNotFoundException) {
            Log.e(TAG, "No application found to load/save a zip file.")
            false
        }
}

private const val TAG = "BackupDialogFragment"
