/*
 * Copyright (C) 2026 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.backup.data.smart

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.vibhor1102.macrion.core.database.DATABASE_VERSION
import io.github.vibhor1102.macrion.core.database.entity.ActionEntity
import io.github.vibhor1102.macrion.core.database.entity.ActionType
import io.github.vibhor1102.macrion.core.database.entity.CompleteActionEntity
import io.github.vibhor1102.macrion.core.database.entity.CompleteEventEntity
import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.database.entity.CountersEntity
import io.github.vibhor1102.macrion.core.database.entity.EventEntity
import io.github.vibhor1102.macrion.core.database.entity.EventType
import io.github.vibhor1102.macrion.core.database.entity.ScenarioEntity
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SmartBackupDataSourceTests {

    @Test
    fun deserialize_normalizesNamesAndCounterReferences_withoutTrimmingPayload() {
        val serializer = ScenarioSerializer()
        val source = ScenarioBackup(
            version = DATABASE_VERSION,
            screenWidth = 1080,
            screenHeight = 2400,
            scenario = CompleteScenario(
                scenario = ScenarioEntity(1, " Scenario ", 600, 0.0, false),
                counters = listOf(
                    CountersEntity("Score", 1, 1.0),
                    CountersEntity(" Score ", 1, 2.0),
                ),
                events = listOf(CompleteEventEntity(
                    event = EventEntity(1, 1, " Event ", 1, 0, true, EventType.TRIGGER_EVENT),
                    conditions = emptyList(),
                    actions = listOf(CompleteActionEntity(
                        action = ActionEntity(
                            id = 1, eventId = 1, name = " Notify ", type = ActionType.NOTIFICATION,
                            notificationMessageText = "  value={ Score }  ", notificationImportance = 3,
                        ),
                        intentExtras = emptyList(), eventsToggle = emptyList(),
                    )),
                )),
            ),
        )
        val bytes = ByteArrayOutputStream().also { serializer.serialize(source, it) }.toByteArray()

        val restored = serializer.deserialize(
            ByteArrayInputStream(bytes), BackupArchiveFormat.KLICKR_COMPATIBLE,
        )!!.scenario

        assertEquals("Scenario", restored.scenario.name)
        assertEquals("Event", restored.events.single().event.name)
        assertEquals("Notify", restored.events.single().actions.single().action.name)
        assertEquals(listOf("Score", "Score (2)"), restored.counters.map { it.name }.sorted())
        assertEquals("  value={Score (2)}  ",
            restored.events.single().actions.single().action.notificationMessageText)
    }

    @Test
    fun sameScreenSize_sameOrientation_noWarning() {
        assertFalse(hasDifferentScreenSize(1080, 2400, 1080, 2400))
    }

    @Test
    fun sameScreenSize_differentOrientation_noWarning() {
        assertFalse(hasDifferentScreenSize(1080, 2400, 2400, 1080))
    }

    @Test
    fun differentScreenSize_warning() {
        assertTrue(hasDifferentScreenSize(1080, 2400, 1440, 3200))
    }
}
