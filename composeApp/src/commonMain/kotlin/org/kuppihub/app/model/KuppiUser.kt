package org.kuppihub.app.model

data class KuppiUser(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    var idToken: String,
    val refreshToken: String? = null
)