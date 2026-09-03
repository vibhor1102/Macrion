/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation

import android.view.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView

import io.github.vibhor1102.macrion.core.common.overlays.R

@Composable
internal fun ListDialogScaffold(
    topBar: View,
    list: View,
    enforceMinimumHeight: Boolean,
    listBottomPadding: Boolean,
) {
    val minimumHeight = if (enforceMinimumHeight) {
        Modifier.heightIn(min = dimensionResource(R.dimen.bottom_sheet_min_height))
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(minimumHeight)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        AndroidView(factory = { topBar }, modifier = Modifier.fillMaxWidth())
        AndroidView(
            factory = { list },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(
                    if (listBottomPadding) {
                        Modifier.padding(bottom = dimensionResource(R.dimen.margin_vertical_default))
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@Composable
internal fun MoveToDialogScaffold(field: View) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.margin_horizontal_default),
                vertical = dimensionResource(R.dimen.margin_vertical_extra_large),
            ),
    ) {
        AndroidView(
            factory = {
                field.apply { setBackgroundResource(R.color.listBackground) }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
