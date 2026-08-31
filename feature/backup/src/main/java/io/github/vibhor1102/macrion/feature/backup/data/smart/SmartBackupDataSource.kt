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
package io.github.vibhor1102.macrion.feature.backup.data.smart

import android.graphics.Point
import android.util.Log

import io.github.vibhor1102.macrion.core.database.DATABASE_VERSION
import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.database.entity.ConditionEntity
import io.github.vibhor1102.macrion.core.database.entity.ConditionType
import io.github.vibhor1102.macrion.core.database.entity.EventType
import io.github.vibhor1102.macrion.feature.backup.data.base.CONDITION_BACKUP_MATCH_REGEX
import io.github.vibhor1102.macrion.feature.backup.data.base.MACRION_CONDITION_BACKUP_MATCH_REGEX
import io.github.vibhor1102.macrion.feature.backup.data.base.MACRION_FORMAT_NAME
import io.github.vibhor1102.macrion.feature.backup.data.base.MACRION_SMART_SCENARIO_BACKUP_MATCH_REGEX
import io.github.vibhor1102.macrion.feature.backup.data.base.LEGACY_CONDITION_BACKUP_MATCH_REGEX
import io.github.vibhor1102.macrion.feature.backup.data.base.SMART_SCENARIO_BACKUP_MATCH_REGEX
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupAdditionalFile
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import io.github.vibhor1102.macrion.feature.backup.data.base.ScenarioBackupDataSource
import io.github.vibhor1102.macrion.feature.backup.data.base.ScenarioBackupSerializer
import io.github.vibhor1102.macrion.feature.backup.data.base.backupFolderName
import io.github.vibhor1102.macrion.feature.backup.data.base.scenarioBackupFileName
import io.github.vibhor1102.macrion.feature.backup.data.base.withMacrionSubExtension
import io.github.vibhor1102.macrion.feature.backup.data.base.commitStagedFiles
import io.github.vibhor1102.macrion.feature.backup.data.base.resolveContainedFile

import java.io.File

