package org.kuppihub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task // Required for the manual await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.kuppihub.app.auth.AndroidGoogleAuth
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.MainScreen

// Add intent imports
import android.content.Intent

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: AndroidGoogleAuth
    private val _currentUserState = MutableStateFlow<KuppiUser?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuth = AndroidGoogleAuth(this, _currentUserState)
        enableEdgeToEdge()

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                lifecycleScope.launch {
                    // Explicit type declaration helps compiler resolve methods
                    val firebaseUser: FirebaseUser? = googleAuth.handleLoginResult(result.data!!) as FirebaseUser?

                    if (firebaseUser != null) {
                        try {
                            // Uses the manual extension function defined at the bottom
                            val tokenResult = firebaseUser.getIdToken(false).await()
                            val token = tokenResult.token ?: ""

                            val user = KuppiUser(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "User",
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString(),
                                idToken = token
                            )

                            KuppiRepository.syncUserToBackend(user)
                            println("LOGIN SUCCESS & SYNCED: ${user.name}")
                        } catch (e: Exception) {
                            println("Login sync failed: ${e.message}")
                        }
                    }
                }
            }
        }

        setContent {
            var firebaseUser by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }
            var currentIdToken by remember { mutableStateOf("") }

            DisposableEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    firebaseUser = firebaseAuth.currentUser
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            LaunchedEffect(firebaseUser) {
                if (firebaseUser != null) {
                    try {
                        // Uses the manual extension function defined at the bottom
                        val result = firebaseUser!!.getIdToken(false).await()
                        currentIdToken = result.token ?: ""
                    } catch (e: Exception) {
                        println("Error fetching token: ${e.message}")
                        currentIdToken = ""
                    }
                } else {
                    currentIdToken = ""
                }
            }

            val kuppiUser = remember(firebaseUser, currentIdToken) {
                firebaseUser?.let { user ->
                    KuppiUser(
                        id = user.uid,
                        name = user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        idToken = currentIdToken
                    )
                }
            }

            MainScreen(
                currentUser = kuppiUser,
                onGoogleLoginClick = {
                    launcher.launch(googleAuth.getSignInIntent())
                },
                onLogoutClick = {
                    lifecycleScope.launch {
                        googleAuth.signOut()
                        FirebaseAuth.getInstance().signOut()
                        currentIdToken = ""
                    }
                }
            )
        }
    }
}

/**
 * 🛠️ MANUAL EXTENSION FUNCTION
 * This replaces the need for 'kotlinx-coroutines-play-services'
 * and fixes the "Unresolved reference: await" error.
 */
suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
}