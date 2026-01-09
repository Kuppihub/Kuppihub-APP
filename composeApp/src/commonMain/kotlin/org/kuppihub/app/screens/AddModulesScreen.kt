package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kuppihubappnew.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.SearchModuleItem
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White

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
    var isOffline by remember { mutableStateOf(false) }

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
                val errorMsg = getString(Res.string.search_failed_message)
                snackbarHostState.showSnackbar(errorMsg)
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // Header Content
                // Search Bar in the Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(Res.string.search_modules_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
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
                                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.clear_search))
                                }
                            }
                        },
                        singleLine = true
                    )
                }
                
                if (isLoading || isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KuppiGradients.PageBackground) // Background Gradient
                .padding(p)
        ) {

            // VIEW A: SEARCH RESULTS
            if (searchQuery.length >= 2) {
                if (searchResults.isEmpty() && !isSearching) {
                    Text(stringResource(Res.string.no_modules_found), modifier = Modifier.align(Alignment.Center))
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
                                            val addedMsg = getString(Res.string.added_to_dashboard_snackbar, item.code)
                                            snackbarHostState.showSnackbar(addedMsg)
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
                        Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(Res.string.no_internet_connection), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(Res.string.cannot_load_faculties), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }
                else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        item {
                            Text(
                                stringResource(Res.string.browse_by_faculty),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(faculties) { faculty ->
                            Card(
                                onClick = { onFacultyClick(faculty.id) },
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            faculty.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            stringResource(Res.string.departments_count, faculty.children.size),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = stringResource(Res.string.go),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
