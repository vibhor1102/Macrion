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
package io.github.vibhor1102.macrion.feature.revenue.ui.paywall

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.revenue.ui.PaywallDialogContent

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
internal class PaywallFragment : DialogFragment() {

    companion object {
        /** Tag for ads loading dialog fragment. */
        const val FRAGMENT_TAG = "AdsLoadingDialog"
    }

    /** ViewModel providing the click scenarios data to the UI. */
    private val viewModel: AdsLoadingViewModel by viewModels()
    private val dialogState = mutableStateOf<DialogState?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.dialogState.collect(::updateDialogState) }
            }
        }

        viewModel.loadAdIfNeeded(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = ComposeView(requireContext()).apply {
            setContent {
                MacrionTheme {
                    PaywallDialogContent(
                        state = dialogState.value,
                        onTrial = {
                            viewModel.requestTrial()
                            dismiss()
                        },
                        onWatchAd = { activity?.let(viewModel::showAd) },
                        onPurchase = { activity?.let(viewModel::launchPlayStoreBillingFlow) },
                        onUnderstood = ::dismiss,
                    )
                }
            }
        }

        return BottomSheetDialog(requireContext()).apply {
            setContentView(content)
            setCancelable(false)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    this@PaywallFragment.dismiss()
                    true
                } else {
                    false
                }
            }

            create()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun updateDialogState(state: DialogState) {
        dialogState.value = state

        if (state == DialogState.AdWatched) {
            lifecycleScope.launch {
                delay(1.seconds)
                dismiss()
            }
        }
    }
}
