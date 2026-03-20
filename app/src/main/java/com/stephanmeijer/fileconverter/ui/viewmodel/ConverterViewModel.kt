package com.stephanmeijer.fileconverter.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.stephanmeijer.fileconverter.engine.ConversionResult
import com.stephanmeijer.fileconverter.engine.SelectedFile
import kotlinx.coroutines.Job

sealed class ConversionState {
    data object Idle : ConversionState()
    data object Initializing : ConversionState()
    data class Converting(val progress: Float = -1f) : ConversionState()
    data class Success(val result: ConversionResult) : ConversionState()
    data class Error(val message: String) : ConversionState()
    data object Cancelled : ConversionState()
}

class ConverterViewModel : ViewModel() {
    var selectedFile by mutableStateOf<SelectedFile?>(null)
        private set
    var inputFormat by mutableStateOf<String?>(null)
        private set
    var outputFormat by mutableStateOf<String?>(null)
        private set
    var conversionState by mutableStateOf<ConversionState>(ConversionState.Idle)
        private set
    var engineReady by mutableStateOf(false)
        private set

    internal var conversionJob: Job? = null

    fun onFilePicked(file: SelectedFile) {
        selectedFile = file
        inputFormat = file.detectedFormat
        conversionState = ConversionState.Idle
    }
    fun onInputFormatChanged(format: String) { inputFormat = format }
    fun onOutputFormatChanged(format: String) { outputFormat = format }
    internal fun updateEngineReady(ready: Boolean) { engineReady = ready }
    internal fun updateConversionState(state: ConversionState) { conversionState = state }

    fun cancelConversion() {
        conversionJob?.cancel()
        conversionState = ConversionState.Cancelled
    }

    val canConvert: Boolean get() =
        selectedFile != null && inputFormat != null && outputFormat != null &&
        engineReady && conversionState !is ConversionState.Converting
}
