package com.example.mybudget.data.local.entity

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

data class Category(
    val id: Long = 0,
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val monthlyLimit: Double = 0.0,
    val iconId: Int? = null
)
