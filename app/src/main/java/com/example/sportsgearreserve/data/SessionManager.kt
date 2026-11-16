package com.example.sportsgearreserve.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sportsgearreserve.models.User
import com.example.sportsgearreserve.models.UserRole

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sports_gear_session", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString("USER_ID", user.userId)
            putString("PREFIX", user.prefix)
            putString("FIRST_NAME", user.firstName)
            putString("MIDDLE_NAME", user.middleName)
            putString("LAST_NAME", user.lastName)
            putString("FULL_NAME", user.fullName)
            putString("EMAIL", user.email)
            putString("STUDENT_ID", user.studentId)
            putString("ROLE", user.role.name)
            apply()
        }
    }

    fun getUser(): User? {
        val userId = prefs.getString("USER_ID", null) ?: return null
        val prefix = prefs.getString("PREFIX", "") ?: ""
        val firstName = prefs.getString("FIRST_NAME", "") ?: ""
        val middleName = prefs.getString("MIDDLE_NAME", "") ?: ""
        val lastName = prefs.getString("LAST_NAME", "") ?: ""
        val email = prefs.getString("EMAIL", "") ?: ""
        val studentId = prefs.getString("STUDENT_ID", null)
        val roleStr = prefs.getString("ROLE", UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }
        return User(
            userId = userId,
            prefix = prefix,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            email = email,
            studentId = studentId,
            role = role
        )
    }

    fun isLoggedIn(): Boolean = prefs.getString("USER_ID", null) != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
