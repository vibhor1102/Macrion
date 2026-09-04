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
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportComposeViewHolder
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.adapter.ReportTriggerConditionCard
import io.github.vibhor1102.macrion.feature.smart.debugging.ui.dialog.report.details.condition.EventOccurrenceItem

class EventOccurrenceConditionTriggerViewHolder(parent: ViewGroup) : ReportComposeViewHolder<EventOccurrenceItem.Trigger>(
    parent = parent,
    content = { item -> ReportTriggerConditionCard(item.conditionName, item.description, item.iconRes) },
) {

    fun bind(item: EventOccurrenceItem.Trigger) {
        bindComposeItem(item)
    }
}
