/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.intent.extras

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExtraConfigDialogTest {

    @Test
    fun `numeric input accepts transient and in-range values`() {
        assertEquals("-", acceptedExtraValue(0.toByte(), "-"))
        assertEquals("127", acceptedExtraValue(0.toByte(), "127"))
        assertNull(acceptedExtraValue(0.toByte(), "128"))
        assertEquals("-1.25", acceptedExtraValue(0.0, "-1.25"))
        assertNull(acceptedExtraValue(0.0, "not a number"))
    }

    @Test
    fun `character input remains one character`() {
        assertEquals("a", acceptedExtraValue('x', "abc"))
        assertEquals("", acceptedExtraValue('x', ""))
    }
}
