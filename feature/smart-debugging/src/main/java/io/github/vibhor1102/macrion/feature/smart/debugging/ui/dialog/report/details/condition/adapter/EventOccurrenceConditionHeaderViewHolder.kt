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
package io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.adapter

import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import io.github.vibhor1102.macrion.feature.smart.debugging.R
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportComposeViewHolder
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportKeyValueCard
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.EventOccurrenceItem

class EventOccurrenceConditionHeaderViewHolder(parent: ViewGroup) : ReportComposeViewHolder<EventOccurrenceItem.Header>(
    parent = parent,
    content = { item ->
        ReportKeyValueCard(
            title = stringResource(R.string.item_event_occurrence_details_header_title),
            value = item.conditionOperatorValueText,
        )
    },
) {

    fun bind(item: EventOccurrenceItem.Header) {
        bindComposeItem(item)
    }
}
