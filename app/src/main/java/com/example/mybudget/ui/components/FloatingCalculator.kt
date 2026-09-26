package com.example.mybudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FloatingCalculator() {
    var isExpanded by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    var offsetY by remember { mutableStateOf(500f) }
    
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val fabSizePx = with(density) { 56.dp.toPx() }
    val paddingPx = with(density) { 32.dp.toPx() }
    val maxX = screenWidthPx - fabSizePx - paddingPx

    
    // Calculator state
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewInput by remember { mutableStateOf(true) }

    fun onKeyClick(key: String) {
        when (key) {
            "C" -> {
                display = "0"
                operand1 = null
                operator = null
                isNewInput = true
            }
            "⌫" -> {
                if (display.length > 1) {
                    display = display.dropLast(1)
                } else {
                    display = "0"
                    isNewInput = true
                }
            }
            "+", "-", "×", "÷" -> {
                val currentValue = display.toDoubleOrNull() ?: 0.0
                if (operand1 != null && operator != null && !isNewInput) {
                    val result = when (operator) {
                        "+" -> operand1!! + currentValue
                        "-" -> operand1!! - currentValue
                        "×" -> operand1!! * currentValue
                        "÷" -> if (currentValue != 0.0) operand1!! / currentValue else 0.0
                        else -> currentValue
                    }
                    display = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
                    operand1 = result
                } else {
                    operand1 = currentValue
                }
                operator = key
                isNewInput = true
            }
            "=" -> {
                val currentValue = display.toDoubleOrNull() ?: 0.0
                if (operand1 != null && operator != null) {
                    val result = when (operator) {
                        "+" -> operand1!! + currentValue
                        "-" -> operand1!! - currentValue
                        "×" -> operand1!! * currentValue
                        "÷" -> if (currentValue != 0.0) operand1!! / currentValue else 0.0
                        else -> currentValue
                    }
                    display = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
                    operand1 = null
                    operator = null
                    isNewInput = true
                }
            }
            "." -> {
                if (isNewInput) {
                    display = "0."
                    isNewInput = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> {
                if (isNewInput) {
                    display = key
                    isNewInput = false
                } else {
                    if (display == "0") display = key else display += key
                }
            }
        }
    }

    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    // Close if tapped outside
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calculator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { isExpanded = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (operand1 != null && operator != null) "${if (operand1!! % 1.0 == 0.0) operand1!!.toLong() else operand1} $operator" else " ",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = display,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val buttons = listOf(
                        listOf("C", "⌫", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("0", ".", "=")
                    )
                    
                    buttons.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { btn ->
                                val weight = if (btn == "C" || btn == "0") 2f else 1f
                                val color = when (btn) {
                                    "C", "⌫" -> MaterialTheme.colorScheme.error
                                    "+", "-", "×", "÷", "=" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                                val textColor = if (color == MaterialTheme.colorScheme.secondaryContainer) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                                
                                Button(
                                    onClick = { onKeyClick(btn) },
                                    modifier = Modifier.weight(weight).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = textColor)
                                ) {
                                    Text(text = btn, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), offsetY.roundToInt()) }
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val targetX = if (offsetX.value < maxX / 2) 0f else maxX
                                    offsetX.animateTo(
                                        targetValue = targetX,
                                        animationSpec = spring()
                                    )
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                                offsetY += dragAmount.y
                            }
                        )
                    }
            ) {
                IconButton(
                    onClick = { isExpanded = true },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Calculate,
                        contentDescription = "Open Calculator",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
