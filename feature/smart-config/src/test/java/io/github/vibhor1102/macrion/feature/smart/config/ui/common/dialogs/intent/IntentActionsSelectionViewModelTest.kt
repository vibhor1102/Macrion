/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.common.dialogs.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentActionsSelectionViewModelTest {

    @Test
    fun `reinitialization preserves edited selection`() {
        val viewModel = IntentActionsSelectionViewModel()
        viewModel.initialize("initial.action", requestBroadcast = false)
        viewModel.setActionSelectionState("edited.action", isSelected = true)

        viewModel.initialize("initial.action", requestBroadcast = false)

        assertEquals("edited.action", viewModel.getSelectedAction())
    }
}
