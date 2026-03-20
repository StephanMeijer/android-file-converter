package com.stephanmeijer.fileconverter.engine

import java.io.File

data class MediaConversionResult(
    val outputFile: File,
    val error: String?
)
