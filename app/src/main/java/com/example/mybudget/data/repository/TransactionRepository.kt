package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
import com.example.mybudget.data.local.entity.Wallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FieldValue
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

    private var lastAddedTransaction: Transaction? = null

    fun getLastAddedTransaction(): Transaction? = lastAddedTransaction

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
            .where(Filter.or(
                Filter.equalTo("walletId", walletId),
                Filter.equalTo("toWalletId", walletId)
            ))
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

        val batch = firestore.batch()
        
        // Save the transaction
        batch.set(txDocRef, newTx)
        
        // Update source wallet
        batch.update(sourceWalletRef, "currentBalance", FieldValue.increment(amountModifier))
        
        // Update destination wallet if transfer
        if (newTx.type == TransactionType.TRANSFER && destWalletRef != null) {
            batch.update(destWalletRef, "currentBalance", FieldValue.increment(newTx.amount))
        }

        batch.commit().await()
        lastAddedTransaction = newTx
    }

    suspend fun undoLastTransaction() {
        val tx = lastAddedTransaction ?: return
        
        val amountModifier = when (tx.type) {
            TransactionType.INCOME -> -tx.amount
            TransactionType.EXPENSE -> tx.amount
            TransactionType.TRANSFER -> tx.amount
        }
        
        val txDocRef = transactionsCollection.document(tx.id.toString())
        val sourceWalletRef = walletsCollection.document(tx.walletId.toString())
        val destWalletRef = tx.toWalletId?.let { walletsCollection.document(it.toString()) }

        val batch = firestore.batch()
        
        // Delete the transaction
        batch.delete(txDocRef)
        
        // Reverse source wallet balance
        batch.update(sourceWalletRef, "currentBalance", FieldValue.increment(amountModifier))
        
        // Reverse destination wallet balance if transfer
        if (tx.type == TransactionType.TRANSFER && destWalletRef != null) {
            batch.update(destWalletRef, "currentBalance", FieldValue.increment(-tx.amount))
        }

        batch.commit().await()
        lastAddedTransaction = null
    }

    suspend fun deleteTransaction(tx: Transaction) {
        val amountModifier = when (tx.type) {
            TransactionType.INCOME -> -tx.amount
            TransactionType.EXPENSE -> tx.amount
            TransactionType.TRANSFER -> tx.amount
        }
        
        val txDocRef = transactionsCollection.document(tx.id.toString())
        val sourceWalletRef = walletsCollection.document(tx.walletId.toString())
        val destWalletRef = tx.toWalletId?.let { walletsCollection.document(it.toString()) }

        val batch = firestore.batch()
        
        // Delete the transaction
        batch.delete(txDocRef)
        
        // Reverse source wallet balance
        batch.update(sourceWalletRef, "currentBalance", FieldValue.increment(amountModifier))
        
        // Reverse destination wallet balance if transfer
        if (tx.type == TransactionType.TRANSFER && destWalletRef != null) {
            batch.update(destWalletRef, "currentBalance", FieldValue.increment(-tx.amount))
        }

        batch.commit().await()
        if (lastAddedTransaction?.id == tx.id) {
            lastAddedTransaction = null
        }
    }
}
