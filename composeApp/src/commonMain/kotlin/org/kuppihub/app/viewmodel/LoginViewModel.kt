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
    private val _isVerificationMode = MutableStateFlow(false)
    val isVerificationMode = _isVerificationMode.asStateFlow()
    private var tempUser: KuppiUser? = null


    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signIn(email, pass)

            result.onSuccess { user ->
                // ✅ Login Success
                // Check if email is verified (optional security step)
                if (user.isEmailVerified) {
                    _loginSuccessUser.value = user
                } else {
                    // Decide if you want to block unverified logins:
                    // Option A: Allow login anyway (standard behavior)
                    _loginSuccessUser.value = user

                    // Option B: Force verification (uncomment below if needed)
                    // tempUser = user
                    // _isVerificationMode.value = true
                }
            }.onFailure { exception ->
                // ❌ Login Failed: Show real error (e.g. "Wrong password", "Network error")
                _errorMessage.value = "Login Failed: ${exception.message}"
            }

            _isLoading.value = false
        }
    }

    // Update the function signature
    fun signUpWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _errorMessage.value = "❌ Name is required."
                return@launch
            }

            _isLoading.value = true
            _errorMessage.value = null

            // Pass 'name' to the repository
            val result = authRepository.signUp(email, pass, name)

            result.onSuccess { user ->
                // ... same verification logic ...
                val emailSent = authRepository.sendEmailVerification(user)
                if (emailSent) {
                    tempUser = user
                    _isVerificationMode.value = true
                } else {
                    _errorMessage.value = "Account created, but verification email failed."
                }
            }.onFailure { exception ->
                _errorMessage.value = "Sign Up Failed: ${exception.message}"
            }

            _isLoading.value = false
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            val userToCheck = tempUser ?: return@launch

            // Reload user from server
            val refreshedUser = authRepository.reloadUser(userToCheck)

            if (refreshedUser != null && refreshedUser.isEmailVerified) {
                // ✅ Verified! Now log them in fully.
                _loginSuccessUser.value = refreshedUser
                _isVerificationMode.value = false
            } else {
                _errorMessage.value = "⚠️ Email not verified yet. Please check your inbox."
            }
            _isLoading.value = false
        }
    }

    // 🆕 Resend Button Logic
    fun resendVerification() {
        viewModelScope.launch {
            tempUser?.let {
                authRepository.sendEmailVerification(it)
                _errorMessage.value = "✅ Verification email resent!"
            }
        }
    }

    // Helper to go back to login form
    fun resetToLogin() {
        _isVerificationMode.value = false
        _errorMessage.value = null
        tempUser = null
    }
}
