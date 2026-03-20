package com.stephanmeijer.fileconverter.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stephanmeijer.fileconverter.engine.SelectedFile
import com.stephanmeijer.fileconverter.engine.processPickedFile

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

