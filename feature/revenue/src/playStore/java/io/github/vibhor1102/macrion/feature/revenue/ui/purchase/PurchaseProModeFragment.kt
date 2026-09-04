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
package io.github.vibhor1102.macrion.feature.revenue.ui.purchase

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

import io.github.vibhor1102.macrion.core.base.extensions.safeStartWebBrowserActivity
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.revenue.ui.PurchaseDialogContent
import com.google.android.material.bottomsheet.BottomSheetBehavior

import com.google.android.material.bottomsheet.BottomSheetDialog

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class PurchaseProModeFragment : DialogFragment() {

    companion object {
        /** Tag for ads loading dialog fragment. */
        const val FRAGMENT_TAG = "PurchaseProModeFragment"
    }

    private val viewModel: PurchaseProModeViewModel by viewModels()
    private val dialogState = mutableStateOf<PurchaseDialogState>(PurchaseDialogState.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.dialogState.collect(::updateDialogState) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val content = ComposeView(requireContext()).apply {
            setContent {
                MacrionTheme {
                    PurchaseDialogContent(
                        state = dialogState.value,
                        onOpenSource = {
                            context.safeStartWebBrowserActivity("https://github.com/vibhor1102/Macrion")
                        },
                        onPurchase = { viewModel.launchPlayStoreBillingFlow(requireActivity()) },
                    )
                }
            }
        }

        return BottomSheetDialog(requireContext()).apply {
            setContentView(content)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    this@PurchaseProModeFragment.dismiss()
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

    private fun updateDialogState(state: PurchaseDialogState) {
        dialogState.value = state
    }
}
