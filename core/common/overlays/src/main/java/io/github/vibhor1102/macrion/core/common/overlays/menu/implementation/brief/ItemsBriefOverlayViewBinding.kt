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
package io.github.vibhor1102.macrion.core.common.overlays.menu.implementation.brief

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.common.overlays.databinding.OverlayViewActionBriefLandBinding
import io.github.vibhor1102.macrion.core.common.overlays.databinding.OverlayViewActionBriefPortBinding
import io.github.vibhor1102.macrion.core.ui.views.gesturerecord.GestureRecordView
import io.github.vibhor1102.macrion.core.ui.views.itembrief.ItemBriefView
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.R as UiR

class ItemsBriefOverlayViewBinding private constructor(
    val root: View,
    val viewBrief: ItemBriefView,
    val viewRecorder: GestureRecordView,
    val layoutInstructions: View,
    val recordingIcon: ImageView,
    val layoutActionList: View,
    val listActions: RecyclerView,
    val textActionIndex: TextView,
    val buttonMovePrevious: Button,
    val buttonMoveNext: Button,
    val buttonDelete: Button,
    val buttonPlay: Button,
    val emptyScenarioCard: View,
) {

    private val emptyText = mutableIntStateOf(0)

    companion object {

        fun inflate(inflater: LayoutInflater, orientation: Int) =
            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                ItemsBriefOverlayViewBinding(OverlayViewActionBriefPortBinding.inflate(inflater))
            else
                ItemsBriefOverlayViewBinding(OverlayViewActionBriefLandBinding.inflate(inflater))
    }

    constructor(binding: OverlayViewActionBriefPortBinding) : this(
        root = binding.root,
        viewBrief = binding.viewBrief,
        viewRecorder = binding.viewRecord,
        layoutInstructions = binding.layoutInstructions,
        recordingIcon = binding.iconRecording,
        layoutActionList = binding.layoutActionList,
        listActions = binding.listActions,
        textActionIndex = binding.textActionIndex,
        buttonMovePrevious = binding.buttonMovePrevious,
        buttonMoveNext = binding.buttonMoveNext,
        buttonDelete = binding.buttonDelete,
        buttonPlay = binding.buttonPlayAction,
        emptyScenarioCard = binding.emptyScenarioCard,
    ) {
        binding.backgroundInstructions.setFade(FadeDirection.TOP)
        binding.backgroundList.setFade(FadeDirection.BOTTOM)
        binding.emptyScenarioCard.setEmptyContent(emptyText)
    }

    constructor(binding: OverlayViewActionBriefLandBinding) : this(
        root = binding.root,
        viewBrief = binding.viewBrief,
        viewRecorder = binding.viewRecord,
        layoutInstructions = binding.layoutInstructions,
        recordingIcon = binding.iconRecording,
        layoutActionList = binding.layoutActionList,
        listActions = binding.listActions,
        textActionIndex = binding.textActionIndex,
        buttonMovePrevious = binding.buttonMovePrevious,
        buttonMoveNext = binding.buttonMoveNext,
        buttonDelete = binding.buttonDelete,
        buttonPlay = binding.buttonPlayAction,
        emptyScenarioCard = binding.emptyScenarioCard,
    ) {
        binding.backgroundInstructions.setFade(FadeDirection.TOP)
        binding.backgroundList.setFade(FadeDirection.LEFT)
        binding.emptyScenarioCard.setEmptyContent(emptyText)
    }

    fun setEmptyText(textRes: Int) {
        emptyText.intValue = textRes
    }
}

private enum class FadeDirection { TOP, BOTTOM, LEFT }

private fun ComposeView.setFade(direction: FadeDirection) {
    isClickable = false
    setContent {
        val opaque = Color.Black
        val middle = Color.Black.copy(alpha = 0.53f)
        val transparent = Color.Transparent
        val colors = when (direction) {
            FadeDirection.TOP -> arrayOf(0f to opaque, 0.7f to middle, 1f to transparent)
            FadeDirection.BOTTOM -> arrayOf(0f to transparent, 0.3f to middle, 1f to opaque)
            FadeDirection.LEFT -> arrayOf(0f to opaque, 0.7f to middle, 1f to transparent)
        }
        val brush = if (direction == FadeDirection.LEFT) {
            Brush.horizontalGradient(colorStops = colors)
        } else {
            Brush.verticalGradient(colorStops = colors)
        }
        Box(Modifier.fillMaxSize().background(brush))
    }
}

private fun ComposeView.setEmptyContent(textState: androidx.compose.runtime.MutableIntState) {
    setContent {
        MacrionTheme {
            ElevatedCard(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (textState.intValue != 0) {
                        Text(
                            text = stringResource(textState.intValue),
                            color = colorResource(UiR.color.overlayViewPrimary).copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
