/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import io.github.vibhor1102.macrion.BuildConfig
import io.github.vibhor1102.macrion.core.base.crash.CrashEnvironment
import io.github.vibhor1102.macrion.core.base.crash.CrashReportFactory
import io.github.vibhor1102.macrion.core.base.crash.CrashReportStore
import org.acra.config.CoreConfiguration
import org.acra.config.ReportingAdministrator
import org.acra.builder.LastActivityManager
import org.acra.builder.ReportBuilder
import org.acra.data.CrashReportData
import org.acra.ktx.initAcra
import org.acra.plugins.Plugin
import org.acra.plugins.PluginLoader
import org.acra.scheduler.SenderScheduler
import org.acra.scheduler.SenderSchedulerFactory
import java.io.File

fun Context.crashReportStore() = CrashReportStore(File(noBackupFilesDir, "crash-reports"))

fun Context.crashEnvironment(): CrashEnvironment {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return CrashEnvironment(
        BuildConfig.VERSION_NAME,
        androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(packageInfo),
        BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE,
        Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.MANUFACTURER, Build.MODEL,
        if (android.os.Process.is64Bit()) "${Build.SUPPORTED_64_BIT_ABIS.firstOrNull()}" else "${Build.SUPPORTED_32_BIT_ABIS.firstOrNull()}",
    )
}

fun Application.initializeLocalCrashReporting() {
    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
    try {
        initAcra {
            reportContent = emptyList()
            pluginLoader = LocalCrashPluginLoader()
            alsoReportToAndroidFramework = true
        }
    } catch (_: Exception) {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        android.util.Log.w("LocalCrashReporting", "Local crash capture could not be initialized")
    }
}

/** Explicit construction avoids service discovery accidentally adding collectors or senders after an upgrade. */
class LocalCrashPluginLoader : PluginLoader {
    override fun <T : Plugin> load(clazz: Class<T>): List<T> =
        listOf(LocalCrashAdministrator(), NoCrashSenderSchedulerFactory()).filter { clazz.isInstance(it) }.map { clazz.cast(it)!! }

    override fun <T : Plugin> loadEnabled(config: CoreConfiguration, clazz: Class<T>): List<T> = load(clazz)
}

class LocalCrashAdministrator : ReportingAdministrator {
    override fun shouldStartCollecting(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder): Boolean {
        // ACRA 5.13.1 invokes this BEFORE its collectors and persister. Never let errors reopen that pipeline.
        try {
            if (reportBuilder.isEndApplication && reportBuilder.uncaughtExceptionThread != null) {
                reportBuilder.exception?.let { error ->
                    context.crashReportStore().save(CrashReportFactory().create(error, context.crashEnvironment(),
                        reportBuilder.uncaughtExceptionThread === Looper.getMainLooper().thread))
                }
            }
        } catch (_: Throwable) {
            // Best effort, including OOM/disk-full: preserve Android's original crash handling, no raw fallback.
        }
        return false
    }

    override fun shouldSendReport(context: Context, config: CoreConfiguration, crashReportData: CrashReportData) = false
    override fun shouldFinishActivity(context: Context, config: CoreConfiguration, lastActivityManager: LastActivityManager) = false
}

class NoCrashSenderSchedulerFactory : SenderSchedulerFactory {
    override fun create(context: Context, config: CoreConfiguration) = object : SenderScheduler {
        override fun scheduleReportSending(onlySendSilentReports: Boolean) = Unit
    }
}
