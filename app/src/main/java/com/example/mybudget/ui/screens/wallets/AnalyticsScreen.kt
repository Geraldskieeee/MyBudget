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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mybudget.data.local.entity.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    val totalAmount = categoryTotals.sumOf { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF32D74B),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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
                // Line Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LineChart(
                        data = dailyTotals,
                        totalAmount = totalAmount,
                        currencyFormat = currencyFormat
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
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
fun LineChart(
    data: List<Float>,
    totalAmount: Double,
    currencyFormat: NumberFormat
) {
    if (data.isEmpty()) return
    
    var animationPlayed by remember { mutableStateOf(false) }
    val animateProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500)
    )
    
    LaunchedEffect(key1 = data) {
        animationPlayed = true
    }

    val maxAmount = max(data.maxOrNull() ?: 1f, 1f)
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = onSurfaceVariant.copy(alpha = 0.1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Total for Month",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = currencyFormat.format(totalAmount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
        ) {
            if (data.isEmpty()) return@Canvas
            
            val canvasWidth = size.width
            val canvasHeight = size.height
            val pointSpacing = canvasWidth / (data.size - 1).coerceAtLeast(1)
            val strokeWidth = 4.dp.toPx()
            
            // Draw 3 horizontal grid lines (0, 50%, 100%)
            for (i in 0..2) {
                val y = canvasHeight - (canvasHeight * (i / 2f))
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
                // Draw Y-axis labels
                val labelAmount = maxAmount * (i / 2f)
                val formattedLabel = if (labelAmount >= 1000) {
                    "$${String.format("%.1fk", labelAmount / 1000)}"
                } else {
                    "$${labelAmount.toInt()}"
                }
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = formattedLabel,
                    style = TextStyle(color = onSurfaceVariant, fontSize = 10.sp),
                    topLeft = Offset(0f, y - 16.dp.toPx())
                )
            }
            
            val path = Path()
            
            var previousPointX = 0f
            var previousPointY = canvasHeight - (data.first() / maxAmount) * canvasHeight * animateProgress
            
            path.moveTo(previousPointX, previousPointY)
            
            val dataPoints = mutableListOf(Offset(previousPointX, previousPointY))
            
            for (i in 1 until data.size) {
                val currentPointX = i * pointSpacing
                val currentPointY = canvasHeight - (data[i] / maxAmount) * canvasHeight * animateProgress
                
                val controlPointX = (previousPointX + currentPointX) / 2
                
                path.cubicTo(
                    controlPointX, previousPointY,
                    controlPointX, currentPointY,
                    currentPointX, currentPointY
                )
                
                dataPoints.add(Offset(currentPointX, currentPointY))
                previousPointX = currentPointX
                previousPointY = currentPointY
            }
            
            val brush = Brush.linearGradient(
                colors = listOf(Color(0xFF6C63FF), Color(0xFF32D74B))
            )
            
            drawPath(
                path = path,
                brush = brush,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            
            val fillPath = Path().apply {
                addPath(path)
                lineTo(canvasWidth, canvasHeight)
                lineTo(0f, canvasHeight)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
            
            // Draw points on days that have transactions
            for (i in data.indices) {
                if (data[i] > 0f) {
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx() * animateProgress,
                        center = dataPoints[i]
                    )
                    drawCircle(
                        color = Color(0xFF6C63FF),
                        radius = 4.dp.toPx() * animateProgress,
                        center = dataPoints[i]
                    )
                }
            }
            
            // Draw X-axis labels (Start, Middle, End of month)
            drawText(
                textMeasurer = textMeasurer,
                text = "1st",
                style = TextStyle(color = onSurfaceVariant, fontSize = 10.sp),
                topLeft = Offset(0f, canvasHeight + 4.dp.toPx())
            )
            val midDay = data.size / 2
            drawText(
                textMeasurer = textMeasurer,
                text = "${midDay}th",
                style = TextStyle(color = onSurfaceVariant, fontSize = 10.sp),
                topLeft = Offset((canvasWidth / 2f) - 12.dp.toPx(), canvasHeight + 4.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "${data.size}th",
                style = TextStyle(color = onSurfaceVariant, fontSize = 10.sp),
                topLeft = Offset(canvasWidth - 24.dp.toPx(), canvasHeight + 4.dp.toPx())
            )
        }
    }
}
