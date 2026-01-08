package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class NotificationRepository(private val client: HttpClient) {

    suspend fun registerDevice(token: String, firebaseUid: String?, idToken: String?) {
        try {
            val payload = RegisterDeviceRequest(
                fcm_token = token,
                firebase_uid = firebaseUid,
                device_type = getDeviceType()
            )

            client.post(ApiConstants.NOTIFICATION_DEVICES) {
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

    suspend fun getNotifications(page: Int = 1, idToken: String): List<NotificationItem> {
        return try {
            val response = client.get(ApiConstants.NOTIFICATIONS_BASE) {
                parameter("page", page)
                if (idToken.isNotEmpty()) {
                    header("Authorization", "Bearer $idToken")
                }
            }.body<NotificationResponse>()

            response.data
        } catch (e: Exception) {
            println("❌ Failed to fetch notifications: ${e.message}")
            emptyList()
        }
    }

    // Mark as Read
    suspend fun markAsRead(notificationId: Int, idToken: String): Boolean {
        return try {
            val response = client.put(ApiConstants.getMarkAsReadUrl(notificationId)) {
                header("Authorization", "Bearer $idToken")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("❌ Failed to mark notification as read: ${e.message}")
            false
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