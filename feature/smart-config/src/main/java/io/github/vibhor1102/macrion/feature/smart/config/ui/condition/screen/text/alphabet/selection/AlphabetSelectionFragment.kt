/* Copyright (C) 2026 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet.selection

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet.AlphabetDownloadUiState
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet.AlphabetModelSheet
import io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet.AlphabetSelectionItem

@AndroidEntryPoint
class AlphabetSelectionFragment : BottomSheetDialogFragment() {
    companion object { const val FRAGMENT_TAG = "AlphabetSelectionFragment" }

    private val viewModel: AlphabetSelectionViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    val items = viewModel.items.collectAsStateWithLifecycle(initialValue = emptyList()).value
                    val isEditing = viewModel.isEditingCondition.collectAsStateWithLifecycle(initialValue = true).value
                    LaunchedEffect(isEditing) {
                        if (!isEditing) {
                            Log.e(TAG, "Closing AlphabetSelectionFragment because there is no condition edited")
                            dismiss()
                        }
                    }
                    AlphabetModelSheet(items, false, true, ::dismiss, ::onItemClicked)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    private fun onItemClicked(item: AlphabetSelectionItem) {
        if (item !is AlphabetSelectionItem.Alphabet) return
        when (item.downloadState) {
            AlphabetDownloadUiState.Downloaded -> viewModel.selectModel(item.alphabet)
            AlphabetDownloadUiState.NotDownloaded -> viewModel.downloadModel(item.alphabet)
            is AlphabetDownloadUiState.Downloading, AlphabetDownloadUiState.Error -> Unit
        }
    }
}

private const val TAG = "AlphabetSelectionFragment"
