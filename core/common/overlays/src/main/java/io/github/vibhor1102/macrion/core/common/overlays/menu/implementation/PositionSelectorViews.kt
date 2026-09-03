/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.core.common.overlays.menu.implementation

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import io.github.vibhor1102.macrion.core.common.overlays.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.core.ui.views.itembrief.ItemBriefView

internal class PositionSelectorViews(
    context: Context,
    safeInsetTopPx: Int,
) {
    val root = FrameLayout(context)
    val positionSelector = ItemBriefView(context)
    val instructions: ComposeView

    private var instructionText by mutableIntStateOf(R.string.toast_configure_single_click)

    init {
        val safeInsetTopDp = (safeInsetTopPx / context.resources.displayMetrics.density).dp
        root.addView(positionSelector, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))

        instructions = ComposeView(context).apply {
            isClickable = false
            setContent {
                MacrionTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Black,
                                    0.7f to Color.Black.copy(alpha = 0.53f),
                                    1f to Color.Transparent,
                                ),
                            )
                            .padding(
                                start = 32.dp,
                                top = safeInsetTopDp + 4.dp,
                                end = 32.dp,
                                bottom = 32.dp,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(instructionText),
                            color = colorResource(R.color.md_theme_light_onPrimary),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        root.addView(instructions, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ))
    }

    fun setInstruction(@StringRes text: Int) {
        instructionText = text
    }
}
