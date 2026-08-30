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
package io.github.vibhor1102.macrion.feature.tutorial.navigation

import android.content.Context
import android.content.Intent

import io.github.vibhor1102.macrion.core.common.navigation.TutorialNavigator
import io.github.vibhor1102.macrion.core.common.overlays.manager.OverlayManager.Companion.showAsOverlay
import io.github.vibhor1102.macrion.core.common.tutorial.domain.TutorialRepository
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.Tip
import io.github.vibhor1102.macrion.feature.tutorial.domain.model.TutorialSlideshow
import io.github.vibhor1102.macrion.feature.tutorial.ui.TutorialActivity
import io.github.vibhor1102.macrion.feature.tutorial.ui.dialogs.createStopWithVolumeDownTutorialDialog
import io.github.vibhor1102.macrion.feature.tutorial.ui.slideshow.createTutorialSlideshowDialog

import javax.inject.Inject

internal class TutorialNavigatorImpl @Inject constructor(
    private val tutorialRepository: TutorialRepository,
) : TutorialNavigator {

    override fun startTutorialActivity(context: Context) {
        context.startActivity(Intent(context, TutorialActivity::class.java))
    }

    override fun showTipDialog(context: Context, tip: Tip, onDismissed: (() -> Unit)?) {
        when (tip) {
            Tip.STOP_WITH_VOLUME_DOWN -> context.createStopWithVolumeDownTutorialDialog(
                tutorialRepository = tutorialRepository,
                onDismissed = onDismissed,
            ).showAsOverlay()

            Tip.IMAGE_CAPTURE -> context.createTutorialSlideshowDialog(
                slideshowType = TutorialSlideshow.Type.IMAGE_CONDITION_CAPTURE,
                onDismissed = onDismissed,
            )?.showAsOverlay()

            Tip.IMAGE_DETECTION_AREA -> context.createTutorialSlideshowDialog(
                slideshowType = TutorialSlideshow.Type.IMAGE_CONDITION_DETECTION_AREA,
                pageIndex = 1,
                onDismissed = onDismissed,
            )?.showAsOverlay()

            Tip.NUMBER_DETECTION_AREA -> context.createTutorialSlideshowDialog(
                slideshowType = TutorialSlideshow.Type.NUMBER_CONDITION_DETECTION_AREA,
                onDismissed = onDismissed,
            )?.showAsOverlay()

            Tip.TEXT_DETECTION_AREA -> context.createTutorialSlideshowDialog(
                slideshowType = TutorialSlideshow.Type.TEXT_CONDITION_DETECTION_AREA,
                onDismissed = onDismissed,
            )?.showAsOverlay()
        }
    }
}
