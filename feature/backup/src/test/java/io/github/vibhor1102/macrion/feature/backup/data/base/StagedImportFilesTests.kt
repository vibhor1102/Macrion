package io.github.vibhor1102.macrion.feature.backup.data.base

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.io.IOException

class StagedImportFilesTests {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun onlyReferencedFilesAreCommitted() {
        val staging = temporaryFolder.newFolder("staging")
        val destination = temporaryFolder.newFolder("destination")
        staging.resolve("used.png").writeBytes(byteArrayOf(1, 2, 3))
        staging.resolve("unused.png").writeBytes(byteArrayOf(4, 5, 6))

        commitStagedFiles(staging, destination, listOf("used.png"))

        assertArrayEquals(byteArrayOf(1, 2, 3), destination.resolve("used.png").readBytes())
        assertFalse(destination.resolve("unused.png").exists())
    }

    @Test
    fun identicalExistingFileIsAccepted() {
        val staging = temporaryFolder.newFolder("staging")
        val destination = temporaryFolder.newFolder("destination")
        staging.resolve("shared.png").writeBytes(byteArrayOf(1, 2, 3))
        destination.resolve("shared.png").writeBytes(byteArrayOf(1, 2, 3))

        commitStagedFiles(staging, destination, listOf("shared.png"))

        assertTrue(destination.resolve("shared.png").exists())
    }

    @Test(expected = IOException::class)
    fun conflictingExistingFileRejectsAndRollsBackNewFiles() {
        val staging = temporaryFolder.newFolder("staging")
        val destination = temporaryFolder.newFolder("destination")
        staging.resolve("new.png").writeBytes(byteArrayOf(1))
        staging.resolve("conflict.png").writeBytes(byteArrayOf(2))
        destination.resolve("conflict.png").writeBytes(byteArrayOf(3))

        try {
            commitStagedFiles(staging, destination, listOf("new.png", "conflict.png"))
        } finally {
            assertFalse(destination.resolve("new.png").exists())
            assertArrayEquals(byteArrayOf(3), destination.resolve("conflict.png").readBytes())
        }
    }

    @Test(expected = IOException::class)
    fun pathOutsideStagingDirectoryIsRejected() {
        val staging = temporaryFolder.newFolder("staging")
        val destination = temporaryFolder.newFolder("destination")

        commitStagedFiles(staging, destination, listOf("../outside.png"))
    }
}
