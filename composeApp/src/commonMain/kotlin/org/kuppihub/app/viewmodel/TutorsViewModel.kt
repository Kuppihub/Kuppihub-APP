package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.Tutor

class TutorsViewModel : ViewModel() {
    private val _tutors = MutableStateFlow<List<Tutor>>(emptyList())
    val tutors = _tutors.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        // 1. Load from cache immediately
        val cached = LocalDashboardRepo.getCachedTutors()
        if (cached.isNotEmpty()) {
            _tutors.value = cached
            _isLoading.value = false // Show content immediately
        }

        // 2. Fetch fresh data from network
        fetchTutors()
    }

    fun fetchTutors() {
        viewModelScope.launch {
            // Only show loading if we have no data
            if (_tutors.value.isEmpty()) {
                _isLoading.value = true
            }

            try {
                val result = KuppiRepository.getTutors()
                if (result.isNotEmpty()) {
                    _tutors.value = result
                    LocalDashboardRepo.saveTutors(result) // Update cache
                }
            } catch (e: Exception) {
                // Network error? We already have cache, so just ignore or show snackbar
                // If cache was empty, we are still empty, so isLoading will stop and UI might show empty state
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
