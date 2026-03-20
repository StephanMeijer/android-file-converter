package com.stephanmeijer.fileconverter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stephanmeijer.fileconverter.engine.FormatDetector
import com.stephanmeijer.fileconverter.engine.SelectedFile
import com.stephanmeijer.fileconverter.engine.processPickedFile
import com.stephanmeijer.fileconverter.ui.theme.FileConverterTheme

class MainActivity : ComponentActivity() {

    private var initialFile by mutableStateOf<SelectedFile?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            processIncomingIntent(intent)
        }
        setContent {
            FileConverterTheme {
                FileConverterApp(initialFile = initialFile)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processIncomingIntent(intent)
    }

    private fun processIncomingIntent(intent: Intent) {
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            Intent.ACTION_VIEW -> intent.data
            else -> return
        }
        uri ?: return
        val picked = processPickedFile(this, uri) ?: return
        val withFormat = if (picked.detectedFormat == null) {
            val mime = intent.type ?: ""
            val mimeFormat = FormatDetector.detectFormatFromMime(mime)
            if (mimeFormat != null) picked.copy(detectedFormat = mimeFormat) else picked
        } else {
            picked
        }
        initialFile = withFormat
    }
}
