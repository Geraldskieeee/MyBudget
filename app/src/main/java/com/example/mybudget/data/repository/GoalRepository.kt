package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val collection
        get() = firestore.collection("users").document(userId).collection("goals")

    fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val goals = snapshot?.documents?.mapNotNull { it.toObject(Goal::class.java) } ?: emptyList()
            trySend(goals)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addGoal(goal: Goal) {
        val id = if (goal.id == 0L) System.currentTimeMillis() else goal.id
        val newGoal = goal.copy(id = id)
        collection.document(id.toString()).set(newGoal).await()
    }

    suspend fun updateGoal(goal: Goal) {
        collection.document(goal.id.toString()).set(goal).await()
    }

    suspend fun deleteGoal(goal: Goal) {
        collection.document(goal.id.toString()).delete().await()
    }
}
