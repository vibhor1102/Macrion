/*
 * Copyright (C) 2023 Kevin Buzeau
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
package io.github.vibhor1102.macrion.feature.backup.data

import android.content.ContentResolver
import android.graphics.Point
import android.net.Uri
import android.util.Log

import io.github.vibhor1102.macrion.core.database.entity.CompleteScenario
import io.github.vibhor1102.macrion.core.dumb.data.database.DumbScenarioWithActions
import io.github.vibhor1102.macrion.feature.backup.data.dumb.DumbBackupDataSource
import io.github.vibhor1102.macrion.feature.backup.data.smart.SmartBackupDataSource
import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import io.github.vibhor1102.macrion.feature.backup.data.base.MACRION_MANIFEST_FILE_NAME
import io.github.vibhor1102.macrion.feature.backup.data.base.MACRION_FORMAT_NAME
import io.github.vibhor1102.macrion.feature.backup.data.base.detectBackupEntryFormat
import io.github.vibhor1102.macrion.feature.backup.data.base.mergeDetectedFormat
import io.github.vibhor1102.macrion.feature.backup.data.base.MalformedBackupArchiveException
import io.github.vibhor1102.macrion.core.base.extensions.getInt
import io.github.vibhor1102.macrion.core.base.extensions.getString

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

/** [BackupEngine] internal implementation. */
internal class BackupEngine(appDataDir: File, private val contentResolver: ContentResolver) {

    private val dumbBackupDataSource: DumbBackupDataSource = DumbBackupDataSource(appDataDir)
    private val smartBackupDataSource: SmartBackupDataSource = SmartBackupDataSource(appDataDir)

    /**
     * Creates a new backup file.
     *
     * @param zipFileUri the uri of the file to write the backup into. Must be retrieved using the DocumentProvider.
     * @param smartScenarios the scenarios to backup.
     * @param screenSize the size of this device screen.
     * @param progress the object notified about the backup progress.
     */
    suspend fun createBackup(
        zipFileUri: Uri,
        smartScenarios: List<CompleteScenario>,
        dumbScenarios: List<DumbScenarioWithActions>,
        screenSize: Point,
        format: BackupArchiveFormat,
        progress: BackupProgress,
    ) {
        Log.d(TAG, "Create backup: $zipFileUri for scenarios: $smartScenarios")
        dumbBackupDataSource.reset()
        smartBackupDataSource.reset()

        var currentProgress = 0
        progress.onProgressChanged(currentProgress, dumbScenarios.size + smartScenarios.size)

        // Create the zip file containing the scenarios and their events conditions.
        withContext(Dispatchers.IO) {
            try {
                var omittedComponentCount = 0
                ZipOutputStream(contentResolver.openOutputStream(zipFileUri, "wt")).use { zipStream ->
                    if (format == BackupArchiveFormat.MACRION_NATIVE) {
                        zipStream.putNextEntry(ZipEntry(MACRION_MANIFEST_FILE_NAME))
                        zipStream.write(
                            """{"format":"macrion","containerVersion":1,"databaseVersion":${io.github.vibhor1102.macrion.core.database.DATABASE_VERSION}}"""
                                .toByteArray(),
                        )
                    }

                    dumbScenarios.map { dumbScenario ->
                        if (format == BackupArchiveFormat.KLICKR_COMPATIBLE) {
                            KlickrCompatibilityProjector.projectDumbScenario(dumbScenario).also {
                                omittedComponentCount += it.omittedComponentCount
                            }.value
                        } else {
                            dumbScenario
                        }
                    }.forEach { dumbScenario ->
                        Log.d(TAG, "Backup dumb scenario ${dumbScenario.scenario.id}")

                        dumbBackupDataSource.addScenarioToZipFile(zipStream, dumbScenario, screenSize, format)

                        currentProgress++
                        progress.onProgressChanged(currentProgress, dumbScenarios.size + smartScenarios.size)
                    }

                    smartScenarios.map { completeScenario ->
                        if (format == BackupArchiveFormat.KLICKR_COMPATIBLE) {
                            KlickrCompatibilityProjector.projectSmartScenario(completeScenario).also {
                                omittedComponentCount += it.omittedComponentCount
                            }.value
                        } else {
                            completeScenario
                        }
                    }.forEach { completeScenario ->
                        Log.d(TAG, "Backup smart scenario ${completeScenario.scenario.id}")

                        smartBackupDataSource.addScenarioToZipFile(zipStream, completeScenario, screenSize, format)

                        currentProgress++
                        progress.onProgressChanged(currentProgress, dumbScenarios.size + smartScenarios.size)
                    }
                }

                progress.onCompleted(dumbScenarios, smartScenarios, 0, false, omittedComponentCount)
            } catch (ioEx: IOException) {
                Log.e(TAG, "Error while creating backup archive.")
                progress.onError(BackupError.GENERIC)
            } catch (isEx: IllegalStateException) {
                Log.e(TAG, "Error while creating backup archive, target folder can't be written")
                progress.onError(BackupError.GENERIC)
            } catch (secEx: SecurityException) {
                Log.e(TAG, "Error while creating backup archive, permission is denied")
                progress.onError(BackupError.GENERIC)
            }
        }
    }

