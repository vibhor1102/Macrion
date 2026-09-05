/* Copyright (C) 2026 Vibhor Goel */
package io.github.vibhor1102.macrion.core.dumb.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Normalizes user-visible dumb scenario and action names. */
object Migration2to3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE `dumb_scenario_table` SET `name` = trim(`name`)")
        db.execSQL("UPDATE `dumb_action_table` SET `name` = trim(`name`)")
    }
}
