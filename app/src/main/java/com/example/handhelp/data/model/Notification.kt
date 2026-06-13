package com.example.handhelp.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",           // destinataire
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val missionId: String = "",        // lien vers mission si applicable
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    MISSION_JOINED,       // un bénévole vient de rejoindre ta mission
    MISSION_FULL,         // ta mission est complète
    MISSION_REMINDER,     // rappel avant une mission
    MISSION_COMPLETED,    // mission marquée terminée
    NEW_MISSION,          // nouvelle mission disponible (bénévole)
    GENERAL               // notification générale
}