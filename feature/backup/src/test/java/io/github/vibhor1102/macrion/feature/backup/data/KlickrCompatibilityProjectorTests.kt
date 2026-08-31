package io.github.vibhor1102.macrion.feature.backup.data

import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.database.entity.ScenarioEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class KlickrCompatibilityProjectorTests {

    @Test
    fun currentProfileIsExplicitAndLosslessForAnEmptySelection() {
        val plan = KlickrCompatibilityProjector.createPlan(emptyList(), emptyList())

        assertEquals(BackupArchiveFormat.KLICKR_COMPATIBLE, plan.format)
        assertEquals("klickr-d466f1620c54", plan.profile?.id)
        assertEquals(23, plan.profile?.portableDatabaseVersion)
        assertEquals(false, plan.profile?.crossReaderVerified)
        assertEquals(0, plan.omittedComponentCount)
        assertEquals(0, plan.excludedScenarioCount)
        assertTrue(plan.losses.isEmpty())
    }

    @Test
    fun structuredLossesAreCounted() {
        val plan = BackupExportPlan(
            dumbScenarios = emptyList(),
            smartScenarios = emptyList(),
            format = BackupArchiveFormat.KLICKR_COMPATIBLE,
            losses = listOf(
                KlickrCompatibilityLoss(
                    reason = KlickrCompatibilityLossReason.UNSUPPORTED_COMPONENT,
                    componentCount = 2,
                    scenarioId = 42,
                ),
            ),
        )

        assertEquals(2, plan.omittedComponentCount)
    }

    @Test
    fun projectionIsDetachedFromOriginalEntities() {
        val original = CompleteScenario(
            scenario = ScenarioEntity(
                id = 42,
                name = "Test",
                detectionQuality = 600,
            ),
            events = emptyList(),
            counters = emptyList(),
        )

        val projected = KlickrCompatibilityProjector.projectSmartScenario(original).value!!

        assertEquals(original, projected)
        assertNotSame(original, projected)
        assertNotSame(original.scenario, projected.scenario)
    }
}
