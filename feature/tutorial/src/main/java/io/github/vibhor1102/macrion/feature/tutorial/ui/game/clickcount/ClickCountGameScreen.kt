/*
 * Copyright (C) 2026 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.tutorial.ui.game.clickcount

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.tutorial.subject.quickclickgame.QuickClickGameTargetState
import io.github.vibhor1102.macrion.core.common.tutorial.domain.model.tutorial.subject.quickclickgame.QuickClickGameTargetType
import io.github.vibhor1102.macrion.feature.tutorial.R
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

@Composable
internal fun ClickCountGameScreen(
    uiStateFlow: StateFlow<ClickCountGameUiState?>,
    showOverlayMenuPlaceholder: Boolean,
    onOverlayMenuPositioned: (IntOffset) -> Unit,
    onTargetHit: (QuickClickGameTargetType) -> Unit,
    onStartGame: () -> Unit,
) {
    val state by uiStateFlow.collectAsStateWithLifecycle()
    val uiState = state ?: return
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) {
        LandscapeGame(uiState, showOverlayMenuPlaceholder, onOverlayMenuPositioned, onTargetHit, onStartGame)
    } else {
        PortraitGame(uiState, showOverlayMenuPlaceholder, onOverlayMenuPositioned, onTargetHit, onStartGame)
    }
}

@Composable
private fun PortraitGame(
    state: ClickCountGameUiState,
    showPlaceholder: Boolean,
    onPositioned: (IntOffset) -> Unit,
    onTargetHit: (QuickClickGameTargetType) -> Unit,
    onStartGame: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OverlayPlaceholder(showPlaceholder, onPositioned)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Instructions(state.instructionsResId)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Stat(stringResource(R.string.message_score, state.gameScore), Modifier.weight(1f))
                    Stat(stringResource(R.string.message_high_score, state.highScore), Modifier.weight(1f))
                }
            }
        }
        GameArea(state, onTargetHit, onStartGame, Modifier.weight(1f))
        Timer(state)
    }
}

@Composable
private fun LandscapeGame(
    state: ClickCountGameUiState,
    showPlaceholder: Boolean,
    onPositioned: (IntOffset) -> Unit,
    onTargetHit: (QuickClickGameTargetType) -> Unit,
    onStartGame: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OverlayPlaceholder(showPlaceholder, onPositioned)
            Instructions(state.instructionsResId, Modifier.weight(1f))
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GameArea(state, onTargetHit, onStartGame, Modifier.weight(1f))
            Column(Modifier.width(175.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat(stringResource(R.string.message_score, state.gameScore))
                Stat(stringResource(R.string.message_high_score, state.highScore))
                Timer(state)
            }
        }
    }
}

@Composable
private fun OverlayPlaceholder(show: Boolean, onPositioned: (IntOffset) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .size(
                width = dimensionResource(R.dimen.tutorial_overlay_menu_width),
                height = dimensionResource(R.dimen.tutorial_overlay_menu_height),
            )
            .alpha(if (show) 1f else 0f)
            .onGloballyPositioned {
                val p = it.positionInWindow()
                onPositioned(IntOffset(p.x.roundToInt(), p.y.roundToInt()))
            },
    ) {}
}

@Composable
private fun Instructions(resId: Int, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Text(stringResource(resId), Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Stat(text: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Text(text, Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Timer(state: ClickCountGameUiState) {
    val transition = rememberInfiniteTransition(label = "timerBlink")
    val blinkingAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "timerAlpha",
    )
    Card(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.message_time_left, state.timerValue),
            modifier = Modifier.fillMaxWidth().padding(16.dp).alpha(if (state.isGameStarted) blinkingAlpha else 1f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GameArea(
    state: ClickCountGameUiState,
    onTargetHit: (QuickClickGameTargetType) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier,
) {
    OutlinedCard(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (state.isGameStarted) {
                state.targets.forEach { (type, targetState) ->
                    GameTarget(type, targetState, maxWidth, maxHeight) { onTargetHit(type) }
                }
            } else {
                Button(onClick = onStartGame, modifier = Modifier.align(Alignment.Center)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(stringResource(R.string.button_text_tutorial_start_game), Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun GameTarget(
    type: QuickClickGameTargetType,
    state: QuickClickGameTargetState,
    areaWidth: Dp,
    areaHeight: Dp,
    onClick: () -> Unit,
) {
    val imageTarget = type.name.startsWith("IMAGE_")
    val width = if (imageTarget) dimensionResource(R.dimen.tutorial_game_target_size) else 150.dp
    val height = if (imageTarget) dimensionResource(R.dimen.tutorial_game_target_size) else 48.dp
    val margin = dimensionResource(R.dimen.tutorial_game_target_margin)
    val x = (areaWidth * state.position.x - width / 2).coerceIn(margin, (areaWidth - width - margin).coerceAtLeast(margin))
    val y = (areaHeight * state.position.y - height / 2).coerceIn(margin, (areaHeight - height - margin).coerceAtLeast(margin))
    val modifier = Modifier.offset(x, y).size(width, height)

    if (imageTarget) {
        val interactionSource = remember { MutableInteractionSource() }
        Image(
            painter = painterResource(type.drawableRes()),
            contentDescription = null,
            modifier = modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        )
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(type.label(state))
        }
    }
}

private fun QuickClickGameTargetType.drawableRes(): Int = when (this) {
    QuickClickGameTargetType.IMAGE_BLUE -> R.drawable.ic_target_blue
    QuickClickGameTargetType.IMAGE_RED -> R.drawable.ic_target_red
    QuickClickGameTargetType.IMAGE_GREEN -> R.drawable.ic_target_green
    QuickClickGameTargetType.IMAGE_YELLOW -> R.drawable.ic_target_yellow
    else -> error("Not an image target: $this")
}

@Composable
private fun QuickClickGameTargetType.label(state: QuickClickGameTargetState): String = when (this) {
    QuickClickGameTargetType.TEXT_DAY -> stringResource(R.string.button_text_day)
    QuickClickGameTargetType.TEXT_GOODBYE -> stringResource(R.string.button_text_goodbye)
    QuickClickGameTargetType.TEXT_HELLO -> stringResource(R.string.button_text_hello)
    QuickClickGameTargetType.TEXT_NIGHT -> stringResource(R.string.button_text_night)
    QuickClickGameTargetType.NUMBER -> (state as? QuickClickGameTargetState.ChangingContent)?.content?.toString().orEmpty()
    else -> ""
}
