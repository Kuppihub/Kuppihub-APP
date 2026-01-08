package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class NotificationRepository(private val client: HttpClient) {

    // Use localhost for Desktop if running locally, or your https URL
    private val BASE_URL = "https://kuppihub.org/api/notifications"

    suspend fun registerDevice(token: String, firebaseUid: String?, idToken: String?) {
        try {
            val payload = RegisterDeviceRequest(
                fcm_token = token,
                firebase_uid = firebaseUid,
                device_type = getDeviceType()
            )

            client.post("$BASE_URL/devices") {
                contentType(ContentType.Application.Json)
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

    // 🔴 FIXED: Now parses the JSON Object instead of a List
    suspend fun getNotifications(page: Int = 1, idToken: String): List<NotificationItem> {
        return try {
            val response = client.get(BASE_URL) {
                parameter("page", page)
                if (idToken.isNotEmpty()) {
                    header("Authorization", "Bearer $idToken")
                }
            }.body<NotificationResponse>() // 👈 Map to the Wrapper Class

            response.data // Return just the list inside 'data'
        } catch (e: Exception) {
            println("❌ Failed to fetch notifications: ${e.message}")
            emptyList()
        }
    }
}

expect fun getDeviceType(): String

@Serializable
data class RegisterDeviceRequest(
    val fcm_token: String,
    val firebase_uid: String?,
    val device_type: String
)

// 🔴 NEW WRAPPER CLASS (Matches your backend response)
@Serializable
data class NotificationResponse(
    val success: Boolean,
    val data: List<NotificationItem>
)

@Serializable
data class NotificationItem(
    val id: Int,
    val title: String,
    val body: String,
    val is_read: Boolean,
    val created_at: String? = null
)