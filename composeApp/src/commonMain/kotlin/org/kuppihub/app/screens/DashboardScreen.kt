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
    onModuleClick: (Int, String) -> Unit,
    onAddModuleClick: () -> Unit // 👈 1. New parameter for navigation
) {
    var displayModules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val localList = LocalDashboardRepo.getSavedModules()

        if (localList.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        val idsToFetch = localList.map { it.module.id }

        try {
            val freshData = KuppiRepository.getDashboardDetails(idsToFetch)
            displayModules = freshData
        } catch (e: Exception) {
            e.printStackTrace()
            displayModules = localList
            errorText = "Offline mode: Data might be outdated."
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { KuppiLogo() }
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (displayModules.isEmpty()) {
                // --- EMPTY STATE WITH BUTTON ---
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No modules added yet.", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Start by adding your first module.", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(24.dp))

                    // 👈 2. The Redirect Button
                    Button(onClick = onAddModuleClick) {
                        Text("Browse Modules")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            )
                        }
                    }
                }
            }
        }
    }
}