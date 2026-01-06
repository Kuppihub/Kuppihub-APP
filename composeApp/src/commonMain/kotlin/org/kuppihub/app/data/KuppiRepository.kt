package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.DashboardIdsResponse
import org.kuppihub.app.model.Department
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.KuppiResponse
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.model.ModuleResponse
import org.kuppihub.app.model.SearchApiResponse
import org.kuppihub.app.model.SearchModuleItem
import org.kuppihub.app.model.SyncUserRequest
import org.kuppihub.app.model.UpdateDashboardRequest

object KuppiRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private const val BASE_URL = "https://kuppihub.org/api"

    private var cachedHierarchy: Map<String, Faculty>? = null

    suspend fun getFaculties(): List<Faculty> {
        if (cachedHierarchy == null) {
            // FIX IS HERE: Added <Map<String, Faculty>>
            val response = client
                .get("https://kuppihub.org/api/hierarchy")
                .body<Map<String, Faculty>>()

            // Inject IDs
            response.forEach { (key, faculty) ->
                faculty.id = key
                faculty.children.forEach { (deptKey, dept) -> dept.id = deptKey }
            }
            cachedHierarchy = response
        }

        return cachedHierarchy!!.values.sortedBy { it.order }
    }

    suspend fun getFaculty(id: String): Faculty? {
        if (cachedHierarchy == null) getFaculties()
        return cachedHierarchy?.get(id)
    }

    suspend fun getDepartment(facultyId: String, deptId: String): Department? {
        val faculty = getFaculty(facultyId)
        return faculty?.children?.get(deptId)
    }
    suspend fun getSemester(facultyId: String, deptId: String, semId: String): org.kuppihub.app.model.Semester? {
        // First find the department
        val dept = getDepartment(facultyId, deptId)
        // Then look for the semester inside it using the ID (semId)
        return dept?.children?.get(semId)
    }


    suspend fun getModulesByIds(ids: List<Int>): List<ModuleResponse> {
        if (ids.isEmpty()) return emptyList()

        // Join the numbers into a string: "28,29,30"
        val idsString = ids.joinToString(",")

        // API Call: https://kuppihub.org/api/modules-by-ids?ids=28,29...
        return client
            .get("https://kuppihub.org/api/modules-by-ids?ids=$idsString")
            .body<List<ModuleResponse>>()
    }

    // Inside KuppiRepository object

    suspend fun getDashboardDetails(ids: List<Int>): List<ModuleResponse> {
        if (ids.isEmpty()) return emptyList()

        // Convert list [33, 34] to string "33,34"
        val idString = ids.joinToString(",")

        // Call the endpoint
        // Assuming 'client' is your Ktor HttpClient defined in the Repo
        val response: List<ModuleResponse> = client.get("https://kuppihub.org/api/dashboard-modules") {
            parameter("ids", idString)
        }.body()

        return response
    }

    // Inside KuppiRepository object

    suspend fun getKuppis(moduleId: Int): List<KuppiResponse> {
        return try {
            client.get("https://kuppihub.org/api/kuppis") {
                parameter("moduleId", moduleId)
            }.body()
        } catch (e: Exception) {
            println("Error fetching kuppis: ${e.message}")
            emptyList()
        }
    }


    // Inside KuppiRepository object

    suspend fun searchModules(query: String): List<SearchModuleItem> {
        if (query.length < 2) return emptyList() // Don't search for 1 letter

        return try {
            // Calls: https://kuppihub.org/api/search-modules?q=cs
            val response = client.get("https://kuppihub.org/api/search-modules") {
                parameter("q", query)
            }.body<SearchApiResponse>()

            response.data
        } catch (e: Exception) {
            println("Search Error: ${e.message}")
            emptyList()
        }
    }

    // 1. Sync User after Login
    suspend fun syncUserToBackend(user: KuppiUser) {
        try {
            client.post("$BASE_URL/users") {
                contentType(ContentType.Application.Json)
                setBody(
                    SyncUserRequest(
                        firebaseUid = user.id,
                        email = user.email,
                        displayName = user.name,
                        photoUrl = user.photoUrl
                    )
                )
            }
            println("✅ User Synced to Backend")
        } catch (e: Exception) {
            println("❌ Failed to sync user: ${e.message}")
        }
    }

    // 2. Get Saved Module IDs from Cloud
    suspend fun fetchCloudDashboardIds(uid: String): List<Int>? {
        return try {
            val response: DashboardIdsResponse = client.get("$BASE_URL/user-dashboard?firebase_uid=$uid").body()
            response.moduleIds
        } catch (e: Exception) {
            println("⚠️ Offline: Could not fetch cloud dashboard.")
            null // 👈 Return NULL instead of emptyList()
        }
    }



    // 3. Save Module IDs to Cloud
    suspend fun updateCloudDashboard(uid: String, moduleIds: List<Int>) {
        try {
            client.post("$BASE_URL/user-dashboard") {
                contentType(ContentType.Application.Json)
                setBody(UpdateDashboardRequest(uid, moduleIds))
            }
            println("✅ Dashboard Synced to Cloud")
        } catch (e: Exception) {
            println("❌ Failed to update dashboard: ${e.message}")
        }
    }


    suspend fun syncAndLoadDashboard(firebaseUid: String): List<ModuleResponse> {
        // A. Load Local Data
        val localModules = LocalDashboardRepo.getSavedModules()
        val localIds = localModules.map { it.module.id }.toSet()

        // B. Ask Server
        val serverIdsList = fetchCloudDashboardIds(firebaseUid)

        // 🛑 STOP: If serverIdsList is null, we are OFFLINE.
        // Just return local data and DO NOT try to push/merge.
        if (serverIdsList == null) {
            println("⚠️ Offline Mode: Skipping sync. Using local data.")
            return localModules
        }

        // If we are here, we are ONLINE. Proceed with Sync.
        val serverIds = serverIdsList.toSet()

        try {
            // C. MERGE: Combine Local + Server IDs
            val allIds = (localIds + serverIds).toList()

            if (allIds.isEmpty()) return emptyList()

            // D. SYNC UP: Only push if we actually have something NEW
            if (localIds.any { !serverIds.contains(it) }) {
                updateCloudDashboard(firebaseUid, allIds)
                println("☁️ Syncing: Pushed new local modules to server.")
            }

            // E. FETCH DETAILS & CACHE
            val freshModules = getDashboardDetails(allIds)
            LocalDashboardRepo.saveModuleList(freshModules)

            return freshModules

        } catch (e: Exception) {
            println("⚠️ Error during sync merge: ${e.message}")
            return localModules
        }
    }
    // 🗑️ DELETE LOGIC
    // Returns TRUE if synced, FALSE if offline
    suspend fun removeModule(moduleId: Int, firebaseUid: String?): Boolean {
        // 1. Always remove local first
        LocalDashboardRepo.removeModule(moduleId)

        if (firebaseUid != null) {
            return try {
                // 2. Try to sync
                val updatedList = LocalDashboardRepo.getSavedModules()
                val updatedIds = updatedList.map { it.module.id }

                updateCloudDashboard(firebaseUid, updatedIds)
                true // ✅ Success: Internet is working
            } catch (e: Exception) {
                println("⚠️ Offline Delete: ${e.message}")
                false // ❌ Fail: Internet is down (Local only)
            }
        }
        return true // Guest mode is always "Success" (Local)
    }



}