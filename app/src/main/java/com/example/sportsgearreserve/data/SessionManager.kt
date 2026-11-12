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
            putString("FIRST_NAME", user.firstName)
            putString("LAST_NAME", user.lastName)
            putString("FULL_NAME", user.fullName)
            putString("EMAIL", user.email)
            putString("STUDENT_ID", user.studentId)
            putString("ROLE", user.role.name)
            apply()
        }
        addRegisteredUser(user)
    }

    fun addRegisteredUser(user: User) {
        val set = prefs.getStringSet("REGISTERED_USERS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(userToString(user))
        prefs.edit().putStringSet("REGISTERED_USERS_SET", set).apply()
        SportsRepository.ensureUserExists(user)
    }

    fun syncRegisteredUsersWithRepository() {
        val set = prefs.getStringSet("REGISTERED_USERS_SET", emptySet()) ?: emptySet()
        for (str in set) {
            stringToUser(str)?.let { user ->
                SportsRepository.ensureUserExists(user)
            }
        }
        getUser()?.let { activeUser ->
            SportsRepository.ensureUserExists(activeUser)
        }
    }

    fun getUser(): User? {
        val userId = prefs.getString("USER_ID", null) ?: return null
        val fullName = prefs.getString("FULL_NAME", "") ?: ""
        val firstName = prefs.getString("FIRST_NAME", fullName.split(" ").firstOrNull() ?: "") ?: ""
        val lastName = prefs.getString("LAST_NAME", fullName.split(" ").drop(1).joinToString(" ")) ?: ""
        val email = prefs.getString("EMAIL", "") ?: ""
        val studentId = prefs.getString("STUDENT_ID", null)
        val roleStr = prefs.getString("ROLE", UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }
        return User(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            studentId = studentId,
            role = role
        )
    }

    fun isLoggedIn(): Boolean = prefs.getString("USER_ID", null) != null

    fun logout() {
        prefs.edit().remove("USER_ID").remove("FIRST_NAME").remove("LAST_NAME")
            .remove("FULL_NAME").remove("EMAIL").remove("STUDENT_ID").remove("ROLE").apply()
    }

    private fun userToString(user: User): String =
        "${user.userId}|${user.firstName}|${user.lastName}|${user.email}|${user.studentId ?: ""}|${user.role.name}|${user.createdAt}"

    private fun stringToUser(str: String): User? {
        val parts = str.split("|")
        if (parts.size < 7) return null
        return User(
            userId = parts[0],
            firstName = parts[1],
            lastName = parts[2],
            email = parts[3],
            studentId = parts[4].ifBlank { null },
            role = try { UserRole.valueOf(parts[5]) } catch (e: Exception) { UserRole.STUDENT },
            createdAt = parts[6].toLongOrNull() ?: System.currentTimeMillis()
        )
    }
}
