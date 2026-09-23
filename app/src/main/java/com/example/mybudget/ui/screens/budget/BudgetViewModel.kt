package com.example.mybudget.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.local.entity.Category
import com.example.mybudget.data.local.entity.TransactionType
import com.example.mybudget.data.repository.CategoryRepository
import com.example.mybudget.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategorySpending(
    val category: Category,
    val spentAmount: Double
) {
    val progress: Float
        get() = if (category.monthlyLimit > 0) (spentAmount / category.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
        
    val isOverBudget: Boolean
        get() = category.monthlyLimit > 0 && spentAmount > category.monthlyLimit
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    val categorySpendings: StateFlow<List<CategorySpending>> = combine(
        categoryRepository.getAllCategories(),
        transactionRepository.getAllTransactions()
    ) { categories, transactions ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val thisMonthExpenses = transactions.filter { tx ->
            if (tx.type != TransactionType.EXPENSE) return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = tx.dateTimestamp }
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val spendingMap = thisMonthExpenses.groupBy { it.categoryId }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        categories
            .filter { it.type == TransactionType.EXPENSE }
            .map { category ->
                CategorySpending(
                    category = category,
                    spentAmount = spendingMap[category.id] ?: 0.0
                )
            }
            .sortedByDescending { it.spentAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val totalSpent: StateFlow<Double> = combine(categorySpendings) { (spendings) ->
        spendings.sumOf { it.spentAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalBudget: StateFlow<Double> = combine(categorySpendings) { (spendings) ->
        spendings.sumOf { it.category.monthlyLimit }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun updateCategoryLimit(category: Category, limit: Double) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(monthlyLimit = limit))
        }
    }

    fun addCategory(name: String, limit: Double) {
        viewModelScope.launch {
            categoryRepository.addCategory(
                Category(name = name, type = TransactionType.EXPENSE, monthlyLimit = limit)
            )
        }
    }
}
