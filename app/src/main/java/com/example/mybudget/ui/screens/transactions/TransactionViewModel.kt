package com.example.mybudget.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
import com.example.mybudget.data.repository.TransactionRepository
import com.example.mybudget.data.repository.CategoryRepository
import com.example.mybudget.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    
    val allCategories = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val wallets = walletRepository.getAllWallets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions = kotlinx.coroutines.flow.combine(
        transactionRepository.getAllTransactions(),
        walletRepository.getAllWallets()
    ) { transactions, wallets ->
        val activeWalletIds = wallets.map { it.id }.toSet()
        transactions.filter { it.walletId in activeWalletIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    
    @kotlin.OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val categories = selectedType
        .flatMapLatest { type ->
            categoryRepository.getCategoriesByType(type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(
        walletId: Long,
        amount: Double,
        type: TransactionType,
        note: String,
        toWalletId: Long? = null,
        categoryId: Long? = null
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                walletId = walletId,
                amount = amount,
                type = type,
                note = note,
                dateTimestamp = System.currentTimeMillis(),
                toWalletId = toWalletId,
                categoryId = categoryId
            )
            transactionRepository.addTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            val category = com.example.mybudget.data.local.entity.Category(
                name = name,
                type = type
            )
            categoryRepository.addCategory(category)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}
