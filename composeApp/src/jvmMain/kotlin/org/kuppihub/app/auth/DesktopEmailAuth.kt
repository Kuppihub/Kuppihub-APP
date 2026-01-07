package org.kuppihub.app.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.KuppiUser
import java.util.Properties

class DesktopEmailAuth {

    private val properties = Properties().apply {
        try {
            val stream = DesktopEmailAuth::class.java.getResourceAsStream("/secrets.properties")
            if (stream != null) load(stream)
        } catch (e: Exception) { e.printStackTrace() }
    }

    // This uses the Web API Key you added earlier
    private val FIREBASE_API_KEY = properties.getProperty("FIREBASE_WEB_API_KEY") ?: ""

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
    }

    // --- LOGIN ---
    suspend fun signIn(email: String, pass: String): KuppiUser? {
        return try {
            val response: EmailAuthResponse = client.post(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY"
            ) {
                contentType(ContentType.Application.Json)
                setBody(EmailAuthRequest(email = email, password = pass))
            }.body()
            response.toKuppiUser()
        } catch (e: Exception) {
            println("❌ Desktop Login Failed: ${e.message}")
            null
        }
    }

    // --- SIGN UP ---
    suspend fun signUp(email: String, pass: String): KuppiUser? {
        return try {
            val response: EmailAuthResponse = client.post(
                "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$FIREBASE_API_KEY"
            ) {
                contentType(ContentType.Application.Json)
                setBody(EmailAuthRequest(email = email, password = pass))
            }.body()
            response.toKuppiUser()
        } catch (e: Exception) {
            println("❌ Desktop Sign Up Failed: ${e.message}")
            null
        }
    }
}

// --- REST DATA MODELS ---
@Serializable
data class EmailAuthRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
data class EmailAuthResponse(
    val localId: String,
    val email: String,
    val displayName: String? = null,
    val idToken: String,
    val refreshToken: String,
    val expiresIn: String
) {
    fun toKuppiUser(): KuppiUser {
        return KuppiUser(
            id = localId,
            email = email,
            name = displayName ?: email.substringBefore("@"),
            photoUrl = null,
            idToken = idToken,
            refreshToken = refreshToken
        )
    }
}