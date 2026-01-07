package org.kuppihub.app.screens.addkuppi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LinkSection(
    title: String,
    icon: ImageVector,
    color: Color,
    links: List<String>, // 👈 Changed from List<LinkUiItem> to List<String>
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, String) -> Unit
) {
    Column {
        // Header with Icon and Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of Input Fields
        links.forEachIndexed { index, url ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { onUpdate(index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Paste link here") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                IconButton(onClick = { onRemove(index) }) {
                    Icon(Icons.Default.Delete, "Remove")
                }
            }
        }

        // Add Button
        TextButton(onClick = onAdd) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add another link")
        }
    }
}