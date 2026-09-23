package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
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
class TransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val userDoc
        get() = firestore.collection("users").document(userId)
        
    private val transactionsCollection
        get() = userDoc.collection("transactions")
        
    private val walletsCollection
        get() = userDoc.collection("wallets")

    fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
            trySend(items.sortedByDescending { it.dateTimestamp })
        }
        awaitClose { subscription.remove() }
    }

    fun getTransactionsByWallet(walletId: Long): Flow<List<Transaction>> = callbackFlow {
        val subscription = transactionsCollection
            .whereEqualTo("walletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(Transaction::class.java) } ?: emptyList()
                trySend(items.sortedByDescending { it.dateTimestamp })
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val id = if (transaction.id == 0L) System.currentTimeMillis() else transaction.id
        val newTx = transaction.copy(id = id)

        val amountModifier = when (newTx.type) {
            TransactionType.INCOME -> newTx.amount
            TransactionType.EXPENSE -> -newTx.amount
            TransactionType.TRANSFER -> -newTx.amount
        }

        val txDocRef = transactionsCollection.document(id.toString())
        val sourceWalletRef = walletsCollection.document(newTx.walletId.toString())
        val destWalletRef = newTx.toWalletId?.let { walletsCollection.document(it.toString()) }

        firestore.runTransaction { transactionFirestore ->
            // Update source wallet
            val sourceWalletSnapshot = transactionFirestore.get(sourceWalletRef)
            val sourceWallet = sourceWalletSnapshot.toObject(Wallet::class.java)
            if (sourceWallet != null) {
                transactionFirestore.set(sourceWalletRef, sourceWallet.copy(currentBalance = sourceWallet.currentBalance + amountModifier))
            }

            // Update destination wallet if transfer
            if (newTx.type == TransactionType.TRANSFER && destWalletRef != null) {
                val destWalletSnapshot = transactionFirestore.get(destWalletRef)
                val destWallet = destWalletSnapshot.toObject(Wallet::class.java)
                if (destWallet != null) {
                    transactionFirestore.set(destWalletRef, destWallet.copy(currentBalance = destWallet.currentBalance + newTx.amount))
                }
            }

            // Save the transaction
            transactionFirestore.set(txDocRef, newTx)
        }.await()
    }
}
