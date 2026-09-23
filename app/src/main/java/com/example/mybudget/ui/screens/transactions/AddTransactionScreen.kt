package com.example.mybudget.ui.screens.transactions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybudget.data.local.entity.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    val selectedType by viewModel.selectedType.collectAsState()
    
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
    
    // Auto-select first wallet if available
    LaunchedEffect(wallets) {
        if (selectedWalletId == null && wallets.isNotEmpty()) {
            selectedWalletId = wallets.first().id
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                val options = TransactionType.values()
                options.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { 
                            viewModel.selectedType.value = type
                            selectedCategoryId = null // Reset category when type changes
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) {
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = walletExpanded,
                    onDismissRequest = { walletExpanded = false }
                ) {
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text("${wallet.name} (₱${String.format(java.util.Locale.US, "%.2f", wallet.currentBalance)})") },
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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toWalletExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = toWalletExpanded,
                        onDismissRequest = { toWalletExpanded = false }
                    ) {
                        wallets.filter { it.id != selectedWalletId }.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text("${wallet.name} (₱${String.format(java.util.Locale.US, "%.2f", wallet.currentBalance)})") },
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
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₱") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            } // End of Card column
            } // End of Card

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

                        viewModel.addTransaction(
                            walletId = selectedWalletId!!,
                            amount = amountValue,
                            type = selectedType,
                            note = note,
                            toWalletId = if (selectedType == TransactionType.TRANSFER) selectedToWalletId else null,
                            categoryId = if (selectedType != TransactionType.TRANSFER) selectedCategoryId else null
                        )
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
