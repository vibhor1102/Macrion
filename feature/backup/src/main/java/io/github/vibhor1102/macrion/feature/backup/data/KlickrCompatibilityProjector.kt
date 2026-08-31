/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.feature.backup.data

import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.dumb.data.database.DumbScenarioWithActions

/**
 * Creates the subset that the supported Klick'r backup format can represent.
 * At database version 23, Macrion and Klick'r still share the same scenario model.
 */
internal object KlickrCompatibilityProjector {

    fun projectSmartScenario(scenario: CompleteScenario): KlickrCompatibilityProjection<CompleteScenario> =
        KlickrCompatibilityProjection(scenario)

    fun projectDumbScenario(scenario: DumbScenarioWithActions): KlickrCompatibilityProjection<DumbScenarioWithActions> =
        KlickrCompatibilityProjection(scenario)
}

internal data class KlickrCompatibilityProjection<T>(
    val value: T,
    val omittedComponentCount: Int = 0,
)
