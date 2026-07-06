package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@Composable
fun MarketScreen(
    viewModel: GameViewModel,
    marketListings: List<MarketListingWithMetadata>,
    modifier: Modifier = Modifier
) {
    var marketSubTab by remember { mutableStateOf(0) } // 0: All, 1: My Listings
    var searchQuery by remember { mutableStateOf("") }

    // Filtered listings
    val displayedListings = remember(marketListings, marketSubTab, searchQuery) {
        marketListings.filter { listingWithMeta ->
            val isCorrectTab = if (marketSubTab == 0) {
                // Show ALL except user listed if we want, or show all including user listed
                // Let's show all items, but mark user listed with a different indicator
                true
            } else {
                // User listed only
                !listingWithMeta.listing.isSimulated
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val skin = listingWithMeta.skin
                skin.name.contains(searchQuery, ignoreCase = true) || 
                skin.weapon.contains(searchQuery, ignoreCase = true)
            }

            isCorrectTab && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(16.dp)
    ) {
        Text(
            text = "РЫНОК СКИНОВ",
            color = Color(0xFFE6E1E5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Market Subtabs Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { marketSubTab = 0 },
                modifier = Modifier.weight(1.0f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (marketSubTab == 0) Color(0xFF4A4458) else Color(0xFF2B2930),
                    contentColor = if (marketSubTab == 0) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Все лоты", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { marketSubTab = 1 },
                modifier = Modifier.weight(1.0f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (marketSubTab == 1) Color(0xFF4A4458) else Color(0xFF2B2930),
                    contentColor = if (marketSubTab == 1) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Мои лоты", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Commission disclaimer warning
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0DD0BCFF))
                .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "info",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Комиссия рынка составляет 20%. При покупке вашего скина симулятором вы получите 80% от установленной цены.",
                    color = Color(0xFFCAC4D0),
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск скина (например, AWM)...", fontSize = 12.sp, color = Color(0xFF4A4458)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search", tint = Color(0xFF4A4458)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color(0xFF4A4458)
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (displayedListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (marketSubTab == 1) "У вас нет активных лотов на продаже." else "Лоты не найдены.",
                    color = Color(0xFFCAC4D0),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Listings List
            LazyColumn(
                modifier = Modifier.weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedListings) { listingWithMeta ->
                    MarketListingRow(
                        item = listingWithMeta,
                        isMyListing = !listingWithMeta.listing.isSimulated,
                        onBuyClick = { viewModel.buyMarketListing(listingWithMeta.listing.id) },
                        onCancelClick = { viewModel.cancelMarketListing(listingWithMeta.listing.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketListingRow(
    item: MarketListingWithMetadata,
    isMyListing: Boolean,
    onBuyClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = item.skin
    val listing = item.listing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, if (isMyListing) Color(0xFFFF9800) else Color(0xFF4A4458), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left details (Skin preview, weapon details, seller)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1.1f)
            ) {
                // Geometric rarity avatar box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C1B1F))
                        .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(skin.rarity.color.copy(alpha = 0.15f))
                    )
                    Text(
                        text = if (skin.isKnife) "🔪" else "🔫",
                        fontSize = 18.sp
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = skin.weapon,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(skin.rarity.color, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = skin.rarity.displayName.uppercase(),
                                color = Color.Black,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = skin.name,
                        color = skin.rarity.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Продавец: ${listing.sellerName}",
                        color = Color(0xFFCAC4D0),
                        fontSize = 8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right details (Price + Buy/Cancel action button)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(0.9f)
            ) {
                // Price display in Gold
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "G",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.2f", listing.price),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Buy / Cancel button
                if (isMyListing) {
                    Button(
                        onClick = onCancelClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1B1F),
                            contentColor = Color(0xFFFF8A80)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF4A4458)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Снять", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onBuyClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Купить", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
