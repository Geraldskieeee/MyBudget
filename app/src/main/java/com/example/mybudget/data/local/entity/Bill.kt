package com.example.mybudget.data.local.entity

data class Bill(
    val id: Long = 0,
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: String = ""
)
