package org.kuppihub.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.KuppiResponse
import org.kuppihub.app.model.ModuleResponse

object LocalDashboardRepo {
    // Keys for storage
    private const val KEY_MODULES = "dashboard_modules"

    // Initialize Settings (Works on Android & iOS)
    private val settings: Settings = Settings()

    // Setup JSON formatter (Safe mode)
    private val json = Json { ignoreUnknownKeys = true }

    // ==========================================
    // 📦 PART 1: MODULES (Dashboard)
    // ==========================================

    // 1. Get all Saved Modules
    fun getSavedModules(): List<ModuleResponse> {
        val jsonString = settings.getStringOrNull(KEY_MODULES)

        return if (!jsonString.isNullOrBlank()) {
            try {
                json.decodeFromString<List<ModuleResponse>>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // 2. Add a Module
    fun addModule(module: ModuleResponse) {
        val currentList = getSavedModules().toMutableList()

        // Only add if it doesn't exist yet
        if (currentList.none { it.module.id == module.module.id }) {
            currentList.add(module)
            saveModuleList(currentList)
        }
    }

    // 3. Remove a Module
    fun removeModule(moduleId: Int) {
        val currentList = getSavedModules().toMutableList()
        currentList.removeAll { it.module.id == moduleId }
        saveModuleList(currentList)
    }

    // Helper: Save the module list to disk
    fun saveModuleList(list: List<ModuleResponse>) {
        val jsonString = json.encodeToString(list)
        settings[KEY_MODULES] = jsonString
    }

    // ==========================================
    // 📺 PART 2: KUPPI VIDEOS (Offline Support)
    // ==========================================

    // 1. Get Cached Videos (Reads from Disk)
    fun getCachedKuppis(moduleId: Int): List<KuppiResponse> {
        val key = "videos_$moduleId"
        val jsonString = settings.getStringOrNull(key)

        println("DEBUG_REPO: Reading Key [$key]")

        if (jsonString.isNullOrBlank()) {
            println("DEBUG_REPO: Key [$key] is EMPTY.")
            return emptyList()
        }

        return try {
            val list = json.decodeFromString<List<KuppiResponse>>(jsonString)
            println("DEBUG_REPO: Success! Loaded ${list.size} videos from disk.")
            list
        } catch (e: Exception) {
            println("DEBUG_REPO: CRASH while reading! Error: ${e.message}")
            e.printStackTrace() // Print the real error to Logcat
            emptyList()
        }
    }

    // 2. Save Videos to Disk
    fun saveKuppis(moduleId: Int, videos: List<KuppiResponse>) {
        val key = "videos_$moduleId"
        try {
            val jsonString = json.encodeToString(videos)
            settings[key] = jsonString
            println("DEBUG_REPO: Saved ${videos.size} videos to [$key]. Text length: ${jsonString.length}")
        } catch (e: Exception) {
            println("DEBUG_REPO: CRASH while saving! Error: ${e.message}")
            e.printStackTrace()
        }
    }
}