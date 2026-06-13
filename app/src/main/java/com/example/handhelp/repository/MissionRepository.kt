package com.example.handhelp.repository

import android.util.Log
import com.example.handhelp.data.model.Mission
import com.example.handhelp.data.model.MissionStatus
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
class MissionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val missionsCollection = firestore.collection("missions")
    private val notificationsCollection = firestore.collection("notifications")

    // ─────────────────────────────────────────
    // CREATE — Créer une nouvelle mission
    // ─────────────────────────────────────────
    suspend fun createMission(mission: Mission): Result<String> {
        return try {
            val docRef = missionsCollection.document()
            val missionWithId = mission.copy(id = docRef.id)
            docRef.set(missionWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // READ — Toutes les missions actives
    // ─────────────────────────────────────────
    fun getActiveMissions(): Flow<Result<List<Mission>>> = callbackFlow {
        val listener = missionsCollection
            .whereEqualTo("status", MissionStatus.ACTIVE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val missions = snapshot?.documents?.mapNotNull {
                    it.toObject(Mission::class.java)
                } ?: emptyList()
                trySend(Result.success(missions))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Missions d'un organisateur
    // ─────────────────────────────────────────
    fun getMissionsByOrganizer(organizerId: String): Flow<Result<List<Mission>>> = callbackFlow {
        val listener = missionsCollection
            .whereEqualTo("organizerId", organizerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val missions = snapshot?.documents?.mapNotNull {
                    it.toObject(Mission::class.java)
                } ?: emptyList()
                trySend(Result.success(missions))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Missions d'un bénévole
    // ─────────────────────────────────────────
    fun getMissionsByParticipant(userId: String): Flow<Result<List<Mission>>> = callbackFlow {
        val listener = missionsCollection
            .whereArrayContains("participants", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val missions = snapshot?.documents?.mapNotNull {
                    it.toObject(Mission::class.java)
                } ?: emptyList()
                trySend(Result.success(missions))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Mission par ID
    // ─────────────────────────────────────────
    suspend fun getMissionById(missionId: String): Result<Mission> {
        return try {
            val doc = missionsCollection.document(missionId).get().await()
            val mission = doc.toObject(Mission::class.java)
            if (mission != null) Result.success(mission)
            else Result.failure(Exception("Mission introuvable"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — S'inscrire + envoyer notification
    // ─────────────────────────────────────────
    suspend fun joinMission(missionId: String, userId: String): Result<Unit> {
        return try {
            val missionDoc = missionsCollection.document(missionId)

            // 1. Récupérer la mission
            val missionSnapshot = missionDoc.get().await()
            val mission = missionSnapshot.toObject(Mission::class.java)
                ?: return Result.failure(Exception("Mission introuvable"))

            // 2. Vérifications
            if (userId in mission.participants) {
                return Result.failure(Exception("Vous êtes déjà inscrit à cette mission"))
            }
            if (mission.volunteersEnrolled >= mission.volunteersNeeded) {
                return Result.failure(Exception("Cette mission est complète"))
            }

            // 3. Mettre à jour les participants
            val newParticipants = mission.participants + userId
            missionDoc.update(
                mapOf(
                    "participants" to newParticipants,
                    "volunteersEnrolled" to newParticipants.size
                )
            ).await()

            Log.d("MISSION", "✅ Inscription réussie pour userId=$userId")

            // 4. Récupérer le nom du bénévole
            val volunteerDoc = firestore.collection("users")
                .document(userId).get().await()
            val volunteerName = volunteerDoc.getString("displayName") ?: "Un bénévole"

            Log.d("MISSION", "👤 Bénévole : $volunteerName")
            Log.d("MISSION", "📣 Envoi notif à organisateur : ${mission.organizerId}")

            // 5. Notifier l'organisateur — inscription
            val joinNotifRef = notificationsCollection.document()
            val joinNotif = Notification(
                id = joinNotifRef.id,
                userId = mission.organizerId,
                title = "Nouveau bénévole ! 🎉",
                body = "$volunteerName vient de rejoindre votre mission \"${mission.title}\"",
                type = NotificationType.MISSION_JOINED,
                missionId = missionId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            joinNotifRef.set(joinNotif).await()
            Log.d("MISSION", "✅ Notification MISSION_JOINED créée")

            // 6. Notifier si mission complète
            if (newParticipants.size >= mission.volunteersNeeded) {
                val fullNotifRef = notificationsCollection.document()
                val fullNotif = Notification(
                    id = fullNotifRef.id,
                    userId = mission.organizerId,
                    title = "Mission complète ! ✅",
                    body = "Votre mission \"${mission.title}\" a atteint le nombre maximum de bénévoles.",
                    type = NotificationType.MISSION_FULL,
                    missionId = missionId,
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
                fullNotifRef.set(fullNotif).await()
                Log.d("MISSION", "✅ Notification MISSION_FULL créée")
            }

            // 7. Notifier le bénévole — confirmation inscription
            val confirmNotifRef = notificationsCollection.document()
            val confirmNotif = Notification(
                id = confirmNotifRef.id,
                userId = userId,
                title = "Inscription confirmée ✅",
                body = "Vous êtes inscrit à la mission \"${mission.title}\" le ${mission.date}.",
                type = NotificationType.MISSION_JOINED,
                missionId = missionId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            confirmNotifRef.set(confirmNotif).await()
            Log.d("MISSION", "✅ Notification confirmation bénévole créée")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MISSION", "❌ Erreur joinMission : ${e.message}")
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Se désinscrire
    // ─────────────────────────────────────────
    suspend fun leaveMission(missionId: String, userId: String): Result<Unit> {
        return try {
            val missionDoc = missionsCollection.document(missionId)
            val missionSnapshot = missionDoc.get().await()
            val mission = missionSnapshot.toObject(Mission::class.java)
                ?: return Result.failure(Exception("Mission introuvable"))

            val newParticipants = mission.participants - userId
            missionDoc.update(
                mapOf(
                    "participants" to newParticipants,
                    "volunteersEnrolled" to newParticipants.size
                )
            ).await()

            // Notifier l'organisateur
            val notifRef = notificationsCollection.document()
            val notif = Notification(
                id = notifRef.id,
                userId = mission.organizerId,
                title = "Désinscription d'un bénévole",
                body = "Un bénévole s'est désinscrit de votre mission \"${mission.title}\".",
                type = NotificationType.GENERAL,
                missionId = missionId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            notifRef.set(notif).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Terminer une mission
    // ─────────────────────────────────────────
    suspend fun updateMissionStatus(
        missionId: String,
        status: MissionStatus
    ): Result<Unit> {
        return try {
            val missionDoc = missionsCollection.document(missionId)
            missionDoc.update("status", status.name).await()

            // Si mission terminée → notifier tous les participants
            if (status == MissionStatus.COMPLETED) {
                val missionSnapshot = missionDoc.get().await()
                val mission = missionSnapshot.toObject(Mission::class.java)
                mission?.participants?.forEach { participantId ->
                    val notifRef = notificationsCollection.document()
                    val notif = Notification(
                        id = notifRef.id,
                        userId = participantId,
                        title = "Mission terminée 🏆",
                        body = "La mission \"${mission.title}\" est maintenant terminée. Merci pour votre engagement !",
                        type = NotificationType.MISSION_COMPLETED,
                        missionId = missionId,
                        isRead = false,
                        createdAt = System.currentTimeMillis()
                    )
                    notifRef.set(notif).await()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // DELETE — Supprimer une mission
    // ─────────────────────────────────────────
    suspend fun deleteMission(missionId: String): Result<Unit> {
        return try {
            missionsCollection.document(missionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // SEARCH — Recherche missions
    // ─────────────────────────────────────────
    suspend fun searchMissions(query: String): Result<List<Mission>> {
        return try {
            val snapshot = missionsCollection
                .whereEqualTo("status", MissionStatus.ACTIVE.name)
                .get()
                .await()
            val missions = snapshot.documents.mapNotNull {
                it.toObject(Mission::class.java)
            }
            val filtered = missions.filter { mission ->
                mission.title.contains(query, ignoreCase = true) ||
                        mission.description.contains(query, ignoreCase = true) ||
                        mission.location.contains(query, ignoreCase = true) ||
                        mission.category.contains(query, ignoreCase = true) ||
                        mission.organizerName.contains(query, ignoreCase = true)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}