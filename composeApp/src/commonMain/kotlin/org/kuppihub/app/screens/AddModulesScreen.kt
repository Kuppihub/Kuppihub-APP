package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.SearchModuleItem
import org.kuppihub.app.ui.components.KuppiLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModulesScreen(onFacultyClick: (String) -> Unit) {
    // Data States
    var faculties by remember { mutableStateOf<List<Faculty>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<SearchModuleItem>>(emptyList()) }
    var savedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // UI States
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) } // Track if search API is running

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Initial Load (Faculties + Saved IDs)
    LaunchedEffect(Unit) {
        faculties = KuppiRepository.getFaculties()
        savedIds = LocalDashboardRepo.getSavedModules().map { it.module.id }.toSet()
        isLoading = false
    }

    // 2. Search Logic (Runs when query changes)
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            searchResults = KuppiRepository.searchModules(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    Scaffold(
        topBar = {
            Column {
                // Custom Search Top Bar
                TopAppBar(
                    title = {
                        // Using a TextField directly in the Title for the Search Bar look
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search modules (e.g. 'cs' or 'math')") },
                            modifier = Modifier
                                .fillMaxWidth(),

                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    },
                    // Show Logo above search bar if you want, or replace it.
                    // Usually for a search screen, the search bar *is* the main thing.
                    // But if you want the branding, put it in a Column above or handle differently.
                    // For now, this replaces the Logo with the Search Bar effectively.
                )
                if (isLoading || isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {

            // VIEW A: SEARCH RESULTS (When user is typing)
            if (searchQuery.length >= 2) {
                if (searchResults.isEmpty() && !isSearching) {
                    Text("No modules found.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { item ->
                            val isAlreadySaved = savedIds.contains(item.id)

                            // Reuse ModuleCard logic but adapted for SearchItem
                            // We convert SearchItem -> ModuleResponse temporarily for the Card
                            val tempModuleResponse = item.toModuleResponse()

                            ModuleCard(
                                item = tempModuleResponse,
                                isAdded = isAlreadySaved,
                                isInDashboardScreen = false,
                                onActionButtonClick = {
                                    if (!isAlreadySaved) {
                                        // Save to DB
                                        LocalDashboardRepo.addModule(tempModuleResponse)
                                        savedIds = savedIds + item.id

                                        scope.launch {
                                            snackbarHostState.showSnackbar("Added ${item.code} to Dashboard")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // VIEW B: BROWSE FACULTIES (Default View)
            else {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        // Optional: Header
                        item {
                            Text(
                                "Browse by Faculty",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        items(faculties) { faculty ->
                            Card(
                                onClick = { onFacultyClick(faculty.id) },
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(faculty.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${faculty.children.size} Departments", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}