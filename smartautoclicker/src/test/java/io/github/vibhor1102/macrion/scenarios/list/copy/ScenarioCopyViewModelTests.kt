/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.scenarios.list.copy

import io.github.vibhor1102.macrion.core.domain.IRepository
import io.github.vibhor1102.macrion.core.dumb.domain.DumbRepository
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScenarioCopyViewModelTests {

    @Test
    fun initializeCopyName_whenAlreadyEdited_preservesEditedName() {
        val viewModel = ScenarioCopyViewModel(
            mainDispatcher = UnconfinedTestDispatcher(),
            smartRepository = mockk<IRepository>(),
            dumbRepository = mockk<DumbRepository>(),
        )
        viewModel.initializeCopyName("Default copy")
        viewModel.setCopyName("My copy")

        viewModel.initializeCopyName("Default copy")

        assertEquals("My copy", viewModel.copyName.value)
    }
}
