/* Copyright (C) 2026 Kevin Buzeau */
package com.buzbuz.smartautoclicker.feature.smart.debugging.ui.dialog.report.overview

import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.DebugReportEventOccurrence
import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.DebugReportOverview
import com.buzbuz.smartautoclicker.core.smart.debugging.domain.model.report.getDurationsNs
import java.math.BigDecimal
import java.math.RoundingMode

internal data class ExecutionLimiterIdleTime(
    val waitDurationNs: Long,
    val measuredDurationNs: Long,
)

/**
 * Build the share of detection-related time deliberately spent waiting for the Execution Limiter.
 * Event action phases are removed from the active processing duration so they cannot dilute the result.
 */
internal fun buildExecutionLimiterIdleTime(
    overview: DebugReportOverview,
    occurrences: List<DebugReportEventOccurrence>,
): ExecutionLimiterIdleTime? {
    val activeDurationNs = overview.activeDetectionDuration.inWholeNanoseconds
    val waitDurationNs = overview.executionLimiterWaitDuration.inWholeNanoseconds
    if (activeDurationNs < 0L || waitDurationNs < 0L) return null
    if (waitDurationNs == 0L) return ExecutionLimiterIdleTime(0L, activeDurationNs)

    var previousActionsCompletedAtNs: Long? = null
    var actionDurationNs = 0L
    for (occurrence in occurrences) {
        val durations = occurrence.getDurationsNs(previousActionsCompletedAtNs) ?: return null
        actionDurationNs = try {
            Math.addExact(actionDurationNs, durations.actionsDurationNs)
        } catch (_: ArithmeticException) {
            return null
        }
        previousActionsCompletedAtNs = occurrence.actionsCompletedAtNs
    }

    val activeNonActionDurationNs = (activeDurationNs - actionDurationNs).coerceAtLeast(0L)
    val measuredDurationNs = try {
        Math.addExact(waitDurationNs, activeNonActionDurationNs)
    } catch (_: ArithmeticException) {
        return null
    }
    return ExecutionLimiterIdleTime(waitDurationNs, measuredDurationNs)
}

internal fun ExecutionLimiterIdleTime.formatPercentage(): String {
    if (waitDurationNs == 0L || measuredDurationNs == 0L) return "0%"
    if (waitDurationNs == measuredDurationNs) return "100%"

    val percentage = BigDecimal.valueOf(waitDurationNs)
        .multiply(BigDecimal.valueOf(100L))
        .divide(BigDecimal.valueOf(measuredDurationNs), 8, RoundingMode.HALF_UP)
    if (percentage < BigDecimal("0.1")) return "<0.1%"

    val rounded = percentage.setScale(1, RoundingMode.HALF_UP).min(BigDecimal("99.9"))
    return "${rounded.toPlainString()}%"
}
