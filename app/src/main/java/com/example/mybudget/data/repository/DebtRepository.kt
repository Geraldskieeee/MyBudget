package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Debt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val collection
        get() = firestore.collection("users").document(userId).collection("debts")

    fun getAllDebts(): Flow<List<Debt>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val debts = snapshot?.documents?.mapNotNull { it.toObject(Debt::class.java) } ?: emptyList()
            trySend(debts)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addDebt(debt: Debt) {
        val id = if (debt.id == 0L) System.currentTimeMillis() else debt.id
        val newDebt = debt.copy(id = id)
        collection.document(id.toString()).set(newDebt).await()
    }

    suspend fun updateDebt(debt: Debt) {
        collection.document(debt.id.toString()).set(debt).await()
    }

    suspend fun deleteDebt(debt: Debt) {
        collection.document(debt.id.toString()).delete().await()
    }
}
