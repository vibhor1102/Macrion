package io.github.vibhor1102.macrion.feature.backup.data.base

internal enum class BackupArchiveFormat {
    MACRION_NATIVE,
    KLICKR_COMPATIBLE,
}

internal const val MACRION_MANIFEST_FILE_NAME = "macrion-manifest.macrion.json"
internal const val MACRION_FORMAT_NAME = "macrion"

internal fun detectBackupEntryFormat(fileName: String): BackupArchiveFormat? = when {
    fileName == MACRION_MANIFEST_FILE_NAME -> BackupArchiveFormat.MACRION_NATIVE
    fileName.matches(MACRION_SMART_SCENARIO_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.MACRION_NATIVE
    fileName.matches(MACRION_DUMB_SCENARIO_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.MACRION_NATIVE
    fileName.matches(MACRION_CONDITION_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.MACRION_NATIVE
    fileName.matches(SMART_SCENARIO_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.KLICKR_COMPATIBLE
    fileName.matches(DUMB_SCENARIO_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.KLICKR_COMPATIBLE
    fileName.matches(CONDITION_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.KLICKR_COMPATIBLE
    fileName.matches(LEGACY_CONDITION_BACKUP_MATCH_REGEX.toRegex()) -> BackupArchiveFormat.KLICKR_COMPATIBLE
    else -> null
}

internal fun mergeDetectedFormat(
    current: BackupArchiveFormat?,
    detected: BackupArchiveFormat?,
): BackupArchiveFormat? {
    if (current != null && detected != null && current != detected) {
        throw MalformedBackupArchiveException()
    }
    return detected ?: current
}

internal class MalformedBackupArchiveException : IllegalArgumentException()

internal fun String.withMacrionSubExtension(): String {
    val extensionIndex = lastIndexOf('.')
    return if (extensionIndex > 0) {
        substring(0, extensionIndex) + ".macrion" + substring(extensionIndex)
    } else {
        "$this.macrion"
    }
}

internal fun String.withoutMacrionSubExtension(): String =
    replace(Regex("\\.macrion(?=\\.[^./]+$)"), "")
