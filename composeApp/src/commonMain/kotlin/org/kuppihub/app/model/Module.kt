package org.kuppihub.app.model

import kotlinx.serialization.Serializable

// The outer object in the list
@Serializable
data class ModuleResponse(
    val module_id: Int,
    val video_count: Int,
    val module: ModuleDetails
)

// The inner details
@Serializable
data class ModuleDetails(
    val id: Int,
    val code: String, // e.g. "CS1040"
    val name: String, // e.g. "Program Construction"
    val description: String? = null
)