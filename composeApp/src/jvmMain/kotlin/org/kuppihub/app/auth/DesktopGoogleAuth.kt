package org.kuppihub.app.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Properties

class DesktopGoogleAuth {
    private val properties = Properties().apply {
        try {
            // This looks for "secrets.properties" inside the built JAR/Resources
            val stream = DesktopGoogleAuth::class.java.getResourceAsStream("/secrets.properties")
            if (stream != null) {
                load(stream)
            } else {
                println("⚠️ WARNING: secrets.properties not found! Login will fail.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val CLIENT_ID = properties.getProperty("DESKTOP_GOOGLE_CLIENT_ID") ?: ""
    private val CLIENT_SECRET = properties.getProperty("DESKTOP_GOOGLE_CLIENT_SECRET") ?: ""


    private val REDIRECT_URI = "http://localhost:5000"
    private val SCOPE = "email profile openid"
    private val AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"

    suspend fun signIn(): String? = withContext(Dispatchers.IO) {
        val result = CompletableDeferred<String?>()

        val server = embeddedServer(Netty, port = 5000) {
            routing {
                get("/") {
                    val code = call.request.queryParameters["code"]
                    if (code != null) {
                        call.respondText("Login Successful! You can close this tab and return to the app.")
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

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(loginUrl))
            } else {
                println("Desktop browsing not supported! Open: $loginUrl")
            }

            return@withContext result.await()

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            server.stop(1000, 2000)
        }
    }

    suspend fun exchangeCodeForToken(code: String): String? {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        try {
            val response: HttpResponse = client.post("https://oauth2.googleapis.com/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "code" to code,
                        "client_id" to CLIENT_ID,     // 👈 Uses the file value
                        "client_secret" to CLIENT_SECRET, // 👈 Uses the file value (Safe!)
                        "redirect_uri" to REDIRECT_URI,
                        "grant_type" to "authorization_code"
                    ).formUrlEncode()
                )
            }

            if (response.status.value == 200) {
                return "User Logged In"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client.close()
        }
        return null
    }

    private fun urlEncode(s: String): String {
        return URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
    }
}