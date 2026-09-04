/* Copyright (C) 2026 Kevin Buzeau; Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.condition.screen.text.alphabet.required

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class RequiredAlphabetFragment : BottomSheetDialogFragment() {
    companion object { const val FRAGMENT_TAG = "RequiredAlphabetFragment" }

    private val viewModel: RequiredAlphabetViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    val items = viewModel.items.collectAsStateWithLifecycle(initialValue = emptyList()).value
                    val canContinue = viewModel.canContinue.collectAsStateWithLifecycle(initialValue = false).value
                    AlphabetModelSheet(items, true, canContinue, ::dismiss, ::onItemClicked)
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
        if (item.downloadState == AlphabetDownloadUiState.NotDownloaded) viewModel.downloadModel(item.alphabet)
    }
}
