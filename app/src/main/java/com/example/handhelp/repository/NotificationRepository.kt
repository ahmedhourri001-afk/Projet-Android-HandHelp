package com.example.handhelp.repository

import com.example.handhelp.data.model.Notification
import com.example.handhelp.data.model.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    // ─────────────────────────────────────────
    // READ — Notifications d'un utilisateur (temps réel)
    // ─────────────────────────────────────────
    fun getUserNotifications(userId: String): Flow<Result<List<Notification>>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject(Notification::class.java)
                } ?: emptyList()
                trySend(Result.success(notifications))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Nombre de non-lues
    // ─────────────────────────────────────────
    fun getUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // CREATE — Créer une notification Firestore
    // ─────────────────────────────────────────
    suspend fun createNotification(notification: Notification): Result<Unit> {
        return try {
            val docRef = notificationsCollection.document()
            val notifWithId = notification.copy(id = docRef.id)
            docRef.set(notifWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Marquer une notification comme lue
    // ─────────────────────────────────────────
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Marquer TOUTES comme lues
    // ─────────────────────────────────────────
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val unread = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unread.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // DELETE — Supprimer une notification
    // ─────────────────────────────────────────
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // DELETE — Supprimer toutes les notifications
    // ─────────────────────────────────────────
    suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        return try {
            val all = notificationsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val batch = firestore.batch()
            all.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // Sauvegarder le token FCM de l'utilisateur
    // ─────────────────────────────────────────
    suspend fun saveFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // Envoyer une notif quand un bénévole rejoint
    // (appelé depuis MissionRepository après joinMission)
    // ─────────────────────────────────────────
    suspend fun notifyOrganizerVolunteerJoined(
        organizerId: String,
        volunteerName: String,
        missionTitle: String,
        missionId: String
    ): Result<Unit> {
        val notification = Notification(
            userId = organizerId,
            title = "Nouveau bénévole ! 🎉",
            body = "$volunteerName vient de rejoindre votre mission \"$missionTitle\"",
            type = NotificationType.MISSION_JOINED,
            missionId = missionId
        )
        return createNotification(notification)
    }

    // ─────────────────────────────────────────
    // Envoyer une notif quand la mission est complète
    // ─────────────────────────────────────────
    suspend fun notifyMissionFull(
        organizerId: String,
        missionTitle: String,
        missionId: String
    ): Result<Unit> {
        val notification = Notification(
            userId = organizerId,
            title = "Mission complète ! ✅",
            body = "Votre mission \"$missionTitle\" a atteint le nombre maximum de bénévoles.",
            type = NotificationType.MISSION_FULL,
            missionId = missionId
        )
        return createNotification(notification)
    }

    // ─────────────────────────────────────────
    // Notifier les bénévoles d'une nouvelle mission
    // ─────────────────────────────────────────
    suspend fun notifyNewMission(
        participantIds: List<String>,
        missionTitle: String,
        missionId: String
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            participantIds.forEach { userId ->
                val docRef = notificationsCollection.document()
                val notification = Notification(
                    id = docRef.id,
                    userId = userId,
                    title = "Nouvelle mission disponible 🌟",
                    body = "Une nouvelle mission \"$missionTitle\" vient d'être publiée !",
                    type = NotificationType.NEW_MISSION,
                    missionId = missionId
                )
                batch.set(docRef, notification)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}