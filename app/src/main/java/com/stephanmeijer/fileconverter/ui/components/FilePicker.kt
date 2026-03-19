package com.stephanmeijer.fileconverter.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stephanmeijer.fileconverter.engine.FormatDetector
import com.stephanmeijer.fileconverter.engine.SelectedFile
import java.io.File

@Composable
fun rememberFilePicker(onFilePicked: (SelectedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            processPickedFile(context, it)?.let(onFilePicked)
        }
    }
    return remember(launcher) { { launcher.launch(arrayOf("*/*")) } }
}

private fun processPickedFile(context: Context, uri: Uri): SelectedFile? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
        if (!it.moveToFirst()) return null
        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
        val displayName = if (nameIdx >= 0) it.getString(nameIdx) else "unknown"
        val size = if (sizeIdx >= 0) it.getLong(sizeIdx) else 0L
        val cacheFile = File(context.cacheDir, "input/$displayName")
        cacheFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        }
        SelectedFile(uri, displayName, size, cacheFile, FormatDetector.detectFormat(displayName))
    }
}