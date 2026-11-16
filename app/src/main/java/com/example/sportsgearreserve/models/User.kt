package com.example.sportsgearreserve.models

import java.io.Serializable

data class User(
    val userId: String,
    val prefix: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String,
    val studentId: String? = null,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    val fullName: String
        get() {
            val parts = listOf(prefix, firstName, middleName, lastName).filter { it.isNotBlank() }
            return if (parts.isNotEmpty()) parts.joinToString(" ") else email
        }
}
