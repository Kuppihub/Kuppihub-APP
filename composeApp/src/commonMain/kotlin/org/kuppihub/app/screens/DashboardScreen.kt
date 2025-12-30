package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Note: No "LocalContext" import needed anymore!
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {

    // State to hold our saved modules
    var savedModules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }

    // Load data when screen opens
    LaunchedEffect(Unit) {
        // FIX 1: removed 'context' argument
        savedModules = LocalDashboardRepo.getSavedModules()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Dashboard") }) }
    ) { p ->
        if (savedModules.isEmpty()) {
            Box(modifier = Modifier.padding(p).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No modules added yet.\nGo to 'Add Modules' to select some!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(p)
            ) {
                items(savedModules) { item ->
                    ModuleCard(
                        item = item,
                        onActionButtonClick = {
                            // FIX 2: removed 'context' argument here
                            LocalDashboardRepo.removeModule(item.module.id)

                            // FIX 3: removed 'context' argument here too
                            savedModules = LocalDashboardRepo.getSavedModules()
                        },
                        isAdded = true
                    )
                }
            }
        }
    }
}