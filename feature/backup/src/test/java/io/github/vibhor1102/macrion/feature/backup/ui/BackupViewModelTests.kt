/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.backup.ui

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.github.vibhor1102.macrion.core.display.config.DisplayConfigManager
import io.github.vibhor1102.macrion.feature.backup.domain.BackupRepository
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class BackupViewModelTests {

    @Test
    fun initialize_whenAlreadyInitialized_preservesCurrentState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = BackupViewModel(
            repository = mockk<BackupRepository>(),
            displayConfigManager = mockk<DisplayConfigManager>(),
        )
        viewModel.initialize(context, isImport = false)
        viewModel.setKlickrCompatibleExport(context, enabled = true)

        viewModel.initialize(context, isImport = false)

        assertTrue(viewModel.backupState.value?.klickrCompatibleChecked == true)
    }
}
