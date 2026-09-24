package com.example.mybudget.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Close
import com.example.mybudget.ui.components.PhysicalWalletHeader
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybudget.ui.screens.transactions.TransactionItem
import com.example.mybudget.data.local.entity.Bill
import com.example.mybudget.data.local.entity.Goal
import com.example.mybudget.data.local.entity.Debt
import com.example.mybudget.data.local.entity.DebtType
import com.example.mybudget.data.local.entity.Wallet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddBillClick: () -> Unit = {},
    onAddGoalClick: () -> Unit = {},
    onAddDebtClick: () -> Unit = {},
    onDeleteBill: (Bill) -> Unit = {},
    onDeleteGoal: (Goal) -> Unit = {},
    onDeleteDebt: (Debt) -> Unit = {},
    onSettleDebt: (Debt, Long) -> Unit = { _, _ -> },
    onWalletClick: (Wallet) -> Unit = {},
    onDeleteWallet: (Wallet) -> Unit = {},
    onAddWalletClick: () -> Unit = {}
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    
    val activeGoal by viewModel.activeGoal.collectAsState()
    
    val upcomingBills by viewModel.upcomingBills.collectAsState()

    val debts by viewModel.debts.collectAsState()

    val wallets by viewModel.wallets.collectAsState()
    var isWalletSelectionMode by remember { mutableStateOf(false) }
    var selectedWallets by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteWalletDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isWalletSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedWallets.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isWalletSelectionMode = false
                            selectedWallets = emptySet()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            selectedWallets = if (selectedWallets.size == wallets.size) emptySet() else wallets.map { it.id }.toSet()
                        }) {
                            Text(if (selectedWallets.size == wallets.size) "Deselect All" else "Select All", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showDeleteWalletDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onAddWalletClick) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Wallet")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Total Balance Card
            item {
                if (wallets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Empty",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No wallets yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click the + button to create one",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    PhysicalWalletHeader(
                        wallets = wallets,
                        totalBalance = totalBalance,
                        selectedWallets = selectedWallets,
                        onWalletClick = { wallet ->
                            if (isWalletSelectionMode) {
                                if (selectedWallets.contains(wallet.id)) {
                                    selectedWallets -= wallet.id
                                    if (selectedWallets.isEmpty()) isWalletSelectionMode = false
                                } else {
                                    selectedWallets += wallet.id
                                }
                            } else {
                                onWalletClick(wallet)
                            }
                        },
                        onWalletLongClick = { wallet ->
                            if (!isWalletSelectionMode) {
                                isWalletSelectionMode = true
                                selectedWallets += wallet.id
                            }
                        }
                    )
                }
            }


            
            // 3. Savings Progress
            item {
                SavingsProgress(
                    goal = activeGoal,
                    onAddClick = onAddGoalClick,
                    onDeleteClick = onDeleteGoal
                )
            }
            
            // 4. Upcoming Bills
            item {
                UpcomingBillsSection(
                    bills = upcomingBills,
                    onAddClick = onAddBillClick,
                    onDeleteClick = onDeleteBill
                )
            }

            // 4.5. Debts Section
            item {
                DebtsSection(
                    debts = debts,
                    wallets = wallets,
                    onAddClick = onAddDebtClick,
                    onDeleteClick = onDeleteDebt,
                    onSettleClick = onSettleDebt
                )
            }

            // 5. Recent Transactions Header
            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }
            
            // 6. Transactions List
            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }

    if (showDeleteWalletDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteWalletDialog = false },
            title = { Text("Delete Wallets") },
            text = { 
                Text(
                    if (selectedWallets.size == 1) "Are you sure you want to delete the selected wallet? This action cannot be undone."
                    else "Are you sure you want to delete ${selectedWallets.size} wallets? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val toDelete = wallets.filter { it.id in selectedWallets }
                    toDelete.forEach { onDeleteWallet(it) }
                    isWalletSelectionMode = false
                    selectedWallets = emptySet()
                    showDeleteWalletDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteWalletDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            Column {
                Text(
                    text = "Total Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₱${String.format(java.util.Locale.US, "%.2f", balance)}",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TodayExpensesCard(expenses: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Spent Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("₱${String.format(java.util.Locale.US, "%.2f", expenses)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavingsProgress(goal: Goal?, onAddClick: () -> Unit, onDeleteClick: (Goal) -> Unit) {
    var showDelete by remember(goal) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp)
            .combinedClickable(
                onClick = { showDelete = false },
                onLongClick = { showDelete = true }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (goal != null) {
                val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Savings Goal: ${goal.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Row {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.tertiary)
                        }
                        if (showDelete) {
                            var showGoalDeleteDialog by remember { mutableStateOf(false) }
                            IconButton(onClick = { 
                                showGoalDeleteDialog = true 
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                            }
                            
                            if (showGoalDeleteDialog) {
                                AlertDialog(
                                    onDismissRequest = { showGoalDeleteDialog = false },
                                    title = { Text("Delete Goal") },
                                    text = { Text("Are you sure you want to delete this savings goal? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            onDeleteClick(goal)
                                            showGoalDeleteDialog = false
                                            showDelete = false
                                        }) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showGoalDeleteDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("₱${String.format(java.util.Locale.US, "%.0f", goal.currentAmount)} saved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₱${String.format(java.util.Locale.US, "%.0f", goal.targetAmount)} goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("No Savings Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap the + button to add a new savings goal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpcomingBillsSection(bills: List<Bill>, onAddClick: () -> Unit, onDeleteClick: (Bill) -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Bills",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Bill", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        if (bills.isEmpty()) {
            Text(
                text = "No upcoming bills.",
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
                var showDelete by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.combinedClickable(
                        onClick = { showDelete = false },
                        onLongClick = { showDelete = true }
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).width(120.dp)) {
                        Text(bill.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(bill.dueDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("₱${String.format(java.util.Locale.US, "%.2f", bill.amount)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            if (showDelete) {
                                var showBillDeleteDialog by remember { mutableStateOf(false) }
                                IconButton(onClick = { showBillDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Bill", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                                
                                if (showBillDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showBillDeleteDialog = false },
                                        title = { Text("Delete Bill") },
                                        text = { Text("Are you sure you want to delete this bill? This action cannot be undone.") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                onDeleteClick(bill)
                                                showBillDeleteDialog = false
                                                showDelete = false
                                            }) {
                                                Text("Delete", color = MaterialTheme.colorScheme.error)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showBillDeleteDialog = false }) {
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
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DebtsSection(
    debts: List<Debt>, 
    wallets: List<Wallet>, 
    onAddClick: () -> Unit, 
    onDeleteClick: (Debt) -> Unit,
    onSettleClick: (Debt, Long) -> Unit
) {
    var debtToSettle by remember { mutableStateOf<Debt?>(null) }

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Debts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Debt", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        if (debts.isEmpty()) {
            Text(
                text = "No debts recorded.",
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(debts, key = { it.id }) { debt ->
                    var showDelete by remember { mutableStateOf(false) }
                    
                    val cardColor = if (debt.type == DebtType.OWED_BY_ME) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    val contentColor = if (debt.type == DebtType.OWED_BY_ME) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    
                    Card(
                        modifier = Modifier.combinedClickable(
                            onClick = { showDelete = false },
                            onLongClick = { showDelete = true }
                        ),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).width(200.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(debt.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = contentColor, modifier = Modifier.weight(1f))
                                if (showDelete) {
                                    var showDebtDeleteDialog by remember { mutableStateOf(false) }
                                    IconButton(onClick = { showDebtDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Debt", tint = contentColor, modifier = Modifier.size(16.dp))
                                    }
                                    
                                    if (showDebtDeleteDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showDebtDeleteDialog = false },
                                            title = { Text("Delete Debt") },
                                            text = { Text("Are you sure you want to delete this debt? This action cannot be undone.") },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    onDeleteClick(debt)
                                                    showDebtDeleteDialog = false
                                                    showDelete = false
                                                }) {
                                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showDebtDeleteDialog = false }) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (debt.type == DebtType.OWED_BY_ME) "You Owe" else "Owes You", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Due: ${debt.dueDate}", style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
                            if (debt.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Note: ${debt.notes}", style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f), maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("₱${String.format(java.util.Locale.US, "%.2f", debt.amount)}", color = contentColor, fontWeight = FontWeight.Bold)
                                
                                Button(
                                    onClick = { debtToSettle = debt },
                                    colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = cardColor),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Settle", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (debtToSettle != null) {
        var selectedWalletId by remember { mutableStateOf<Long?>(wallets.firstOrNull()?.id) }
        
        AlertDialog(
            onDismissRequest = { debtToSettle = null },
            title = { Text("Settle Debt") },
            text = { 
                Column {
                    Text(if (debtToSettle?.type == DebtType.OWED_BY_ME) "Select wallet to pay from:" else "Select wallet to receive into:")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (wallets.isEmpty()) {
                        Text("No wallets available. Please create a wallet first.", color = MaterialTheme.colorScheme.error)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                            items(wallets) { wallet ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().combinedClickable { selectedWalletId = wallet.id }.padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedWalletId == wallet.id,
                                        onClick = { selectedWalletId = wallet.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(wallet.name, style = MaterialTheme.typography.bodyMedium)
                                        Text("Balance: ₱${String.format(java.util.Locale.US, "%.2f", wallet.currentBalance)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedWalletId?.let { walletId ->
                            debtToSettle?.let { debt ->
                                onSettleClick(debt, walletId)
                            }
                        }
                        debtToSettle = null
                    },
                    enabled = selectedWalletId != null
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToSettle = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
