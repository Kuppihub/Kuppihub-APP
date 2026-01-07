package org.kuppihub.app.data

import org.kuppihub.app.auth.DesktopEmailAuth
import org.kuppihub.app.model.KuppiUser

class JvmAuthRepository : AuthRepository {
    private val auth = DesktopEmailAuth()

    override suspend fun signIn(email: String, pass: String): KuppiUser? {
        return auth.signIn(email, pass)
    }

    override suspend fun signUp(email: String, pass: String): KuppiUser? {
        return auth.signUp(email, pass)
    }
}

actual fun getAuthRepository(): AuthRepository = JvmAuthRepository()