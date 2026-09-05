/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.feature.revenue.domain

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class StartupConsentCompletionTests {
    @Test fun `F-Droid completes startup consent immediately without displaying a form`() {
        var completions = 0
        RevenueRepository().startUserConsentRequestUiFlowIfNeeded(mock<Activity>()) { completions++ }
        assertEquals(1, completions)
    }
}
