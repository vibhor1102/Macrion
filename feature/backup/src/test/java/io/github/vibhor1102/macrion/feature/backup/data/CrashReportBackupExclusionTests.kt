/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.feature.backup.data

import android.graphics.Point
import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.database.entity.ScenarioEntity
import io.github.vibhor1102.macrion.core.dumb.data.database.DumbScenarioEntity
import io.github.vibhor1102.macrion.core.dumb.data.database.DumbScenarioWithActions
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import io.github.vibhor1102.macrion.feature.backup.data.smart.SmartBackupDataSource
import io.github.vibhor1102.macrion.feature.backup.data.dumb.DumbBackupDataSource
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CrashReportBackupExclusionTests {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun `both scenario exporters and both formats exclude unrelated crash report files`() {
        val files = temporary.newFolder("files")
        val noBackup = temporary.newFolder("no_backup")
        // Also protect against future placement under files: exporters must not recursively archive it.
        for (root in listOf(files, noBackup)) {
            File(root, "crash-reports").mkdirs()
            File(root, "crash-reports/report.json").writeText("PRIVATE_CRASH_SENTINEL")
            File(root, "crash-reports/report.seen").writeText("PRIVATE_PROMPT_SENTINEL")
        }
        for (format in BackupArchiveFormat.entries) {
            val output = ByteArrayOutputStream()
            ZipOutputStream(output).use { zip ->
                SmartBackupDataSource(files).addScenarioToZipFile(zip,
                    CompleteScenario(ScenarioEntity(1, "Smart", 600, 0.0, false), emptyList(), emptyList()),
                    Point(1080, 2400), format)
                DumbBackupDataSource(files).addScenarioToZipFile(zip,
                    DumbScenarioWithActions(DumbScenarioEntity(2, "Dumb", 1, false, 1, false, false), emptyList(), null),
                    Point(1080, 2400), format)
            }
            var jsonCount = 0
            ZipInputStream(output.toByteArray().inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    assertFalse(entry.name.contains("crash-reports"))
                    val body = zip.readBytes().toString(Charsets.UTF_8)
                    assertFalse(body.contains("PRIVATE_CRASH_SENTINEL"))
                    assertFalse(body.contains("PRIVATE_PROMPT_SENTINEL"))
                    if (entry.name.endsWith(".json")) jsonCount++
                    entry = zip.nextEntry
                }
            }
            assertEquals(2, jsonCount)
        }
    }
}