    /**
     * Loads a backup file.
     *
     * @param zipFileUri the uri of the file to load the backup from. Must be retrieved using the DocumentProvider.
     * @param screenSize the size of this device screen.
     * @param progress the object notified about the backup import progress.
     */
    suspend fun loadBackup(zipFileUri: Uri, screenSize: Point, progress: BackupProgress) {
        Log.i(TAG, "Load backup: $zipFileUri")

        dumbBackupDataSource.reset()
        smartBackupDataSource.reset()

        var currentProgress = 0
        progress.onProgressChanged(currentProgress, null)

        withContext(Dispatchers.IO) {
            try {
                val archiveFormat = detectArchiveFormat(contentResolver, zipFileUri)
                ZipInputStream(contentResolver.openInputStream(zipFileUri)).use { zipStream ->
                    generateSequence { zipStream.nextEntry }
                        .forEach { zipEntry ->
                            if (zipEntry.isDirectory) return@forEach

                            val entryFormat = detectBackupEntryFormat(zipEntry.name)
                            if (zipEntry.name == MACRION_MANIFEST_FILE_NAME) return@forEach
                            if (entryFormat != archiveFormat) return@forEach

                            Log.d(TAG, "Extracting file ${zipEntry.name}")
                            when {
                                dumbBackupDataSource.extractFromZip(zipStream, zipEntry.name, archiveFormat) -> {
                                    Log.d(TAG, "Dumb scenario file ${zipEntry.name} extracted.")

                                    currentProgress++
                                    progress.onProgressChanged(currentProgress, null)
                                }

                                smartBackupDataSource.extractFromZip(zipStream, zipEntry.name, archiveFormat) -> {
                                    if (smartBackupDataSource.isScenarioBackupFileZipEntry(zipEntry.name, archiveFormat)) {
                                        Log.d(TAG, "Smart scenario file ${zipEntry.name} extracted")

                                        currentProgress++
                                        progress.onProgressChanged(currentProgress, null)
                                    }
                                }

                                else -> Log.w(TAG, "Nothing found to handle zip entry ${zipEntry.name}")
                            }
                        }
                }

                progress.onVerification?.invoke()
                dumbBackupDataSource.verifyExtractedScenarios(screenSize)
                smartBackupDataSource.verifyExtractedScenarios(screenSize)

                Log.i(TAG, "Backup loading completed: $zipFileUri")
                Log.i(TAG, "Inserting extracted scenarios into database")

                progress.onCompleted(
                    dumbBackupDataSource.validBackups,
                    smartBackupDataSource.validBackups,
                    dumbBackupDataSource.failureCount + smartBackupDataSource.failureCount,
                    smartBackupDataSource.screenCompatWarning,
                    0,
                )
            } catch (malformed: MalformedBackupArchiveException) {
                Log.e(TAG, "Backup archive is malformed or mixes formats", malformed)
                progress.onError(BackupError.MALFORMED_ARCHIVE)
            } catch (ioEx: IOException) {
                Log.e(TAG, "Error while loading backup archive", ioEx)
                progress.onError(BackupError.GENERIC)
            } catch (secEx: SecurityException) {
                Log.e(TAG, "Error while loading backup archive, permission is denied", secEx)
                progress.onError(BackupError.GENERIC)
            } catch (iaEx: IllegalArgumentException) {
                Log.e(TAG, "Error while loading backup archive, file is invalid", iaEx)
                progress.onError(BackupError.GENERIC)
            } catch (npEx: NullPointerException) {
                Log.e(TAG, "Error while loading backup archive, file path is null", npEx)
                progress.onError(BackupError.GENERIC)
            }
        }
    }
}

