package com.example.handhelp.data.model

data class Mission(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val volunteersNeeded: Int = 0,
    val volunteersEnrolled: Int = 0,
    val organizerId: String = "",
    val organizerName: String = "",
    val imageUrl: String = "",
    val tags: List<String> = emptyList(),
    val status: MissionStatus = MissionStatus.ACTIVE,
    val participants: List<String> = emptyList(), // liste des UIDs inscrits
    val createdAt: Long = System.currentTimeMillis()
)

enum class MissionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}