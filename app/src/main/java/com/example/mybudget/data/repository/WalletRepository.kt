package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val collection
        get() = firestore.collection("users").document(userId).collection("wallets")

    fun getAllWallets(): Flow<List<Wallet>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val wallets = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Wallet::class.java)
            } ?: emptyList()
            trySend(wallets)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getWalletById(id: Long): Wallet? {
        return try {
            val query = collection.whereEqualTo("id", id).get().await()
            if (query.isEmpty) null else query.documents[0].toObject(Wallet::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getWalletByIdAsFlow(id: Long): Flow<Wallet?> = callbackFlow {
        val subscription = collection.whereEqualTo("id", id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val wallet = if (snapshot != null && !snapshot.isEmpty) {
                snapshot.documents[0].toObject(Wallet::class.java)
            } else {
                null
            }
            trySend(wallet)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addWallet(name: String, startingBalance: Double, iconId: Int?) {
        val id = System.currentTimeMillis()
        val wallet = Wallet(
            id = id,
            name = name,
            startingBalance = startingBalance,
            currentBalance = startingBalance,
            iconId = iconId
        )
        // Store the document with the Long ID converted to string as the document ID
        collection.document(id.toString()).set(wallet).await()
    }

    suspend fun updateWallet(wallet: Wallet) {
        collection.document(wallet.id.toString()).set(wallet).await()
    }

    suspend fun deleteWallet(wallet: Wallet) {
        collection.document(wallet.id.toString()).delete().await()
    }
}
