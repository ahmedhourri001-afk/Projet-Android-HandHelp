package com.example.handhelp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    // ─────────────────────────────────────────
    // UPDATE — Informations du profil (nom, téléphone, bio)
    // ─────────────────────────────────────────
    suspend fun updateProfile(
        uid: String,
        displayName: String,
        phone: String,
        bio: String
    ): Result<Unit> {
        return try {
            // 1. Mise à jour Firestore
            firestore.collection("users").document(uid)
                .update(
                    mapOf(
                        "displayName" to displayName,
                        "phone" to phone,
                        "bio" to bio
                    )
                ).await()

            // 2. Mise à jour Firebase Auth (displayName)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}