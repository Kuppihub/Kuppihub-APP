package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.Department
import org.kuppihub.app.model.Faculty
import org.kuppihub.app.model.ModuleResponse

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

}