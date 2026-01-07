package org.kuppihub.app.data

import org.kuppihub.app.model.KuppiUser

interface AuthRepository {
    suspend fun signIn(email: String, pass: String): KuppiUser?
    suspend fun signUp(email: String, pass: String): KuppiUser?
}

// This magic function finds the right platform code (Android vs Desktop vs iOS)
expect fun getAuthRepository(): AuthRepository