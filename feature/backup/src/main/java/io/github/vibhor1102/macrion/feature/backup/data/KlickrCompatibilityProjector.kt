/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.backup.data

import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.dumb.data.database.DumbScenarioWithActions
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat

/** The Klick'r portable format targeted by compatible exports, including its verification status. */
internal data class KlickrCompatibilityProfile(
    val id: String,
    val displayName: String,
    val portableDatabaseVersion: Int,
    val crossReaderVerified: Boolean,
)

internal val CURRENT_KLICKR_COMPATIBILITY_PROFILE = KlickrCompatibilityProfile(
    id = "klickr-d466f1620c54",
    displayName = "Klick’r 4.0.1-1-gd466f162 (verification pending)",
    portableDatabaseVersion = 23,
    crossReaderVerified = false,
)

internal enum class KlickrCompatibilityLossReason {
    UNSUPPORTED_COMPONENT,
    BROKEN_REFERENCE,
    MEANINGLESS_EVENT,
    MEANINGLESS_SCENARIO,
}

internal data class KlickrCompatibilityLoss(
    val reason: KlickrCompatibilityLossReason,
    val componentCount: Int,
    val scenarioId: Long,
)

internal data class KlickrCompatibilityProjection<T>(
    val value: T?,
    val losses: List<KlickrCompatibilityLoss> = emptyList(),
) {
    val isExportable: Boolean get() = value != null
}

internal data class BackupExportPlan(
    val dumbScenarios: List<DumbScenarioWithActions>,
    val smartScenarios: List<CompleteScenario>,
    val format: BackupArchiveFormat,
    val profile: KlickrCompatibilityProfile? = null,
    val losses: List<KlickrCompatibilityLoss> = emptyList(),
    val excludedScenarioCount: Int = 0,
) {
    val omittedComponentCount: Int = losses.sumOf(KlickrCompatibilityLoss::componentCount)
}

/**
 * Creates the subset that the supported Klick'r backup format can represent.
 * At database version 23, Macrion and Klick'r still share the same scenario model.
 */
internal object KlickrCompatibilityProjector {

    fun createPlan(
        dumbScenarios: List<DumbScenarioWithActions>,
        smartScenarios: List<CompleteScenario>,
    ): BackupExportPlan {
        val dumbProjections = dumbScenarios.map(::projectDumbScenario)
        val smartProjections = smartScenarios.map(::projectSmartScenario)

        return BackupExportPlan(
            dumbScenarios = dumbProjections.mapNotNull { it.value },
            smartScenarios = smartProjections.mapNotNull { it.value },
            format = BackupArchiveFormat.KLICKR_COMPATIBLE,
            profile = CURRENT_KLICKR_COMPATIBILITY_PROFILE,
            losses = dumbProjections.flatMap { it.losses } + smartProjections.flatMap { it.losses },
            excludedScenarioCount = dumbProjections.count { !it.isExportable } +
                smartProjections.count { !it.isExportable },
        )
    }

    fun projectSmartScenario(scenario: CompleteScenario): KlickrCompatibilityProjection<CompleteScenario> =
        KlickrCompatibilityProjection(scenario.detachedCopy())

    fun projectDumbScenario(scenario: DumbScenarioWithActions): KlickrCompatibilityProjection<DumbScenarioWithActions> =
        KlickrCompatibilityProjection(scenario.detachedCopy())
}

private fun CompleteScenario.detachedCopy(): CompleteScenario = copy(
    scenario = scenario.copy(),
    events = events.map { event ->
        event.copy(
            event = event.event.copy(),
            actions = event.actions.map { action ->
                action.copy(
                    action = action.action.copy(),
                    intentExtras = action.intentExtras.map { it.copy() },
                    eventsToggle = action.eventsToggle.map { it.copy() },
                )
            },
            conditions = event.conditions.map { it.copy() },
        )
    },
    counters = counters.map { it.copy() },
)

private fun DumbScenarioWithActions.detachedCopy(): DumbScenarioWithActions = copy(
    scenario = scenario.copy(),
    dumbActions = dumbActions.map { it.copy() },
    stats = stats?.copy(),
)
