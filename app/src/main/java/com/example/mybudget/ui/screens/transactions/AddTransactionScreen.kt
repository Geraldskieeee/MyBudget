package com.example.mybudget.ui.screens.transactions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mybudget.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybudget.data.local.entity.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    initialType: String? = null,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    val selectedType by viewModel.selectedType.collectAsState()
    
    LaunchedEffect(initialType) {
        if (initialType != null) {
            when (initialType.uppercase()) {
                "INCOME" -> viewModel.selectedType.value = TransactionType.INCOME
                "EXPENSE" -> viewModel.selectedType.value = TransactionType.EXPENSE
                "TRANSFER" -> viewModel.selectedType.value = TransactionType.TRANSFER
            }
        }
    }
    
    // Wallets dropdown state
    val wallets by viewModel.wallets.collectAsState()
    var selectedWalletId by remember { mutableStateOf<Long?>(null) }
    var walletExpanded by remember { mutableStateOf(false) }
    
    // Destination Wallet state (for transfers)
    var selectedToWalletId by remember { mutableStateOf<Long?>(null) }
    var toWalletExpanded by remember { mutableStateOf(false) }

    // Category dropdown state
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    // Add Category Dialog State
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    // Confirmation Dialog State
    var showConfirmDialog by remember { mutableStateOf(false) }
    var actionToConfirm by remember { mutableStateOf<() -> Unit>({}) }
    
    // Auto-select first wallet if available
    LaunchedEffect(wallets) {
        if (selectedWalletId == null && wallets.isNotEmpty()) {
            selectedWalletId = wallets.first().id
        }
    }

    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF5F2EB), // Cream background
        topBar = {
            Surface(
                color = Color(0xFF32D74B),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 8.dp
            ) {
                TopAppBar(
                    title = { 
                        val titleStr = when (selectedType) {
                            TransactionType.INCOME -> "Add Income"
                            TransactionType.EXPENSE -> "Add Expense"
                            TransactionType.TRANSFER -> "Transfer Money"
                        }
                        Text(titleStr) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Source Wallet Selector
            ExposedDropdownMenuBox(
                expanded = walletExpanded,
                onExpandedChange = { walletExpanded = !walletExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = wallets.find { it.id == selectedWalletId }?.name ?: "Select Wallet",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (selectedType == TransactionType.TRANSFER) "From Wallet" else "Wallet") },
                    leadingIcon = {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Wallet Icon", tint = Color(0xFF1B6B43))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletExpanded) },
                    modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = walletExpanded,
                    onDismissRequest = { walletExpanded = false }
                ) {
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text("${wallet.name} (₱${String.format(java.util.Locale.US, "%,.2f", wallet.currentBalance)})") },
                            onClick = {
                                selectedWalletId = wallet.id
                                walletExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedType == TransactionType.TRANSFER) {
                // Destination Wallet Selector
                ExposedDropdownMenuBox(
                    expanded = toWalletExpanded,
                    onExpandedChange = { toWalletExpanded = !toWalletExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = wallets.find { it.id == selectedToWalletId }?.name ?: "Select Destination Wallet",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Wallet") },
                        leadingIcon = {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = "Wallet Icon", tint = Color(0xFF1B6B43))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toWalletExpanded) },
                        modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = toWalletExpanded,
                        onDismissRequest = { toWalletExpanded = false }
                    ) {
                        wallets.filter { it.id != selectedWalletId }.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text("${wallet.name} (₱${String.format(java.util.Locale.US, "%,.2f", wallet.currentBalance)})") },
                                onClick = {
                                    selectedToWalletId = wallet.id
                                    toWalletExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categoryLabel = "Category"
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Select $categoryLabel",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(categoryLabel) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = "Category Icon", tint = Color(0xFF1B6B43))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("+ Add New", color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            onClick = {
                                categoryExpanded = false
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Text("$", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(start = 16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                leadingIcon = {
                    Icon(Icons.Outlined.EditNote, contentDescription = "Note Icon", tint = Color(0xFF1B6B43))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountValue = amount.replace(",", "").toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        if (selectedWalletId == null) {
                            Toast.makeText(context, "Please select a wallet", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (selectedType == TransactionType.TRANSFER && selectedToWalletId == null) {
                            Toast.makeText(context, "Please select a destination wallet", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedType != TransactionType.TRANSFER && selectedCategoryId == null && categories.isNotEmpty()) {
                            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        actionToConfirm = {
                            viewModel.addTransaction(
                                walletId = selectedWalletId!!,
                                amount = amountValue,
                                type = selectedType,
                                note = note,
                                toWalletId = if (selectedType == TransactionType.TRANSFER) selectedToWalletId else null,
                                categoryId = if (selectedType != TransactionType.TRANSFER) selectedCategoryId else null
                            )
                            onNavigateBack()
                        }
                        showConfirmDialog = true
                    } else {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5132)),
                shape = RoundedCornerShape(50)
            ) {
                val btnStr = when (selectedType) {
                    TransactionType.INCOME -> "Save Income"
                    TransactionType.EXPENSE -> "Save Expense"
                    TransactionType.TRANSFER -> "Transfer"
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(btnStr, style = MaterialTheme.typography.titleMedium)
                }
            } // End of Button
        } // End of Column
    }
    
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName, selectedType)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Action") },
            text = { Text("Are you sure you want to save this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    actionToConfirm()
                    showConfirmDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
