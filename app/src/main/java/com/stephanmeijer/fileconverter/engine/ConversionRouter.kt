package com.stephanmeijer.fileconverter.engine

object ConversionRouter {
    enum class EngineType { PANDOC, FFMPEG }

    fun getEngine(inputFormat: String): EngineType =
        when (FormatDetector.categoryOf(inputFormat)) {
            FormatDetector.Category.AUDIO, FormatDetector.Category.VIDEO -> EngineType.FFMPEG
            FormatDetector.Category.DOCUMENT -> EngineType.PANDOC
            null -> EngineType.PANDOC
        }

    fun validateConversion(inputFormat: String, outputFormat: String): Boolean {
        val inputCategory = FormatDetector.categoryOf(inputFormat) ?: return false
        val validOutputs = FormatDetector.validOutputFormats(inputCategory)
        return outputFormat in validOutputs
    }
}
