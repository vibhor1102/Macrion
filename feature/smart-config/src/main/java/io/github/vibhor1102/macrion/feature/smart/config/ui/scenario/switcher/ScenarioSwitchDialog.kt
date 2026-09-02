/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.feature.smart.config.ui.scenario.switcher

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.vibhor1102.macrion.core.common.overlays.base.viewModels
import io.github.vibhor1102.macrion.core.common.overlays.dialog.OverlayDialog
import io.github.vibhor1102.macrion.core.domain.model.scenario.Scenario
import io.github.vibhor1102.macrion.core.processing.domain.model.ScenarioSwitchResult
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import io.github.vibhor1102.macrion.feature.smart.config.R
import io.github.vibhor1102.macrion.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ScenarioSwitchDialog(private val onScenarioSelected: suspend (Scenario) -> ScenarioSwitchResult) :
    OverlayDialog(R.style.ScenarioConfigTheme) {
    private val viewModel: ScenarioSwitchViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java, creator = { scenarioSwitchViewModel() })
    private var isSwitching by mutableStateOf(false)
    private var failedScenario by mutableStateOf<Scenario?>(null)
    private val snackbar = SnackbarHostState()

    override fun onCreateView(): ViewGroup = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { MacrionTheme { this@ScenarioSwitchDialog.Content() } }
    }
    override fun onDialogCreated(dialog: BottomSheetDialog) = Unit
    override fun back() { if (!isSwitching) super.back() }
    override fun onStop() { isSwitching = false; failedScenario = null; super.onStop() }

    @Composable private fun Content() {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val columns = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        val enabled = !state.isLoading && !isSwitching && state.isPaused && state.currentScenario != null
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = ::back, enabled = !isSwitching) { Icon(painterResource(R.drawable.ic_cancel), null) }
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(context.getString(R.string.dialog_title_scenario_switcher), style = MaterialTheme.typography.titleLarge)
                        Text(context.getString(R.string.scenario_switcher_current,
                            state.currentScenario?.name ?: context.getString(R.string.scenario_switcher_current_unknown)),
                            style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
        ) { padding ->
            when {
                state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.alternatives.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(context.getString(R.string.scenario_switcher_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyVerticalGrid(GridCells.Fixed(columns), Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(state.alternatives, key = { it.id.toString() }) { scenario -> ScenarioCard(scenario, enabled) }
                }
            }
        }
    }

    @Composable private fun ScenarioCard(scenario: Scenario, enabled: Boolean) {
        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = enabled) { onScenarioClicked(scenario) }, shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_smart), null, Modifier.size(28.dp))
                Text(scenario.name, Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (isSwitching && failedScenario?.id == scenario.id) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
    }

    private fun onScenarioClicked(scenario: Scenario) {
        val state = viewModel.uiState.value
        when {
            isSwitching -> return
            !state.isPaused || state.currentScenario == null -> showError(R.string.scenario_switcher_error_paused)
            state.alternatives.none { it.id == scenario.id } -> showError(R.string.scenario_switcher_error_unavailable)
            else -> startSwitch(scenario)
        }
    }
    private fun startSwitch(scenario: Scenario) {
        isSwitching = true; failedScenario = scenario
        lifecycleScope.launch {
            try {
                when (onScenarioSelected(scenario)) {
                    ScenarioSwitchResult.Success -> super@ScenarioSwitchDialog.back()
                    ScenarioSwitchResult.ServiceUnavailable -> showError(R.string.scenario_switcher_error_service)
                    ScenarioSwitchResult.InvalidProcessingState -> showError(R.string.scenario_switcher_error_paused)
                    ScenarioSwitchResult.ProjectionUnavailable -> showError(R.string.scenario_switcher_error_projection)
                    ScenarioSwitchResult.CurrentScenario -> showError(R.string.scenario_switcher_error_current)
                    ScenarioSwitchResult.ScenarioUnavailable -> showError(R.string.scenario_switcher_error_unavailable)
                    ScenarioSwitchResult.PersistenceFailure -> showError(R.string.scenario_switcher_error_persistence)
                }
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { showError(R.string.scenario_switcher_error_unknown) }
        }
    }
    private fun showError(message: Int) {
        isSwitching = false
        val retry = failedScenario?.let { failed -> viewModel.uiState.value.alternatives.firstOrNull { it.id == failed.id } }
        lifecycleScope.launch {
            val result = snackbar.showSnackbar(context.getString(message),
                actionLabel = retry?.let { context.getString(R.string.scenario_switcher_retry) })
            if (result == SnackbarResult.ActionPerformed && retry != null) startSwitch(retry)
        }
    }
}
