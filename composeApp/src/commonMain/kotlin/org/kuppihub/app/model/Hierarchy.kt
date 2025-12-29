package org.kuppihub.app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Faculty(
    val name: String,
    val order: Int,
    val levels: List<String> = emptyList(),
    val children: Map<String, Department> = emptyMap()
) {
    // We will fill this manually after fetching
    @Transient var id: String = ""
}

@Serializable
data class Department(
    val name: String,
    val order: Int,
    val children: Map<String, Semester> = emptyMap()
) {
    @Transient var id: String = ""
}

@Serializable
data class Semester(
    var id: String = "",
    val name: String,
    val order: Int,
    val modules: List<Int> = emptyList()
)