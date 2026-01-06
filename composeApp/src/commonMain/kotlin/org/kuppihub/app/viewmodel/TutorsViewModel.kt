package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.Tutor

class TutorsViewModel : ViewModel() {
    private val _tutors = MutableStateFlow<List<Tutor>>(emptyList())
    val tutors = _tutors.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchTutors()
    }

    fun fetchTutors() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = KuppiRepository.getTutors()
            _tutors.value = result
            _isLoading.value = false
        }
    }
}