/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.core.ui.bindings.lists

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import io.github.vibhor1102.macrion.core.ui.R
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

/** Native RecyclerView host with Compose loading and empty states. */
class LoadableListViews(
    context: Context,
    @param:StringRes private val emptyText: Int,
    @param:StringRes private val emptySecondaryText: Int? = null,
) {
    val root = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }

    val list = RecyclerView(
        ContextThemeWrapper(context, R.style.AppTheme_RecyclerViewFastScroll),
    ).apply {
        layoutManager = LinearLayoutManager(context)
        visibility = View.GONE
    }

    private var state by mutableStateOf(CollectionState.Loading)

    init {
        root.addView(list, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
        root.addView(ComposeView(context).apply {
            setContent {
                MacrionTheme {
                    val foreground = colorResource(R.color.overlayViewPrimary)
                    when (state) {
                        CollectionState.Loading -> Column(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 435.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                        CollectionState.Empty -> Column(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 435.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(emptyText),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = foreground,
                            )
                            emptySecondaryText?.let { secondary ->
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 32.dp),
                                    color = foreground.copy(alpha = 0.3f),
                                )
                                Text(
                                    text = stringResource(secondary),
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    color = foreground.copy(alpha = 0.7f),
                                )
                            }
                        }
                        CollectionState.Content -> Unit
                    }
                }
            }
        }, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
    }

    fun updateState(items: Collection<Any>?) {
        state = when {
            items == null -> CollectionState.Loading
            items.isEmpty() -> CollectionState.Empty
            else -> CollectionState.Content
        }
        list.visibility = if (state == CollectionState.Content) View.VISIBLE else View.GONE
    }
}

private enum class CollectionState { Loading, Empty, Content }
