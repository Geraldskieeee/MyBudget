package com.example.mybudget.data.local.entity

data class Transaction(
    val id: Long = 0,
    val walletId: Long = 0,
    val categoryId: Long? = null,
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val dateTimestamp: Long = 0,
    val note: String = "",
    val toWalletId: Long? = null // For Transfers
)
