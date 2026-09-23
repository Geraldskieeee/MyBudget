package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Bill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val collection
        get() = firestore.collection("users").document(userId).collection("bills")

    fun getAllBills(): Flow<List<Bill>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val bills = snapshot?.documents?.mapNotNull { it.toObject(Bill::class.java) } ?: emptyList()
            trySend(bills)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addBill(bill: Bill) {
        val id = if (bill.id == 0L) System.currentTimeMillis() else bill.id
        val newBill = bill.copy(id = id)
        collection.document(id.toString()).set(newBill).await()
    }

    suspend fun updateBill(bill: Bill) {
        collection.document(bill.id.toString()).set(bill).await()
    }

    suspend fun deleteBill(bill: Bill) {
        collection.document(bill.id.toString()).delete().await()
    }
}
