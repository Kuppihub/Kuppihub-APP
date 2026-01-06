package org.kuppihub.app.model

import kotlinx.serialization.Serializable

// This represents one row in your local database
@Serializable
data class UserModuleEntity(
    val moduleId: Int,
    val code: String,       // e.g. "CS1040"
    val name: String,       // e.g. "Program Construction"
    val videoCount: Int,
    val isSynced: Boolean = true // True = Saved to server, False = Waiting to upload
)

// Helper: Convert Network Model -> Local Database Model
fun ModuleDetails.toEntity(videoCount: Int): UserModuleEntity {
    return UserModuleEntity(
        moduleId = this.id,
        code = this.code,
        name = this.name,
        videoCount = videoCount,
        isSynced = true
    )
}