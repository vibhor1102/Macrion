/*
 * Copyright (C) 2025 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.event.adapter

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.stringResource
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportComposeViewHolder
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportIconTransition
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportNameValueRow
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.event.DebugEventStateItem

class EventStateEventViewHolder(parent: ViewGroup) : ReportComposeViewHolder<DebugEventStateItem.EventState>(
    parent = parent,
    content = { item ->
        ReportNameValueRow(name = item.eventName, value = "") {
            ReportIconTransition(
                startIcon = if (item.haveChanged) (!item.isEnabled).toEventStateIcon() else null,
                endIcon = item.isEnabled.toEventStateIcon(),
                separator = stringResource(R.string.event_state_changed_separator),
            )
        }
    },
) {

    fun bind(item: DebugEventStateItem.EventState) {
        bindComposeItem(item)
    }

}

@DrawableRes
private fun Boolean.toEventStateIcon(): Int =
    if (this) R.drawable.ic_confirm else R.drawable.ic_cancel
