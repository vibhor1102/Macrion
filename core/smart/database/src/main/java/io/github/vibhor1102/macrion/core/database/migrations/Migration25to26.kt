/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.core.database.migrations

import android.content.ContentValues
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.vibhor1102.macrion.core.database.ACTION_TABLE
import io.github.vibhor1102.macrion.core.database.CONDITION_TABLE
import io.github.vibhor1102.macrion.core.database.COUNTERS_TABLE
import io.github.vibhor1102.macrion.core.database.EVENT_TABLE
import io.github.vibhor1102.macrion.core.database.SCENARIO_TABLE

/** Establishes trimmed names while preserving every exact counter reference. */
object Migration25to26 : Migration(25, 26) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE `$SCENARIO_TABLE` SET `name` = trim(`name`)")
        db.execSQL("UPDATE `$EVENT_TABLE` SET `name` = trim(`name`)")
        db.execSQL("UPDATE `$ACTION_TABLE` SET `name` = trim(`name`), `external_action_name` = trim(`external_action_name`)")
        db.execSQL("UPDATE `$CONDITION_TABLE` SET `name` = trim(`name`)")

        val scenarioIds = mutableListOf<Long>()
        db.query("SELECT DISTINCT `scenarioId` FROM `$COUNTERS_TABLE`").use { cursor ->
            while (cursor.moveToNext()) scenarioIds += cursor.getLong(0)
        }
        scenarioIds.forEach { scenarioId -> db.normalizeCounters(scenarioId) }
    }

    private fun SupportSQLiteDatabase.normalizeCounters(scenarioId: Long) {
        data class StoredCounter(val oldName: String, val value: Double)
        val counters = mutableListOf<StoredCounter>()
        query(
            "SELECT `counterName`, `startingValue` FROM `$COUNTERS_TABLE` " +
                "WHERE `scenarioId` = ? ORDER BY (`counterName` != trim(`counterName`)), `counterName`",
            arrayOf(scenarioId),
        ).use { cursor ->
            while (cursor.moveToNext()) counters += StoredCounter(cursor.getString(0), cursor.getDouble(1))
        }

        val used = mutableSetOf<String>()
        val renames = counters.associate { counter ->
            val base = counter.oldName.trim().ifEmpty { "Counter" }
            var name = base
            var suffix = 2
            while (!used.add(name)) name = "$base (${suffix++})"
            counter.oldName to name
        }

        renames.filter { (old, new) -> old != new }.forEach { (old, new) ->
            updateExactCounterReferences(scenarioId, old, new)
        }
        delete(COUNTERS_TABLE, "`scenarioId` = ?", arrayOf(scenarioId.toString()))
        counters.forEach { counter ->
            insert(COUNTERS_TABLE, 0, ContentValues().apply {
                put("counterName", renames.getValue(counter.oldName))
                put("scenarioId", scenarioId)
                put("startingValue", counter.value)
            })
        }
    }

    private fun SupportSQLiteDatabase.updateExactCounterReferences(scenarioId: Long, old: String, new: String) {
        val eventScope = "`eventId` IN (SELECT `id` FROM `$EVENT_TABLE` WHERE `scenario_id` = ?)"
        listOf("counter_name", "counter_operation_counter_name").forEach { column ->
            execSQL(
                "UPDATE `$ACTION_TABLE` SET `$column` = ? WHERE $eventScope AND `$column` = ?",
                arrayOf<Any?>(new, scenarioId, old),
            )
        }
        listOf("counter_name", "counter_value_counter_name", "number_counter_value_counter_name").forEach { column ->
            execSQL(
                "UPDATE `$CONDITION_TABLE` SET `$column` = ? WHERE $eventScope AND `$column` = ?",
                arrayOf<Any?>(new, scenarioId, old),
            )
        }
        listOf("notification_message_text", "text_value").forEach { column ->
            execSQL(
                "UPDATE `$ACTION_TABLE` SET `$column` = replace(`$column`, ?, ?) WHERE $eventScope",
                arrayOf<Any?>("{$old}", "{$new}", scenarioId),
            )
        }
    }
}
