package com.chemscanner.omniscient.marrow.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOVEREIGN USER REPOSITORY v2.0
 * Migrated to MARROW for SDK independence.
 */
@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun login(email: String, password: String): AuthResult {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String): AuthResult {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun addScanToUserHistory(userId: String, scanId: Long) {
        val database = FirebaseDatabase.getInstance().getReference("users/$userId/scanHistory")
        database.child(scanId.toString()).setValue(System.currentTimeMillis())
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
