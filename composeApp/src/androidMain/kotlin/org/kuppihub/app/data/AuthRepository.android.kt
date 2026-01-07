package org.kuppihub.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.kuppihub.app.model.KuppiUser

class AndroidAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, pass: String): KuppiUser? {
        return try {
            val result = Firebase.auth.signInWithEmailAndPassword(email, pass)
            val user = result.user
            val token = user?.getIdToken(false)
            if (user != null && token != null) {
                // Map to your App's User Model
                KuppiUser(user.uid, user.email ?: "", user.displayName ?: "User", user.photoURL, token, null)
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    override suspend fun signUp(email: String, pass: String): KuppiUser? {
        return try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email, pass)
            val user = result.user
            val token = user?.getIdToken(false)
            if (user != null && token != null) {
                KuppiUser(user.uid, user.email ?: "", user.displayName ?: "User", user.photoURL, token, null)
            } else null
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}

actual fun getAuthRepository(): AuthRepository = AndroidAuthRepository()