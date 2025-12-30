package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Department
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.Semester
import org.kuppihub.app.ui.components.KuppiLogo

// In screens/AddModulesScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModulesScreen(onFacultyClick: (String) -> Unit) {
    var faculties by remember { mutableStateOf<List<Faculty>>(emptyList()) }

    LaunchedEffect(Unit) {
        faculties = KuppiRepository.getFaculties()
    }

    // Wrapped in Scaffold to show the Logo
    Scaffold(
        topBar = {
            TopAppBar(
                title = { KuppiLogo() } // Full "Kuppi Hub" Logo
            )
        }
    ) { p ->
        LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(p)) {
            items(faculties) { faculty ->
                Card(
                    onClick = { onFacultyClick(faculty.id) },
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(faculty.name, style = MaterialTheme.typography.titleMedium)
                        Text("${faculty.children.size} Departments", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

