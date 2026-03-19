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
): (String, String, String) -> Unit {
    val context = LocalContext.current
    var pendingContent by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        if (uri != null && pendingContent != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(pendingContent!!.toByteArray())
                }
                onSaved(true)
            } catch (_: Exception) {
                onSaved(false)
            }
        }
        pendingContent = null
    }

    return remember(launcher) {
        { suggestedName: String, _: String, content: String ->
            pendingContent = content
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
        else -> format
    }
}
