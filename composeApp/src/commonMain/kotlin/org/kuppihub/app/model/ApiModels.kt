package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. Payload to Sync User
@Serializable
data class SyncUserRequest(
    @SerialName("firebase_uid") val firebaseUid: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("photo_url") val photoUrl: String?,
    @SerialName("is_verified") val isVerified: Boolean = true,
    @SerialName("auth_provider") val authProvider: String = "google"
)

// 2. Response for Dashboard IDs (GET)
@Serializable
data class DashboardIdsResponse(
    val moduleIds: List<Int>
)

// 3. Payload to Update Dashboard (POST)
@Serializable
data class UpdateDashboardRequest(
    @SerialName("firebase_uid") val firebaseUid: String,
    val moduleIds: List<Int>
)