package com.stephanmeijer.fileconverter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stephanmeijer.fileconverter.engine.ConversionPreset
import com.stephanmeijer.fileconverter.engine.FormatDetector
import com.stephanmeijer.fileconverter.engine.SelectedFile
import com.stephanmeijer.fileconverter.ui.components.AdvancedOptions
import com.stephanmeijer.fileconverter.ui.components.FormatDropdown
import com.stephanmeijer.fileconverter.ui.viewmodel.ConversionState
import com.stephanmeijer.fileconverter.ui.viewmodel.ConverterViewModel

@Composable
fun ConverterScreenContent(
    viewModel: ConverterViewModel,
    onPickFile: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onConvert: () -> Unit,
    onSave: () -> Unit = {},
    onPresetChanged: (ConversionPreset) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Top row: title + info icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "File Converter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            IconButton(onClick = onNavigateToAbout) {
                Icon(Icons.Default.Info, contentDescription = "About")
            }
        }

        FileSelectionCard(viewModel.selectedFile, onPickFile)
        FormatSelectionBar(
            viewModel.inputFormat, viewModel.outputFormat,
            viewModel.selectedFile?.detectedFormat != null && viewModel.inputFormat == viewModel.selectedFile?.detectedFormat,
            viewModel.detectedCategory,
            viewModel::onInputFormatChanged, viewModel::onOutputFormatChanged
        )
        AdvancedOptions(
            category = viewModel.detectedCategory,
            selectedPreset = viewModel.selectedPreset,
            onPresetChanged = onPresetChanged,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onConvert, enabled = viewModel.canConvert,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.SwapHoriz, null, Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Convert", style = MaterialTheme.typography.titleMedium)
        }
        when (val state = viewModel.conversionState) {
            is ConversionState.Initializing -> {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Initializing converter...")
            }
            is ConversionState.Converting -> {
                val progress = state.progress
                if (progress < 0f) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Converting...")
                OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
            is ConversionState.Error -> {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                    Text(state.message, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            is ConversionState.Success -> {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Conversion complete!", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium)
                        if (state.result.warnings.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("Warnings: ${state.result.warnings.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            FilledTonalButton(onClick = onSave) {
                                Icon(Icons.Default.Save, contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
            is ConversionState.MediaSuccess -> {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Conversion complete!", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            FilledTonalButton(onClick = onSave) {
                                Icon(Icons.Default.Save, contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
            is ConversionState.Idle -> {}
            is ConversionState.Cancelled -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Conversion cancelled.", Modifier.padding(16.dp))
                }
            }
        }
        }
    }
}

@Composable
private fun FileSelectionCard(selectedFile: SelectedFile?, onPickFile: () -> Unit) {
    if (selectedFile == null) {
        OutlinedCard(Modifier.fillMaxWidth().height(120.dp).clickable { onPickFile() }, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.UploadFile, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Select a file to convert", style = MaterialTheme.typography.titleMedium)
                Text("Tap to browse", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.InsertDriveFile, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(selectedFile.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatFileSize(selectedFile.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    selectedFile.detectedFormat?.let {
                        AssistChip(onClick = {}, label = { Text(FormatDetector.getDisplayName(it)) }, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                TextButton(onClick = onPickFile) { Text("Change") }
            }
        }
    }
}

@Composable
private fun FormatSelectionBar(
    inputFormat: String?, outputFormat: String?, isInputDetected: Boolean,
    detectedCategory: FormatDetector.Category?,
    onInputChanged: (String) -> Unit, onOutputChanged: (String) -> Unit
) {
    val outputFormats = if (detectedCategory != null) FormatDetector.validOutputFormats(detectedCategory)
                        else FormatDetector.commonOutputFormats
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FormatDropdown("From", inputFormat, FormatDetector.commonInputFormats, FormatDetector::getDisplayName, onInputChanged, Modifier.fillMaxWidth(), isDetected = isInputDetected)
        Icon(Icons.Default.ArrowDownward, "to", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        FormatDropdown("To", outputFormat, outputFormats, FormatDetector::getDisplayName, onOutputChanged, Modifier.fillMaxWidth())
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
}
