/*
 * Copyright (C) 2026 Vibhor Goel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package io.github.vibhor1102.macrion.core.database.migrations

import androidx.room.migration.Migration

/** Manual migrations registered by every [ClickDatabase] builder. */
val clickDatabaseMigrations: Array<Migration> = arrayOf(
    Migration1to2,
    Migration2to3,
    Migration3to4,
    Migration4to5,
    Migration5to6,
    Migration6to7,
    Migration9to10,
    Migration10to11,
    Migration12to13,
    Migration19to20,
    Migration21to22,
    Migration23to24,
    Migration25to26,
)
