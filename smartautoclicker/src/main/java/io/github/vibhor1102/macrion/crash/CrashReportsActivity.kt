/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.github.vibhor1102.macrion.BuildConfig
import io.github.vibhor1102.macrion.R
import io.github.vibhor1102.macrion.core.base.crash.CrashReportStore
import io.github.vibhor1102.macrion.core.ui.compose.MacrionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration

/** Private local viewer. No share intent, upload button, or durable upload approval. */
class CrashReportsActivity : AppCompatActivity() {
    private var reports by mutableStateOf<List<CrashReportStore.Report>>(emptyList())
    private var failed by mutableStateOf(false)
    private var loading by mutableStateOf(true)
    private var expanded by mutableStateOf<String?>(null)
    private var sending by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MacrionTheme {
                Scaffold(topBar = {
                    TopAppBar(title = { Text(stringResource(R.string.crash_reports_title)) },
                        navigationIcon = { TextButton(onClick = ::finish) { Text(stringResource(R.string.content_desc_back)) } })
                }) { padding ->
                    LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { Text(stringResource(R.string.crash_report_local_notice)) }
                        if (BuildConfig.DEBUG) item {
                            Button(enabled = !loading, onClick = { reload { createSample() } }) {
                                Text(stringResource(R.string.crash_reports_sample))
                            }
                        }
                        if (loading) item { Text(stringResource(R.string.crash_reports_loading)) }
                        else if (failed) item { Text(stringResource(R.string.crash_reports_error)) }
                        else if (reports.isEmpty()) item { Text(stringResource(R.string.crash_reports_empty)) }
                        items(reports, key = { it.id }) { report ->
                            Card {
                                Column(Modifier.padding(12.dp)) {
                                    Text(stringResource(R.string.crash_report_label, report.id.take(8)))
                                    TextButton(onClick = { expanded = if (expanded == report.id) null else report.id }) {
                                        Text(stringResource(R.string.crash_report_review))
                                    }
                                    TextButton(enabled = !loading && sending == null, onClick = { send(report) }) {
                                        Text(stringResource(if (sending == report.id) R.string.crash_report_sending else R.string.crash_report_send))
                                    }
                                    TextButton(enabled = !loading && sending == null, onClick = { reload { crashReportStore().delete(report.id) } }) {
                                        Text(stringResource(R.string.crash_report_discard))
                                    }
                                    if (expanded == report.id) SelectionContainer { Text(report.body) }
                                }
                            }
                        }
                    }
                }
            }
        }
        reload()
    }

    private fun send(report: CrashReportStore.Report) {
        if (sending != null) return
        sending = report.id
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    CrashReportUploader().send(report.body).also { outcome ->
                        if (outcome == CrashReportUploader.Result.SENT || outcome == CrashReportUploader.Result.ALREADY_SENT) {
                            crashReportStore().delete(report.id)
                        }
                    }
                }
            }
            val outcome = result.getOrNull()
            val message = when (outcome) {
                CrashReportUploader.Result.SENT, CrashReportUploader.Result.ALREADY_SENT -> R.string.crash_report_sent
                CrashReportUploader.Result.REJECTED -> R.string.crash_report_rejected
                else -> R.string.crash_report_send_failed
            }
            android.widget.Toast.makeText(this@CrashReportsActivity, message, android.widget.Toast.LENGTH_LONG).show()
            sending = null
            reload()
        }
    }

    private fun reload(action: () -> Unit = {}) {
        if (loading && reports.isNotEmpty()) return
        loading = true
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    action()
                    crashReportStore().pending().also { entries ->
                        entries.forEach { crashReportStore().markPrompted(it.id) }
                    }
                }
            }
            reports = result.getOrDefault(emptyList())
            failed = result.isFailure
            loading = false
        }
    }

    private fun createSample() {
        if (!BuildConfig.DEBUG) return
        val error = IllegalStateException("Synthetic failure for tester@example.com; token=sample-secret; /storage/emulated/0/private.txt",
            IllegalArgumentException("Synthetic parser error at index 3"))
        LocalCrashAdministrator().shouldStartCollecting(this, CoreConfiguration(),
            ReportBuilder().exception(error).uncaughtExceptionThread(Thread.currentThread()).endApplication())
    }
}
