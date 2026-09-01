/*
 * Copyright (C) 2026 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.tutorial.ui.game.timing

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vibhor1102.macrion.feature.tutorial.R
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

@Composable
internal fun TimingGameScreen(
    uiStateFlow: StateFlow<TimingGameUiState?>,
    showOverlayMenuPlaceholder: Boolean,
    onOverlayMenuPositioned: (IntOffset) -> Unit,
    onTimingClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val uiState by uiStateFlow.collectAsStateWithLifecycle()
    val state = uiState ?: return
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (landscape) {
        TimingGameLandscape(state, showOverlayMenuPlaceholder, onOverlayMenuPositioned, onTimingClick, onRetryClick)
    } else {
        TimingGamePortrait(state, showOverlayMenuPlaceholder, onOverlayMenuPositioned, onTimingClick, onRetryClick)
    }
}

@Composable
private fun TimingGamePortrait(
    state: TimingGameUiState,
    showPlaceholder: Boolean,
    onPositioned: (IntOffset) -> Unit,
    onTimingClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OverlayMenuPlaceholder(showPlaceholder, onPositioned)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(stringResource(R.string.message_target_diff, state.targetTotalDiffMs), Modifier.weight(1f))
                    StatCard(stringResource(R.string.message_total_diff, state.cumulativeTimeDiffMs.toSignedString()), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(stringResource(R.string.message_click_count, state.clickCount, state.targetClickCount), Modifier.weight(1f))
                    StatCard(stringResource(R.string.message_last_diff, state.lastTimeDiffMs.toSignedString()), Modifier.weight(1f))
                }
            }
        }
        GameButtons(state.isWon == null, onTimingClick, onRetryClick, Modifier.weight(1f))
        InstructionsCard(state.instructionsResId)
    }
}

@Composable
private fun TimingGameLandscape(
    state: TimingGameUiState,
    showPlaceholder: Boolean,
    onPositioned: (IntOffset) -> Unit,
    onTimingClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OverlayMenuPlaceholder(showPlaceholder, onPositioned)
        Column(Modifier.weight(1f)) {
            InstructionsCard(state.instructionsResId)
            GameButtons(state.isWon == null, onTimingClick, onRetryClick, Modifier.weight(1f))
        }
        Column(Modifier.width(175.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.message_target_diff, state.targetTotalDiffMs))
            StatCard(stringResource(R.string.message_total_diff, state.cumulativeTimeDiffMs.toSignedString()))
            StatCard(stringResource(R.string.message_click_count, state.clickCount, state.targetClickCount))
            StatCard(stringResource(R.string.message_last_diff, state.lastTimeDiffMs.toSignedString()))
        }
    }
}

@Composable
private fun OverlayMenuPlaceholder(show: Boolean, onPositioned: (IntOffset) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .size(
                width = dimensionResource(R.dimen.tutorial_overlay_menu_width),
                height = dimensionResource(R.dimen.tutorial_overlay_menu_height),
            )
            .alpha(if (show) 1f else 0f)
            .onGloballyPositioned {
                val position = it.positionInWindow()
                onPositioned(IntOffset(position.x.roundToInt(), position.y.roundToInt()))
            },
    ) {}
}

@Composable
private fun StatCard(text: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun GameButtons(
    enabled: Boolean,
    onTimingClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(onClick = onTimingClick, enabled = enabled, modifier = Modifier.size(64.dp)) {
            Image(painterResource(R.drawable.ic_tutorial_timer), contentDescription = null)
        }
        FilledIconButton(onClick = onRetryClick, modifier = Modifier.size(64.dp)) {
            Image(painterResource(R.drawable.ic_tutorial_retry), contentDescription = null)
        }
    }
}

@Composable
private fun InstructionsCard(instructionsResId: Int) {
    Card(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(instructionsResId),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private fun Long.toSignedString(): String = if (this >= 0) "+$this" else "$this"
