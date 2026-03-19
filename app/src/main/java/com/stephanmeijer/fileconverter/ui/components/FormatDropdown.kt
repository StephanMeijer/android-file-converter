package com.stephanmeijer.fileconverter.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatDropdown(
    label: String,
    selectedFormat: String?,
    formats: List<String>,
    formatDisplayName: (String) -> String,
    onFormatSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDetected: Boolean = false,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (selectedFormat != null) {
                val name = formatDisplayName(selectedFormat)
                if (isDetected) "$name (detected)" else name
            } else {
                ""
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select format...") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            formats.forEach { format ->
                DropdownMenuItem(
                    text = { Text(formatDisplayName(format)) },
                    onClick = {
                        onFormatSelected(format)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
