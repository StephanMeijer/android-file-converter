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
import com.stephanmeijer.fileconverter.engine.PandocEngine
import com.stephanmeijer.fileconverter.navigation.AboutScreen
import com.stephanmeijer.fileconverter.navigation.ConverterScreen
import com.stephanmeijer.fileconverter.ui.components.MimeTypes
import com.stephanmeijer.fileconverter.ui.components.rememberFilePicker
import com.stephanmeijer.fileconverter.ui.components.rememberOutputSaver
import com.stephanmeijer.fileconverter.ui.screens.AboutScreenContent
import com.stephanmeijer.fileconverter.ui.screens.ConverterScreenContent
import com.stephanmeijer.fileconverter.ui.viewmodel.ConversionState
import com.stephanmeijer.fileconverter.ui.viewmodel.ConverterViewModel
import kotlinx.coroutines.launch

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
            converterViewModel.updateEngineReady(true)
            converterViewModel.updateConversionState(ConversionState.Idle)
        } catch (e: Exception) {
            converterViewModel.updateConversionState(
                ConversionState.Error("Engine init failed: ${e.message}")
            )
        } finally {
            ConversionForegroundService.stop(context)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = ConverterScreen) {
            composable<ConverterScreen> {
            val pickFile = rememberFilePicker(onFilePicked = converterViewModel::onFilePicked)
            val saveOutput = rememberOutputSaver { _ -> }
            ConverterScreenContent(
                viewModel = converterViewModel,
                onPickFile = pickFile,
                onNavigateToAbout = { navController.navigate(AboutScreen) },
                onConvert = {
                    scope.launch {
                        converterViewModel.updateConversionState(ConversionState.Converting())
                        ConversionForegroundService.start(context)
                        try {
                             val file = converterViewModel.selectedFile!!
                             val inputBytes = file.cachedPath.readBytes()
                             val result = PandocEngine.convert(
                                 inputBytes = inputBytes,
                                fromFormat = converterViewModel.inputFormat!!,
                                toFormat = converterViewModel.outputFormat!!,
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
                        } catch (e: Exception) {
                            converterViewModel.updateConversionState(
                                ConversionState.Error("Conversion failed: ${e.message}")
                            )
                        } finally {
                            ConversionForegroundService.stop(context)
                        }
                    }
                },
                onSave = {
                    val state = converterViewModel.conversionState
                    if (state is ConversionState.Success) {
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
