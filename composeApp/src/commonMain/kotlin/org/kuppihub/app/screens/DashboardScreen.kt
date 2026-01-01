package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients // 👈 Import your gradients
import org.kuppihub.app.ui.theme.Blue50       // 👈 Import your solid background color
import org.kuppihub.app.ui.theme.White

@Composable
fun DashboardScreen(
    onModuleClick: (Int, String) -> Unit,
    onAddModuleClick: () -> Unit
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
            displayModules = KuppiRepository.getDashboardDetails(idsToFetch)
        } catch (e: Exception) {
            e.printStackTrace()
            displayModules = localList
            errorText = "Offline mode: Data might be outdated."
        }
        isLoading = false
    }

    Scaffold(
        // 1.  APPLY THEME BACKGROUND (Blue50)
        containerColor = Blue50,

        // 2.  CUSTOM GRADIENT HEADER
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // 2. TIGHT TOOLBAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp) // 👈 Fixed Compact Height (Standard is 56dp or 64dp, 48dp is very tight)
                        .padding(horizontal = 16.dp), // Only side padding, NO vertical padding
                    contentAlignment = Alignment.CenterStart
                ) {
                    KuppiLogo()
                }
            }
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (displayModules.isEmpty()) {

                // --- EMPTY STATE ---
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No modules added yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start by adding your first module.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. GRADIENT BUTTON
                    Button(
                        onClick = onAddModuleClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent // Transparent to show gradient
                        ),
                        contentPadding = PaddingValues(), // Remove default padding
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(KuppiGradients.PrimaryButton) // Apply Gradient
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Browse Modules", color = White)
                        }
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
                        // Note: Ensure your ModuleCard itself uses the White background
                        // and BlueBorder logic we discussed!
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
