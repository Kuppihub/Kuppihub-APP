package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.ModuleResponse // Import this

class DashboardViewModel : ViewModel() {
    private val repository = KuppiRepository

    // 1. Existing: Hierarchy (Faculties/Departments)
    private val _faculties = MutableStateFlow<List<Faculty>>(emptyList())
    val faculties = _faculties.asStateFlow()

    // 2. NEW: User's Dashboard Modules (The list of subjects they follow)
    private val _dashboardModules = MutableStateFlow<List<ModuleResponse>>(emptyList())
    val dashboardModules = _dashboardModules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    init {
        // We load the hierarchy immediately so "Add Module" screens are ready
        fetchHierarchy()
    }

    // Renamed from 'fetchData' to be more specific
    private fun fetchHierarchy() {
        viewModelScope.launch {
            // Only set loading if we don't have data yet
            if (_faculties.value.isEmpty()) _isLoading.value = true
            try {
                _faculties.value = repository.getFaculties()
            } catch (e: Exception) {
                _errorMessage.value = "Hierarchy Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 3. NEW: Call this from your UI after Login
    fun loadUserDashboard(userId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            // ✅ FIX: Clear any old errors before starting!
            _errorMessage.value = null

            try {
                if (userId != null) {
                    val mergedModules = repository.syncAndLoadDashboard(userId)
                    _dashboardModules.value = mergedModules
                } else {
                    val localModules = org.kuppihub.app.data.LocalDashboardRepo.getSavedModules()
                    _dashboardModules.value = localModules
                }
            } catch (e: Exception) {
                _errorMessage.value = "Offline Mode: Showing cached data"
                // Fallback to local
                _dashboardModules.value = org.kuppihub.app.data.LocalDashboardRepo.getSavedModules()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add this inside DashboardViewModel class
    fun removeModule(moduleId: Int, userId: String?) {
        // Optimistic Update
        val currentList = _dashboardModules.value
        _dashboardModules.value = currentList.filter { it.module.id != moduleId }

        viewModelScope.launch {
            val isSynced = repository.removeModule(moduleId, userId)

            if (isSynced) {
                _snackbarEvent.emit("Removed from Dashboard")
                // ✅ FIX: If sync worked, we are online. Clear the error!
                _errorMessage.value = null
            } else {
                _snackbarEvent.emit("Removed Locally (Synced when Online)")
                // We don't set errorMessage here because a Snackbar is enough for this action
            }
        }
    }
}
