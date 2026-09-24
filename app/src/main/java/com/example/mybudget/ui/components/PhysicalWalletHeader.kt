package com.example.mybudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun PhysicalWalletHeader(
    wallets: List<Wallet>,
    totalBalance: Double,
    modifier: Modifier = Modifier
) {
    val visibleWallets = wallets.take(5)
    val cardOffset = 28.dp
    val pocketHeight = 180.dp
    
    // The total height depends on how many cards are sticking out.
    // Base height is the pocket. Each card adds 28dp to the top.
    val totalHeight = pocketHeight + (cardOffset * visibleWallets.size)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Draw the stacked cards (from back to front)
        visibleWallets.forEachIndexed { index, wallet ->
            // Generate a distinct color based on the wallet's ID or name
            val colorHash = kotlin.math.abs(wallet.name.hashCode())
            val hue = (colorHash % 360).toFloat()
            val color = Color.hsv(hue, 0.7f, 0.8f)
            
            Box(
                modifier = Modifier
                    .offset(y = cardOffset * index)
                    .fillMaxWidth()
                    // Create a tiered width effect (back cards look smaller due to perspective)
                    .padding(horizontal = (12 + (visibleWallets.size - index - 1) * 4).dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(color)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = wallet.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US).format(wallet.currentBalance),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Draw the Leather Pocket (Foreground)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(pocketHeight)
                .clip(RoundedCornerShape(32.dp))
                .background(RichBlack)
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
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(totalBalance),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Balance",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
