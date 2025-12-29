package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.Department
import org.kuppihub.app.model.Faculty

object KuppiRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

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
}