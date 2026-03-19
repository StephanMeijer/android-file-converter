package com.stephanmeijer.fileconverter

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stephanmeijer.fileconverter.navigation.AboutScreen
import com.stephanmeijer.fileconverter.navigation.ConverterScreen
import com.stephanmeijer.fileconverter.ui.components.rememberFilePicker
import com.stephanmeijer.fileconverter.ui.screens.AboutScreenContent
import com.stephanmeijer.fileconverter.ui.screens.ConverterScreenContent
import com.stephanmeijer.fileconverter.ui.viewmodel.ConverterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileConverterApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ConverterScreen) {
        composable<ConverterScreen> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("File Converter") },
                        actions = {
                            IconButton(onClick = { navController.navigate(AboutScreen) }) {
                                Icon(Icons.Default.Info, contentDescription = "About")
                            }
                        }
                    )
                }
            ) { padding ->
                val converterViewModel: ConverterViewModel = viewModel()
                val pickFile = rememberFilePicker(onFilePicked = converterViewModel::onFilePicked)
                ConverterScreenContent(
                    viewModel = converterViewModel,
                    onPickFile = pickFile,
                    onConvert = { /* wired in Task 11 */ },
                    modifier = Modifier.padding(padding)
                )
            }
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
