package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TutorResponse(
    val students: List<Tutor>
)

@Serializable
data class Tutor(
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String?,
    @SerialName("linkedin_url") val linkedinUrl: String?,
    val faculty: String?,
    val department: String?,
    @SerialName("video_count") val videoCount: Int,
    @SerialName("modules_done") val modulesDone: List<String>
)