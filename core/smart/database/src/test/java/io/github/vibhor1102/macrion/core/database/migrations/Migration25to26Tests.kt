/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.core.database.migrations

import android.content.ContentValues
import android.content.Context
import android.os.Build
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.vibhor1102.macrion.core.database.ACTION_TABLE
import io.github.vibhor1102.macrion.core.database.ClickDatabase
import io.github.vibhor1102.macrion.core.database.CONDITION_TABLE
import io.github.vibhor1102.macrion.core.database.COUNTERS_TABLE
import io.github.vibhor1102.macrion.core.database.EVENT_TABLE
import io.github.vibhor1102.macrion.core.database.SCENARIO_TABLE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class Migration25to26Tests {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), ClickDatabase::class.java,
    )
    private lateinit var dbPath: String

    @Before fun setUp() {
        dbPath = ApplicationProvider.getApplicationContext<Context>()
            .getDatabasePath("migration-25-to-26-test").path
    }

    @Test fun migrate_trimsLabelsAndCounters_rewritesReferences_preservesPayloadWhitespace() {
        helper.createDatabase(dbPath, 25).use { db ->
            db.insert(SCENARIO_TABLE, 0, values("id" to 1L, "name" to " Scenario ",
                "detection_quality" to 1200, "compute_rate" to 0.0, "randomize" to 0, "keep_screen_on" to 0))
            db.insert(EVENT_TABLE, 0, values("id" to 10L, "scenario_id" to 1L, "name" to " Event ",
                "operator" to 0, "priority" to 0, "enabled_on_start" to 1, "type" to "TRIGGER_EVENT"))
            db.insert(COUNTERS_TABLE, 0, values("counterName" to "Score", "scenarioId" to 1L, "startingValue" to 1.0))
            db.insert(COUNTERS_TABLE, 0, values("counterName" to " Score ", "scenarioId" to 1L, "startingValue" to 2.0))
            db.insert(ACTION_TABLE, 0, values("id" to 20L, "eventId" to 10L, "priority" to 0,
                "name" to " Change ", "type" to "CHANGE_COUNTER", "counter_name" to " Score ",
                "counter_operation" to "ADD", "counter_operation_value_type" to "COUNTER",
                "counter_operation_counter_name" to " Score "))
            db.insert(ACTION_TABLE, 0, values("id" to 21L, "eventId" to 10L, "priority" to 1,
                "name" to " Notify ", "type" to "NOTIFICATION", "notification_importance" to 3,
                "notification_message_text" to "  value={ Score }  "))
            db.insert(CONDITION_TABLE, 0, values("id" to 30L, "eventId" to 10L, "name" to " Condition ",
                "type" to "ON_COUNTER_REACHED", "priority" to 0, "counter_name" to " Score ",
                "counter_comparison_operation" to "EQUALS", "counter_operation_value_type" to "COUNTER",
                "counter_value_counter_name" to " Score "))
        }

        helper.runMigrationsAndValidate(dbPath, 26, true, Migration25to26).use { db ->
            assertEquals("Scenario", db.string("SELECT name FROM $SCENARIO_TABLE WHERE id=1"))
            assertEquals("Event", db.string("SELECT name FROM $EVENT_TABLE WHERE id=10"))
            assertEquals(listOf("Score", "Score (2)"), db.strings(
                "SELECT counterName FROM $COUNTERS_TABLE WHERE scenarioId=1 ORDER BY counterName"))
            assertEquals("Score (2)", db.string("SELECT counter_name FROM $ACTION_TABLE WHERE id=20"))
            assertEquals("Score (2)", db.string("SELECT counter_operation_counter_name FROM $ACTION_TABLE WHERE id=20"))
            assertEquals("Score (2)", db.string("SELECT counter_name FROM $CONDITION_TABLE WHERE id=30"))
            assertEquals("Score (2)", db.string("SELECT counter_value_counter_name FROM $CONDITION_TABLE WHERE id=30"))
            assertEquals("  value={Score (2)}  ", db.string(
                "SELECT notification_message_text FROM $ACTION_TABLE WHERE id=21"))
            assertEquals("Change", db.string("SELECT name FROM $ACTION_TABLE WHERE id=20"))
            assertEquals("Condition", db.string("SELECT name FROM $CONDITION_TABLE WHERE id=30"))
        }
    }

    private fun values(vararg pairs: Pair<String, Any>) = ContentValues().apply {
        pairs.forEach { (key, value) ->
            when (value) {
                is String -> put(key, value)
                is Long -> put(key, value)
                is Int -> put(key, value)
                is Double -> put(key, value)
                else -> error("Unsupported test value")
            }
        }
    }
    private fun SupportSQLiteDatabase.string(sql: String): String = query(sql).use { cursor ->
        assertTrue(cursor.moveToFirst()); cursor.getString(0)
    }
    private fun SupportSQLiteDatabase.strings(sql: String): List<String> = query(sql).use { cursor ->
        buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
    }
}
