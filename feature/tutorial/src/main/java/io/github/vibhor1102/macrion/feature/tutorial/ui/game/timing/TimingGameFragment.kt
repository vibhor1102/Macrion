/*
 * Copyright (C) 2026 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.tutorial.ui.game.timing

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController

import io.github.vibhor1102.macrion.core.common.overlays.manager.OverlayManager
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.tutorial.ui.dialogs.createTutorialSuccessDialog
import io.github.vibhor1102.macrion.feature.tutorial.ui.overlay.TutorialFullscreenOverlay

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimingGameFragment : Fragment() {

    private val viewModel: TimingGameViewModel by viewModels()
    private var showOverlayMenuPlaceholder by mutableStateOf(true)
    private var overlayMenuPosition: IntOffset? = null

    @Inject lateinit var overlayManager: OverlayManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    Surface(Modifier.fillMaxSize()) {
                        TimingGameScreen(
                            uiStateFlow = viewModel.uiState,
                            showOverlayMenuPlaceholder = showOverlayMenuPlaceholder,
                            onOverlayMenuPositioned = ::onOverlayMenuPositioned,
                            onTimingClick = viewModel::onTimingButtonHit,
                            onRetryClick = viewModel::resetGame,
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.shouldDisplayStepOverlay.collect(::showHideStepOverlay) }
                launch { viewModel.shouldDisplayFloatingUi.collect(::showHideFloatingUi) }
                launch { viewModel.shouldDisplayCompletionDialog.collect(::showCompletionDialog) }
                launch {
                    viewModel.shouldStopGame.collect { shouldStop ->
                        if (shouldStop) findNavController().navigateUp()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        overlayMenuPosition?.let(::lockMenuPosition)
        if (viewModel.shouldDisplayFloatingUi.value && overlayManager.isOverlayStackHidden()) {
            overlayManager.restoreVisibility()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopDetection()

        if (viewModel.shouldDisplayFloatingUi.value) {
            overlayManager.hideAll()
        }
        overlayManager.removeTopOverlay()
        overlayManager.unlockMenuPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopTutorial()
    }

    private fun showHideStepOverlay(show: Boolean) {
        showOverlayMenuPlaceholder = !overlayManager.isOverlayStackVisible()

        overlayManager.apply {
            if (show) setTopOverlay(TutorialFullscreenOverlay())
            else removeTopOverlay()
        }
    }

    private fun showHideFloatingUi(show: Boolean) {
        overlayManager.apply {
            if (show) restoreVisibility()
            else hideAll()
        }
    }

    private fun showCompletionDialog(show: Boolean) {
        if (!show) return

        requireContext().createTutorialSuccessDialog {
            findNavController().navigateUp()
        }.show()
    }

    private fun onOverlayMenuPositioned(position: IntOffset) {
        overlayMenuPosition = position
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) lockMenuPosition(position)
    }

    private fun lockMenuPosition(position: IntOffset) {
        overlayManager.lockMenuPosition(
            Point(position.x, position.y)
        )
    }
}
