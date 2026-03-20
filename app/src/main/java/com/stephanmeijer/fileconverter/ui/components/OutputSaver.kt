package com.stephanmeijer.fileconverter.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberOutputSaver(
    onSaved: (Boolean) -> Unit
): (String, String, ByteArray) -> Unit {
    val context = LocalContext.current
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        if (uri != null && pendingBytes != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(pendingBytes!!)
                }
                onSaved(true)
            } catch (_: Exception) {
                onSaved(false)
            }
        }
        pendingBytes = null
    }

    return remember(launcher) {
        { suggestedName: String, _: String, bytes: ByteArray ->
            pendingBytes = bytes
            launcher.launch(suggestedName)
        }
    }
}

object MimeTypes {
    fun forFormat(format: String): String = when (format) {
        "html" -> "text/html"
        "markdown", "gfm", "commonmark" -> "text/markdown"
        "latex" -> "application/x-latex"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "odt" -> "application/vnd.oasis.opendocument.text"
        "epub" -> "application/epub+zip"
        "rst" -> "text/x-rst"
        "json" -> "application/json"
        "plain" -> "text/plain"
        "rtf" -> "application/rtf"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "mp3" -> "audio/mpeg"
        "aac" -> "audio/aac"
        "m4a" -> "audio/mp4"
        "wav" -> "audio/wav"
        "flac" -> "audio/flac"
        "ogg" -> "audio/ogg"
        "opus" -> "audio/opus"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "mov" -> "video/quicktime"
        "webm" -> "video/webm"
        else -> "application/octet-stream"
    }

    fun extensionForFormat(format: String): String = when (format) {
        "html" -> "html"
        "markdown", "gfm", "commonmark" -> "md"
        "latex" -> "tex"
        "docx" -> "docx"
        "odt" -> "odt"
        "epub" -> "epub"
        "rst" -> "rst"
        "json" -> "json"
        "plain" -> "txt"
        "rtf" -> "rtf"
        "pptx" -> "pptx"
        "typst" -> "typ"
        "org" -> "org"
        "mp3" -> "mp3"
        "aac" -> "aac"
        "m4a" -> "m4a"
        "wav" -> "wav"
        "flac" -> "flac"
        "ogg" -> "ogg"
        "opus" -> "opus"
        "mp4" -> "mp4"
        "mkv" -> "mkv"
        "mov" -> "mov"
        "webm" -> "webm"
        else -> format
    }
}
