package org.kuppihub.app.screens.addkuppi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.kuppihub.app.viewmodel.AddKuppiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKuppiScreen(
    moduleId: Int,
    userId: String,
    onBackClick: () -> Unit
) {
    val viewModel = remember { AddKuppiViewModel() }
    val scrollState = rememberScrollState()

    // Helper state for manual entry if ID is -1
    var inputModuleId by remember { mutableStateOf(if (moduleId != -1) moduleId.toString() else "") }

    // ViewModel State
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val youtubeLinks by viewModel.youtubeLinks.collectAsState()
    val telegramLinks by viewModel.telegramLinks.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState() // Make sure this exists in ViewModel
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val success by viewModel.submissionSuccess.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    LaunchedEffect(success) {
        if (success == true) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Kuppi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Adds nice spacing between all items
        ) {
            // 1. Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("How to upload:", style = MaterialTheme.typography.labelLarge)
                        Text("1. Upload video to YouTube or Telegram.")
                        Text("2. Copy the link and paste it below.")
                    }
                }
            }

            // 2. Module ID Field
            if (moduleId == -1) {
                OutlinedTextField(
                    value = inputModuleId,
                    onValueChange = { if (it.all { char -> char.isDigit() }) inputModuleId = it },
                    label = { Text("Module ID (Number)") },
                    placeholder = { Text("e.g. 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Main Text Fields
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description.value = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // 4. Language Selector (Fixes 'Invalid Code' error)
            Column {
                Text("Language", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val languages = listOf("en" to "English", "si" to "Sinhala", "ta" to "Tamil")
                    languages.forEach { (code, label) ->
                        FilterChip(
                            selected = languageCode == code,
                            onClick = { viewModel.languageCode.value = code },
                            label = { Text(label) },
                            leadingIcon = if (languageCode == code) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                    }
                }
            }

            Divider()

            // 5. YouTube Section (Using External LinkSection)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection(
                        title = "YouTube Links",
                        icon = Icons.Default.SmartDisplay, // 🔴 Red Icon
                        color = Color(0xFFFF0000),
                        links = youtubeLinks,
                        onAdd = { viewModel.addLink(viewModel.youtubeLinks) },
                        onRemove = { idx -> viewModel.removeLink(viewModel.youtubeLinks, idx) },
                        onUpdate = { idx, txt -> viewModel.updateLink(viewModel.youtubeLinks, idx, txt) }
                    )
                }
            }

            // 6. Telegram Section (Using External LinkSection)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection(
                        title = "Telegram Links",
                        icon = Icons.Default.Send, // 🔵 Blue Icon
                        color = Color(0xFF0088CC),
                        links = telegramLinks,
                        onAdd = { viewModel.addLink(viewModel.telegramLinks) },
                        onRemove = { idx -> viewModel.removeLink(viewModel.telegramLinks, idx) },
                        onUpdate = { idx, txt -> viewModel.updateLink(viewModel.telegramLinks, idx, txt) }
                    )
                }
            }

            // 7. Error & Submit
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val finalId = if (moduleId != -1) moduleId else (inputModuleId.toIntOrNull() ?: 0)
                    if (finalId > 0 && userId.isNotBlank()) {
                        viewModel.submitKuppi(finalId, userId)
                    }
                },
                enabled = !isSubmitting && (moduleId != -1 || inputModuleId.isNotBlank()),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Submit Kuppi")
                }
            }

            // Extra spacing at bottom for scrolling
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}