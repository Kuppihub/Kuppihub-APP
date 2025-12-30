package org.kuppihub.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import org.kuppihub.app.model.Department



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelOneScreen(facultyId: String, onItemClick: (String) -> Unit, onBackClick: () -> Unit) {
    var items by remember { mutableStateOf<List<Department>>(emptyList()) }
    var screenTitle by remember { mutableStateOf("Loading...") }

    LaunchedEffect(facultyId) {
        val faculty = KuppiRepository.getFaculty(facultyId)
        if (faculty != null) {
            // MAGIC IS HERE: Read the first string from the 'levels' array
            // If it's Medicine, this becomes "Year". If Engineering, "Department".
            val levelName = faculty.levels.getOrNull(0) ?: "Item"

            screenTitle = "Select $levelName" // e.g. "Select Year" or "Select Department"

            // Re-use the 'Department' class to hold Years/Programs since the JSON structure is identical
            items = faculty.children.values.sortedBy { it.order }
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
                    onClick = { onItemClick(item.id) },
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}