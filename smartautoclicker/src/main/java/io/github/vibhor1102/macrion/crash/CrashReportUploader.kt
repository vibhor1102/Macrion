/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import io.github.vibhor1102.macrion.core.base.crash.CrashReportStore
import java.net.HttpURLConnection
import java.net.URL

/** Uploads one user-selected local report. It never schedules or retries work. */
class CrashReportUploader(
    private val endpoint: URL = URL(ENDPOINT),
    private val openConnection: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
) {
    enum class Result { SENT, ALREADY_SENT, REJECTED, RETRY_LATER }

    fun send(body: String): Result {
        val bytes = body.toByteArray(Charsets.UTF_8)
        require(bytes.size <= CrashReportStore.MAX_BYTES)
        require(endpoint.protocol == "https" || isLoopback(endpoint))
        val connection = openConnection(endpoint)
        return try {
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setFixedLengthStreamingMode(bytes.size)
            connection.outputStream.use { it.write(bytes) }
            when (connection.responseCode) {
                HttpURLConnection.HTTP_CREATED -> Result.SENT
                HttpURLConnection.HTTP_OK -> Result.ALREADY_SENT
                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT,
                HttpURLConnection.HTTP_ENTITY_TOO_LARGE,
                HttpURLConnection.HTTP_UNSUPPORTED_TYPE -> Result.REJECTED
                else -> Result.RETRY_LATER
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun isLoopback(url: URL) = url.protocol == "http" &&
        (url.host == "127.0.0.1" || url.host == "localhost" || url.host == "::1")

    companion object {
        const val ENDPOINT = "https://macrion-crash-reports.vibhor1102.workers.dev/v1/reports"
    }
}
