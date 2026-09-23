package com.example.mybudget.data.local.entity

enum class DebtType {
    OWED_BY_ME,
    OWED_TO_ME
}

data class Debt(
    val id: Long = 0,
    val name: String = "",
    val amount: Double = 0.0,
    val type: DebtType = DebtType.OWED_BY_ME,
    val dueDate: String = "",
    val notes: String = "",
    val isSettled: Boolean = false
)
