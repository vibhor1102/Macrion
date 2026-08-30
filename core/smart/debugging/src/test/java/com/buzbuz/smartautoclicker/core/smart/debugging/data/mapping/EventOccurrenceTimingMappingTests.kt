/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.core.smart.debugging.data.mapping

import com.buzbuz.smartautoclicker.core.smart.debugging.debugReportMessage
import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.DebugReportEventOccurrence
import com.buzbuz.smartautoclicker.core.smart.debugging.triggerEventMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventOccurrenceTimingMappingTests {

    @Test
    fun `nanosecond boundaries survive protobuf mapping exactly`() {
        val expected = occurrence(
            detectedAtNs = 833_123_456L,
            actionsCompletedAtNs = 900_987_654L,
        )

        assertEquals(expected, expected.toProtobuf().toDomain())
    }

    @Test
    fun `legacy protobuf keeps modern timing absent`() {
        val legacy = debugReportMessage {
            relativeTimestampMs = 900L
            triggerEventMessage = triggerEventMessage { eventId = 42L }
        }.toDomain()!!

        assertEquals(900L, legacy.relativeTimestampMs)
        assertNull(legacy.detectedAtNs)
        assertNull(legacy.actionsCompletedAtNs)
    }

    private fun occurrence(
        detectedAtNs: Long,
        actionsCompletedAtNs: Long,
    ) = DebugReportEventOccurrence.TriggerEvent(
        eventId = 42L,
        relativeTimestampMs = detectedAtNs / 1_000_000L,
        detectedAtNs = detectedAtNs,
        actionsCompletedAtNs = actionsCompletedAtNs,
        conditionsResults = emptyList(),
        counterChanges = emptyList(),
        eventStateChanges = emptyList(),
    )
}
