package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KuppiResponse(
    val id: Int,
    val title: String,
    val description: String? = null,

    @SerialName("created_at")
    val createdAt: String, // e.g. "2025-10-03T09:37..."

    @SerialName("youtube_links")
    val youtubeLinks: List<String>? = emptyList(),

    @SerialName("telegram_links")
    val telegramLinks: List<String>? = emptyList(),

    @SerialName("material_urls")
    val materialLinks: List<String>? = emptyList(),

    val owner: Owner? = null
)

@Serializable
data class Owner(
    val name: String
)