/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.overview

import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.DebugReportEventOccurrence
import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.DebugReportOverview
import kotlin.time.Duration.Companion.nanoseconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExecutionLimiterIdleTimeTests {

    @Test
    fun `no limiter waiting is always zero including legacy reports`() {
        val result = buildExecutionLimiterIdleTime(
            overview = overview(activeNs = 1_000L, waitNs = 0L),
            occurrences = listOf(occurrence(null, null)),
        )

        assertEquals("0%", result?.formatPercentage())
    }

    @Test
    fun `action time is excluded from the denominator`() {
        val result = buildExecutionLimiterIdleTime(
            overview = overview(activeNs = 1_000L, waitNs = 100L),
            occurrences = listOf(occurrence(detectedAtNs = 50L, actionsCompletedAtNs = 950L)),
        )

        assertEquals(ExecutionLimiterIdleTime(waitDurationNs = 100L, measuredDurationNs = 200L), result)
        assertEquals("50.0%", result?.formatPercentage())
    }

    @Test
    fun `percentage formatting keeps meaningful endpoints`() {
        assertEquals("<0.1%", ExecutionLimiterIdleTime(1L, 2_000L).formatPercentage())
        assertEquals("99.9%", ExecutionLimiterIdleTime(9_999L, 10_000L).formatPercentage())
        assertEquals("100%", ExecutionLimiterIdleTime(10_000L, 10_000L).formatPercentage())
    }

    @Test
    fun `action duration beyond active duration is safely clamped`() {
        val result = buildExecutionLimiterIdleTime(
            overview = overview(activeNs = 100L, waitNs = 50L),
            occurrences = listOf(occurrence(detectedAtNs = 10L, actionsCompletedAtNs = 210L)),
        )

        assertEquals("100%", result?.formatPercentage())
    }

    @Test
    fun `legacy or malformed occurrences cannot estimate a nonzero report`() {
        assertNull(buildExecutionLimiterIdleTime(overview(1_000L, 100L), listOf(occurrence(null, null))))
        assertNull(buildExecutionLimiterIdleTime(overview(1_000L, 100L), listOf(occurrence(200L, 199L))))
    }

    private fun overview(activeNs: Long, waitNs: Long) = DebugReportOverview(
        scenarioId = 1L,
        duration = 0.nanoseconds,
        frameCount = 0L,
        averageFrameProcessingDuration = 0.nanoseconds,
        imageEventFulfilledCount = 0,
        triggerEventFulfilledCount = 0,
        counterNames = emptySet(),
        activeDetectionDuration = activeNs.nanoseconds,
        executionLimiterWaitDuration = waitNs.nanoseconds,
    )

    private fun occurrence(
        detectedAtNs: Long?,
        actionsCompletedAtNs: Long?,
    ) = DebugReportEventOccurrence.TriggerEvent(
        eventId = 1L,
        relativeTimestampMs = 0L,
        detectedAtNs = detectedAtNs,
        actionsCompletedAtNs = actionsCompletedAtNs,
        conditionsResults = emptyList(),
        counterChanges = emptyList(),
        eventStateChanges = emptyList(),
    )
}
