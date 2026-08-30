/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.feature.smart.debugging.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTests {

    @Test
    fun `timeline phase duration always uses comparable milliseconds`() {
        assertEquals("+< 1", 999_999L.formatDebugTimelinePhaseDurationValue())
        assertEquals("+1.2", 1_234_567L.formatDebugTimelinePhaseDurationValue())
        assertEquals("+331.0", 331_000_000L.formatDebugTimelinePhaseDurationValue())
        assertEquals("+999.9", 999_949_999L.formatDebugTimelinePhaseDurationValue())
        assertEquals("+1000", 999_950_000L.formatDebugTimelinePhaseDurationValue())
        assertEquals("+1235", 1_234_567_890L.formatDebugTimelinePhaseDurationValue())
    }
}
