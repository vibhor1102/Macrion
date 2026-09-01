/* Copyright (C) 2024 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.scenarios.list.copy

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTextField
import io.github.vibhor1102.macrion.core.ui.compose.MacrionDialogSurface
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

@AndroidEntryPoint
class ScenarioCopyDialog : DialogFragment() {
    companion object {
        const val FRAGMENT_TAG_COPY_DIALOG = "ScenarioCopyDialog"
        private const val FRAGMENT_ARG_KEY_SCENARIO_ID = ":copy:fragment_args_key_scenario_id"
        private const val FRAGMENT_ARG_KEY_IS_SMART = ":copy:fragment_args_key_is_smart"
        private const val FRAGMENT_ARG_KEY_DEFAULT_COPY_NAME = ":copy:fragment_args_key_default_copy_name"

        fun newInstance(scenarioId: Long, isSmart: Boolean, defaultName: String? = null) =
            ScenarioCopyDialog().apply {
                arguments = Bundle().apply {
                    putLong(FRAGMENT_ARG_KEY_SCENARIO_ID, scenarioId)
                    putBoolean(FRAGMENT_ARG_KEY_IS_SMART, isSmart)
                    defaultName?.let { putString(FRAGMENT_ARG_KEY_DEFAULT_COPY_NAME, it) }
                }
            }
    }

    private val viewModel: ScenarioCopyViewModel by viewModels()
    private val scenarioId by lazy { requireArguments().getLong(FRAGMENT_ARG_KEY_SCENARIO_ID) }
    private val isSmartScenario by lazy { requireArguments().getBoolean(FRAGMENT_ARG_KEY_IS_SMART) }
    private val defaultName by lazy { requireArguments().getString(FRAGMENT_ARG_KEY_DEFAULT_COPY_NAME) ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setCopyName(defaultName)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_copy_scenario)
            .setView(ComposeView(requireContext()).apply {
                setContent {
                    MacrionTheme {
                        MacrionDialogSurface { CopyNameField(viewModel, defaultName) }
                    }
                }
            })
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener { onConfirm() }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun onConfirm() {
        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
        viewModel.copyScenario(scenarioId, isSmartScenario, ::dismiss)
    }
}

@Composable
private fun CopyNameField(viewModel: ScenarioCopyViewModel, defaultName: String) {
    var name by remember { mutableStateOf(defaultName) }
    val isError by viewModel.copyNameError.collectAsStateWithLifecycle(false)
    val focusRequester = remember { FocusRequester() }
    MacrionTextField(
        value = name,
        onValueChange = { name = it; viewModel.setCopyName(it) },
        label = stringResource(R.string.default_click_name),
        isError = isError,
        maxLength = 60,
        modifier = Modifier.padding(16.dp).focusRequester(focusRequester),
    )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
