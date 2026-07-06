package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CaseOpeningScreen(
    result: CaseOpeningRollResult,
    onClose: () -> Unit,
    onQuickSell: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Setup roulette skins
    val rouletteSkins = remember {
        val list = mutableListOf<Skin>()
        // Fill first 34 items with random skins of lower rarities
        val allSkins = SkinData.skins
        for (i in 0 until 34) {
            list.add(allSkins.random())
        }
        // Item 35 is our actual result!
        result.pulledSkin?.let {
            list.add(it)
        } ?: list.add(allSkins.random())
        // Add 5 more random skins behind it
        for (i in 0 until 5) {
            list.add(allSkins.random())
        }
        list
    }

    var isScrollFinished by remember { mutableStateOf(false) }

    // Start rolling scroll animation
    LaunchedEffect(result.isRolling) {
        if (result.isRolling) {
            // Wait brief moment, then start scroll
            delay(100)
            listState.animateScrollToItem(
                index = 34, // aligns our pulled skin near the center
                scrollOffset = -150
            )
            isScrollFinished = true
        }
    }

    Dialog(
        onDismissRequest = { if (isScrollFinished) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = isScrollFinished,
            dismissOnClickOutside = isScrollFinished,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6100F12)),
            contentAlignment = Alignment.Center
        ) {
            // Ambient glow behind
            val activeGlowColor = if (isScrollFinished && result.pulledSkin != null) {
                result.pulledSkin.rarity.color
            } else {
                Color(0xFFD0BCFF)
            }

            Box(
                modifier = Modifier
                    .size(340.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(activeGlowColor.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (!isScrollFinished) "ОТКРЫТИЕ КЕЙСА..." else "ПОЛУЧЕН НОВЫЙ СКИН!",
                    color = Color(0xFFD0BCFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The Roulette Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF1C1B1F))
                        .border(1.dp, Color(0xFF4A4458))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Items List
                    LazyRow(
                        state = listState,
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 160.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(rouletteSkins) { index, skin ->
                            RouletteItemCard(
                                skin = skin,
                                isTarget = index == 34 && isScrollFinished
                            )
                        }
                    }

                    // Red Target Indicator line (Center of roulette)
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(Color(0xFFFF5252))
                    ) {}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detailed pulled skin outcome
                AnimatedVisibility(
                    visible = isScrollFinished && result.pulledSkin != null,
                    enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
                    exit = fadeOut()
                ) {
                    val skin = result.pulledSkin!!
                    val valueText = if (skin.rarity == Rarity.COMMON || skin.rarity == Rarity.UNCOMMON || skin.rarity == Rarity.RARE) {
                        val base = skin.baseValueSilver * 0.5
                        "${if (result.isStattrak) (base * 1.5).toInt() else base.toInt()} S"
                    } else {
                        val base = skin.baseValueGold * 0.4
                        "${String.format("%.1f", if (result.isStattrak) base * 1.5 else base)} G"
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (result.isStattrak) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE91E63), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "StatTrak*",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = skin.weapon,
                            color = Color(0xFFE6E1E5),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = skin.name,
                            color = skin.rarity.color,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Actions Area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Quick sell Button
                            Button(
                                onClick = {
                                    onQuickSell(result.ownedSkinId)
                                    onClose()
                                },
                                modifier = Modifier.weight(1.0f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2B2930),
                                    contentColor = Color(0xFFFF8A80)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF4A4458))
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = "sell", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ПРОДАТЬ ($valueText)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Keep skin Button
                            Button(
                                onClick = onClose,
                                modifier = Modifier.weight(1.0f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "keep", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "В ИНВЕНТАРЬ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouletteItemCard(
    skin: Skin,
    isTarget: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isTarget) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "target_scale"
    )

    Box(
        modifier = modifier
            .size(width = 90.dp, height = 90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B2930))
            .border(
                width = if (isTarget) 2.dp else 1.dp,
                color = if (isTarget) skin.rarity.color else Color(0xFF4A4458),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rarity bottom color stripe
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
            Text(
                text = skin.weapon,
                color = Color(0xFFCAC4D0),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = skin.name,
                color = skin.rarity.color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}
