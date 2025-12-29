package org.kuppihub.app.model

import kotlinx.serialization.Serializable

// 1. The Top Level (e.g., "Faculty of Engineering")
@Serializable
data class Faculty(
    val name: String,
    val order: Int,
    val levels: List<String>, // e.g., ["Department", "Semester"]
    // The API uses keys like "cse", "ent", so we use a Map
    val children: Map<String, Department> = emptyMap()
)

// 2. The Second Level (e.g., "Computer Science & Engineering")
@Serializable
data class Department(
    val name: String,
    val order: Int,
    // The API uses keys like "s1", "s2"
    val children: Map<String, Semester> = emptyMap()
)

// 3. The Bottom Level (e.g., "Semester 2")
@Serializable
data class Semester(
    val name: String,
    val order: Int,
    // The list of Module IDs (e.g., [28, 29, 30])
    val modules: List<Int> = emptyList()
)