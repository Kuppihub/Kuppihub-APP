package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.ModuleResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelThreeScreen(facultyId: String, childId: String, semesterId: String) {
    var modules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var title by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var debugText by remember { mutableStateOf("") } // To show errors on screen

    LaunchedEffect(Unit) {
        println("DEBUG: Level 3 Started for Faculty: $facultyId, Dept: $childId, Sem: $semesterId")

        // 1. Get Semester
        val semester = KuppiRepository.getSemester(facultyId, childId, semesterId)

        if (semester == null) {
            println("DEBUG: ERROR - Semester object is NULL!")
            debugText = "Error: Semester data not found in cache."
            isLoading = false
            return@LaunchedEffect
        }

        title = semester.name
        val moduleIds = semester.modules
        println("DEBUG: Found Semester '${semester.name}'. Module IDs: $moduleIds")

        if (moduleIds.isEmpty()) {
            println("DEBUG: This semester has 0 modules.")
            debugText = "No modules listed for this semester."
        } else {
            // 2. Fetch Modules
            try {
                println("DEBUG: Fetching details for IDs: $moduleIds")
                val result = KuppiRepository.getModulesByIds(moduleIds)
                println("DEBUG: API Response Size: ${result.size}")

                if (result.isEmpty()) {
                    debugText = "API returned 0 modules."
                }

                modules = result
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: API FAILED - ${e.message}")
                debugText = "API Error: ${e.message}"
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (modules.isEmpty()) {
                // Show the debug reason on screen
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No modules found.")
                    Spacer(Modifier.height(8.dp))
                    Text(debugText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modules) { item ->
                        ModuleCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(item: ModuleResponse) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Module Code Bubble (e.g. "CS1040")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.module.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center: Name and Video Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (item.video_count > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Videos",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.video_count} Videos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}