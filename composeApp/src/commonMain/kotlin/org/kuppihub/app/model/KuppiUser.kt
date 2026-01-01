package org.kuppihub.app.model

data class KuppiUser(
    val name: String,
    val email: String,
    val photoUrl: String? = null
)