package org.kuppihub.app.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchApiResponse(
    val data: List<SearchModuleItem>
)

@Serializable
data class SearchModuleItem(
    val id: Int,
    val code: String,
    val name: String,
    val video_count: Int
) {
    // Helper to convert this search result into the format your Local DB saves
    fun toModuleResponse(): ModuleResponse {
        return ModuleResponse(
            // 1. Pass the 'id' as 'module_id' (Required by your ModuleResponse class)
            module_id = id,

            // 2. Use 'ModuleDetails' instead of 'Module'
            module = ModuleDetails(
                id = id,
                code = code,
                name = name,
                description = null // Search API doesn't return description, so it's null
            ),

            // 3. Pass video count
            video_count = video_count
        )
    }
}