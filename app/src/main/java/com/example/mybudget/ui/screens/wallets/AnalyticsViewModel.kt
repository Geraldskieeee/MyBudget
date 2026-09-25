package com.example.mybudget.ui.screens.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.local.entity.Category
import com.example.mybudget.data.local.entity.TransactionType
import com.example.mybudget.data.repository.CategoryRepository
import com.example.mybudget.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

    private val _selectedType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedType: StateFlow<TransactionType> = _selectedType.asStateFlow()

    val transactions = transactionRepository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryTotals = combine(transactions, categories, selectedMonth, selectedType) { txs, cats, month, type ->
        val monthStart = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthEnd = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val filteredTxs = txs.filter { it.type == type && it.dateTimestamp in monthStart..monthEnd }
        
        val totals = mutableMapOf<Category?, Double>()
        filteredTxs.forEach { tx ->
            val cat = cats.find { it.id == tx.categoryId }
            val amount = totals[cat] ?: 0.0
            totals[cat] = amount + tx.amount
        }
        
        totals.toList().sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotals = combine(transactions, selectedMonth, selectedType) { txs, month, type ->
        val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailySums = FloatArray(daysInMonth) { 0f }

        val monthStart = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthEnd = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val filteredTxs = txs.filter { it.type == type && it.dateTimestamp in monthStart..monthEnd }
        
        filteredTxs.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.dateTimestamp }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            if (day in 1..daysInMonth) {
                dailySums[day - 1] += tx.amount.toFloat()
            }
        }
        
        dailySums.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        _selectedMonth.update { cal ->
            (cal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        }
    }

    fun previousMonth() {
        _selectedMonth.update { cal ->
            (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        }
    }

    fun setTransactionType(type: TransactionType) {
        _selectedType.value = type
    }
}
