package com.example.mybudget.data.local.entity

data class Goal(
    val id: Long = 0,
    val name: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0
)
