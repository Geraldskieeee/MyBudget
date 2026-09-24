package com.example.mybudget.ui.screens.wallets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.mybudget.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletsScreen(
    navController: NavController,
    viewModel: WalletViewModel
) {
    val wallets by viewModel.wallets.collectAsState()
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedWallets by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedWallets.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedWallets = emptySet()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        // Using Text Button for Select All since there's no native SelectAll icon by default in some versions
                        TextButton(onClick = {
                            selectedWallets = if (selectedWallets.size == wallets.size) {
                                emptySet()
                            } else {
                                wallets.map { it.id }.toSet()
                            }
                        }) {
                            Text(if (selectedWallets.size == wallets.size) "Deselect All" else "Select All", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("My Wallets") },
                    actions = {
                        IconButton(onClick = { /* ViewModel automatically syncs with Firestore, but this can serve as visual reassurance or we can trigger a manual fetch if needed */ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddWallet.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Wallet")
                }
            }
        }
    ) { paddingValues ->
        if (wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val totalBalance = wallets.sumOf { it.currentBalance }
                    com.example.mybudget.ui.components.PhysicalWalletHeader(
                        wallets = wallets,
                        totalBalance = totalBalance,
                        selectedWallets = selectedWallets,
                        onWalletClick = { wallet ->
                            if (isSelectionMode) {
                                val isSelected = selectedWallets.contains(wallet.id)
                                if (isSelected) {
                                    selectedWallets -= wallet.id
                                    if (selectedWallets.isEmpty()) isSelectionMode = false
                                } else {
                                    selectedWallets += wallet.id
                                }
                            } else {
                                navController.navigate(Screen.WalletDetails.createRoute(wallet.id))
                            }
                        },
                        onWalletLongClick = { wallet ->
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedWallets += wallet.id
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    toDelete.forEach { viewModel.deleteWallet(it) }
                    isSelectionMode = false
                    selectedWallets = emptySet()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
