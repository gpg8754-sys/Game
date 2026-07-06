package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProfile

@Composable
fun GameHeader(
    profile: PlayerProfile?,
    modifier: Modifier = Modifier
) {
    val level = profile?.level ?: 1
    val xp = profile?.xp ?: 0
    val xpNeeded = level * 100
    val progress = xp.toFloat() / xpNeeded.toFloat()

    val rankName = when {
        level <= 2 -> "Silver I"
        level <= 4 -> "Silver II"
        level <= 6 -> "Silver III"
        level <= 8 -> "Gold Nova I"
        level <= 10 -> "Gold Nova II"
        level <= 12 -> "Gold Nova III"
        level <= 14 -> "Master Guardian"
        level <= 17 -> "Legendary Eagle"
        level <= 21 -> "Supreme Master"
        else -> "Global Elite"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1F))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Profile Details
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile Initials / Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A4458))
                    .border(2.dp, Color(0xFFD0BCFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (profile?.name?.take(2)?.uppercase() ?: "P1"),
                    color = Color(0xFFD0BCFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = profile?.name ?: "Player1337",
                    color = Color(0xFFE6E1E5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Lvl $level",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF4A4458),
                        fontSize = 11.sp
                    )
                    Text(
                        text = rankName,
                        color = Color(0xFFD0BCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Currency Details (Silver & Gold)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // GOLD Balance
            Box(
                modifier = Modifier
                    .background(Color(0xFF2B2930), RoundedCornerShape(50))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "G",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format("%.2f", profile?.gold ?: 0.0),
                        color = Color(0xFFE6E1E5),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // SILVER Balance
            Box(
                modifier = Modifier
                    .background(Color(0xFF2B2930), RoundedCornerShape(50))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "S",
                        color = Color(0xFFC0C0C0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format("%.1f", profile?.silver ?: 0.0),
                        color = Color(0xFFE6E1E5),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
