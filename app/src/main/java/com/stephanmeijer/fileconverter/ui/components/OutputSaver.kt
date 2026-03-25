package com.stephanmeijer.fileconverter.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

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

@Composable
fun rememberShareHandler(): (String, String, ByteArray) -> Unit {
    val context = LocalContext.current
    return remember {
        { suggestedName: String, mimeType: String, bytes: ByteArray ->
            shareFile(context, suggestedName, mimeType, bytes)
        }
    }
}

private fun shareFile(context: Context, fileName: String, mimeType: String, bytes: ByteArray) {
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val tempFile = File(shareDir, fileName)
    tempFile.writeBytes(bytes)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, null))
}
