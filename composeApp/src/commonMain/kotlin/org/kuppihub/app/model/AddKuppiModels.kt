package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddKuppiRequest(
    @SerialName("module_id")
    val moduleId: Int,

    val title: String,
    val description: String,

    @SerialName("language_code")
    val languageCode: String,

    @SerialName("index_no")
    val indexNo: String = "N/A", // The backend should overwrite this from the Token

    @SerialName("is_kuppi")
    val isKuppi: Boolean = true,

    @SerialName("youtube_links")
    val youtubeLinks: List<String>? = null,

    @SerialName("telegram_links")
    val telegramLinks: List<String>? = null,

    // These names must match your sample JSON exactly:
    @SerialName("gdrive_cloud_video_urls")
    val gdriveLinks: List<String>? = null,

    @SerialName("onedrive_cloud_video_urls")
    val onedriveLinks: List<String>? = null,

    @SerialName("material_urls")
    val materialLinks: List<String>? = null,

    @SerialName("allowed_domains")
    val allowedDomains: List<String>? = null,

    // These fields are NOT in your sample JSON, so we shouldn't send them
    // or we should mark them as Transient/Optional if the UI uses them but API doesn't.
    // I removed 'videoUrl', 'mapUrl', 'addedBy' as they were causing issues before.
)