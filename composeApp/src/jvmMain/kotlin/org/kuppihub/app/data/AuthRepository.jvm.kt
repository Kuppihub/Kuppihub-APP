package org.kuppihub.app.data

import org.kuppihub.app.auth.DesktopEmailAuth
import org.kuppihub.app.model.KuppiUser

class JvmAuthRepository : AuthRepository {
    private val auth = DesktopEmailAuth()

    override suspend fun signIn(email: String, pass: String): Result<KuppiUser> {
        val user = auth.signIn(email, pass)
        return if (user != null) {
            Result.success(user)
        } else {
            // Since DesktopEmailAuth doesn't throw exceptions yet, we return a generic failure
            Result.failure(Exception("Login failed. Check your email or password."))
        }
    }

    override suspend fun signUp(email: String, pass: String, name: String): Result<KuppiUser> {
        val user = auth.signUp(email, pass)
        return if (user != null) {
            // 🆕 Set the name immediately after sign up
            auth.setDisplayName(user.idToken, name)
            // Return user with the new name
            Result.success(user.copy(name = name))
        } else {
            Result.failure(Exception("Sign up failed."))
        }
    }
    override suspend fun sendEmailVerification(user: KuppiUser): Boolean {
        return auth.sendVerificationEmail(user.idToken)
    }

    override suspend fun reloadUser(user: KuppiUser): KuppiUser? {
        // We use the lookup API to get fresh data
        return auth.getUserData(user.idToken)
    }
}

actual fun getAuthRepository(): AuthRepository = JvmAuthRepository()