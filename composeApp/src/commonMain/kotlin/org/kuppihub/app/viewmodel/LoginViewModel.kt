package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kuppihub.app.data.getAuthRepository
import org.kuppihub.app.model.KuppiUser

class LoginViewModel : ViewModel() {

    // 1. Get the Repository (Auto-selects Desktop/Android logic)
    private val authRepository = getAuthRepository()

    // 2. State for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Holds the user if login is successful
    private val _loginSuccessUser = MutableStateFlow<KuppiUser?>(null)
    val loginSuccessUser = _loginSuccessUser.asStateFlow()

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val user = authRepository.signIn(email, pass)

            if (user != null) {
                _loginSuccessUser.value = user // Triggers navigation in UI
            } else {
                _errorMessage.value = "❌ Login Failed. Check email or password."
            }
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val user = authRepository.signUp(email, pass)

            if (user != null) {
                _loginSuccessUser.value = user
            } else {
                _errorMessage.value = "❌ Sign Up Failed. Email might be in use."
            }
            _isLoading.value = false
        }
    }
}