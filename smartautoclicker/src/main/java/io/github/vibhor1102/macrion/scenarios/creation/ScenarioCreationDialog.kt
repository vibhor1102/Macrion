/* Copyright (C) 2023 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.scenarios.creation

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTextField
import io.github.vibhor1102.macrion.core.ui.compose.MacrionDialogSurface
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

@AndroidEntryPoint
class ScenarioCreationDialog : DialogFragment() {
    companion object { const val FRAGMENT_TAG = "ScenarioCreationDialog" }
    private val viewModel: ScenarioCreationViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext()).apply {
            setCancelable(false)
            setContentView(ComposeView(context).apply {
                setContent {
                    MacrionTheme {
                        MacrionDialogSurface { ScenarioCreationContent(viewModel, ::dismiss) }
                    }
                }
            })
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dismiss(); true
                } else false
            }
            create()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
}

@Composable
private fun ScenarioCreationContent(viewModel: ScenarioCreationViewModel, onDismiss: () -> Unit) {
    val name by viewModel.name.collectAsStateWithLifecycle("")
    val nameError by viewModel.nameError.collectAsStateWithLifecycle(false)
    val selection by viewModel.scenarioTypeSelectionState.collectAsStateWithLifecycle(null)
    val creationState by viewModel.creationState.collectAsStateWithLifecycle(CreationState.CONFIGURING)
    val showWarning by viewModel.showPaidLimitationWarning.collectAsStateWithLifecycle(false)
    LaunchedEffect(creationState) {
        if (creationState == CreationState.SAVED) onDismiss()
    }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            Text(stringResource(R.string.dialog_title_add_scenario), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
            TextButton(onClick = viewModel::createScenario, enabled = creationState == CreationState.CONFIGURING) {
                Text(stringResource(android.R.string.ok))
            }
        }
        MacrionTextField(
            value = name,
            onValueChange = viewModel::setName,
            label = stringResource(R.string.input_field_label_scenario_name),
            isError = nameError,
            maxLength = 60,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        ScenarioTypeCard(ScenarioTypeItem.Dumb, selection?.selectedItem == ScenarioTypeSelection.DUMB, false) {
            viewModel.setSelectedType(ScenarioTypeSelection.DUMB)
        }
        ScenarioTypeCard(ScenarioTypeItem.Smart, selection?.selectedItem == ScenarioTypeSelection.SMART, showWarning) {
            viewModel.setSelectedType(ScenarioTypeSelection.SMART)
        }
    }
}

@Composable
private fun ScenarioTypeCard(item: ScenarioTypeItem, selected: Boolean, showWarning: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(painterResource(item.iconRes), contentDescription = null)
            Column {
                Text(stringResource(item.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(item.descriptionText), style = MaterialTheme.typography.bodySmall)
                if (showWarning && item is ScenarioTypeItem.Smart) {
                    Text(stringResource(R.string.item_desc_smart_scenario_pro_mode), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
