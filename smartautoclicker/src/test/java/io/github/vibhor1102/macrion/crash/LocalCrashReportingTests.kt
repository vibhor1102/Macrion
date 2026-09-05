/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import org.acra.builder.LastActivityManager
import org.acra.builder.ReportBuilder
import org.acra.builder.ReportExecutor
import org.acra.collector.Collector
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportDataFactory
import org.acra.file.ReportLocator
import org.acra.scheduler.SchedulerStarter
import org.acra.sender.ReportSenderFactory
import org.acra.startup.StartupProcessor
import org.acra.util.ProcessFinisher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LocalCrashReportingTests {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val config = CoreConfiguration(pluginLoader = LocalCrashPluginLoader(), reportContent = emptyList(), alsoReportToAndroidFramework = true)

    @Before fun clearReports() { context.crashReportStore().pending().forEach { context.crashReportStore().delete(it.id) } }

    @Test fun `ACRA executor captures sanitized local report but never collects persists or schedules its own report`() {
        val collector = mock<CrashReportDataFactory>()
        val finisher = mock<ProcessFinisher>()
        val activities = mock<LastActivityManager>()
        val scheduler = mock<SchedulerStarter>()
        val handler = mock<Thread.UncaughtExceptionHandler>()
        val builder = ReportBuilder().exception(IllegalStateException("email=tester@example.com token=private"))
            .customData(mapOf("scenarioName" to "PRIVATE_SCENARIO_CONTENT"))
            .uncaughtExceptionThread(Thread.currentThread()).endApplication()
        val executor = ReportExecutor(context, config, collector, handler, finisher, scheduler, activities)
        executor.isEnabled = true
        executor.execute(builder)
        val reports = context.crashReportStore().pending()
        assertEquals(1, reports.size)
        assertFalse(reports.single().body.contains("tester@example.com"))
        assertFalse(reports.single().body.contains("token=private"))
        assertFalse(reports.single().body.contains("PRIVATE_SCENARIO_CONTENT"))
        assertFalse(reports.single().prompted)
        assertTrue(File(context.noBackupFilesDir, "crash-reports/${reports.single().id}.json").isFile)
        assertFalse(File(context.filesDir, "crash-reports").exists())
        verify(collector, never()).createCrashData(builder)
        verify(handler).uncaughtException(Thread.currentThread(), builder.exception!!)
        org.mockito.Mockito.verifyNoInteractions(scheduler, finisher)
        assertTrue(ReportLocator(context).approvedReports.isEmpty())
        assertTrue(ReportLocator(context).unapprovedReports.isEmpty())
    }

    @Test fun `storage failure blocks ACRA collection rather than falling back to raw reports`() {
        val unwritable = object : ContextWrapper(context) {
            override fun getNoBackupFilesDir(): File = File(context.cacheDir, "not-a-directory").apply { writeText("test") }
        }
        val result = LocalCrashAdministrator().shouldStartCollecting(unwritable, config,
            ReportBuilder().exception(RuntimeException("private")).uncaughtExceptionThread(Thread.currentThread()).endApplication())
        assertFalse(result)
        assertTrue(context.crashReportStore().pending().isEmpty())
    }

    @Test fun `explicit plugin list has no collectors senders or startup processors`() {
        val loader = LocalCrashPluginLoader()
        assertTrue(loader.load(Collector::class.java).isEmpty())
        assertTrue(loader.load(ReportSenderFactory::class.java).isEmpty())
        assertTrue(loader.load(StartupProcessor::class.java).isEmpty())
        NoCrashSenderSchedulerFactory().create(context, config).scheduleReportSending(false)
        assertTrue(context.crashReportStore().pending().isEmpty())
    }

    @Test fun `handled exceptions do not create reports`() {
        assertFalse(LocalCrashAdministrator().shouldStartCollecting(context, config, ReportBuilder().exception(RuntimeException())))
        assertTrue(context.crashReportStore().pending().isEmpty())
    }
}
