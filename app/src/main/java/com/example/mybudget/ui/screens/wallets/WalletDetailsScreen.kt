package com.example.mybudget.ui.screens.wallets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletDetailsViewModel = hiltViewModel()
) {
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val formatter = DecimalFormat("#,###.##")
    val dateFormatter = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
    val dateTimeFormatter = SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wallet?.name?.uppercase() ?: "WALLET", fontFamily = FontFamily.SansSerif) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (wallet == null) return@Scaffold

        val blocks = remember(transactions, wallet) {
            val sortedTransactions = transactions.sortedBy { it.dateTimestamp }
            val result = mutableListOf<LedgerBlock>()
            var currentRunningBalance = wallet!!.startingBalance
            var currentExpenseGroup = mutableListOf<Transaction>()

            for (t in sortedTransactions) {
                if (t.type == TransactionType.INCOME) {
                    if (currentExpenseGroup.isNotEmpty()) {
                        val groupTotal = currentExpenseGroup.sumOf { it.amount }
                        currentRunningBalance -= groupTotal
                        val dateStr = dateFormatter.format(Date(currentExpenseGroup.first().dateTimestamp))
                        result.add(ExpenseGroupBlock(dateStr, currentExpenseGroup.toList(), groupTotal, currentRunningBalance))
                        currentExpenseGroup.clear()
                    }
                    
                    val old = currentRunningBalance
                    currentRunningBalance += t.amount
                    result.add(IncomeBlock(old, t.amount, currentRunningBalance))
                } else {
                    if (currentExpenseGroup.isNotEmpty()) {
                        val lastDate = dateFormatter.format(Date(currentExpenseGroup.last().dateTimestamp))
                        val thisDate = dateFormatter.format(Date(t.dateTimestamp))
                        if (lastDate != thisDate) {
                            val groupTotal = currentExpenseGroup.sumOf { it.amount }
                            currentRunningBalance -= groupTotal
                            result.add(ExpenseGroupBlock(lastDate, currentExpenseGroup.toList(), groupTotal, currentRunningBalance))
                            currentExpenseGroup.clear()
                        }
                    }
                    currentExpenseGroup.add(t)
                }
            }
            
            if (currentExpenseGroup.isNotEmpty()) {
                val groupTotal = currentExpenseGroup.sumOf { it.amount }
                currentRunningBalance -= groupTotal
                val dateStr = dateFormatter.format(Date(currentExpenseGroup.first().dateTimestamp))
                result.add(ExpenseGroupBlock(dateStr, currentExpenseGroup.toList(), groupTotal, currentRunningBalance))
            }
            
            result
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Current Balance",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "₱${formatter.format(wallet?.currentBalance ?: 0.0)}",
                                color = Color.White,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(blocks) { block ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (block) {
                        is IncomeBlock -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AddCircle, contentDescription = "Income", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Income Added", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Previous Balance", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "₱${formatter.format(block.oldBalance)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Income Amount", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "+ ₱${formatter.format(block.incomeAmount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "New Balance", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "₱${formatter.format(block.newBalance)}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        is ExpenseGroupBlock -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = block.dateStr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    block.expenses.forEach { transaction ->
                                        val amount = transaction.amount
                                        val note = transaction.note.ifEmpty { transaction.type.name.lowercase().replaceFirstChar { it.uppercase() } }
                                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.dateTimestamp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.RemoveCircle, contentDescription = "Expense", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(text = note, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                                                    Text(text = timeStr, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                            Text(text = "- ₱${formatter.format(amount)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Total Spent", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "₱${formatter.format(block.groupTotal)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Remaining Balance", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "₱${formatter.format(block.newBalance)}", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                } // End of when
            } // End of Box
        } // End of items
        
        if (blocks.isNotEmpty() && blocks.last() is ExpenseGroupBlock) {
            // Total at the bottom is redundant if we show balance in the block
            // but we can leave it or remove it. We'll remove it since Balance is shown.
        }
    } // End of LazyColumn
} // End of Scaffold
} // End of fun WalletDetailsScreen


sealed class LedgerBlock
data class IncomeBlock(val oldBalance: Double, val incomeAmount: Double, val newBalance: Double) : LedgerBlock()
data class ExpenseGroupBlock(val dateStr: String, val expenses: List<Transaction>, val groupTotal: Double, val newBalance: Double) : LedgerBlock()
