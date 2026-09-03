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
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.common.overlays.R
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
    val layoutInstructions: ComposeView,
    val layoutActionList: View,
    val listActions: RecyclerView,
    val emptyScenarioCard: View,
    private val controlPanel: ComposeView,
    private val orientation: Int,
) {

    val recordingIcon = ImageView(root.context).apply {
        setImageResource(UiR.drawable.ic_recording)
    }
    private val emptyText = mutableIntStateOf(0)
    private val controlState = mutableStateOf(ItemBriefControlsState())

    private var onMovePrevious: () -> Unit = {}
    private var onDelete: () -> Unit = {}
    private var onPosition: () -> Unit = {}
    private var onPlay: () -> Unit = {}
    private var onMoveNext: () -> Unit = {}

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
        layoutActionList = binding.layoutActionList,
        listActions = binding.listActions,
        emptyScenarioCard = binding.emptyScenarioCard,
        controlPanel = binding.controlPanel,
        orientation = Configuration.ORIENTATION_PORTRAIT,
    ) {
        binding.backgroundList.setFade(FadeDirection.BOTTOM)
        binding.emptyScenarioCard.setEmptyContent(emptyText)
        setInstructionsContent(isPortrait = true)
        setControlPanelContent()
    }

    constructor(binding: OverlayViewActionBriefLandBinding) : this(
        root = binding.root,
        viewBrief = binding.viewBrief,
        viewRecorder = binding.viewRecord,
        layoutInstructions = binding.layoutInstructions,
        layoutActionList = binding.layoutActionList,
        listActions = binding.listActions,
        emptyScenarioCard = binding.emptyScenarioCard,
        controlPanel = binding.controlPanel,
        orientation = Configuration.ORIENTATION_LANDSCAPE,
    ) {
        binding.backgroundList.setFade(FadeDirection.LEFT)
        binding.emptyScenarioCard.setEmptyContent(emptyText)
        setInstructionsContent(isPortrait = false)
        setControlPanelContent()
    }

    fun setEmptyText(textRes: Int) {
        emptyText.intValue = textRes
    }

    fun setControlCallbacks(
        onMovePrevious: () -> Unit,
        onDelete: () -> Unit,
        onPosition: () -> Unit,
        onPlay: () -> Unit,
        onMoveNext: () -> Unit,
    ) {
        this.onMovePrevious = onMovePrevious
        this.onDelete = onDelete
        this.onPosition = onPosition
        this.onPlay = onPlay
        this.onMoveNext = onMoveNext
    }

    fun updateControls(state: ItemBriefControlsState) {
        controlState.value = state
    }

    private fun setControlPanelContent() {
        controlPanel.setContent {
            MacrionTheme {
                ItemBriefControls(
                    state = controlState.value,
                    isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT,
                    onMovePrevious = { onMovePrevious() },
                    onDelete = { onDelete() },
                    onPosition = { onPosition() },
                    onPlay = { onPlay() },
                    onMoveNext = { onMoveNext() },
                )
            }
        }
    }

    private fun setInstructionsContent(isPortrait: Boolean) {
        layoutInstructions.setContent {
            val colors = arrayOf(
                0f to Color.Black,
                0.7f to Color.Black.copy(alpha = 0.53f),
                1f to Color.Transparent,
            )
            MacrionTheme {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colorStops = colors))
                        .padding(
                            start = 32.dp,
                            top = if (isPortrait) 12.dp else 8.dp,
                            end = 32.dp,
                            bottom = 24.dp,
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AndroidView(factory = { recordingIcon }, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(if (isPortrait) 8.dp else 16.dp))
                    Text(
                        text = stringResource(R.string.overlay_instructions_gesture_record),
                        color = colorResource(UiR.color.overlayViewPrimary),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Immutable
data class ItemBriefControlsState(
    val indexText: String = "",
    val canMovePrevious: Boolean = false,
    val canDelete: Boolean = false,
    val canSelectPosition: Boolean = false,
    val canPlay: Boolean = false,
    val canMoveNext: Boolean = false,
)

@androidx.compose.runtime.Composable
private fun ItemBriefControls(
    state: ItemBriefControlsState,
    isPortrait: Boolean,
    onMovePrevious: () -> Unit,
    onDelete: () -> Unit,
    onPosition: () -> Unit,
    onPlay: () -> Unit,
    onMoveNext: () -> Unit,
) {
    if (isPortrait) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BriefIconButton(UiR.drawable.ic_move_left, state.canMovePrevious, onMovePrevious)
            Spacer(Modifier.width(16.dp))
            BriefIconButton(UiR.drawable.ic_delete, state.canDelete, onDelete)
            Spacer(Modifier.width(32.dp))
            PositionCard(state, onPosition, Modifier.weight(1f).height(48.dp))
            Spacer(Modifier.width(32.dp))
            BriefIconButton(UiR.drawable.ic_play_arrow, state.canPlay, onPlay)
            Spacer(Modifier.width(16.dp))
            BriefIconButton(UiR.drawable.ic_move_right, state.canMoveNext, onMoveNext)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BriefIconButton(UiR.drawable.ic_move_up, state.canMovePrevious, onMovePrevious)
            Spacer(Modifier.height(16.dp))
            BriefIconButton(UiR.drawable.ic_delete, state.canDelete, onDelete)
            Spacer(Modifier.height(28.dp))
            PositionCard(state, onPosition, Modifier.width(48.dp))
            Spacer(Modifier.height(28.dp))
            BriefIconButton(UiR.drawable.ic_play_arrow, state.canPlay, onPlay)
            Spacer(Modifier.height(16.dp))
            BriefIconButton(UiR.drawable.ic_move_down, state.canMoveNext, onMoveNext)
        }
    }
}

@androidx.compose.runtime.Composable
private fun BriefIconButton(icon: Int, enabled: Boolean, onClick: () -> Unit) {
    FilledTonalIconButton(onClick = onClick, enabled = enabled) {
        Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

@androidx.compose.runtime.Composable
private fun PositionCard(state: ItemBriefControlsState, onClick: () -> Unit, modifier: Modifier) {
    ElevatedCard(onClick = onClick, enabled = state.canSelectPosition, modifier = modifier) {
        Box(Modifier.fillMaxSize().padding(vertical = if (state.indexText.contains('\n')) 8.dp else 0.dp), contentAlignment = Alignment.Center) {
            Text(
                text = state.indexText,
                color = colorResource(UiR.color.overlayViewPrimary).copy(
                    alpha = if (state.canSelectPosition) 1f else 0.38f,
                ),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = if (state.indexText.contains('\n')) 3 else 1,
            )
        }
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
