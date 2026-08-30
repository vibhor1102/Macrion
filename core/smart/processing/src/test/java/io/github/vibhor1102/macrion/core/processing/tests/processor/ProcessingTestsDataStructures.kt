/*
 * Copyright (C) 2024 Kevin Buzeau
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
package io.github.vibhor1102.macrion.core.processing.tests.processor

import android.graphics.Bitmap
import android.graphics.Point

import io.github.vibhor1102.macrion.core.base.identifier.Identifier
import io.github.vibhor1102.macrion.core.domain.model.action.ToggleEvent
import io.github.vibhor1102.macrion.core.domain.model.condition.ScreenCondition
import io.github.vibhor1102.macrion.core.domain.model.counter.Counter
import io.github.vibhor1102.macrion.core.domain.model.event.ScreenEvent
import io.github.vibhor1102.macrion.core.domain.model.event.TriggerEvent
import io.github.vibhor1102.macrion.core.domain.model.scenario.Scenario
import io.github.vibhor1102.macrion.core.processing.data.processor.ConditionsResults
import io.github.vibhor1102.macrion.core.processing.domain.model.ProcessedConditionResult


internal data class TestScenario(
    val scenario: Scenario,
    val screenEvents: List<ScreenEvent>,
    val triggerEvents: List<TriggerEvent>,
    val counters: List<Counter> = emptyList(),
)

internal data class TestImageCondition(
    val imageCondition: ScreenCondition.Image,
    val mockedBitmap: Bitmap,
)

internal data class TestEventToggle(
    val targetId: Identifier,
    val toggleType: ToggleEvent.ToggleType,
)

internal fun TestImageCondition.expectedResult(detected: Boolean) = ProcessedConditionResult.Screen(
    isFulfilled = detected == imageCondition.shouldBeDetected,
    haveBeenDetected = detected,
    condition = imageCondition,
    position = Point(0, 0),
    confidenceRate = 0.0,
    size = Point(0, 0),
)

internal fun TriggerEvent.expectedResult(detected: Boolean): List<ProcessedConditionResult.Trigger> =
    ConditionsResults().apply {
        addResult(
            conditionId = id.databaseId,
            result = ProcessedConditionResult.Trigger(isFulfilled = detected, condition = conditions.first()))
    }.getAllTriggerConditionsResults()