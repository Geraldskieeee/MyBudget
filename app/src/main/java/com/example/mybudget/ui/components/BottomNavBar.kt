package com.example.mybudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mybudget.ui.navigation.Screen

@Composable
fun BottomNavBar(
    navController: NavController,
    isAnimationsEnabled: Boolean = true
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var isFabExpanded by remember { mutableStateOf(false) }
    
    val topLevelRoutes = listOf(
        Screen.Home.route,
        Screen.Budget.route,
        Screen.Analytics.route,
        Screen.More.route
    )

    if (currentRoute in topLevelRoutes) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(16.dp, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        Screen.Home,
                        Screen.Budget,
                        Screen.AddTransaction,
                        Screen.Analytics,
                        Screen.More
                    )
                    
                    items.forEach { screen ->
                        if (screen == Screen.AddTransaction) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val isSelected = currentRoute == screen.route
                            val icon = when (screen) {
                                Screen.Home -> Icons.Filled.Home
                                Screen.Budget -> Icons.Filled.PieChart
                                Screen.Analytics -> Icons.Filled.BarChart
                                Screen.More -> Icons.Filled.Settings
                                else -> Icons.Filled.Home
                            }
                            
                            val title = if (screen == Screen.More) "Settings" else screen.title
                            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(title, color = color, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            
            // Expandable FAB Options
            var showPopup by remember { mutableStateOf(false) }
            
            androidx.compose.runtime.LaunchedEffect(isFabExpanded) {
                if (isFabExpanded) {
                    showPopup = true
                } else {
                    kotlinx.coroutines.delay(300) // Wait for exit animation
                    showPopup = false
                }
            }
            
            if (showPopup) {
                androidx.compose.ui.window.Popup(
                    alignment = Alignment.BottomCenter,
                    onDismissRequest = { isFabExpanded = false },
                    properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isFabExpanded = false },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        AnimatedVisibility(
                            visible = isFabExpanded,
                            enter = if (isAnimationsEnabled) fadeIn() + scaleIn(initialScale = 0.5f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)) else EnterTransition.None,
                            exit = if (isAnimationsEnabled) fadeOut() + scaleOut(targetScale = 0.5f, transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)) else ExitTransition.None,
                            modifier = Modifier
                                .offset(y = (-60).dp) // Center of the arc (slightly above bottom bar)
                        ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(32.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Top Row (3 items)
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            MiniFab(
                                icon = Icons.Filled.Star,
                                label = "Goal",
                                color = Color(0xFF9C27B0)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddGoal.route)
                            }
                            MiniFab(
                                icon = Icons.Filled.DateRange,
                                label = "Bill",
                                color = Color(0xFFE91E63)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddBill.route)
                            }
                            MiniFab(
                                icon = Icons.Filled.Person,
                                label = "Debt",
                                color = Color(0xFF795548)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddDebt.route)
                            }
                        }
                        
                        // Bottom Row (4 items)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MiniFab(
                                icon = Icons.Filled.ArrowUpward,
                                label = "Income",
                                color = Color(0xFF4CAF50)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddTransaction.createRoute("INCOME"))
                            }
                            MiniFab(
                                icon = Icons.Filled.ArrowDownward,
                                label = "Expense",
                                color = Color(0xFFF44336)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddTransaction.createRoute("EXPENSE"))
                            }
                            MiniFab(
                                icon = Icons.Filled.SwapHoriz,
                                label = "Transfer",
                                color = Color(0xFF2196F3)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddTransaction.createRoute("TRANSFER"))
                            }
                            MiniFab(
                                icon = Icons.Filled.AccountBalanceWallet,
                                label = "Wallet",
                                color = Color(0xFFFF9800)
                            ) {
                                isFabExpanded = false
                                navController.navigate(Screen.AddWallet.route)
                            }
                        }
                    }
                }
                        } // End of AnimatedVisibility
                    } // End of full-screen Box
                } // End of Popup
            }
            
            // Floating Add Button (outside the Surface to avoid clipping)
            val rotation by animateFloatAsState(
                targetValue = if (isFabExpanded) 45f else 0f,
                animationSpec = if (isAnimationsEnabled) tween(300) else tween(0)
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-32).dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF32D74B))
                    .clickable { isFabExpanded = !isFabExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add, 
                    contentDescription = "Expand Options", 
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp).rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun MiniFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Transparent
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
