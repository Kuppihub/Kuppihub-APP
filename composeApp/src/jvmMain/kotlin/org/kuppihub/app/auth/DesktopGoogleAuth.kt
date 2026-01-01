package org.kuppihub.app.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.KuppiUser
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Properties

class DesktopGoogleAuth {

    // Load Secrets
    private val properties = Properties().apply {
        try {
            val stream = DesktopGoogleAuth::class.java.getResourceAsStream("/secrets.properties")
            if (stream != null) load(stream)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private val CLIENT_ID = properties.getProperty("DESKTOP_GOOGLE_CLIENT_ID") ?: ""
    private val CLIENT_SECRET = properties.getProperty("DESKTOP_GOOGLE_CLIENT_SECRET") ?: ""
    private val REDIRECT_URI = "http://localhost:5000"
    private val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
    private val SCOPE = "email profile openid"

    // 1. Get the Auth Code (Opens Browser)
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

    // 2. NEW: Exchange Code for Real User Data
    suspend fun getRealUser(code: String): KuppiUser? {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            // A. Get Access Token
            val tokenResponse: GoogleTokenResponse = client.post("https://oauth2.googleapis.com/token") {
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

            // B. Get User Profile using the Token
            val userInfo: GoogleUserInfo = client.get("https://www.googleapis.com/oauth2/v2/userinfo") {
                header("Authorization", "Bearer ${tokenResponse.access_token}")
            }.body()

            // C. Return Real Data
            return KuppiUser(
                name = userInfo.name,
                email = userInfo.email,
                photoUrl = userInfo.picture
            )

        } catch (e: Exception) {
            println("❌ Failed to fetch user: ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            client.close()
        }
    }

    private fun urlEncode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
}

// Helper Classes to parse Google JSON
@Serializable
data class GoogleTokenResponse(val access_token: String, val id_token: String, val expires_in: Int)

@Serializable
data class GoogleUserInfo(val id: String, val email: String, val name: String, val picture: String? = null)