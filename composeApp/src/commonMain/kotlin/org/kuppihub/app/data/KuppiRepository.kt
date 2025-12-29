package org.kuppihub.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.kuppihub.app.model.Faculty

class KuppiRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    // The API returns a JSON Object where keys are strings ("it", "engineering")
    // and values are the Faculty objects.
    suspend fun getFaculties(): List<Faculty> {
        val responseMap: Map<String, Faculty> = client
            .get("https://kuppihub.org/api/hierarchy")
            .body()

        // 1. Convert the Map values to a List
        // 2. Sort by the 'order' field (1, 2, 3...)
        return responseMap.values.sortedBy { it.order }
    }
}