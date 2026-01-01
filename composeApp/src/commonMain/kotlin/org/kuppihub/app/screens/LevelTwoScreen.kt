package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Semester
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.Blue50
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White

// ... imports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelTwoScreen(
    facultyId: String,
    childId: String,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var items by remember { mutableStateOf<List<Semester>>(emptyList()) }
    var screenTitle by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        println("DEBUG: Level 2 Started. Fac: $facultyId, Dept: $childId")

        val faculty = KuppiRepository.getFaculty(facultyId)
        val levelName = faculty?.levels?.getOrNull(1) ?: "Module List"

        val childNode = KuppiRepository.getDepartment(facultyId, childId)

        if (childNode != null) {
            screenTitle = "$levelName List"

            // --- FIX STARTS HERE ---
            // map { (key, value) } gives us access to the ID (key)
            items = childNode.children.map { (key, semester) ->
                // We create a copy of the semester object, enforcing the ID from the key
                semester.copy(id = key)
            }.sortedBy { it.order }
            // --- FIX ENDS HERE ---

            println("DEBUG: Level 2 Items Loaded: ${items.map { "${it.name}=${it.id}" }}")
        } else {
            println("DEBUG: Level 2 - ChildNode (Dept) was NULL")
        }
    }

    Scaffold(
        containerColor = Blue50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // TIGHT TOOLBAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 4.dp), // Less padding for navigation icon
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        KuppiLogo(showText = false)
                        Spacer(Modifier.width(12.dp))
                        Text(screenTitle, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { p ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(p),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    onClick = {
                        println("DEBUG: Clicking Semester: ${item.name} with ID: '${item.id}'")
                        onItemClick(item.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name)
                        Text(
                            "${item.modules.size} Modules",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
