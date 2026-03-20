package com.stephanmeijer.fileconverter.engine

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

internal fun processPickedFile(context: Context, uri: Uri): SelectedFile? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
        if (!it.moveToFirst()) return null
        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
        val displayName = if (nameIdx >= 0) it.getString(nameIdx) else "unknown"
        val size = if (sizeIdx >= 0) it.getLong(sizeIdx) else 0L
        // Sanitize to prevent path traversal attacks from share intents
        val sanitized = displayName.replace("/", "_").replace("\\", "_").replace("..", "_")
        val cacheFile = File(context.cacheDir, "input/$sanitized")
        cacheFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        }
        SelectedFile(uri, displayName, size, cacheFile, FormatDetector.detectFormat(displayName))
    }
}
