package com.example.mybudget.ui.screens.transactions

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybudget.data.local.entity.Transaction
import com.example.mybudget.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val wallets by viewModel.wallets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF32D74B),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    var showDelete by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier.combinedClickable(
                            onClick = { showDelete = false },
                            onLongClick = { showDelete = true }
                        )
                    ) {
                        TransactionItem(transaction = transaction, categories = allCategories, wallets = wallets, showDelete = showDelete, onDelete = { 
                            viewModel.deleteTransaction(it)
                            showDelete = false
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    categories: List<com.example.mybudget.data.local.entity.Category> = emptyList(),
    wallets: List<com.example.mybudget.data.local.entity.Wallet> = emptyList(),
    showDelete: Boolean = false,
    onDelete: (Transaction) -> Unit = {}
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormatter.format(Date(transaction.dateTimestamp))

    val color = when (transaction.type) {
        TransactionType.INCOME -> Color(0xFF4CAF50) // Green
        TransactionType.EXPENSE -> Color(0xFFF44336) // Red
        TransactionType.TRANSFER -> Color(0xFF2196F3) // Blue
    }
    
    val sign = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Colored Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when (transaction.type) {
                        TransactionType.INCOME -> Icons.Filled.ArrowUpward
                        TransactionType.EXPENSE -> Icons.Filled.ArrowDownward
                        TransactionType.TRANSFER -> Icons.Filled.SwapHoriz
                    }
                    Icon(imageVector = iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    val categoryName = categories.find { it.id == transaction.categoryId }?.name ?: transaction.type.name.lowercase().replaceFirstChar { it.uppercase() }
                    val walletName = wallets.find { it.id == transaction.walletId }?.name
                    
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val subtitle = buildString {
                        if (walletName != null) append(walletName)
                        if (walletName != null && transaction.note.isNotBlank()) append(" • ")
                        if (transaction.note.isNotBlank()) append(transaction.note)
                        if (isNotEmpty()) append(" • ")
                        append(dateString)
                    }
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$sign₱${String.format(java.util.Locale.US, "%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (showDelete) {
                    Spacer(modifier = Modifier.width(8.dp))
                    var showConfirmDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showConfirmDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                    
                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Delete Transaction") },
                            text = { Text("Are you sure you want to delete this transaction?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    onDelete(transaction)
                                    showConfirmDialog = false
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
