package com.example.handhelp.repository

import com.example.handhelp.data.model.Mission
import com.example.handhelp.data.model.MissionStatus
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
    // Référence à la collection Firestore
    private val missionsCollection = firestore.collection("missions")

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
    // READ — Toutes les missions actives (temps réel)
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
                val missions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Mission::class.java)
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
                val missions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Mission::class.java)
                } ?: emptyList()
                trySend(Result.success(missions))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Missions auxquelles un bénévole participe
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
                val missions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Mission::class.java)
                } ?: emptyList()
                trySend(Result.success(missions))
            }
        awaitClose { listener.remove() }
    }

    // ─────────────────────────────────────────
    // READ — Une mission par ID (one-shot)
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
    // UPDATE — Modifier une mission
    // ─────────────────────────────────────────
    suspend fun updateMission(mission: Mission): Result<Unit> {
        return try {
            missionsCollection.document(mission.id).set(mission).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — S'inscrire à une mission
    // ─────────────────────────────────────────
    suspend fun joinMission(missionId: String, userId: String): Result<Unit> {
        return try {
            val doc = missionsCollection.document(missionId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(doc)
                val mission = snapshot.toObject(Mission::class.java)
                    ?: throw Exception("Mission introuvable")

                if (userId in mission.participants) {
                    throw Exception("Vous êtes déjà inscrit à cette mission")
                }
                if (mission.volunteersEnrolled >= mission.volunteersNeeded) {
                    throw Exception("Cette mission est complète")
                }

                val newParticipants = mission.participants + userId
                transaction.update(doc, "participants", newParticipants)
                transaction.update(doc, "volunteersEnrolled", newParticipants.size)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Se désinscrire d'une mission
    // ─────────────────────────────────────────
    suspend fun leaveMission(missionId: String, userId: String): Result<Unit> {
        return try {
            val doc = missionsCollection.document(missionId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(doc)
                val mission = snapshot.toObject(Mission::class.java)
                    ?: throw Exception("Mission introuvable")

                val newParticipants = mission.participants - userId
                transaction.update(doc, "participants", newParticipants)
                transaction.update(doc, "volunteersEnrolled", newParticipants.size)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────
    // UPDATE — Changer le statut d'une mission
    // ─────────────────────────────────────────
    suspend fun updateMissionStatus(missionId: String, status: MissionStatus): Result<Unit> {
        return try {
            missionsCollection.document(missionId)
                .update("status", status.name)
                .await()
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
    // SEARCH — Recherche par mot-clé
    // ─────────────────────────────────────────
    suspend fun searchMissions(query: String): Result<List<Mission>> {
        return try {
            // Firestore ne supporte pas la recherche full-text nativement
            // On charge toutes les missions actives et on filtre côté client
            val snapshot = missionsCollection
                .whereEqualTo("status", MissionStatus.ACTIVE.name)
                .get()
                .await()
            val missions = snapshot.documents.mapNotNull { it.toObject(Mission::class.java) }
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