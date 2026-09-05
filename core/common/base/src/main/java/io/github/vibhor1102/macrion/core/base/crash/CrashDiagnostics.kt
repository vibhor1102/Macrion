/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.core.base.crash

import java.util.WeakHashMap

/** Only technical state belongs here. Never pass scenario objects or automation payloads. */
data class DetectionCrashContext(
    val operation: Operation,
    val screenWidth: Int,
    val screenHeight: Int,
    val areaLeft: Int,
    val areaTop: Int,
    val areaWidth: Int,
    val areaHeight: Int,
    val threshold: Int,
    val textLength: Int? = null,
    val modelId: String? = null,
    val originalWidth: Int? = null,
    val originalHeight: Int? = null,
    val scaledWidth: Int? = null,
    val scaledHeight: Int? = null,
    val color: Int? = null,
    val numberFormat: Int? = null,
) {
    enum class Operation { IMAGE, COLOR, TEXT, NUMBER }
}

object CrashDiagnostics {
    enum class Event { HOME_OPENED, SETTINGS_OPENED }
    data class Entry(val event: Event, val elapsedNanos: Long)
    private val contexts = WeakHashMap<Throwable, DetectionCrashContext>()
    private val events = ArrayDeque<Entry>()

    @Synchronized fun record(event: Event) {
        if (events.size == 50) events.removeFirst()
        events.addLast(Entry(event, System.nanoTime()))
    }

    @Synchronized fun snapshot(): List<Entry> = events.toList()
    @Synchronized fun context(error: Throwable): DetectionCrashContext? = contexts[error]
    @Synchronized fun attach(error: Throwable, context: DetectionCrashContext) {
        if (contexts.size >= 8) contexts.clear()
        contexts[error] = context
    }
}

/** Preserve the original exception type and trace, without logging user content. */
fun Exception.throwWithContext(context: DetectionCrashContext): Nothing {
    runCatching { CrashDiagnostics.attach(this, context) }
    throw this
}
