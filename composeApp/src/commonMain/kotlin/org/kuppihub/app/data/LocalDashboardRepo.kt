package org.kuppihub.app.data

import com.russhwolf.settings.Settings
// We don't need 'import com.russhwolf.settings.set' if we use explicit putString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.KuppiResponse
import org.kuppihub.app.model.ModuleResponse

object LocalDashboardRepo {
    private const val KEY_MODULES = "dashboard_modules"

    // Initialize Settings (Works on Android & iOS)
    private val settings: Settings = Settings()
    private val videoCache = mutableMapOf<Int, List<KuppiResponse>>()

    fun getCachedKuppis(moduleId: Int): List<KuppiResponse> {
        return videoCache[moduleId] ?: emptyList()
    }

    fun saveKuppis(moduleId: Int, videos: List<KuppiResponse>) {
        videoCache[moduleId] = videos
        // TODO: Save 'videoCache' to a file/database here so it works after app restart
    }

    // 1. Get all Saved Modules
    fun getSavedModules(): List<ModuleResponse> {
        val jsonString = settings.getString(KEY_MODULES, "")

        if (jsonString.isBlank()) return emptyList()

        return try {
            // FIX: Explicitly tell it to decode a List<ModuleResponse>
            Json.decodeFromString<List<ModuleResponse>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 2. Add a Module
    fun addModule(module: ModuleResponse) {
        val currentList = getSavedModules().toMutableList()

        // Prevent duplicates (Check by ID)
        if (currentList.none { it.module.id == module.module.id }) {
            currentList.add(module)
            saveList(currentList)
        }
    }

    // 3. Remove a Module
    fun removeModule(moduleId: Int) {
        val currentList = getSavedModules().toMutableList()

        // Remove items that match the ID
        currentList.removeAll { it.module.id == moduleId }

        saveList(currentList)
    }

    // Helper: Save the list to local storage
    private fun saveList(list: List<ModuleResponse>) {
        val jsonString = Json.encodeToString(list)

        // FIX: Use explicit 'putString' instead of 'settings[] =' to avoid inference errors
        settings.putString(KEY_MODULES, jsonString)
    }
}