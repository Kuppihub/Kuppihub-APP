package org.kuppihub.app.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.kuppihub.app.model.Semester


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelTwoScreen(facultyId: String, childId: String) {
    var items by remember { mutableStateOf<List<Semester>>(emptyList()) }
    var screenTitle by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        // We need the Faculty to get the "Level Name" (e.g. "Term" or "Semester")
        val faculty = KuppiRepository.getFaculty(facultyId)
        val levelName = faculty?.levels?.getOrNull(1) ?: "Module List"

        // We need the Child to get the actual data
        val childNode = KuppiRepository.getDepartment(facultyId, childId)

        if (childNode != null) {
            screenTitle = "$levelName List" // e.g., "Term List" or "Semester List"
            items = childNode.children.values.sortedBy { it.order }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(screenTitle) }) }
    ) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.padding(16.dp)) {
            items(items) { item ->
                Card(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name) // e.g., "Semester 1" or "Term 1"

                        // Show generic count
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