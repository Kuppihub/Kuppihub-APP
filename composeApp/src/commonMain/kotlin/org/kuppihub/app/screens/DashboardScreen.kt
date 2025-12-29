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
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Department
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.Semester
import org.kuppihub.app.viewmodel.DashboardViewModel

// In screens/DashboardScreen.kt
@Composable
fun DashboardScreen(onFacultyClick: (String) -> Unit) {
    var faculties by remember { mutableStateOf<List<Faculty>>(emptyList()) }

    // Fetch Data
    LaunchedEffect(Unit) {
        faculties = KuppiRepository.getFaculties()
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(faculties) { faculty ->
            Card(
                onClick = { onFacultyClick(faculty.id) }, // Pass the ID
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(faculty.name, style = MaterialTheme.typography.titleMedium)
                    Text("${faculty.children.size} Departments", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Create new file: screens/DepartmentScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(facultyId: String, onDeptClick: (String) -> Unit) {
    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    var title by remember { mutableStateOf("Loading...") }

    LaunchedEffect(facultyId) {
        val faculty = KuppiRepository.getFaculty(facultyId)
        if (faculty != null) {
            title = faculty.name
            // Sort departments by order
            departments = faculty.children.values.sortedBy { it.order }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.padding(16.dp)) {
            items(departments) { dept ->
                Card(
                    onClick = { onDeptClick(dept.id) }, // Pass Dept ID (e.g., "cse")
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(dept.name, style = MaterialTheme.typography.titleMedium)
                        Text("Tap to view Semesters", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}


// Create new file: screens/SemesterScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterScreen(facultyId: String, deptId: String) {
    var semesters by remember { mutableStateOf<List<Semester>>(emptyList()) }
    var title by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        val dept = KuppiRepository.getDepartment(facultyId, deptId)
        if (dept != null) {
            title = dept.name
            semesters = dept.children.values.sortedBy { it.order }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { p ->
        LazyColumn(contentPadding = p, modifier = Modifier.padding(16.dp)) {
            items(semesters) { semester ->
                Card(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(semester.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${semester.modules.size} Modules", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}