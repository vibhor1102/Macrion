/*
 * Copyright (C) 2025 Kevin Buzeau
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.overview

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.NavBarDialogContent
import io.github.vibhor1102.macrion.core.common.overlays.dialog.implementation.navbar.viewModels
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.debugging.di.DebuggingViewModelsEntryPoint
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.activity.EventActivityDialog

import kotlinx.coroutines.launch
import kotlin.getValue


class DebugReportOverviewContent(appContext: Context) : NavBarDialogContent(appContext) {

    /** View model for this content. */
    private val viewModel: DebugReportOverviewViewModel by viewModels(
        entryPoint = DebuggingViewModelsEntryPoint::class.java,
        creator = { debugReportOverviewViewModel() },
    )

    private var uiState by mutableStateOf<DebugReportOverviewUiState>(DebugReportOverviewUiState.Loading)

    override fun onCreateView(container: ViewGroup): ViewGroup {
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MacrionTheme {
                    DebugReportOverview(uiState, ::openEventActivity)
                }
            }
        }
    }

    override fun onViewCreated() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::updateOverview) }
            }
        }
    }

    private fun updateOverview(uiState: DebugReportOverviewUiState) {
        when (uiState) {
            is DebugReportOverviewUiState.Loading -> this.uiState = uiState
            is DebugReportOverviewUiState.NotAvailable -> toNotAvailableState()
            is DebugReportOverviewUiState.Available -> this.uiState = uiState
        }
    }

    private fun toNotAvailableState() {
        dialogController.back()
    }


    private fun openEventActivity() {
        dialogController.overlayManager.navigateTo(
            context = context,
            newOverlay = EventActivityDialog(),
            hideCurrent = false,
        )
    }
}
