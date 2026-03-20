package com.stephanmeijer.fileconverter.engine

enum class Level { LOW, MEDIUM, HIGH }
enum class Resolution { P720, P1080, ORIGINAL }

sealed class ConversionPreset {
    data class AudioQuality(val level: Level) : ConversionPreset()
    data class VideoResolution(val res: Resolution) : ConversionPreset()
}

object FFmpegCommandBuilder {

    private val audioExtensions = setOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus")

    private fun extension(path: String): String =
        path.substringAfterLast('.').lowercase()

    private fun codecFlags(ext: String): List<String> = when (ext) {
        "mp4", "mkv", "mov" -> listOf("-c:v", "libx264", "-c:a", "aac")
        "webm"              -> listOf("-c:v", "libvpx-vp9", "-c:a", "libopus")
        "mp3"               -> listOf("-c:a", "libmp3lame")
        "aac"               -> listOf("-c:a", "aac")
        "m4a"               -> listOf("-c:a", "aac")
        "wav"               -> listOf("-c:a", "pcm_s16le")
        "flac"              -> listOf("-c:a", "flac")
        "ogg"               -> listOf("-c:a", "libvorbis")
        "opus"              -> listOf("-c:a", "libopus")
        else                -> emptyList()
    }

    private fun bitrateFlag(level: Level): List<String> = when (level) {
        Level.LOW    -> listOf("-b:a", "96k")
        Level.MEDIUM -> listOf("-b:a", "192k")
        Level.HIGH   -> listOf("-b:a", "320k")
    }

    private fun scaleFlag(res: Resolution): List<String> = when (res) {
        Resolution.P720     -> listOf("-vf", "scale=-2:720")
        Resolution.P1080    -> listOf("-vf", "scale=-2:1080")
        Resolution.ORIGINAL -> emptyList()
    }

    fun buildCommand(
        inputPath: String,
        outputPath: String,
        preset: ConversionPreset
    ): Array<String> {
        val outExt = extension(outputPath)
        val args = mutableListOf("-y", "-i", inputPath)

        when (preset) {
            is ConversionPreset.AudioQuality -> {
                args += codecFlags(outExt)
                args += bitrateFlag(preset.level)
                if (outExt in audioExtensions) {
                    args += "-vn"
                }
            }
            is ConversionPreset.VideoResolution -> {
                args += codecFlags(outExt)
                args += scaleFlag(preset.res)
            }
        }

        args += outputPath
        return args.toTypedArray()
    }
}
