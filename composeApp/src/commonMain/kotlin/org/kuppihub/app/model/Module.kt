package org.kuppihub.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The outer object in the list
@Serializable
data class ModuleResponse(
    val module_id: Int,

    val module: ModuleDetails,
    @SerialName("video_count") // 👈 THIS IS THE FIX
    val video_count: Int       // Now it knows to read "video_count" from JSON


)



// The inner details
@Serializable
data class ModuleDetails(
    val id: Int,
    val code: String, // e.g. "CS1040"
    val name: String, // e.g. "Program Construction"
    val description: String? = null
)