package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen() {
    val viewModel = viewModel { DashboardViewModel() }
    val faculties by viewModel.faculties.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (error != null) {
            Text("Error: $error", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(faculties) { faculty ->
                    FacultyCard(faculty)
                }
            }
        }
    }
}

@Composable
fun FacultyCard(faculty: Faculty) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title: Faculty Name
            Text(
                text = faculty.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Number of Departments/Programs
            // "levels" tells us what the children are (e.g. "Department" or "Program")
            val childType = faculty.levels.firstOrNull() ?: "Department"
            val count = faculty.children.size

            Text(
                text = "$count ${childType}s",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}