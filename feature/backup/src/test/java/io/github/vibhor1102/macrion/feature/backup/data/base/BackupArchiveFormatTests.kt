package io.github.vibhor1102.macrion.feature.backup.data.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupArchiveFormatTests {

    @Test
    fun nativeEntriesAreDetected() {
        assertEquals(
            BackupArchiveFormat.MACRION_NATIVE,
            detectBackupEntryFormat("12/12.macrion.json"),
        )
        assertEquals(
            BackupArchiveFormat.MACRION_NATIVE,
            detectBackupEntryFormat("12/Condition-34.macrion.png"),
        )
        assertEquals(
            BackupArchiveFormat.MACRION_NATIVE,
            detectBackupEntryFormat("dumb-56/56.macrion.json"),
        )
    }

    @Test
    fun klickrEntriesAreDetected() {
        assertEquals(
            BackupArchiveFormat.KLICKR_COMPATIBLE,
            detectBackupEntryFormat("12/12.json"),
        )
        assertEquals(
            BackupArchiveFormat.KLICKR_COMPATIBLE,
            detectBackupEntryFormat("12/Condition-34.png"),
        )
    }

    @Test
    fun unrelatedEntriesAreIgnored() {
        assertNull(detectBackupEntryFormat("notes.txt"))
    }

    @Test(expected = MalformedBackupArchiveException::class)
    fun mixedFormatsAreRejected() {
        mergeDetectedFormat(
            BackupArchiveFormat.MACRION_NATIVE,
            BackupArchiveFormat.KLICKR_COMPATIBLE,
        )
    }

    @Test
    fun macrionSubExtensionRoundTrips() {
        assertEquals(
            "Condition-34.png",
            "Condition-34.png".withMacrionSubExtension().withoutMacrionSubExtension(),
        )
    }
}
