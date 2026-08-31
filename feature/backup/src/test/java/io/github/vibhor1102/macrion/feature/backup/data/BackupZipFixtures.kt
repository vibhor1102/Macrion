package io.github.vibhor1102.macrion.feature.backup.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object BackupZipFixtures {

    val native: InputStream
        get() = zip(
            "macrion-manifest.macrion.json" to
                """{"format":"macrion","containerVersion":1,"databaseVersion":23}""",
            "12/12.macrion.json" to """{"format":"macrion"}""",
            "12/Condition-34.macrion.png" to "image contents",
            "dumb-56/56.macrion.json" to """{"format":"macrion"}""",
        )

    val klickrCompatible: InputStream
        get() = zip(
            "12/12.json" to "{}",
            "12/Condition-34.png" to "image contents",
            "dumb-56/56.json" to "{}",
        )

    val mixed: InputStream
        get() = zip(
            "macrion-manifest.macrion.json" to
                """{"format":"macrion","containerVersion":1,"databaseVersion":23}""",
            "12/12.macrion.json" to """{"format":"macrion"}""",
            "13/13.json" to "{}",
        )

    val nativeWithoutManifest: InputStream
        get() = zip(
            "12/12.macrion.json" to """{"format":"macrion"}""",
        )

    val nativeWithInvalidManifest: InputStream
        get() = zip(
            "macrion-manifest.macrion.json" to
                """{"format":"macrion","containerVersion":2,"databaseVersion":23}""",
            "12/12.macrion.json" to """{"format":"macrion"}""",
        )

    val nativeWithLegacyScenarioJson: InputStream
        get() = zip(
            "macrion-manifest.macrion.json" to
                """{"format":"macrion","containerVersion":1,"databaseVersion":23}""",
            "12/12.macrion.json" to "{}",
        )

    val manifestOnly: InputStream
        get() = zip(
            "macrion-manifest.macrion.json" to
                """{"format":"macrion","containerVersion":1,"databaseVersion":23}""",
        )

    val klickrFilenameWithNativeScenarioJson: InputStream
        get() = zip(
            "12/12.json" to """{"format":"macrion"}""",
        )

    val unrelated: InputStream
        get() = zip(
            "notes.txt" to "This is not a backup.",
        )

    private fun zip(vararg entries: Pair<String, String>): InputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, contents) ->
                zip.putNextEntry(ZipEntry(name).apply { time = 0 })
                zip.write(contents.toByteArray())
                zip.closeEntry()
            }
        }
        return ByteArrayInputStream(output.toByteArray())
    }
}
