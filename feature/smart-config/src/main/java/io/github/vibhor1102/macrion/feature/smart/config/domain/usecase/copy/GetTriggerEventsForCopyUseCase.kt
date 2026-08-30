/*
 * Copyright (C) 2026 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.smart.config.domain.usecase.copy

import io.github.vibhor1102.macrion.core.base.di.Dispatcher
import io.github.vibhor1102.macrion.core.base.di.HiltCoroutineDispatchers.IO
import io.github.vibhor1102.macrion.core.base.identifier.Identifier
import io.github.vibhor1102.macrion.core.domain.IRepository
import io.github.vibhor1102.macrion.core.domain.model.event.TriggerEvent
import io.github.vibhor1102.macrion.feature.smart.config.domain.EditionRepository
import io.github.vibhor1102.macrion.feature.smart.config.domain.usecase.copy.model.EventsForCopy
import kotlinx.coroutines.CoroutineDispatcher

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.filter


class GetTriggerEventsForCopyUseCase @Inject constructor(
    @Dispatcher(IO) dispatcherIo: CoroutineDispatcher,
    editionRepository: EditionRepository,
    smartRepository: IRepository,
) {

    private val editedScenarioId: Flow<Identifier?> = editionRepository.editionState.scenarioState
        .map { scenarioState -> scenarioState.value?.id }

    private val editedEvents: Flow<List<TriggerEvent>> = editionRepository.editionState.editedTriggerEventsState
        .map { editedEventsState -> editedEventsState.value ?: emptyList() }

    private val allTriggerEvents: Flow<List<TriggerEvent>> = smartRepository.allTriggerEvents
        .flowOn(dispatcherIo)


    operator fun invoke(): Flow<EventsForCopy<TriggerEvent>> =
        combine(editedScenarioId, editedEvents, allTriggerEvents) { scenarioId, allEditedEvents, dbEvents ->
            EventsForCopy(
                thisScenario = allEditedEvents
                    .filter { event -> event.isComplete() },
                otherScenario = dbEvents
                    .filter { event -> event.isComplete() && scenarioId != event.scenarioId },
            )
        }
}