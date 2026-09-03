/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme

open class ReportComposeViewHolder<T>(
    parent: ViewGroup,
    content: @Composable (T) -> Unit,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {

    private var item by mutableStateOf<T?>(null)

    init {
        (itemView as ComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                MacrionTheme {
                    item?.let { boundItem -> content(boundItem) }
                }
            }
        }
    }

    fun bindComposeItem(item: T) {
        this.item = item
    }
}
