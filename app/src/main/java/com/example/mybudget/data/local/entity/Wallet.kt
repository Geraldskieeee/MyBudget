package com.example.mybudget.data.local.entity

data class Wallet(
    val id: Long = 0,
    val name: String = "",
    val startingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val iconId: Int? = null
)
