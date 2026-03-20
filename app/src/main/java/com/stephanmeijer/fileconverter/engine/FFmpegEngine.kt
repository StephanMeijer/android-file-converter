package com.stephanmeijer.fileconverter.engine

import android.os.StatFs
import java.io.File

object FFmpegEngine {
    const val FFMPEG_VERSION = "7.1"

    @Volatile private var initialized = false
    @Volatile private var initError: String? = null

    private val audioExtensions = setOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus")

    fun initialize() {
        try {
            FFmpegBridge  // bare reference triggers object init { System.loadLibrary(...) }
            initialized = true
            initError = null
        } catch (e: UnsatisfiedLinkError) {
            initialized = false
            initError = "FFmpeg native library could not be loaded: ${e.message}"
        }
    }

    val isInitialized: Boolean get() = initialized
    val initializationError: String? get() = initError

    private fun hasEnoughStorage(outputDir: File): Boolean {
        return try {
            StatFs(outputDir.absolutePath).availableBytes > 100 * 1024 * 1024
        } catch (_: Exception) { true }
    }

    private fun isAudioOutputFormat(outputPath: String): Boolean =
        outputPath.substringAfterLast('.').lowercase() in audioExtensions

    suspend fun convert(
        inputPath: String,
        outputPath: String,
        preset: ConversionPreset,
        onProgress: (Float) -> Unit = {}
    ): MediaConversionResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        if (!initialized) {
            return@withContext MediaConversionResult(
                outputFile = File(outputPath),
                error = initError ?: "FFmpeg engine not initialized"
            )
        }

        val outputFile = File(outputPath)

        val totalDurationMs = try {
            FFmpegBridge.nativeGetMediaDuration(inputPath).takeIf { it > 0 } ?: -1L
        } catch (_: Exception) { -1L }

        if (isAudioOutputFormat(outputPath)) {
            val audioStreamCount = try { FFmpegBridge.nativeProbeStreams(inputPath) } catch (_: Exception) { -1 }
            if (audioStreamCount == 0) {
                return@withContext MediaConversionResult(
                    outputFile = File(outputPath),
                    error = "This video has no audio track to extract."
                )
            }
        }

        val outputDir = File(outputPath).parentFile ?: File(outputPath).absoluteFile.parentFile
        if (outputDir != null && !hasEnoughStorage(outputDir)) {
            return@withContext MediaConversionResult(
                outputFile = File(outputPath),
                error = "Low storage space — conversion may fail."
            )
        }

        FFmpegBridge.progressCallback = { timeMs ->
            if (totalDurationMs > 0 && timeMs >= 0) {
                val progress = (timeMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                onProgress(progress)
            }
        }

        try {
            val command = FFmpegCommandBuilder.buildCommand(inputPath, outputPath, preset)
            val exitCode = FFmpegBridge.nativeExecute(command)

            FFmpegBridge.progressCallback = null

            if (exitCode == 0 && outputFile.exists() && outputFile.length() > 0) {
                MediaConversionResult(outputFile = outputFile, error = null)
            } else {
                outputFile.delete()
                MediaConversionResult(
                    outputFile = outputFile,
                    error = if (exitCode != 0) FFmpegErrorParser.parse("FFmpeg exited with code $exitCode") else "Output file is empty"
                )
            }
        } catch (e: Exception) {
            FFmpegBridge.progressCallback = null
            outputFile.delete()
            MediaConversionResult(outputFile = outputFile, error = e.message ?: "Unknown error")
        }
    }

    fun cancel() {
        FFmpegBridge.nativeCancel()
    }

    suspend fun getMediaDuration(path: String): Long =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            FFmpegBridge.nativeGetMediaDuration(path)
        }
}
