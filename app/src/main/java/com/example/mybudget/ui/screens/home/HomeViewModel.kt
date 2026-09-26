package com.example.mybudget.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.repository.TransactionRepository
import com.example.mybudget.data.repository.CategoryRepository
import com.example.mybudget.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.example.mybudget.data.repository.SettingsRepository

import com.example.mybudget.data.repository.BillRepository
import com.example.mybudget.data.local.entity.Bill
import com.example.mybudget.data.repository.DebtRepository
import com.example.mybudget.data.local.entity.Debt
import com.example.mybudget.data.local.entity.DebtType
import com.example.mybudget.data.repository.GoalRepository
import com.example.mybudget.data.local.entity.Goal
import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
import com.example.mybudget.data.local.entity.Wallet
import java.util.Calendar

import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val billRepository: BillRepository,
    private val goalRepository: GoalRepository,
    private val debtRepository: DebtRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val totalBalance = walletRepository.getAllWallets()
        .map { wallets -> wallets.sumOf { it.currentBalance } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    
    val categories = categoryRepository.getAllCategories()
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

    val recentTransactions = combine(
        transactionRepository.getAllTransactions(),
        settingsRepository.lastClearedTimestamp
    ) { transactions, timestamp ->
        transactions.filter { it.dateTimestamp > timestamp }.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearRecentTransactions(thresholdTimestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            settingsRepository.setLastClearedTimestamp(thresholdTimestamp)
        }
    }

    val todayExpenses = transactionRepository.getAllTransactions()
        .map { transactions ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            
            transactions
                .filter { it.type == TransactionType.EXPENSE && it.dateTimestamp >= startOfDay }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    val activeGoal = goalRepository.getAllGoals()
        .map { goals -> goals.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val upcomingBills = billRepository.getAllBills()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debts = debtRepository.getAllDebts()
        .map { debts -> debts.filter { !it.isSettled } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBill(name: String, amount: Double, due: String) {
        viewModelScope.launch {
            billRepository.addBill(Bill(name = name, amount = amount, dueDate = due))
        }
    }

    fun addGoal(name: String, targetAmount: Double, currentAmount: Double) {
        viewModelScope.launch {
            goalRepository.addGoal(Goal(name = name, targetAmount = targetAmount, currentAmount = currentAmount))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            billRepository.deleteBill(bill)
        }
    }

    fun addDebt(name: String, amount: Double, type: DebtType, dueDate: String, notes: String) {
        viewModelScope.launch {
            debtRepository.addDebt(Debt(name = name, amount = amount, type = type, dueDate = dueDate, notes = notes))
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }

    fun settleDebt(debt: Debt, walletId: Long) {
        viewModelScope.launch {
            // Create a transaction
            val type = if (debt.type == DebtType.OWED_BY_ME) TransactionType.EXPENSE else TransactionType.INCOME
            val transaction = Transaction(
                walletId = walletId,
                amount = debt.amount,
                type = type,
                dateTimestamp = System.currentTimeMillis(),
                note = "Settled debt: ${debt.name}"
            )
            transactionRepository.addTransaction(transaction)

            // Mark debt as settled
            debtRepository.updateDebt(debt.copy(isSettled = true))
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            walletRepository.deleteWallet(wallet)
        }
    }

    fun exportWalletDataToCsv(context: android.content.Context, wallet: Wallet, onExportComplete: (android.net.Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                val walletTransactions = transactions.filter { it.walletId == wallet.id }
                
                val fileName = "mybudget_${wallet.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
                val file = java.io.File(context.cacheDir, fileName)
                val writer = java.io.FileWriter(file)
                
                writer.append("ID,Amount,Type,Date,Note\n")
                
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                
                walletTransactions.forEach { tx ->
                    val dateString = sdf.format(java.util.Date(tx.dateTimestamp))
                    writer.append("${tx.id},${tx.amount},${tx.type.name},$dateString,${tx.note.replace(",", " ")}\n")
                }
                
                writer.flush()
                writer.close()
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                onExportComplete(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                onExportComplete(null)
            }
        }
    }
}
