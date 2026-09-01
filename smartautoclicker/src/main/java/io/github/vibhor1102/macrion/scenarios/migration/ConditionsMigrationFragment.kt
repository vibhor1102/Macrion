/* Copyright (C) 2025 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.scenarios.migration

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.core.ui.bindings.buttons.LoadableButtonState
import io.github.vibhor1102.macrion.core.ui.compose.MacrionLoadableButton
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

@AndroidEntryPoint
class ConditionsMigrationFragment : DialogFragment() {
    companion object {
        const val FRAGMENT_TAG_CONDITION_MIGRATION_DIALOG = "ConditionsMigrationDialog"
        const val FRAGMENT_RESULT_KEY_COMPLETED = ":$FRAGMENT_TAG_CONDITION_MIGRATION_DIALOG:state"
        fun newInstance() = ConditionsMigrationFragment()
    }

    private val viewModel: ConditionsMigrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setView(ComposeView(requireContext()).apply {
                setContent {
                    MacrionTheme {
                        ConditionsMigrationContent(viewModel) {
                            setFragmentResult(FRAGMENT_RESULT_KEY_COMPLETED, Bundle.EMPTY)
                            dismiss()
                        }
                    }
                }
            })
            .create()
}

@Composable
private fun ConditionsMigrationContent(viewModel: ConditionsMigrationViewModel, onFinished: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(null)
    val buttonState = state?.buttonState
    Column(
        Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.dialog_condition_migration_title), style = MaterialTheme.typography.titleLarge)
        HorizontalDivider()
        Text(stringResource(R.string.message_condition_migration_desc), modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center)
        state?.let { Text(it.textState, modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center) }
        MacrionLoadableButton(
            text = buttonState?.text.orEmpty(),
            loading = buttonState is LoadableButtonState.Loading,
            enabled = buttonState is LoadableButtonState.Loaded.Enabled,
        ) {
            when (state?.migrationState) {
                MigrationState.NOT_STARTED -> viewModel.startMigration()
                MigrationState.FINISHED, MigrationState.FINISHED_WITH_ERROR -> onFinished()
                else -> Unit
            }
        }
    }
}
