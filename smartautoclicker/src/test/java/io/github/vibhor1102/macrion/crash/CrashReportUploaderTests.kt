/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CrashReportUploaderTests {
    @Test fun `sends exact JSON with bounded timeouts and no redirects`() {
        val connection = FakeConnection(201)
        val result = CrashReportUploader(URL("https://example.test/v1/reports")) { connection }.send("{\"schemaVersion\":1}")
        assertEquals(CrashReportUploader.Result.SENT, result)
        assertEquals("{\"schemaVersion\":1}", connection.output.toString(Charsets.UTF_8.name()))
        assertEquals("application/json; charset=utf-8", connection.getRequestProperty("Content-Type"))
        assertEquals(10_000, connection.connectTimeout)
        assertEquals(15_000, connection.readTimeout)
        assertEquals(false, connection.instanceFollowRedirects)
        assertEquals(true, connection.disconnected)
    }

    @Test fun `classifies idempotent permanent and retryable responses`() {
        val expected = mapOf(
            200 to CrashReportUploader.Result.ALREADY_SENT,
            400 to CrashReportUploader.Result.REJECTED,
            409 to CrashReportUploader.Result.REJECTED,
            413 to CrashReportUploader.Result.REJECTED,
            415 to CrashReportUploader.Result.REJECTED,
            429 to CrashReportUploader.Result.RETRY_LATER,
            503 to CrashReportUploader.Result.RETRY_LATER,
            302 to CrashReportUploader.Result.RETRY_LATER,
        )
        expected.forEach { (code, result) ->
            assertEquals(result, CrashReportUploader(URL("https://example.test")) { FakeConnection(code) }.send("{}"))
        }
    }

    @Test fun `rejects cleartext non-loopback endpoints before opening a connection`() {
        var opened = false
        val uploader = CrashReportUploader(URL("http://example.test")) { opened = true; FakeConnection(201) }
        assertThrows(IllegalArgumentException::class.java) { uploader.send("{}") }
        assertEquals(false, opened)
    }

    private class FakeConnection(private val code: Int) : HttpURLConnection(URL("https://example.test")) {
        val output = ByteArrayOutputStream()
        var disconnected = false
        override fun getResponseCode() = code
        override fun getOutputStream(): OutputStream = output
        override fun disconnect() { disconnected = true }
        override fun usingProxy() = false
        override fun connect() = Unit
        override fun getInputStream(): InputStream = InputStream.nullInputStream()
    }
}
