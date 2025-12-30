package org.kuppihub.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse
import org.kuppihub.app.ui.components.KuppiLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onModuleClick:(Int, String) -> Unit
) {
    // We hold the list of modules to display here
    var displayModules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // 1. Get the locally saved list first
        val localList = LocalDashboardRepo.getSavedModules()

        if (localList.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        // 2. Extract IDs to ask the server for updates (e.g. [33, 34])
        val idsToFetch = localList.map { it.module.id }

        try {
            // 3. Fetch fresh data from API
            println("DEBUG: Refreshing Dashboard for IDs: $idsToFetch")
            val freshData = KuppiRepository.getDashboardDetails(idsToFetch)

            // 4. Show the fresh data
            displayModules = freshData
        } catch (e: Exception) {
            e.printStackTrace()
            println("DEBUG: API Failed, falling back to local data.")
            // Fallback: If internet fails, just show the local copy
            displayModules = localList
            errorText = "Offline mode: Data might be outdated."
        }

        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(
            title = { KuppiLogo() }

            ) }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (displayModules.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No modules added yet.", style = MaterialTheme.typography.titleMedium)
                    Text("Go to 'Add Modules' to start.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // List of Modules
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Optional: Show offline warning if API failed
                    if (errorText.isNotEmpty()) {
                        item {
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    items(displayModules) { item ->
                        // FIX 2: We wrap the card in a Box to handle the click.
                        // This way, we don't need to change ModuleCard code at all!
                        Box(
                            modifier = Modifier
                                .clickable { onModuleClick(item.module.id, item.module.code) }
                        ) {
                            ModuleCard(
                                item = item,
                                isAdded = true,
                                isInDashboardScreen = true,
                                onActionButtonClick = {
                                    LocalDashboardRepo.removeModule(item.module.id)
                                    displayModules = displayModules.filter { it.module.id != item.module.id }
                                }
                                // We removed 'onCardClick' here because we are handling it in the Box above
                            )
                        }
                    }
                }
            }
        }
    }
}