/*
 * Copyright (C) 2023 Kevin Buzeau
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
package io.github.vibhor1102.macrion.core.android.intent

import android.net.Uri
import io.github.vibhor1102.macrion.core.android.intent.actions.getAllBroadcastActions

import io.github.vibhor1102.macrion.core.android.intent.actions.getAndroidAPIStartActivityIntentActions
import io.github.vibhor1102.macrion.core.android.intent.actions.toAndroidIntentActions
import io.github.vibhor1102.macrion.core.android.intent.flags.getAndroidAPIBroadcastIntentFlags
import io.github.vibhor1102.macrion.core.android.intent.flags.getAndroidAPIStartActivityIntentFlags
import io.github.vibhor1102.macrion.core.android.intent.flags.toAndroidIntentFlags

data class AndroidIntentApi<Value>(
    val value: Value,
    val displayName: String,
    val helpUri: Uri,
)

fun getBroadcastReceptionIntentActions(): List<AndroidIntentApi<String>> =
    getAllBroadcastActions().toAndroidIntentActions()

fun getStartActivityIntentActions(): List<AndroidIntentApi<String>> =
    getAndroidAPIStartActivityIntentActions().toAndroidIntentActions()

fun getStartActivityIntentFlags(): List<AndroidIntentApi<Int>> =
    getAndroidAPIStartActivityIntentFlags().toAndroidIntentFlags()

fun getBroadcastIntentFlags(): List<AndroidIntentApi<Int>> =
    getAndroidAPIBroadcastIntentFlags().toAndroidIntentFlags()