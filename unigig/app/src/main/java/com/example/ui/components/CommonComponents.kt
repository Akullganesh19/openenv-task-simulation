package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandMint
import com.example.ui.theme.BrandRose
import com.example.ui.theme.TextPrimary

@Composable
fun CoinBadge(
    amount: Int,
    label: String? = null,
    badgeColor: Color = BrandCyan,
    showUsd: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Issue #39: Account for fixed currency exchange rate (1 Coin = $0.10 USD)
    val usdEquivalent = amount * 0.10f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(badgeColor.copy(alpha = 0.12f))
            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = "UniGig Coins",
            tint = badgeColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amount",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (label != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (showUsd) {
                Text(
                    text = "≈ $${String.format("%.2f", usdEquivalent)} USD",
                    color = badgeColor.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun UrgencyBadge(urgency: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon, multiplierLabel) = when (urgency) {
        "CRITICAL" -> Quadruple(BrandRose.copy(alpha = 0.15f), BrandRose, Icons.Default.Bolt, "1.6x")
        "RUSH" -> Quadruple(BrandAmber.copy(alpha = 0.15f), BrandAmber, Icons.Default.Warning, "1.3x")
        else -> Quadruple(BrandMint.copy(alpha = 0.15f), BrandMint, Icons.Default.CheckCircle, "1.0x")
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$urgency ($multiplierLabel)",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EscrowStatusPill(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        "IN_PROGRESS" -> Pair(BrandCyan, "Escrow Locked")
        "COMPLETED" -> Pair(BrandMint, "Escrow Settled")
        "DISPUTED" -> Pair(BrandRose, "Escrow In Dispute")
        else -> Pair(BrandAmber, "Bidding Active")
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OfflineSyncBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BrandMint.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Syncing",
            tint = BrandMint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Offline Mode Active • ElectricSQL local cache synced with Room DB",
            fontSize = 11.sp,
            color = BrandMint,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
