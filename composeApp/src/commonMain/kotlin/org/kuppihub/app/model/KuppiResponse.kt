package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KuppiResponse(
    val id: Int,
    val title: String,
    val description: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("language_code")
    val languageCode: String? = null,

    @SerialName("is_kuppi")
    val isKuppi: Boolean = false,

    @SerialName("youtube_links")
    val youtubeLinks: List<String>? = emptyList(),

    @SerialName("telegram_links")
    val telegramLinks: List<String>? = emptyList(),

    @SerialName("material_urls")
    val materialLinks: List<String>? = emptyList(),

    @SerialName("onedrive_cloud_video_urls")
    val onedriveLinks: List<String>? = emptyList(),

    @SerialName("gdrive_cloud_video_urls")
    val gdriveLinks: List<String>? = emptyList(),

    val owner: Owner? = null
)

@Serializable
data class Owner(
    val name: String
)