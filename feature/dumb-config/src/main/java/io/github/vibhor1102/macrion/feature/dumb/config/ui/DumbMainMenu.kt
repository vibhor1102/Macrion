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
package io.github.vibhor1102.macrion.feature.dumb.config.ui

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.base.identifier.Identifier
import io.github.vibhor1102.macrion.core.base.isStopScenarioKey
import io.github.vibhor1102.macrion.core.common.navigation.TutorialNavigator
import io.github.vibhor1102.macrion.core.common.navigation.getTutorialNavigator
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.menu.OverlayMenu
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.Tip
import io.github.vibhor1102.macrion.core.ui.utils.AnimatedStatesImageButtonController
import io.github.vibhor1102.macrion.feature.dumb.config.R
import io.github.vibhor1102.macrion.feature.dumb.config.databinding.OverlayDumbMainMenuBinding
import io.github.vibhor1102.macrion.feature.dumb.config.di.DumbConfigViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.dumb.config.ui.brief.DumbScenarioBriefMenu
import io.github.vibhor1102.macrion.feature.dumb.config.ui.scenario.DumbScenarioDialog

import kotlinx.coroutines.launch

class DumbMainMenu(
    private val dumbScenarioId: Identifier,
    private val onStopClicked: () -> Unit,
) : OverlayMenu(theme = R.style.AppTheme) {

    /** The view model for this menu. */
    private val viewModel: DumbMainMenuModel by viewModels(
        entryPoint = DumbConfigViewModelsEntryPoint::class.java,
        creator = { dumbMainMenuModel() },
    )

    private val tutorialNavigator: TutorialNavigator by lazy {
        context.getTutorialNavigator()
    }

    /** View binding for the content of the overlay. */
    private lateinit var viewBinding: OverlayDumbMainMenuBinding
    /** Controls the animations of the play/pause button. */
    private lateinit var playPauseButtonController: AnimatedStatesImageButtonController

    /**
     * Tells if this service has handled onKeyEvent with ACTION_DOWN for a key in order to return
     * the correct value when ACTION_UP is received.
     */
    private var keyDownHandled: Boolean = false

    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isPlaying.collect(::updateMenuPlayingState) }
                launch { viewModel.canPlay.collect(::updatePlayPauseButtonEnabledState) }
            }
        }
    }

    override fun onCreateMenu(layoutInflater: LayoutInflater): ViewGroup {
        playPauseButtonController = AnimatedStatesImageButtonController(
            context = context,
            state1StaticRes = R.drawable.ic_play_arrow,
            state2StaticRes = R.drawable.ic_pause,
            state1to2AnimationRes = R.drawable.anim_play_pause,
            state2to1AnimationRes = R.drawable.anim_pause_play,
        )

        viewBinding = OverlayDumbMainMenuBinding.inflate(layoutInflater).apply {
            playPauseButtonController.attachView(btnPlay)
        }

        return viewBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        playPauseButtonController.detachView()
        viewModel.stopEdition()
    }

    override fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        if (!keyEvent.isStopScenarioKey()) return false

        when (keyEvent.action) {
            KeyEvent.ACTION_DOWN -> {
                if (viewModel.stopScenarioPlay()) {
                    keyDownHandled = true
                    return true
                }
            }

            KeyEvent.ACTION_UP -> {
                if (keyDownHandled) {
                    keyDownHandled = false
                    return true
                }
            }
        }

        return false
    }

    /** Refresh the play menu item according to the scenario state. */
    private fun updatePlayPauseButtonEnabledState(canStartDetection: Boolean) =
        setMenuItemViewEnabled(viewBinding.btnPlay, canStartDetection)

    private fun updateMenuPlayingState(isPlaying: Boolean) {
        val currentState = viewBinding.btnPlay.tag
        if (currentState == isPlaying) return

        viewBinding.btnPlay.tag = isPlaying
        if (isPlaying) {
            if (currentState == null) {
                playPauseButtonController.toState2(false)
            } else {
                animateLayoutChanges {
                    setMenuItemVisibility(viewBinding.btnStop, false)
                    setMenuItemVisibility(viewBinding.btnShowActions, false)
                    setMenuItemVisibility(viewBinding.btnActionList, false)
                    playPauseButtonController.toState2(true)
                }
            }
        } else {
            if (currentState == null) {
                playPauseButtonController.toState1(false)
            } else {
                animateLayoutChanges {
                    setMenuItemVisibility(viewBinding.btnStop, true)
                    setMenuItemVisibility(viewBinding.btnShowActions, true)
                    setMenuItemVisibility(viewBinding.btnActionList, true)
                    playPauseButtonController.toState1(true)
                }
            }
        }
    }

    override fun onMenuItemClicked(viewId: Int) {
        when (viewId) {
            R.id.btn_play -> onPlayPauseClicked()
            R.id.btn_stop -> onStopClicked()
            R.id.btn_show_actions -> onShowBriefClicked()
            R.id.btn_action_list -> onDumbScenarioConfigClicked()
        }
    }

    private fun onPlayPauseClicked() {
        if (viewModel.shouldShowStopVolumeDownTutorialDialog()) {
            showStopVolumeDownTutorialDialog()
            return
        }

        viewModel.toggleScenarioPlay()
    }

    private fun onShowBriefClicked() {
        viewModel.startEdition(dumbScenarioId) {
            overlayManager.navigateTo(
                context = context,
                newOverlay = DumbScenarioBriefMenu(
                    onConfigSaved = viewModel::saveEditions
                ),
                hideCurrent = true,
            )
        }
    }

    private fun onDumbScenarioConfigClicked() {
        viewModel.startEdition(dumbScenarioId) {
            overlayManager.navigateTo(
                context = context,
                newOverlay = DumbScenarioDialog(
                    onConfigSaved = viewModel::saveEditions,
                    onConfigDiscarded = viewModel::stopEdition,
                ),
                hideCurrent = true,
            )
        }
    }

    private fun showStopVolumeDownTutorialDialog() {
        tutorialNavigator.showTipDialog(context, Tip.STOP_WITH_VOLUME_DOWN) {
            viewModel.toggleScenarioPlay()
        }
    }
}