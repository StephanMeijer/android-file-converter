package com.stephanmeijer.fileconverter.engine

data class SelectedFile(
    val uri: android.net.Uri,
    val displayName: String,
    val size: Long,
    val cachedPath: java.io.File,
    val detectedFormat: String?
)