private fun detectArchiveFormat(
    contentResolver: ContentResolver,
    zipFileUri: Uri,
): BackupArchiveFormat = contentResolver.openInputStream(zipFileUri).use { inputStream ->
    inputStream ?: throw MalformedBackupArchiveException()
    detectArchiveFormat(inputStream)
}

internal fun detectArchiveFormat(inputStream: InputStream): BackupArchiveFormat {
    var archiveFormat: BackupArchiveFormat? = null
    var nativeManifestFound = false
    var scenarioEntryFound = false

    ZipInputStream(inputStream).use { zipStream ->
        generateSequence { zipStream.nextEntry }
            .forEach { zipEntry ->
                if (zipEntry.isDirectory) return@forEach

                val entryFormat = detectBackupEntryFormat(zipEntry.name)
                archiveFormat = mergeDetectedFormat(archiveFormat, entryFormat)
                if (entryFormat != null && zipEntry.name.endsWith(".json") &&
                    zipEntry.name != MACRION_MANIFEST_FILE_NAME) {
                    scenarioEntryFound = true
                    validateScenarioEntryFormat(zipStream, entryFormat)
                }

                if (zipEntry.name == MACRION_MANIFEST_FILE_NAME) {
                    validateNativeManifest(zipStream)
                    nativeManifestFound = true
                }
            }
    }

    val detectedFormat = archiveFormat ?: throw MalformedBackupArchiveException()
    if (!scenarioEntryFound || (
            detectedFormat == BackupArchiveFormat.MACRION_NATIVE && !nativeManifestFound
        )) {
        throw MalformedBackupArchiveException()
    }
    return detectedFormat
}

private fun validateScenarioEntryFormat(
    zipStream: ZipInputStream,
    format: BackupArchiveFormat,
) {
    val json = try {
        Json.parseToJsonElement(zipStream.readBytes().toString(Charsets.UTF_8)).jsonObject
    } catch (error: RuntimeException) {
        throw MalformedBackupArchiveException()
    }
    val declaredFormat = json.getString("format")
    if (
        (format == BackupArchiveFormat.MACRION_NATIVE && declaredFormat != MACRION_FORMAT_NAME) ||
        (format == BackupArchiveFormat.KLICKR_COMPATIBLE && declaredFormat == MACRION_FORMAT_NAME)
    ) {
        throw MalformedBackupArchiveException()
    }
}

private fun validateNativeManifest(zipStream: ZipInputStream) {
    val manifest = try {
        Json.parseToJsonElement(zipStream.readBytes().toString(Charsets.UTF_8)).jsonObject
    } catch (error: RuntimeException) {
        throw MalformedBackupArchiveException()
    }

    if (
        manifest.getString("format") != MACRION_FORMAT_NAME ||
        manifest.getInt("containerVersion") != 1 ||
        (manifest.getInt("databaseVersion") ?: -1) < 0
    ) {
        throw MalformedBackupArchiveException()
    }
}

/** Tag for logs. */
private const val TAG = "BackupEngine"
