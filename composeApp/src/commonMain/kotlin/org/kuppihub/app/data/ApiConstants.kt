package org.kuppihub.app.data

object ApiConstants {
    const val BASE_URL = "https://kuppihub.org/api"
    
    // Endpoints
    const val HIERARCHY = "$BASE_URL/hierarchy"
    const val MODULES_BY_IDS = "$BASE_URL/modules-by-ids"
    const val DASHBOARD_MODULES = "$BASE_URL/dashboard-modules"
    const val KUPPIS = "$BASE_URL/kuppis"
    const val SEARCH_MODULES = "$BASE_URL/search-modules"
    const val USERS = "$BASE_URL/users"
    const val USER_DASHBOARD = "$BASE_URL/user-dashboard"
    const val TUTORS = "$BASE_URL/tutors"
    const val ADD_KUPPI = "$BASE_URL/add-kuppi"
    
    // Notifications
    const val NOTIFICATIONS_BASE = "$BASE_URL/notifications"
    const val NOTIFICATION_DEVICES = "$NOTIFICATIONS_BASE/devices"
    
    // GitHub Updates
    const val GITHUB_LATEST_RELEASE = "https://api.github.com/repos/Kuppihub/Kuppihub-APP/releases/latest"
    
    fun getMarkAsReadUrl(id: Int) = "$NOTIFICATIONS_BASE/$id/read"
}