internal class SmartBackupDataSource(
    private val appDataDir: File,
): ScenarioBackupDataSource<ScenarioBackup, CompleteScenario>(appDataDir) {

    /** Regex matching a scenario json file into its folder in a backup archive. */
    private val scenarioUnzipMatchRegex = SMART_SCENARIO_BACKUP_MATCH_REGEX.toRegex()
    private val nativeScenarioUnzipMatchRegex = MACRION_SMART_SCENARIO_BACKUP_MATCH_REGEX.toRegex()
    /** Regex matching a condition file (png) into its folder in a backup archive. */
    private val conditionUnzipMatchRegex = CONDITION_BACKUP_MATCH_REGEX.toRegex()
    private val nativeConditionUnzipMatchRegex = MACRION_CONDITION_BACKUP_MATCH_REGEX.toRegex()
    /** Regex matching a legacy condition file (raw pixels) into its folder in a backup archive. */
    private val legacyConditionUnzipMatchRegex = LEGACY_CONDITION_BACKUP_MATCH_REGEX.toRegex()

    var screenCompatWarning = false
        private set

    override val serializer: ScenarioBackupSerializer<ScenarioBackup> = ScenarioSerializer()

    override fun isScenarioBackupFileZipEntry(fileName: String, format: BackupArchiveFormat): Boolean =
        fileName.matches(if (format == BackupArchiveFormat.MACRION_NATIVE) nativeScenarioUnzipMatchRegex else scenarioUnzipMatchRegex)

    override fun isScenarioBackupAdditionalFileZipEntry(fileName: String, format: BackupArchiveFormat): Boolean =
        if (format == BackupArchiveFormat.MACRION_NATIVE) {
            fileName.matches(nativeConditionUnzipMatchRegex)
        } else {
            fileName.matches(conditionUnzipMatchRegex) || fileName.matches(legacyConditionUnzipMatchRegex)
        }

    override fun getBackupAdditionalFilesPaths(
        scenario: CompleteScenario,
        format: BackupArchiveFormat,
    ): Set<BackupAdditionalFile> =
        buildSet {
            scenario.events.forEach { completeEvent ->
                if (completeEvent.event.type == EventType.IMAGE_EVENT) {
                    completeEvent.conditions.forEach { condition ->
                        if (condition.type == ConditionType.ON_IMAGE_DETECTED) {
                            val path = condition.path!!
                            add(BackupAdditionalFile(
                                sourcePath = path,
                                archivePath = if (format == BackupArchiveFormat.MACRION_NATIVE) {
                                    path.withMacrionSubExtension()
                                } else {
                                    path
                                },
                            ))
                        }
                    }
                }
            }
        }

    override fun getBackupZipFolderName(scenario: CompleteScenario): String =
        scenario.backupFolderName()

    override fun getBackupFileName(scenario: CompleteScenario, format: BackupArchiveFormat): String =
        scenario.scenarioBackupFileName(format)

    override fun createBackupFromScenario(
        scenario: CompleteScenario,
        screenSize: Point,
        format: BackupArchiveFormat,
        portableDatabaseVersion: Int?,
    ): ScenarioBackup =
        ScenarioBackup(
            format = if (format == BackupArchiveFormat.MACRION_NATIVE) MACRION_FORMAT_NAME else null,
            scenario = scenario.withArchivePaths(format),
            screenWidth = screenSize.x,
            screenHeight = screenSize.y,
            version = portableDatabaseVersion ?: DATABASE_VERSION,
        )

    override fun verifyExtractedBackup(backup: ScenarioBackup, screenSize: Point): CompleteScenario? {
        Log.i(TAG, "Verifying smart scenario ${backup.scenario.scenario.id}")

        backup.scenario.events.forEach { event ->
            if (event.actions.isEmpty()) {
                Log.w(TAG, "Invalid scenario, action list is empty.")
                return null
            }

            if (event.conditions.isEmpty()) {
                Log.w(TAG, "Invalid scenario, condition list is empty.")
                return null
            }

            if (event.event.type == EventType.IMAGE_EVENT) {
                if (event.conditions.find { condition -> !condition.isScreenCondition() } != null) {
                    Log.w(TAG, "Invalid scenario, condition list is invalid.")
                    return null
                }
            }

            if (event.event.type == EventType.TRIGGER_EVENT) {
                if (event.conditions.find { condition -> !condition.isTriggerCondition() } != null) {
                    Log.w(TAG, "Invalid scenario, condition list is invalid.")
                    return null
                }
            }

            event.conditions.forEach { condition ->
                if (condition.type == ConditionType.ON_IMAGE_DETECTED && (
                            condition.path == null ||
                                !resolveContainedFile(appDataDir, condition.path!!).exists())) {
                    Log.w(TAG, "Invalid screen condition, ${condition.path} file does not exist.")
                    return null
                }
            }
        }

        if (!screenCompatWarning) {
            screenCompatWarning = hasDifferentScreenSize(
                currentWidth = screenSize.x,
                currentHeight = screenSize.y,
                backupWidth = backup.screenWidth,
                backupHeight = backup.screenHeight,
            )
        }

        Log.i(TAG, "Smart scenario is valid, has warnings: $screenCompatWarning")
        return backup.scenario
    }

    override fun reset() {
        super.reset()
        screenCompatWarning = false
    }

    /** Move only assets referenced by scenarios which passed verification into live app storage. */
    fun commitValidAdditionalFiles(destinationDir: File) {
        val referencedPaths = validBackups
            .flatMap { scenario -> scenario.events }
            .flatMap { event -> event.conditions }
            .filter { condition -> condition.type == ConditionType.ON_IMAGE_DETECTED }
            .mapNotNull(ConditionEntity::path)
            .toSet()
        commitStagedFiles(appDataDir, destinationDir, referencedPaths)
    }

    private fun ConditionEntity.isScreenCondition(): Boolean =
        when (type) {
            ConditionType.ON_COLOR_DETECTED,
            ConditionType.ON_IMAGE_DETECTED,
            ConditionType.ON_NUMBER_DETECTED,
            ConditionType.ON_TEXT_DETECTED -> true

            ConditionType.ON_COUNTER_REACHED,
            ConditionType.ON_BROADCAST_RECEIVED,
            ConditionType.ON_TIMER_REACHED -> false
        }

    private fun ConditionEntity.isTriggerCondition(): Boolean =
        when (type) {
            ConditionType.ON_COUNTER_REACHED,
            ConditionType.ON_BROADCAST_RECEIVED,
            ConditionType.ON_TIMER_REACHED -> true

            ConditionType.ON_COLOR_DETECTED,
            ConditionType.ON_IMAGE_DETECTED,
            ConditionType.ON_NUMBER_DETECTED,
            ConditionType.ON_TEXT_DETECTED -> false
        }
}

private fun CompleteScenario.withArchivePaths(format: BackupArchiveFormat): CompleteScenario =
    if (format != BackupArchiveFormat.MACRION_NATIVE) this else copy(
        events = events.map { event ->
            event.copy(
                conditions = event.conditions.map { condition ->
                    condition.copy(path = condition.path?.withMacrionSubExtension())
                }
            )
        }
    )

/** Tag for logs. */
private const val TAG = "SmartBackupEngine"

internal fun hasDifferentScreenSize(
    currentWidth: Int,
    currentHeight: Int,
    backupWidth: Int,
    backupHeight: Int,
): Boolean {
    val sameOrientation = currentWidth == backupWidth && currentHeight == backupHeight
    val rotatedOrientation = currentWidth == backupHeight && currentHeight == backupWidth

    return !sameOrientation && !rotatedOrientation
}
