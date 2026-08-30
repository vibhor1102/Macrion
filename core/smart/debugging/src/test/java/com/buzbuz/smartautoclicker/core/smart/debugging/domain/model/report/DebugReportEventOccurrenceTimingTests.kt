/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DebugReportEventOccurrenceTimingTests {

    @Test
    fun `first occurrence starts its detection interval at session start`() {
        val occurrence = occurrence(detectedAtNs = 331L, actionsCompletedAtNs = 515L)

        assertEquals(
            DebugReportOccurrenceDurations(detectingDurationNs = 331L, actionsDurationNs = 184L),
            occurrence.getDurationsNs(previousActionsCompletedAtNs = null),
        )
    }

    @Test
    fun `later occurrence starts after previous actions complete`() {
        val occurrence = occurrence(detectedAtNs = 833L, actionsCompletedAtNs = 900L)

        assertEquals(
            DebugReportOccurrenceDurations(detectingDurationNs = 318L, actionsDurationNs = 67L),
            occurrence.getDurationsNs(previousActionsCompletedAtNs = 515L),
        )
    }

    @Test
    fun `legacy and malformed timing do not produce durations`() {
        assertNull(occurrence(detectedAtNs = null, actionsCompletedAtNs = null).getDurationsNs(null))
        assertNull(occurrence(detectedAtNs = 400L, actionsCompletedAtNs = 399L).getDurationsNs(null))
        assertNull(occurrence(detectedAtNs = 400L, actionsCompletedAtNs = 450L).getDurationsNs(401L))
    }

    private fun occurrence(
        detectedAtNs: Long?,
        actionsCompletedAtNs: Long?,
    ) = DebugReportEventOccurrence.TriggerEvent(
        eventId = 42L,
        relativeTimestampMs = 0L,
        detectedAtNs = detectedAtNs,
        actionsCompletedAtNs = actionsCompletedAtNs,
        conditionsResults = emptyList(),
        counterChanges = emptyList(),
        eventStateChanges = emptyList(),
    )
}
