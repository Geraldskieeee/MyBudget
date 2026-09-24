package com.example.mybudget.ui.screens.wallets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mybudget.data.local.entity.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    val totalAmount = categoryTotals.sumOf { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = monthFormat.format(selectedMonth.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month")
                }
            }
            
            // Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                    label = { Text("Expenses") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (categoryTotals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data for this month.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Pie Chart / Donut Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        data = categoryTotals.map { it.second.toFloat() },
                        colors = categoryTotals.mapIndexed { index, _ -> 
                            Color.hsv((index * 137.5f) % 360f, 0.7f, 0.9f) 
                        },
                        totalAmount = totalAmount,
                        currencyFormat = currencyFormat
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
                ) {
                    items(categoryTotals) { (category, amount) ->
                        val color = Color.hsv((categoryTotals.indexOfFirst { it.first == category } * 137.5f) % 360f, 0.7f, 0.9f)
                        val percentage = if (totalAmount > 0) (amount / totalAmount) else 0.0
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = category?.name ?: "Uncategorized",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Text(
                                        text = currencyFormat.format(amount),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // Progress Bar
                                var animationPlayed by remember { mutableStateOf(false) }
                                val animatedWidth by animateFloatAsState(
                                    targetValue = if (animationPlayed) percentage.toFloat() else 0f,
                                    animationSpec = tween(durationMillis = 1000)
                                )
                                
                                LaunchedEffect(key1 = percentage) {
                                    animationPlayed = true
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedWidth)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color)
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

@Composable
fun DonutChart(
    data: List<Float>,
    colors: List<Color>,
    totalAmount: Double,
    currencyFormat: NumberFormat
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateSweep by animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(durationMillis = 1500)
    )
    
    LaunchedEffect(key1 = data) {
        animationPlayed = true
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            val strokeWidth = 32.dp.toPx()
            var startAngle = -90f
            
            val total = data.sum()
            
            for (i in data.indices) {
                val sweepAngle = (data[i] / total) * animateSweep
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currencyFormat.format(totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
