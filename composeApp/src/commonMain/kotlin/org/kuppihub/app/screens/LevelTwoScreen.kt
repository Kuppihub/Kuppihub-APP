package org.kuppihub.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Semester

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
        topBar = { TopAppBar(title = { Text(screenTitle) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            ) }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.padding(16.dp)) {
            items(items) { item ->
                Card(
                    onClick = {
                        println("DEBUG: Clicking Semester: ${item.name} with ID: '${item.id}'")
                        onItemClick(item.id)
                    },
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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