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
package io.github.vibhor1102.macrion.core.domain.model.action

import io.github.vibhor1102.macrion.core.base.identifier.Identifier
import io.github.vibhor1102.macrion.core.database.entity.ChangeCounterOperationType
import io.github.vibhor1102.macrion.core.domain.model.counter.CounterOperationValue

data class ChangeCounter(
    override val id: Identifier,
    override val eventId: Identifier,
    override val name: String? = null,
    override var priority: Int,
    val counterName: String,
    val operation: OperationType,
    val operationValue: CounterOperationValue,
): Action() {

    /**
     * Types of counter change of a [ChangeCounter].
     * Keep the same names as the db ones.
     */
    enum class OperationType {
        /** Add to the current counter value. */
        ADD,
        /** Remove from the current counter value. */
        MINUS,
        /** Set the counter to a specific value. */
        SET;

        fun toEntity(): ChangeCounterOperationType = ChangeCounterOperationType.valueOf(name)
    }

    override fun isComplete(): Boolean =
        super.isComplete() && counterName.isNotEmpty() && operationValue.isComplete()

    override fun hashCodeNoIds(): Int =
        name.hashCode() + counterName.hashCode() + operation.hashCode() + operationValue.hashCode()

    override fun deepCopy(): ChangeCounter = copy(
        name = "" + name,
        counterName = "" + counterName,
    )
}