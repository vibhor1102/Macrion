/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.core.base.crash

/** Best-effort filtering, not an anonymity guarantee. Never used as a license to attach user content. */
class CrashRedactor {
    var replacements = 0
        private set
    var truncated = false
        private set

    fun redact(value: String?, limit: Int = 4096): String? {
        if (value == null) return null
        return try {
            if (value.length > limit) truncated = true
            var result = value.take(limit)
            for (pattern in PATTERNS) {
                result = pattern.replace(result) {
                    replacements++
                    "[redacted]"
                }
            }
            result
        } catch (_: Exception) {
            "[message unavailable]"
        }
    }

    companion object {
        // Bound input before regex work; patterns intentionally avoid nested unbounded quantifiers.
        private val PATTERNS = listOf(
            Regex("(?i)\\b(?:https?|content|file)://[^\\s<>\"']+"),
            Regex("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}"),
            Regex("(?i)\\b(?:bearer|basic)\\s+[A-Z0-9+/=._~-]+"),
            Regex("(?i)\\b(?:password|passwd|token|access_token|refresh_token|api[_-]?key|secret|authorization)\\b[\"']?\\s*[:=]\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s,;}]+)"),
            Regex("/(?:storage|sdcard|data/(?:user|user_de|data)|home)/[^\\s<>\"']+"),
        )
    }
}
