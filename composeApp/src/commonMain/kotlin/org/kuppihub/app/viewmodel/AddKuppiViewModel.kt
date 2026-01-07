package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.AddKuppiRequest

class AddKuppiViewModel : ViewModel() {

    // Form Data
    val title = MutableStateFlow("")
    val description = MutableStateFlow("")

    // 1️⃣ ADD THIS LINE HERE:
    val languageCode = MutableStateFlow("en") // Default English

    // Links Lists
    val youtubeLinks = MutableStateFlow<List<String>>(listOf(""))
    val telegramLinks = MutableStateFlow<List<String>>(listOf(""))

    // UI State
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _submissionSuccess = MutableStateFlow<Boolean?>(null)
    val submissionSuccess = _submissionSuccess.asStateFlow()

    val errorMessage = MutableStateFlow<String?>(null)

    // Helper functions
    fun addLink(listFlow: MutableStateFlow<List<String>>) {
        listFlow.value = listFlow.value + ""
    }

    fun removeLink(listFlow: MutableStateFlow<List<String>>, index: Int) {
        val current = listFlow.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            listFlow.value = current
        }
    }

    fun updateLink(listFlow: MutableStateFlow<List<String>>, index: Int, newValue: String) {
        val current = listFlow.value.toMutableList()
        if (index in current.indices) {
            current[index] = newValue
            listFlow.value = current
        }
    }

    fun submitKuppi(moduleId: Int, idToken: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            errorMessage.value = null

            if (moduleId <= 0) {
                errorMessage.value = "❌ Invalid Module ID."
                _isSubmitting.value = false
                return@launch
            }

            val validYoutube = youtubeLinks.value.filter { it.isNotBlank() }
            val validTelegram = telegramLinks.value.filter { it.isNotBlank() }

            val request = AddKuppiRequest(
                title = title.value,
                description = description.value,
                moduleId = moduleId,

                // 2️⃣ USE IT HERE:
                languageCode = languageCode.value,

                youtubeLinks = if (validYoutube.isNotEmpty()) validYoutube else null,
                telegramLinks = if (validTelegram.isNotEmpty()) validTelegram else null,

                // Defaults
                indexNo = "N/A",
                isKuppi = true,
                gdriveLinks = null,
                onedriveLinks = null,
                materialLinks = null,
                allowedDomains = null
            )

            val success = KuppiRepository.addKuppi(request, idToken)

            _submissionSuccess.value = success
            if (!success) {
                errorMessage.value = "❌ Server rejected the request."
            }
            _isSubmitting.value = false
        }
    }
}