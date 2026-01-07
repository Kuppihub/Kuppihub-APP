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
import kotlinx.coroutines.launch
import org.kuppihub.app.viewmodel.AddKuppiViewModel

// 1️⃣ Correct Imports for KMP Firebase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKuppiScreen(
    moduleId: Int,
    userId: String, // Note: This variable holds the ID TOKEN
    onBackClick: () -> Unit
) {
    val viewModel = remember { AddKuppiViewModel() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope() // 2️⃣ Needed to run suspend functions (token fetch)

    // Helper state for manual entry if ID is -1
    var inputModuleId by remember { mutableStateOf(if (moduleId != -1) moduleId.toString() else "") }

    // ViewModel State
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val youtubeLinks by viewModel.youtubeLinks.collectAsState()
    val telegramLinks by viewModel.telegramLinks.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
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

            // Module ID Field
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

            // Main Text Fields
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

            // Language Selector
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

            // YouTube Section
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection(
                        title = "YouTube Links",
                        icon = Icons.Default.SmartDisplay,
                        color = Color(0xFFFF0000),
                        links = youtubeLinks,
                        onAdd = { viewModel.addLink(viewModel.youtubeLinks) },
                        onRemove = { idx -> viewModel.removeLink(viewModel.youtubeLinks, idx) },
                        onUpdate = { idx, txt -> viewModel.updateLink(viewModel.youtubeLinks, idx, txt) }
                    )
                }
            }

            // Telegram Section
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection(
                        title = "Telegram Links",
                        icon = Icons.Default.Send,
                        color = Color(0xFF0088CC),
                        links = telegramLinks,
                        onAdd = { viewModel.addLink(viewModel.telegramLinks) },
                        onRemove = { idx -> viewModel.removeLink(viewModel.telegramLinks, idx) },
                        onUpdate = { idx, txt -> viewModel.updateLink(viewModel.telegramLinks, idx, txt) }
                    )
                }
            }

            // Error Message
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 3️⃣ FIXED SUBMIT BUTTON LOGIC
            Button(
                onClick = {
                    val finalId = if (moduleId != -1) moduleId else (inputModuleId.toIntOrNull() ?: 0)

                    if (finalId > 0 && userId.isNotBlank()) {
                        scope.launch {
                            // A. Default to the passed token (Works for Desktop)
                            var tokenToUse = userId

                            // B. Try to refresh if on Mobile (KMP Style)
                            try {
                                val currentUser = Firebase.auth.currentUser
                                if (currentUser != null) {
                                    // 🛠️ FIX: Handle nullable result safely
                                    val freshToken = currentUser.getIdToken(false)
                                    if (freshToken != null) {
                                        tokenToUse = freshToken
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore errors (happens on Desktop if using custom auth)
                            }

                            // C. Submit with the best token we have
                            viewModel.submitKuppi(finalId, tokenToUse)
                        }

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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}