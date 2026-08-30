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
package io.github.vibhor1102.macrion.core.smart.debugging.data.mapping

import io.github.vibhor1102.macrion.core.smart.debugging.ImageEventMessage
import io.github.vibhor1102.macrion.core.smart.debugging.ImageEventMessageKt.imageConditionResult
import io.github.vibhor1102.macrion.core.smart.debugging.TriggerEventMessage
import io.github.vibhor1102.macrion.core.smart.debugging.TriggerEventMessageKt.triggerConditionResult
import io.github.vibhor1102.macrion.core.smart.debugging.domain.model.report.DebugReportConditionResult


internal fun DebugReportConditionResult.ScreenCondition.toProtobuf(): ImageEventMessage.ImageConditionResult =
    imageConditionResult {
        conditionId = this@toProtobuf.conditionId
        isFulfilled = this@toProtobuf.isFulFilled
        durationMs = this@toProtobuf.detectionDurationMs
        confidenceRate = this@toProtobuf.confidenceRate
    }

internal fun DebugReportConditionResult.TriggerCondition.toProtobuf(): TriggerEventMessage.TriggerConditionResult =
    triggerConditionResult {
        conditionId = this@toProtobuf.conditionId
        isFulfilled = this@toProtobuf.isFulFilled
    }


internal fun ImageEventMessage.ImageConditionResult.toDomain(): DebugReportConditionResult.ScreenCondition =
    DebugReportConditionResult.ScreenCondition(
        conditionId = conditionId,
        isFulFilled = isFulfilled,
        detectionDurationMs = durationMs,
        confidenceRate = confidenceRate,
    )

internal fun TriggerEventMessage.TriggerConditionResult.toDomain(): DebugReportConditionResult.TriggerCondition =
    DebugReportConditionResult.TriggerCondition(
        conditionId = conditionId,
        isFulFilled = isFulfilled,
    )