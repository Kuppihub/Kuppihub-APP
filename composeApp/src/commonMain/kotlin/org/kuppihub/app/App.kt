package org.kuppihub.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.* // 👈 Fixes 'getValue' and 'setValue' errors
import kotlinx.coroutines.launch // 👈 Fixes 'suspend function' errors
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kuppihub.app.auth.GoogleAuthService
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.MainScreen

@Composable
@Preview
fun App(
    authService: GoogleAuthService? = null, // Desktop passes this in
    systemUser: KuppiUser? = null           // Android/iOS passes this in
) {
    MaterialTheme {
        // 1. Global User State
        var currentUser by remember { mutableStateOf(systemUser) }

        // 2. Coroutine Scope (Needed to run 'suspend' functions like signIn)
        val scope = rememberCoroutineScope()

        MainScreen(
            currentUser = currentUser,
            authService = authService,

            // --- A. HANDLE GOOGLE LOGIN ---
            onGoogleLoginClick = {
                scope.launch { // 👈 Run network calls inside this block
                    try {
                        val result = authService?.signIn() // Call the suspend function

                        // Check what we got back. If it's a valid user, update state.
                        // Note: If 'result' is FirebaseUser, we map it to KuppiUser.
                        // If your GoogleAuthService already returns KuppiUser, this is simple:
                        if (result != null) {
                            // Assuming result is KuppiUser. If not, map it here:
                            // currentUser = result.toKuppiUser()
                            // For now, we assume your Service returns the correct type:
                            // If type mismatch persists, let me know!
                            // For safety, I'll cast or assign based on your previous code:
                            currentUser = result as? KuppiUser
                        }
                    } catch (e: Exception) {
                        println("Google Login Failed: ${e.message}")
                    }
                }
            },

            // --- B. HANDLE EMAIL LOGIN SUCCESS ---
            onLoginSuccess = { user ->
                currentUser = user // Update UI immediately
            },

            // --- C. HANDLE LOGOUT ---
            onLogoutClick = {
                scope.launch {
                    authService?.signOut()
                    currentUser = null
                }
            }
        )
    }
}