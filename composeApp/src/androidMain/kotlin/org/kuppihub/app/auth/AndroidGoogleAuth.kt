package org.kuppihub.app.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AndroidGoogleAuth(private val context: Context) : GoogleAuthService {

    // 🔴 REPLACE THIS WITH YOUR REAL WEB CLIENT ID FROM FIREBASE CONSOLE
    private val webClientId = "450668429167-fv1gj9gatenc1qjilqo7rpe5vodqrom7.apps.googleusercontent.com"

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId) // This gets the token for Firebase
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    override suspend fun signIn(): dev.gitlive.firebase.auth.FirebaseUser? {
        // 1. Trigger the Google Login Intent manually (Simplified for KMP)
        // Note: In a pure production app, you usually inject Activity Context.
        // For now, we will assume this is called from an Activity context or we handle the intent result in MainActivity.
        return null // See Step 4 for the Activity handling
    }

    // Helper to get the Intent to launch
    fun getSignInIntent() = googleSignInClient.signInIntent

    // Helper to handle the result after user picks an account
    suspend fun handleLoginResult(intent: android.content.Intent): dev.gitlive.firebase.auth.FirebaseUser? {
        return try {
            // 1. Get Google Account from the Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.await()
            val idToken = account.idToken

            // 2. Pass Token to Firebase
            if (idToken != null) {
                val credential = GoogleAuthProvider.credential(idToken, null)
                val authResult = Firebase.auth.signInWithCredential(credential)
                authResult.user
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
        googleSignInClient.signOut().await()
    }
}