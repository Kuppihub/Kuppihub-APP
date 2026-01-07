package org.kuppihub.app.screens.addkuppi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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

// KMP Firebase Imports
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKuppiScreen(
    moduleId: Int,
    userId: String,
    onBackClick: () -> Unit
) {
    val viewModel = remember { AddKuppiViewModel() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // ViewModel State
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val youtubeLinks by viewModel.youtubeLinks.collectAsState()
    val telegramLinks by viewModel.telegramLinks.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val success by viewModel.submissionSuccess.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    // 🆕 Search State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedModule by viewModel.selectedModule.collectAsState()

    LaunchedEffect(success) {
        if (success == true) onBackClick()
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("How to upload:", style = MaterialTheme.typography.labelLarge)
                        Text("1. Search & Select a Module.")
                        Text("2. Paste YouTube/Telegram links.")
                    }
                }
            }

            // 🆕 SEARCH MODULE FIELD (Only if ID was not passed via Nav)
            if (moduleId == -1) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        label = { Text("Search Module (e.g. CS10)") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = if (selectedModule != null) {
                            { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Results Dropdown
                    if (searchResults.isNotEmpty()) {
                        ElevatedCard(
                            modifier = Modifier
                                .padding(top = 60.dp)
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                searchResults.forEach { module ->
                                    ListItem(
                                        headlineContent = { Text(module.code) },
                                        supportingContent = { Text(module.name) },
                                        modifier = Modifier.clickable { viewModel.selectModule(module) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            // Main Fields
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
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
                    // Use "sin" and "tam" for compatibility
                    val languages = listOf("en" to "English", "si" to "Sinhala", "ta" to "Tamil")
                    languages.forEach { (code, label) ->
                        FilterChip(
                            selected = languageCode == code,
                            onClick = { viewModel.languageCode.value = code },
                            label = { Text(label) },
                            leadingIcon = if (languageCode == code) { { Icon(Icons.Default.Check, null) } } else null
                        )
                    }
                }
            }

            Divider()

            // Links Section
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection("YouTube Links", Icons.Default.SmartDisplay, Color(0xFFFF0000), youtubeLinks,
                        { viewModel.addLink(viewModel.youtubeLinks) }, { viewModel.removeLink(viewModel.youtubeLinks, it) }, { i, s -> viewModel.updateLink(viewModel.youtubeLinks, i, s) })
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LinkSection("Telegram Links", Icons.Default.Send, Color(0xFF0088CC), telegramLinks,
                        { viewModel.addLink(viewModel.telegramLinks) }, { viewModel.removeLink(viewModel.telegramLinks, it) }, { i, s -> viewModel.updateLink(viewModel.telegramLinks, i, s) })
                }
            }

            if (errorMsg != null) Text(errorMsg!!, color = MaterialTheme.colorScheme.error)

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button with Token Logic
            Button(
                onClick = {
                    scope.launch {
                        // 1. Default to the passed token
                        var tokenToUse = userId

                        // 2. Try to refresh if on Mobile (KMP Style)
                        try {
                            val currentUser = Firebase.auth.currentUser
                            if (currentUser != null) {
                                val freshToken = currentUser.getIdToken(false)
                                if (freshToken != null) tokenToUse = freshToken
                            }
                        } catch (_: Exception) { }

                        // 3. Submit
                        viewModel.submitKuppi(moduleId, tokenToUse)
                    }
                },
                // Enable if (ID passed via nav OR module selected via search)
                enabled = !isSubmitting && (moduleId != -1 || selectedModule != null),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White) else Text("Submit Kuppi")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}