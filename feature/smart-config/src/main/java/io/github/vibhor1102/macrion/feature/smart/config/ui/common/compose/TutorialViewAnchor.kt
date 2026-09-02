/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.common.compose

import android.view.View
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Exposes a Compose control to the legacy tutorial monitor as a real Android [View].
 * The transparent view occupies the control's exact bounds and forwards both user and
 * tutorial-driven clicks to the same callback.
 */
@Composable
fun BoxScope.TutorialClickAnchor(
    onViewChanged: (View?) -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val currentClick by rememberUpdatedState(onClick)
    var anchor by remember { mutableStateOf<View?>(null) }
    AndroidView(
        factory = { context ->
            View(context).apply {
                alpha = 0f
                setOnClickListener { currentClick() }
                anchor = this
            }
        },
        update = { it.isEnabled = enabled },
        modifier = Modifier.matchParentSize(),
    )
    DisposableEffect(anchor) {
        anchor?.let(onViewChanged)
        onDispose { onViewChanged(null) }
    }
}

/** Exposes bounds and programmatic clicks without covering the Compose control drawn after it. */
@Composable
fun TutorialViewAnchor(
    onViewChanged: (View?) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentClick by rememberUpdatedState(onClick)
    var anchor by remember { mutableStateOf<View?>(null) }
    AndroidView(
        factory = { context ->
            View(context).apply {
                alpha = 0f
                setOnClickListener { currentClick() }
                anchor = this
            }
        },
        modifier = modifier,
    )
    DisposableEffect(anchor) {
        anchor?.let(onViewChanged)
        onDispose { onViewChanged(null) }
    }
}
