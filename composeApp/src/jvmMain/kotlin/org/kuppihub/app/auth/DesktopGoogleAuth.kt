package org.kuppihub.app.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.KuppiUser
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Properties

class DesktopGoogleAuth {

    private val properties = Properties().apply {
        try {
            val stream = DesktopGoogleAuth::class.java.getResourceAsStream("/secrets.properties")
            if (stream != null) load(stream)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private val CLIENT_ID = properties.getProperty("DESKTOP_GOOGLE_CLIENT_ID") ?: ""
    private val CLIENT_SECRET = properties.getProperty("DESKTOP_GOOGLE_CLIENT_SECRET") ?: ""
    private val FIREBASE_API_KEY = properties.getProperty("FIREBASE_WEB_API_KEY") ?: ""

    private val REDIRECT_URI = "http://localhost:5000"
    private val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
    private val SCOPE = "email profile openid"

    suspend fun signIn(): String? = withContext(Dispatchers.IO) {
        val result = CompletableDeferred<String?>()
        val server = embeddedServer(Netty, port = 5000) {
            routing {
                get("/") {
                    val code = call.request.queryParameters["code"]
                    if (code != null) {
                        call.respondText("Login Successful! You can close this tab.")
                        result.complete(code)
                    } else {
                        call.respondText("Login Failed.")
                        result.complete(null)
                    }
                }
            }
        }.start(wait = false)

        try {
            val loginUrl = "$AUTH_URL?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&response_type=code&scope=${urlEncode(SCOPE)}"
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(loginUrl))
            return@withContext result.await()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            server.stop(1000, 2000)
        }
    }

    suspend fun getRealUser(code: String): KuppiUser? {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true // Important for returnSecureToken
                })
            }
        }

        try {
            println("🔄 Exchanging Code for Google Token...")
            val googleToken: GoogleTokenResponse = client.post("https://oauth2.googleapis.com/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "code" to code,
                        "client_id" to CLIENT_ID,
                        "client_secret" to CLIENT_SECRET,
                        "redirect_uri" to REDIRECT_URI,
                        "grant_type" to "authorization_code"
                    ).formUrlEncode()
                )
            }.body()

            println("🔄 Exchanging Google ID Token for Firebase UID...")

            val firebaseResponse: FirebaseSignInResponse = client.post(
                "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$FIREBASE_API_KEY"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    FirebaseSignInRequest(
                        postBody = "id_token=${googleToken.id_token}&providerId=google.com",
                        requestUri = REDIRECT_URI
                    )
                )
            }.body()

            val firebaseUid = firebaseResponse.localId
            println("✅ Got Firebase UID: $firebaseUid")

            return KuppiUser(
                id = firebaseUid,
                email = firebaseResponse.email,
                name = firebaseResponse.displayName ?: "User",
                photoUrl = firebaseResponse.photoUrl,
                idToken = firebaseResponse.idToken,
                refreshToken = firebaseResponse.refreshToken // ✅ Now this will work
            )

        } catch (e: Exception) {
            println("❌ Auth Failed: ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            client.close()
        }
    }

    suspend fun refreshToken(oldUser: KuppiUser): KuppiUser? {
        if (oldUser.refreshToken == null) return null

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        return try {
            val response: RefreshTokenResponse = client.post("https://securetoken.googleapis.com/v1/token?key=$FIREBASE_API_KEY") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=refresh_token&refresh_token=${oldUser.refreshToken}")
            }.body()

            println("🔄 Token Refreshed Successfully!")
            oldUser.copy(idToken = response.id_token, refreshToken = response.refresh_token)
        } catch (e: Exception) {
            println("❌ Failed to refresh token: ${e.message}")
            null
        } finally {
            client.close()
        }
    }

    private fun urlEncode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
}

// --- CORRECTED DATA CLASSES ---

@Serializable
data class GoogleTokenResponse(
    val access_token: String,
    val id_token: String,
    val expires_in: Int
)

// 1. Request: REMOVED 'refreshToken' (We don't send it here)
@Serializable
data class FirebaseSignInRequest(
    val postBody: String,
    val requestUri: String,
    val returnIdpCredential: Boolean = true,
    val returnSecureToken: Boolean = true
)

// 2. Response: ADDED 'refreshToken' (We receive it here!)
@Serializable
data class FirebaseSignInResponse(
    val localId: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val idToken: String,
    val refreshToken: String // 👈 Added this so we can save it
)

@Serializable
data class RefreshTokenResponse(
    val id_token: String,
    val refresh_token: String,
    val user_id: String
)