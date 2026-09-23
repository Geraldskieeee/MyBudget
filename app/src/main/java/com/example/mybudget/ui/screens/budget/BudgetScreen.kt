package com.example.mybudget.ui.screens.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudget.data.local.entity.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val categorySpendings by viewModel.categorySpendings.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var limitInput by remember { mutableStateOf("") }
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryLimit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget & Allowance") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCategoryDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Budget", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₱${"%,.2f".format(totalBudget)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Spent", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₱${"%,.2f".format(totalSpent)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    val remaining = totalBudget - totalSpent
                    val isOver = remaining < 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isOver) "Over Budget" else "Remaining", style = MaterialTheme.typography.bodyMedium, color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₱${"%,.2f".format(kotlin.math.abs(remaining))}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (categorySpendings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expense categories found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                BudgetPieChart(spendings = categorySpendings, totalSpent = totalSpent, totalBudget = totalBudget)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(categorySpendings) { spending ->
                        CategorySpendingItem(
                            spending = spending,
                            onClick = {
                                selectedCategory = it
                                limitInput = if (it.monthlyLimit > 0) it.monthlyLimit.toString() else ""
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { selectedCategory = null },
            title = { Text("Set Limit for ${selectedCategory?.name}") },
            text = {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    label = { Text("Monthly Limit (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val limit = limitInput.toDoubleOrNull() ?: 0.0
                    selectedCategory?.let { viewModel.updateCategoryLimit(it, limit) }
                    selectedCategory = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Expense Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategoryLimit,
                        onValueChange = { newCategoryLimit = it },
                        label = { Text("Monthly Limit (₱) (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        val limit = newCategoryLimit.toDoubleOrNull() ?: 0.0
                        viewModel.addCategory(newCategoryName, limit)
                        showAddCategoryDialog = false
                        newCategoryName = ""
                        newCategoryLimit = ""
                    }
                }) {
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
}

@Composable
fun BudgetPieChart(spendings: List<CategorySpending>, totalSpent: Double, totalBudget: Double) {
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), 
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFFC107), Color(0xFF795548), Color(0xFF607D8B)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (totalSpent <= 0) {
            Text("No spending this month yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return
        }

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                spendings.forEachIndexed { index, spending ->
                    val sweepAngle = if (totalSpent > 0) ((spending.spentAmount / totalSpent) * 360f).toFloat() else 0f
                    if (sweepAngle > 0) {
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweepAngle
                    }
                }
            }
            // Inner circle for donut hole
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(color = Color.White) // TODO: Handle dark mode gracefully
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (totalBudget > 0) {
                    val percentage = (totalSpent / totalBudget) * 100
                    Text("${"%.1f".format(percentage)}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (percentage > 100) MaterialTheme.colorScheme.error else Color.Black)
                    Text("of Budget", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("₱${"%,.2f".format(totalSpent)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun CategorySpendingItem(spending: CategorySpending, onClick: (Category) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(spending.category) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = spending.category.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (spending.isOverBudget) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(4.dp)) {
                            Text("Over Budget", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = "₱${"%,.2f".format(spending.spentAmount)} / " + 
                           if (spending.category.monthlyLimit > 0) "₱${"%,.2f".format(spending.category.monthlyLimit)}" else "No Limit",
                    color = if (spending.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (spending.isOverBudget) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { spending.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (spending.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}
