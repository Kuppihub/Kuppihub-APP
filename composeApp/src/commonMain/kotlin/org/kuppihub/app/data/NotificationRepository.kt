package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class NotificationRepository(private val client: HttpClient) {

    private val BASE_URL = "https://kuppihub.org/api/notifications" // Removed double slash

    // 🔴 CHANGED: Added 'idToken' parameter so we can send the Header
    suspend fun registerDevice(token: String, firebaseUid: String?, idToken: String?) {
        try {
            val payload = RegisterDeviceRequest(
                fcm_token = token,
                firebase_uid = firebaseUid, // Send String here
                device_type = getDeviceType()
            )

            client.post("$BASE_URL/devices") {
                contentType(ContentType.Application.Json)
                // 🔴 CHANGED: Add Auth Header if we have a token
                if (idToken != null && idToken.isNotEmpty()) {
                    header("Authorization", "Bearer $idToken")
                }
                setBody(payload)
            }
            println("✅ Device Registration Request Sent")
        } catch (e: Exception) {
            println("❌ Failed to register device: ${e.message}")
        }
    }

    // ... getNotifications remains the same ...
}

expect fun getDeviceType(): String

@Serializable
data class RegisterDeviceRequest(
    val fcm_token: String,
    // 🔴 CHANGED: Renamed to match the new Backend expectation
    val firebase_uid: String?,
    val device_type: String
)