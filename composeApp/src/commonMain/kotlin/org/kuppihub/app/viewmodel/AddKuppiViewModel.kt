package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.AddKuppiRequest
import org.kuppihub.app.model.SearchModuleItem

class AddKuppiViewModel : ViewModel() {

    // --- FORM DATA ---
    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val languageCode = MutableStateFlow("en")
    val youtubeLinks = MutableStateFlow<List<String>>(listOf(""))
    val telegramLinks = MutableStateFlow<List<String>>(listOf(""))

    // --- UI STATE ---
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()
    private val _submissionSuccess = MutableStateFlow<Boolean?>(null)
    val submissionSuccess = _submissionSuccess.asStateFlow()
    val errorMessage = MutableStateFlow<String?>(null)

    // --- 🆕 SEARCH STATE ---
    val searchQuery = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<SearchModuleItem>>(emptyList())
    val selectedModule = MutableStateFlow<SearchModuleItem?>(null)
    private var searchJob: Job? = null

    // --- 🆕 SEARCH LOGIC ---
    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        selectedModule.value = null // Reset selection if typing

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms
            if (query.length >= 2) {
                // Call the Repository function you created
                searchResults.value = KuppiRepository.searchModules(query)
            } else {
                searchResults.value = emptyList()
            }
        }
    }

    fun selectModule(module: SearchModuleItem) {
        selectedModule.value = module
        searchQuery.value = "${module.code} - ${module.name}"
        searchResults.value = emptyList() // Close dropdown
    }

    // --- LIST HELPERS ---
    fun addLink(listFlow: MutableStateFlow<List<String>>) { listFlow.value = listFlow.value + "" }
    fun removeLink(listFlow: MutableStateFlow<List<String>>, index: Int) {
        val current = listFlow.value.toMutableList()
        if (index in current.indices) { current.removeAt(index); listFlow.value = current }
    }
    fun updateLink(listFlow: MutableStateFlow<List<String>>, index: Int, newValue: String) {
        val current = listFlow.value.toMutableList()
        if (index in current.indices) { current[index] = newValue; listFlow.value = current }
    }

    // --- SUBMIT ---
    fun submitKuppi(navModuleId: Int, idToken: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            errorMessage.value = null

            // 1. DETERMINE ID: passed via Nav OR selected via Search
            val finalId = if (navModuleId > 0) navModuleId else selectedModule.value?.id ?: 0

            if (finalId <= 0) {
                errorMessage.value = "❌ Please search and select a valid module."
                _isSubmitting.value = false
                return@launch
            }

            val validYoutube = youtubeLinks.value.filter { it.isNotBlank() }
            val validTelegram = telegramLinks.value.filter { it.isNotBlank() }

            val request = AddKuppiRequest(
                title = title.value,
                description = description.value,
                moduleId = finalId,
                languageCode = languageCode.value,
                youtubeLinks = if (validYoutube.isNotEmpty()) validYoutube else null,
                telegramLinks = if (validTelegram.isNotEmpty()) validTelegram else null,
                indexNo = "N/A", isKuppi = true, gdriveLinks = null, onedriveLinks = null, materialLinks = null, allowedDomains = null
            )

            val success = KuppiRepository.addKuppi(request, idToken)
            _submissionSuccess.value = success
            if (!success) errorMessage.value = "❌ Server rejected the request."
            _isSubmitting.value = false
        }
    }
}