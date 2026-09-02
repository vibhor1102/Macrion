/*
 * Copyright (C) 2026 Vibhor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.smart.config.ui.scenario.common

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.vibhor1102.macrion.feature.smart.config.R

@Composable
internal fun EventListRow(
    name: String,
    conditionsCount: String,
    actionsCount: String,
    @StringRes enabledTextRes: Int,
    @DrawableRes enabledIconRes: Int,
    conditionIconRes: Int,
    actionsInError: Boolean,
    showReorderHandle: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(62.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showReorderHandle) {
            LegacyIcon(R.drawable.ic_reorder, Modifier.size(48.dp))
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        Column(Modifier.weight(1f).fillMaxHeight().padding(top = 4.dp)) {
            Text(
                text = name,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                EventDetail(enabledIconRes, androidx.compose.ui.res.stringResource(enabledTextRes), null, Modifier.weight(1f))
                EventDetail(
                    R.drawable.ic_click,
                    actionsCount,
                    if (actionsInError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    Modifier.weight(1f),
                )
                EventDetail(conditionIconRes, conditionsCount, null, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun EventDetail(
    @DrawableRes iconRes: Int,
    text: String,
    tint: Color?,
    modifier: Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LegacyIcon(iconRes, Modifier.size(18.dp), tint)
        Text(text = text, color = tint ?: Color.Unspecified, maxLines = 1)
    }
}

@Composable
private fun LegacyIcon(@DrawableRes iconRes: Int, modifier: Modifier, tint: Color? = null) {
    AndroidView(
        factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER } },
        update = { view ->
            view.setImageResource(iconRes)
            if (tint == null) view.clearColorFilter() else view.setColorFilter(tint.toArgb())
        },
        modifier = modifier,
    )
}
