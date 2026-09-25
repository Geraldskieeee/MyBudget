package com.example.mybudget.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudget.data.local.entity.Wallet
import com.example.mybudget.ui.theme.RichBlack
import java.text.NumberFormat
import java.util.Locale
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhysicalWalletHeader(
    wallets: List<Wallet>,
    totalBalance: Double,
    selectedWallets: Set<Long> = emptySet(),
    onWalletClick: (Wallet) -> Unit = {},
    onWalletLongClick: (Wallet) -> Unit = {},
    onExportWalletClick: (Wallet) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val visibleWallets = wallets.take(5)
    val cardOffset = 36.dp
    val pocketHeight = 200.dp
    
    // Each card adds cardOffset to the Y position.
    // The pocket starts immediately after the last card's offset.
    val pocketOffsetY = cardOffset * visibleWallets.size
    val totalHeight = pocketOffsetY + pocketHeight
    
    var isBalanceVisible by remember { mutableStateOf(true) }
    var clickedWalletId by remember { mutableStateOf<Long?>(null) }
    
    // Reset clicked state if wallets change or selection mode exits
    androidx.compose.runtime.LaunchedEffect(wallets, selectedWallets) {
        if (selectedWallets.isNotEmpty()) clickedWalletId = null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        // Draw the stacked cards (from back to front)
        // Index 0 is drawn first (at the back), with Y=0.
        // Index 1 is drawn next, with Y=cardOffset.
        visibleWallets.forEachIndexed { index, wallet ->
            // Generate a distinct vibrant color based on the wallet's ID/name
            val colorHash = kotlin.math.abs(wallet.name.hashCode())
            val hue = (colorHash % 360).toFloat()
            val baseColor = Color.hsv(hue, 0.6f, 0.9f)
            val isSelected = selectedWallets.contains(wallet.id)
            val color = if (isSelected) baseColor.copy(alpha = 0.5f) else baseColor
            
            // Animation for click effect
            val isClicked = clickedWalletId == wallet.id
            val animatedOffsetY by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isClicked) -80.dp else 0.dp,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                finishedListener = {
                    if (isClicked) {
                        onWalletClick(wallet)
                        clickedWalletId = null
                    }
                }
            )
            
            Box(
                modifier = Modifier
                    .offset(y = (cardOffset * index) + animatedOffsetY)
                    .fillMaxWidth()
                    // Back cards are slightly narrower for perspective
                    .padding(horizontal = ((visibleWallets.size - index) * 6).dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color)
                    .combinedClickable(
                        onClick = { 
                            if (selectedWallets.isEmpty() && clickedWalletId == null) {
                                clickedWalletId = wallet.id
                            } else {
                                onWalletClick(wallet)
                            }
                        },
                        onLongClick = { onWalletLongClick(wallet) }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = wallet.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    
                    val formatter = DecimalFormat("₱#,##0.00")
                    if (wallet.currentBalance % 1.0 == 0.0) {
                        formatter.applyPattern("₱#,##0")
                    }
                    val balanceText = if (isBalanceVisible) {
                        formatter.format(wallet.currentBalance)
                    } else {
                        val digits = wallet.currentBalance.toLong().toString().length
                        "₱${"*".repeat(digits)}"
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = balanceText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onExportWalletClick(wallet) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Share,
                                contentDescription = "Export Wallet",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Draw the Leather Pocket (Foreground)
        Box(
            modifier = Modifier
                .offset(y = pocketOffsetY)
                .fillMaxWidth()
                .height(pocketHeight)
                .clip(RoundedCornerShape(32.dp))
                .background(RichBlack)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .drawBehind {
                    // Draw stitched border
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx()),
                        size = size.copy(width = size.width - 24.dp.toPx(), height = size.height - 24.dp.toPx()),
                        topLeft = androidx.compose.ui.geometry.Offset(12.dp.toPx(), 12.dp.toPx())
                    )
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val formatter = DecimalFormat("₱#,##0.00")
                if (totalBalance % 1.0 == 0.0) {
                    formatter.applyPattern("₱#,##0")
                }
                
                val displayBalance = if (isBalanceVisible) {
                    formatter.format(totalBalance)
                } else {
                    val digits = totalBalance.toLong().toString().length
                    "₱${"*".repeat(digits)}"
                }
                
                Text(
                    text = displayBalance,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Balance",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Balance Visibility",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
