package com.example.mybudget.ui.screens.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudget.data.local.entity.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    // Placeholder for date selection UI
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convert UTC millis to LocalDate
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        currentDate = date
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = Color(0xFF32D74B),
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Budget") },
                text = { Text("New Budget", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Header Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF32D74B), Color(0xFF0F9D58))
                        )
                    )
            )
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Custom Top Bar & Month Selector
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentDate = currentDate.minusMonths(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", tint = Color.White)
                    }
                    Text(
                        text = currentDate.format(dateFormatter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                    IconButton(onClick = { currentDate = currentDate.plusMonths(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Glassmorphism Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BudgetDonutChart(totalSpent = totalSpent, totalBudget = totalBudget)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Total Budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₱${"%,.2f".format(totalBudget)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
                                Text("₱${"%,.2f".format(remaining)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF32D74B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Categories List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (categorySpendings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No budget categories setup.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 100.dp) // Added bottom padding to avoid FAB overlap
                        ) {
                            item {
                                Text(
                                    text = "Budget Categories",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            items(categorySpendings) { spending ->
                                ModernCategorySpendingItem(
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
            title = { Text("Add Budget Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name (e.g. Food)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategoryLimit,
                        onValueChange = { newCategoryLimit = it },
                        label = { Text("Monthly Limit (₱)") },
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
fun BudgetDonutChart(totalSpent: Double, totalBudget: Double) {
    val percentage = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val sweepAngle = (percentage * 360f).coerceAtMost(360f)
    val color = when {
        percentage >= 1f -> Color(0xFFF44336)
        percentage >= 0.8f -> Color(0xFFFF9800)
        else -> Color(0xFF32D74B)
    }
    
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
            // Foreground progress
            if (sweepAngle > 0) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayPercent = (percentage * 100).coerceAtMost(100f)
            Text(
                text = "${displayPercent.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ModernCategorySpendingItem(spending: CategorySpending, onClick: (Category) -> Unit) {
    // Generate a consistent color based on category name
    val colorHash = Math.abs(spending.category.name.hashCode())
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800), 
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4)
    )
    val categoryColor = colors[colorHash % colors.size]

    val progress = spending.progress
    val progressColor = when {
        spending.isOverBudget -> Color(0xFFF44336)
        progress >= 0.8f -> Color(0xFFFF9800)
        else -> categoryColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(spending.category) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountBalanceWallet, 
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = spending.category.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "₱${"%,.0f".format(spending.spentAmount)} / " + 
                               if (spending.category.monthlyLimit > 0) "₱${"%,.0f".format(spending.category.monthlyLimit)}" else "∞",
                        color = if (spending.isOverBudget) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}
