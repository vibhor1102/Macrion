/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.action.intent.flags

import org.junit.Assert.assertEquals
import org.junit.Test

class FlagsSelectionViewModelTest {

    @Test
    fun `reinitialization preserves edited flags`() {
        val viewModel = FlagsSelectionViewModel()
        viewModel.initialize(flags = 0b001, startActivityFlags = true)
        viewModel.setFlagState(flag = 0b010, isSelected = true)

        viewModel.initialize(flags = 0b001, startActivityFlags = true)

        assertEquals(0b011, viewModel.getSelectedFlags())
    }
}
