package com.stephanmeijer.fileconverter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stephanmeijer.fileconverter.engine.ConversionForegroundService
import com.stephanmeijer.fileconverter.engine.ConversionPreset
import com.stephanmeijer.fileconverter.engine.FFmpegEngine
import com.stephanmeijer.fileconverter.engine.FormatDetector
import com.stephanmeijer.fileconverter.engine.Level
import com.stephanmeijer.fileconverter.engine.PandocEngine
import com.stephanmeijer.fileconverter.navigation.AboutScreen
import com.stephanmeijer.fileconverter.navigation.ConverterScreen
import com.stephanmeijer.fileconverter.ui.components.MimeTypes
import com.stephanmeijer.fileconverter.ui.components.rememberFilePicker
import com.stephanmeijer.fileconverter.ui.components.rememberMediaOutputSaver
import com.stephanmeijer.fileconverter.ui.components.rememberOutputSaver
import com.stephanmeijer.fileconverter.ui.screens.AboutScreenContent
import com.stephanmeijer.fileconverter.ui.screens.ConverterScreenContent
import com.stephanmeijer.fileconverter.ui.viewmodel.ConversionState
import com.stephanmeijer.fileconverter.ui.viewmodel.ConverterViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileConverterApp() {
    val navController = rememberNavController()
    val converterViewModel: ConverterViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        converterViewModel.updateConversionState(ConversionState.Initializing)
        ConversionForegroundService.start(context)
        try {
            PandocEngine.initialize(context.applicationContext)
            FFmpegEngine.initialize()
            converterViewModel.updateEngineReady(true)
            converterViewModel.updateConversionState(ConversionState.Idle)
            try {
                File(context.cacheDir, "output").listFiles()?.forEach { it.delete() }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            converterViewModel.updateConversionState(
                ConversionState.Error("Engine init failed: ${e.message}")
            )
        } finally {
            ConversionForegroundService.stop(context)
        }
        ConversionForegroundService.onCancelRequested = { converterViewModel.cancelConversion() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = ConverterScreen) {
            composable<ConverterScreen> {
                val pickFile = rememberFilePicker(onFilePicked = converterViewModel::onFilePicked)
                val saveOutput = rememberOutputSaver { _ -> }
                val saveMediaOutput = rememberMediaOutputSaver { _ -> }
                ConverterScreenContent(
                    viewModel = converterViewModel,
                    onPickFile = pickFile,
                    onNavigateToAbout = { navController.navigate(AboutScreen) },
                    onConvert = {
                        val job = scope.launch {
                            converterViewModel.updateConversionState(ConversionState.Converting())
                            ConversionForegroundService.start(context)
                            try {
                                val file = converterViewModel.selectedFile!!
                                val inputFormat = converterViewModel.inputFormat!!
                                val outputFormat = converterViewModel.outputFormat!!
                                val category = FormatDetector.categoryOf(inputFormat)

                                if (category == FormatDetector.Category.DOCUMENT) {
                                    val inputBytes = file.cachedPath.readBytes()
                                    val result = PandocEngine.convert(
                                        inputBytes = inputBytes,
                                        fromFormat = inputFormat,
                                        toFormat = outputFormat,
                                    )
                                    if (result.error.isNullOrBlank()) {
                                        converterViewModel.updateConversionState(
                                            ConversionState.Success(result)
                                        )
                                    } else {
                                        converterViewModel.updateConversionState(
                                            ConversionState.Error(result.error)
                                        )
                                    }
                                } else {
                                    val pfd = context.contentResolver.openFileDescriptor(file.uri, "r")
                                        ?: throw IllegalStateException("Cannot open file descriptor")
                                    val inputPath = "/proc/self/fd/${pfd.fd}"
                                    val outputDir = File(context.cacheDir, "output").also { it.mkdirs() }
                                    val outputExt = MimeTypes.extensionForFormat(outputFormat)
                                    val outputPath = File(
                                        outputDir,
                                        "${file.displayName.substringBeforeLast('.')}.$outputExt"
                                    ).absolutePath
                                    val preset = converterViewModel.selectedPreset
                                        ?: ConversionPreset.AudioQuality(Level.MEDIUM)

                                    val result = FFmpegEngine.convert(
                                        inputPath = inputPath,
                                        outputPath = outputPath,
                                        preset = preset,
                                        onProgress = { progress ->
                                            converterViewModel.updateConversionState(
                                                ConversionState.Converting(progress)
                                            )
                                            ConversionForegroundService.update(context, progress)
                                        }
                                    )
                                    pfd.close()

                                    if (result.error == null) {
                                        converterViewModel.updateConversionState(
                                            ConversionState.MediaSuccess(result)
                                        )
                                    } else {
                                        result.outputFile.delete()
                                        converterViewModel.updateConversionState(
                                            ConversionState.Error(result.error)
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                converterViewModel.updateConversionState(
                                    ConversionState.Error("Conversion failed: ${e.message}")
                                )
                            } finally {
                                ConversionForegroundService.stop(context)
                            }
                        }
                        converterViewModel.conversionJob = job
                    },
                    onSave = {
                        when (val state = converterViewModel.conversionState) {
                            is ConversionState.Success -> {
                                val inputName = converterViewModel.selectedFile!!
                                    .displayName.substringBeforeLast('.')
                                val ext = MimeTypes.extensionForFormat(
                                    converterViewModel.outputFormat!!
                                )
                                val mime = MimeTypes.forFormat(
                                    converterViewModel.outputFormat!!
                                )
                                saveOutput("$inputName.$ext", mime, state.result.outputBytes)
                            }
                            is ConversionState.MediaSuccess -> {
                                val inputName = converterViewModel.selectedFile!!
                                    .displayName.substringBeforeLast('.')
                                val ext = MimeTypes.extensionForFormat(
                                    converterViewModel.outputFormat!!
                                )
                                val mime = MimeTypes.forFormat(
                                    converterViewModel.outputFormat!!
                                )
                                saveMediaOutput("$inputName.$ext", mime, state.result.outputFile)
                            }
                            else -> {}
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable<AboutScreen> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("About") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                AboutScreenContent(modifier = Modifier.padding(padding))
            }
        }
        }
    }
}

