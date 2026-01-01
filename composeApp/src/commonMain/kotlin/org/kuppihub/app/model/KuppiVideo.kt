package org.kuppihub.app.model

import kotlinx.serialization.Serializable

@Serializable
data class KuppiVideo(
    val id: String,
    val title: String,
    val url: String, // Or youtube_id
    val duration: String? = null
)