package io.github.vibhor1102.macrion.feature.backup.data

import io.github.vibhor1102.macrion.feature.backup.data.base.BackupArchiveFormat
import io.github.vibhor1102.macrion.feature.backup.data.base.MalformedBackupArchiveException

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupArchiveDetectionTests {

    @Test
    fun nativeZipIsDetected() {
        assertEquals(
            BackupArchiveFormat.MACRION_NATIVE,
            detectArchiveFormat(BackupZipFixtures.native),
        )
    }

    @Test
    fun klickrCompatibleZipIsDetected() {
        assertEquals(
            BackupArchiveFormat.KLICKR_COMPATIBLE,
            detectArchiveFormat(BackupZipFixtures.klickrCompatible),
        )
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun mixedZipIsRejected() {
        detectArchiveFormat(BackupZipFixtures.mixed)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun nativeZipWithoutManifestIsRejected() {
        detectArchiveFormat(BackupZipFixtures.nativeWithoutManifest)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun nativeZipWithInvalidManifestIsRejected() {
        detectArchiveFormat(BackupZipFixtures.nativeWithInvalidManifest)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun nativeFilenameWithLegacyScenarioJsonIsRejected() {
        detectArchiveFormat(BackupZipFixtures.nativeWithLegacyScenarioJson)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun manifestWithoutScenariosIsRejected() {
        detectArchiveFormat(BackupZipFixtures.manifestOnly)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun klickrFilenameWithNativeScenarioJsonIsRejected() {
        detectArchiveFormat(BackupZipFixtures.klickrFilenameWithNativeScenarioJson)
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun unrelatedZipIsRejected() {
        detectArchiveFormat(BackupZipFixtures.unrelated)
    }
}
