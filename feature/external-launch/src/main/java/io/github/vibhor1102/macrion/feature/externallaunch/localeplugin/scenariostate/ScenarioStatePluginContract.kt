/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.externallaunch.localeplugin.scenariostate

import android.os.Bundle

internal object ScenarioStatePluginContract {
    const val CONFIGURATION_JSON = "{\"version\":1,\"condition\":\"scenario_open\"}"
    const val CONFIGURATION_ACTIVITY_CLASS_NAME =
        "io.github.vibhor1102.macrion.feature.externallaunch.localeplugin.ui.ScenarioStateConditionConfigurationActivity"

    const val VARIABLE_SCENARIO_NAME = "%klickr_scenario_name"
    const val VARIABLE_SCENARIO_STATE = "%klickr_scenario_state"

    val relevantVariables: Array<String> = arrayOf(
        "$VARIABLE_SCENARIO_NAME\nScenario name\nName of the scenario currently open in Macrion.",
        "$VARIABLE_SCENARIO_STATE\nScenario state\nrunning, paused, hidden, settings, or none.",
    )

    fun createVariables(snapshot: ScenarioStateSnapshot): Bundle = Bundle().apply {
        putString(VARIABLE_SCENARIO_NAME, snapshot.scenarioName)
        putString(VARIABLE_SCENARIO_STATE, snapshot.state.value)
    }
}

internal data class ScenarioStateSnapshot(
    val scenarioName: String,
    val state: ScenarioState,
) {
    val isScenarioOpen: Boolean
        get() = state != ScenarioState.NONE
}

internal enum class ScenarioState(val value: String) {
    RUNNING("running"),
    PAUSED("paused"),
    HIDDEN("hidden"),
    SETTINGS("settings"),
    NONE("none"),
    ;

    companion object {
        fun from(
            isScenarioOpen: Boolean,
            isRunning: Boolean,
            isOverlayHidden: Boolean,
            isSettingsOpen: Boolean,
        ): ScenarioState = when {
            !isScenarioOpen -> NONE
            isSettingsOpen -> SETTINGS
            isOverlayHidden -> HIDDEN
            isRunning -> RUNNING
            else -> PAUSED
        }
    }
}
