package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff // Added icon for offline state
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
    var isSearching by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) } // 👈 New state to track connection errors

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Initial Load (Safe Version)
    LaunchedEffect(Unit) {
        try {
            // Load local saved IDs first (this never fails)
            savedIds = LocalDashboardRepo.getSavedModules().map { it.module.id }.toSet()

            // Try fetching from internet
            faculties = KuppiRepository.getFaculties()
            isOffline = false
        } catch (e: Exception) {
            // If internet fails, just mark as offline. DON'T CRASH.
            e.printStackTrace()
            isOffline = true
        } finally {
            isLoading = false
        }
    }

    // 2. Search Logic (Safe Version)
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            try {
                searchResults = KuppiRepository.searchModules(searchQuery)
            } catch (e: Exception) {
                // If search fails (no internet), just show nothing
                e.printStackTrace()
                searchResults = emptyList()
                scope.launch { snackbarHostState.showSnackbar("Search failed. Check internet.") }
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search modules...") },
                            modifier = Modifier.fillMaxWidth(),
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
                    }
                )
                if (isLoading || isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {

            // VIEW A: SEARCH RESULTS
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
                            val tempModuleResponse = item.toModuleResponse()

                            ModuleCard(
                                item = tempModuleResponse,
                                isAdded = isAlreadySaved,
                                isInDashboardScreen = false,
                                onActionButtonClick = {
                                    if (!isAlreadySaved) {
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
            // VIEW B: BROWSE FACULTIES
            else {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // 👇 HANDLE OFFLINE STATE
                else if (isOffline && faculties.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = "Offline", modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Internet Connection", style = MaterialTheme.typography.bodyLarge)
                        Text("Cannot load faculties.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // Retry Logic
                            isLoading = true
                            scope.launch {
                                try {
                                    faculties = KuppiRepository.getFaculties()
                                    isOffline = false
                                } catch (e: Exception) {
                                    isOffline = true
                                }
                                isLoading = false
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
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