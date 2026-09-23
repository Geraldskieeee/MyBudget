package com.example.mybudget.data.repository

import com.example.mybudget.data.local.entity.Category
import com.example.mybudget.data.local.entity.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    private val collection
        get() = firestore.collection("users").document(userId).collection("categories")

    fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            var categories = snapshot?.documents?.mapNotNull { it.toObject(Category::class.java) } ?: emptyList()
            
            if (categories.isEmpty() && snapshot != null && !snapshot.metadata.hasPendingWrites()) {
                // Seed default categories
                val defaultCategories = listOf(
                    Category(id = 1, name = "Salary", type = TransactionType.INCOME),
                    Category(id = 2, name = "Freelance", type = TransactionType.INCOME),
                    Category(id = 3, name = "Food", type = TransactionType.EXPENSE),
                    Category(id = 4, name = "Transport", type = TransactionType.EXPENSE),
                    Category(id = 5, name = "Entertainment", type = TransactionType.EXPENSE),
                    Category(id = 6, name = "Shopping", type = TransactionType.EXPENSE),
                    Category(id = 7, name = "Utilities", type = TransactionType.EXPENSE)
                )
                defaultCategories.forEach { cat ->
                    collection.document(cat.id.toString()).set(cat)
                }
                categories = defaultCategories
            }
            
            trySend(categories)
        }
        awaitClose { subscription.remove() }
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = callbackFlow {
        val subscription = collection.whereEqualTo("type", type.name).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val categories = snapshot?.documents?.mapNotNull { it.toObject(Category::class.java) } ?: emptyList()
            trySend(categories)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addCategory(category: Category) {
        val id = if (category.id == 0L) System.currentTimeMillis() else category.id
        val newCategory = category.copy(id = id)
        collection.document(id.toString()).set(newCategory).await()
    }

    suspend fun updateCategory(category: Category) {
        collection.document(category.id.toString()).set(category).await()
    }

    suspend fun deleteCategory(category: Category) {
        collection.document(category.id.toString()).delete().await()
    }
}
