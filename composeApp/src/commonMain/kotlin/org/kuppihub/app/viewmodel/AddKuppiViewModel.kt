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
    val languageCode = MutableStateFlow("si") // Default changed to Sinhala as per requirement
    val indexNo = MutableStateFlow("")
    val isKuppi = MutableStateFlow(true)

    // Links
    val youtubeLinks = MutableStateFlow<List<String>>(listOf(""))
    val telegramLinks = MutableStateFlow<List<String>>(listOf("")) // Default empty? User said telegramLinks: [] in initialFormData.
    // However, keeping one empty field for convenience in UI might be better, but user snippet had empty array for others.
    // The user's initialFormData: youtubeLinks: [{...}], others: [].
    // I'll stick to my current implementation of listOf("") for youtube and others to provide at least one input field by default?
    // User said "youtubeLinks: [{ id: generateId(), url: "" }], telegramLinks: [], ...".
    // I will change others to emptyList() to match strictly, but usually it's better UX to show one input.
    // I'll keep them as listOf("") because my UI renders inputs based on this list. If it's empty, no input is shown (unless I have an "Add" button which I do).
    // Actually, if I make them emptyList(), the user has to click "Add" to see the first field.
    // I'll stick to listOf("") for all or just youtube?
    // Let's set youtube to listOf("") and others to emptyList() to match `initialFormData` logic if that's what is implied.
    // But wait, my LinkSection UI shows "No links added yet" if empty.
    // I'll set youtube to listOf("") and others to emptyList().
    
    // Actually, looking at the user's LinkSection code:
    // {links.length === 0 ? (<p>No links added yet...</p>) : ...}
    // So yes, others should be empty.

    val gdriveLinks = MutableStateFlow<List<String>>(emptyList())
    val onedriveLinks = MutableStateFlow<List<String>>(emptyList())
    val materialLinks = MutableStateFlow<List<String>>(emptyList())

    // Domain Restrictions
    val hasRestriction = MutableStateFlow(false)
    val allowedDomains = MutableStateFlow<List<String>>(emptyList())

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

    init {
         // Fix: Telegram should probably be empty by default too based on initialFormData
         telegramLinks.value = emptyList()
    }

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
        // Validation: Remove double quotes and commas
        val sanitized = newValue.replace("\"", "").replace(",", "")
        
        val current = listFlow.value.toMutableList()
        if (index in current.indices) { current[index] = sanitized; listFlow.value = current }
    }

    // Helper for domain toggle
    fun toggleDomain(domain: String) {
        val current = allowedDomains.value.toMutableList()
        if (current.contains(domain)) {
            current.remove(domain)
        } else {
            current.add(domain)
        }
        allowedDomains.value = current
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

            if (title.value.isBlank()) {
                errorMessage.value = "❌ Title is required."
                _isSubmitting.value = false
                return@launch
            }
             if (description.value.isBlank()) {
                errorMessage.value = "❌ Description is required."
                _isSubmitting.value = false
                return@launch
            }

            val validYoutube = youtubeLinks.value.filter { it.isNotBlank() }
            val validTelegram = telegramLinks.value.filter { it.isNotBlank() }
            val validGdrive = gdriveLinks.value.filter { it.isNotBlank() }
            val validOnedrive = onedriveLinks.value.filter { it.isNotBlank() }
            val validMaterial = materialLinks.value.filter { it.isNotBlank() }

            if (validYoutube.isEmpty() && validTelegram.isEmpty() && validGdrive.isEmpty() && validOnedrive.isEmpty() && validMaterial.isEmpty()) {
                errorMessage.value = "❌ Please add at least one link."
                _isSubmitting.value = false
                return@launch
            }

            if (hasRestriction.value && allowedDomains.value.isEmpty()) {
                 errorMessage.value = "❌ Please select at least one domain or disable restriction."
                _isSubmitting.value = false
                return@launch
            }

            val request = AddKuppiRequest(
                title = title.value,
                description = description.value,
                moduleId = finalId,
                languageCode = languageCode.value,
                indexNo = if (indexNo.value.isNotBlank()) indexNo.value else "N/A",
                isKuppi = isKuppi.value,
                youtubeLinks = if (validYoutube.isNotEmpty()) validYoutube else null,
                telegramLinks = if (validTelegram.isNotEmpty()) validTelegram else null,
                gdriveLinks = if (validGdrive.isNotEmpty()) validGdrive else null,
                onedriveLinks = if (validOnedrive.isNotEmpty()) validOnedrive else null,
                materialLinks = if (validMaterial.isNotEmpty()) validMaterial else null,
                allowedDomains = if (hasRestriction.value) allowedDomains.value else null
            )

            val success = KuppiRepository.addKuppi(request, idToken)
            _submissionSuccess.value = success
            if (!success) errorMessage.value = "❌ Server rejected the request."
            _isSubmitting.value = false
        }
    }
}