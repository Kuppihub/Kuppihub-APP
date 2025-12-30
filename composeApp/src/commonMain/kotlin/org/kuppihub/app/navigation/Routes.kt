package org.kuppihub.app.navigation

import kotlinx.serialization.Serializable



@Serializable
object AddModulesRoute
// This represents the Login Screen
@Serializable
object LoginRoute

// This represents the Dashboard (Main) Screen
@Serializable
object DashboardRoute

@Serializable
// Represents the first level of children (Years, Departments, Programs)
data class LevelOneRoute(val facultyId: String)

@Serializable
// Represents the second level (Terms, Semesters)
data class LevelTwoRoute(val facultyId: String, val childId: String)


@Serializable
data class LevelThreeRoute(
    val facultyId: String,
    val childId: String,
    val semesterId: String
)

@Serializable
data class KuppiListRoute(val moduleId: Int, val moduleCode: String)