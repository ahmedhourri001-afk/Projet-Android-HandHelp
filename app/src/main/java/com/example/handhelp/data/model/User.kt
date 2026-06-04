package com.example.handhelp.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.VOLUNTEER,
    val phone: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    VOLUNTEER,
    ORGANIZER
}