package com.example.mybudget.ui.screens.more

import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.repository.GoalRepository
import com.example.mybudget.data.repository.TransactionRepository
import com.example.mybudget.data.repository.WalletRepository
import com.example.mybudget.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    goalRepository: GoalRepository,
    private val auth: FirebaseAuth,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userDisplayName = auth.currentUser?.displayName ?: "My Budget User"
    val userEmail = auth.currentUser?.email ?: "user@mybudget.com"
    val userPhotoUrl = auth.currentUser?.photoUrl?.toString()

    val totalWealth = walletRepository.getAllWallets()
        .map { wallets -> wallets.sumOf { it.currentBalance } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val transactionCount = transactionRepository.getAllTransactions()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val activeGoalsCount = goalRepository.getAllGoals()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isDarkMode = settingsRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun exportDataToCsv(context: Context, onExportComplete: (android.net.Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                
                val fileName = "mybudget_transactions_${System.currentTimeMillis()}.csv"
                val file = File(context.cacheDir, fileName)
                val writer = FileWriter(file)
                
                // Header
                writer.append("ID,Amount,Type,Date,Note\n")
                
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                
                transactions.forEach { tx ->
                    val dateString = sdf.format(Date(tx.dateTimestamp))
                    writer.append("${tx.id},${tx.amount},${tx.type.name},$dateString,${tx.note.replace(",", " ")}\n")
                }
                
                writer.flush()
                writer.close()
                
                val uri = FileProvider.getUriForFile(
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

    fun logout() {
        auth.signOut()
    }

    fun updatePassword(newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } else {
            onResult(false, "No authenticated user found.")
        }
    }
}
