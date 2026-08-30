/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.core.common.actions.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.util.Log

import io.github.vibhor1102.macrion.core.base.Dumpable
import io.github.vibhor1102.macrion.core.base.addDumpTabulationLvl

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.PrintWriter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds


@Singleton
internal class GestureExecutor @Inject constructor() : Dumpable {

    private var completedGestures: Long = 0L
    private var cancelledGestures: Long = 0L
    private var errorGestures: Long = 0L


    fun clear() {
        completedGestures = 0L
        cancelledGestures = 0L
        errorGestures = 0L
    }

    suspend fun dispatchGesture(service: AccessibilityService, gesture: GestureDescription): Boolean {
        val result = withTimeoutOrNull(gesture.timeoutDurationMs().milliseconds) {
            suspendCancellableCoroutine { continuation ->
                try {
                    service.dispatchGesture(
                        /* gesture = */ gesture,
                        /* callback = */ object : GestureResultCallback() {
                            override fun onCompleted(g: GestureDescription?) = continuation.safeResume(true)
                            override fun onCancelled(g: GestureDescription?) = continuation.safeResume(false)
                        },
                        /* handler = */ null,
                    )
                } catch (rEx: RuntimeException) {
                    Log.w(TAG, "System is not responsive, the user might be spamming gesture too quickly", rEx)
                    continuation.safeResume(false)
                }
            }
        }

        if (result == null) {
            Log.w(TAG, "Gesture error, timeout or system error occurred.")
            errorGestures++
            return false
        }

        if (!result) {
            Log.w(TAG, "Gesture has been cancelled.")
            cancelledGestures ++
            return false
        }

        completedGestures++
        return true
    }

    override fun dump(writer: PrintWriter, prefix: CharSequence) {
        val contentPrefix = "${prefix.addDumpTabulationLvl()}- "

        writer.apply {
            append(prefix).println("* GestureExecutor:")
            append(contentPrefix).append("Completed=$completedGestures").println()
            append(contentPrefix).append("Cancelled=$cancelledGestures").println()
            append(contentPrefix).append("Error=$errorGestures").println()
        }
    }
}

private fun <T> Continuation<T>.safeResume(value: T): Unit =
    try {
        resume(value)
    } catch (isEx: IllegalStateException) {
        Log.w(TAG, "Continuation have already been resumed. Did the same event got two results ?", isEx)
        Unit
    }

private fun GestureDescription.durationMs(): Long {
    var maxEndTime = 0L
    for (i in 0 until strokeCount) {
        val stroke = getStroke(i)
        maxEndTime = maxOf(maxEndTime, stroke.startTime + stroke.duration)
    }

    return maxEndTime
}

private fun GestureDescription.timeoutDurationMs(): Long =
    (durationMs() * GESTURE_CALLBACK_TIMEOUT_DURATION_RATIO_MS)
        .coerceIn(GESTURE_CALLBACK_TIMEOUT_MIN_MS, GESTURE_CALLBACK_TIMEOUT_MAX_MS)

private const val GESTURE_CALLBACK_TIMEOUT_MIN_MS = 100L
private const val GESTURE_CALLBACK_TIMEOUT_MAX_MS = 65_000L
private const val GESTURE_CALLBACK_TIMEOUT_DURATION_RATIO_MS = 2

private const val TAG = "GestureExecutor"