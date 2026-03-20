package com.stephanmeijer.fileconverter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stephanmeijer.fileconverter.engine.*

@Composable
fun AdvancedOptions(
    category: FormatDetector.Category?,
    selectedPreset: ConversionPreset?,
    onPresetChanged: (ConversionPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Advanced options", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                when (category) {
                    FormatDetector.Category.AUDIO -> AudioQualityOptions(
                        selected = (selectedPreset as? ConversionPreset.AudioQuality)?.level ?: Level.MEDIUM,
                        onSelected = { onPresetChanged(ConversionPreset.AudioQuality(it)) }
                    )
                    FormatDetector.Category.VIDEO -> VideoResolutionOptions(
                        selected = (selectedPreset as? ConversionPreset.VideoResolution)?.res ?: Resolution.ORIGINAL,
                        onSelected = { onPresetChanged(ConversionPreset.VideoResolution(it)) }
                    )
                    else -> Text("No additional options available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun AudioQualityOptions(selected: Level, onSelected: (Level) -> Unit) {
    Column {
        Text("Audio quality", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Level.entries.forEach { level ->
                FilterChip(selected = selected == level, onClick = { onSelected(level) },
                    label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) })
            }
        }
    }
}

@Composable
private fun VideoResolutionOptions(selected: Resolution, onSelected: (Resolution) -> Unit) {
    Column {
        Text("Video resolution", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Resolution.entries.forEach { res ->
                FilterChip(selected = selected == res, onClick = { onSelected(res) },
                    label = { Text(when (res) {
                        Resolution.P720 -> "720p"
                        Resolution.P1080 -> "1080p"
                        Resolution.ORIGINAL -> "Original"
                    }) })
            }
        }
    }
}
