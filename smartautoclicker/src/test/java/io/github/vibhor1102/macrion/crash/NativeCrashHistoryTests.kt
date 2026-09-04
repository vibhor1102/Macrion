/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import android.app.ApplicationExitInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NativeCrashHistoryTests {
    @Test fun `first supported startup establishes baseline without reporting old exits`() {
        val result = NativeCrashHistory.select(null, listOf(exit(900, ApplicationExitInfo.REASON_CRASH_NATIVE)), 1_000)
        assertEquals(1_000, result.checkpoint)
        assertNull(result.nativeCrash)
    }

    @Test fun `empty first startup still checkpoints current time`() {
        assertEquals(1_000, NativeCrashHistory.select(null, emptyList(), 1_000).checkpoint)
    }

    @Test fun `selects only newest unseen native crash and advances past all exits`() {
        val result = NativeCrashHistory.select(1_000, listOf(
            exit(1_100, ApplicationExitInfo.REASON_CRASH_NATIVE, status = 6),
            exit(1_300, ApplicationExitInfo.REASON_USER_REQUESTED),
            exit(1_200, ApplicationExitInfo.REASON_CRASH_NATIVE, status = 11),
        ), 2_000)
        assertEquals(1_300, result.checkpoint)
        assertEquals(1_200L, result.nativeCrash?.timestamp)
        assertEquals(11, result.nativeCrash?.status)
    }

    @Test fun `does not repeat checkpointed crash or misclassify signals`() {
        val result = NativeCrashHistory.select(1_200, listOf(
            exit(1_200, ApplicationExitInfo.REASON_CRASH_NATIVE),
            exit(1_300, ApplicationExitInfo.REASON_SIGNALED),
            exit(1_400, ApplicationExitInfo.REASON_CRASH),
        ), 2_000)
        assertEquals(1_400, result.checkpoint)
        assertNull(result.nativeCrash)
    }

    private fun exit(timestamp: Long, reason: Int, status: Int = 0) =
        HistoricalExit(timestamp, reason, status, 100, 1234, 5678)
}
