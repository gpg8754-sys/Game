package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    ownedSkins: List<OwnedSkinWithMetadata>,
    modifier: Modifier = Modifier
) {
    var selectedSkinForDetail by remember { mutableStateOf<OwnedSkinWithMetadata?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ИНВЕНТАРЬ",
                color = Color(0xFFE6E1E5),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Всего скинов: ${ownedSkins.size}",
                color = Color(0xFFCAC4D0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (ownedSkins.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "📦",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Инвентарь пуст",
                        color = Color(0xFFE6E1E5),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Перейдите на вкладку кликера, зарабатывайте серебро и открывайте кейсы, чтобы наполнить инвентарь легендарным оружием!",
                        color = Color(0xFFCAC4D0),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            // Inventory Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1.0f)
            ) {
                items(ownedSkins) { item ->
                    InventorySkinCard(item = item, onClick = {
                        selectedSkinForDetail = item
                    })
                }
            }
        }
    }

    // Interactive Detail Dialog
    selectedSkinForDetail?.let { item ->
        InventorySkinDetailDialog(
            item = item,
            onDismiss = { selectedSkinForDetail = null },
            onQuickSell = {
                viewModel.quickSellSkin(item.ownedSkin.id)
                selectedSkinForDetail = null
            },
            onListMarket = { price ->
                viewModel.sellOnMarket(item.ownedSkin.id, price)
                selectedSkinForDetail = null
            }
        )
    }
}

@Composable
fun InventorySkinCard(
    item: OwnedSkinWithMetadata,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val skin = item.skin
    val owned = item.ownedSkin

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B2930))
            .border(
                1.dp,
                if (owned.isListedOnMarket) Color(0xFFFF9800) else Color(0xFF4A4458),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Listed state overlay tag
        if (owned.isListedOnMarket) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFF9800), RoundedCornerShape(topStart = 12.dp, bottomEnd = 4.dp))
                    .align(Alignment.TopStart)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "РЫНОК",
                    color = Color.Black,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Rarity accent bottom line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(skin.rarity.color)
                .align(Alignment.BottomCenter)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (owned.isStattrak) {
                Text(
                    text = "StatTrak*",
                    color = Color(0xFFE91E63),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = skin.weapon,
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = skin.name,
                color = skin.rarity.color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun InventorySkinDetailDialog(
    item: OwnedSkinWithMetadata,
    onDismiss: () -> Unit,
    onQuickSell: () -> Unit,
    onListMarket: (Double) -> Unit
) {
    val skin = item.skin
    val owned = item.ownedSkin

    var marketPriceInput by remember { mutableStateOf("") }
    var isListingState by remember { mutableStateOf(false) }

    val sellMultiplier = if (owned.isStattrak) 1.5 else 1.0
    val isSilverSale = skin.rarity == Rarity.COMMON || skin.rarity == Rarity.UNCOMMON || skin.rarity == Rarity.RARE
    val sellValueText = if (isSilverSale) {
        "${(skin.baseValueSilver * 0.5 * sellMultiplier).toInt()} S"
    } else {
        "${String.format("%.1f", skin.baseValueGold * 0.4 * sellMultiplier)} G"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "close", tint = Color(0xFFCAC4D0))
                    }
                }

                // Header details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (owned.isStattrak) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE91E63), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "StatTrak*",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(skin.rarity.color, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skin.rarity.displayName.uppercase(),
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Graphic drawing (Geometric)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2B2930))
                        .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(skin.rarity.color.copy(alpha = 0.12f), Color.Transparent)
                                )
                            )
                    )
                    Text(
                        text = if (skin.isKnife) "🔪" else "🔫",
                        fontSize = 54.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = skin.weapon,
                    color = Color(0xFFE6E1E5),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = skin.name,
                    color = skin.rarity.color,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (owned.isListedOnMarket) {
                    // Already listed details
                    Text(
                        text = "Этот скин выставлен на продажу на рынке за:",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${owned.marketPrice} G",
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Вы можете отменить объявление на вкладке 'Рынок'",
                        color = Color(0xFFE91E63),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else if (!isListingState) {
                    // Normal actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sell Button
                        Button(
                            onClick = onQuickSell,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B2930),
                                contentColor = Color(0xFFFF8A80)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF4A4458)),
                            modifier = Modifier.weight(1.0f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = "sell", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("БЫСТРАЯ ПРОДАЖА", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(sellValueText, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Market list button
                        Button(
                            onClick = { isListingState = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            modifier = Modifier.weight(1.0f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = "market", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ПРОДАТЬ НА РЫНКЕ", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("В золоте (G)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Enter Market Price mode
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Установите цену на рынке в Gold (G):",
                            color = Color(0xFFE6E1E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Рекомендуемая цена: ${String.format("%.1f", skin.baseValueGold * 1.1)} G",
                            color = Color(0xFFCAC4D0),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = marketPriceInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                    marketPriceInput = input
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFF4A4458)
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("0.0 G", color = Color(0xFF4A4458)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isListingState = false },
                                modifier = Modifier.weight(1.0f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCAC4D0)),
                                border = BorderStroke(1.dp, Color(0xFF4A4458))
                            ) {
                                Text("ОТМЕНА", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val price = marketPriceInput.toDoubleOrNull()
                                    if (price != null && price > 0) {
                                        onListMarket(price)
                                    }
                                },
                                enabled = marketPriceInput.toDoubleOrNull() ?: 0.0 > 0.0,
                                modifier = Modifier.weight(1.0f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                )
                            ) {
                                Text("ВЫСТАВИТЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
