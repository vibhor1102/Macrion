/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.core.dumb.data.database

import android.content.ContentValues
import android.content.Context
import android.os.Build
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class Migration2to3Tests {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), DumbDatabase::class.java,
    )
    private lateinit var dbPath: String

    @Before fun setUp() {
        dbPath = ApplicationProvider.getApplicationContext<Context>()
            .getDatabasePath("dumb-migration-2-to-3-test").path
    }

    @Test fun migrate_trimsScenarioAndActionNames() {
        helper.createDatabase(dbPath, 2).use { db ->
            db.insert("dumb_scenario_table", 0, ContentValues().apply {
                put("id", 1L)
                put("name", " Scenario ")
                put("repeat_count", 1)
                put("is_repeat_infinite", 0)
                put("max_duration_minutes", 1)
                put("is_duration_infinite", 1)
                put("randomize", 0)
            })
            db.insert("dumb_action_table", 0, ContentValues().apply {
                put("id", 2L)
                put("dumb_scenario_id", 1L)
                put("priority", 0)
                put("name", " Action ")
                put("type", "PAUSE")
                put("pause_duration", 100L)
            })
        }

        helper.runMigrationsAndValidate(dbPath, 3, true, Migration2to3).use { db ->
            assertEquals("Scenario", db.string("SELECT name FROM dumb_scenario_table WHERE id=1"))
            assertEquals("Action", db.string("SELECT name FROM dumb_action_table WHERE id=2"))
        }
    }

    private fun SupportSQLiteDatabase.string(sql: String): String = query(sql).use { cursor ->
        assertTrue(cursor.moveToFirst())
        cursor.getString(0)
    }
}
