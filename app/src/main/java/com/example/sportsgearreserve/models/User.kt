package com.example.sportsgearreserve.models

import java.io.Serializable

data class User(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val studentId: String? = null,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    val fullName: String
        get() = "$firstName $lastName".trim()
}
