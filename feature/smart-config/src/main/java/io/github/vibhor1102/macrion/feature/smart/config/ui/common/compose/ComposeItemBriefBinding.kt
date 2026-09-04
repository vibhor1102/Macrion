/*
 * Copyright (C) 2026 Vibhor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.viewbinding.ViewBinding

class ComposeItemBriefBinding(val composeView: ComposeView) : ViewBinding {
    override fun getRoot(): View = composeView
}
