package org.kuppihub.app.data

import org.kuppihub.app.model.KuppiUser

interface AuthRepository {
    // 🔴 CHANGE: Return Result<KuppiUser> to match Android
    suspend fun signIn(email: String, pass: String): Result<KuppiUser>
    suspend fun signUp(email: String, pass: String, name: String): Result<KuppiUser>

    suspend fun sendEmailVerification(user: KuppiUser): Boolean
    suspend fun reloadUser(user: KuppiUser): KuppiUser?
}

expect fun getAuthRepository(): AuthRepository