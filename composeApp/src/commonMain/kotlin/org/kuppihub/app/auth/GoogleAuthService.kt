package org.kuppihub.app.auth

import dev.gitlive.firebase.auth.FirebaseUser

interface GoogleAuthService {
    // This function will trigger the Google Popup
    suspend fun signIn(): FirebaseUser?

    suspend fun signOut()
}