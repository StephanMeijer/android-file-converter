package com.stephanmeijer.fileconverter.engine

object FFmpegErrorParser {
    private val patterns = listOf(
        "Invalid data found when processing input" to "This file appears to be corrupted or in an unsupported format.",
        "No such file or directory" to "The selected file could not be found.",
        "Permission denied" to "Cannot access this file. Permission denied.",
    )

    fun parse(stderr: String): String {
        for ((pattern, message) in patterns) {
            if (stderr.contains(pattern, ignoreCase = true)) return message
        }
        return stderr
    }
}
