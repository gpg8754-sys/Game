package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FloatingText(
    val id: Long,
    val text: String,
    val offset: Offset,
    val color: Color = Color(0xFFD0BCFF)
)

@Composable
fun ClickerScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isClicked by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Cases, 1: Upgrades

    // Scaling animation for clicks
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "click_scale"
    )

    // Floating text list
    var floatingTexts by remember { mutableStateOf(listOf<FloatingText>()) }
    var nextTextId by remember { mutableStateOf(0L) }

    // Level details
    val level = profile.level
    val xp = profile.xp
    val xpNeeded = level * 100
    val progress = xp.toFloat() / xpNeeded.toFloat()

    val levelProgressPercent = (progress * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(top = 8.dp)
    ) {
        // Upper Progress Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ранг: Silver III",
                    fontSize = 11.sp,
                    color = Color(0xFFCAC4D0),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "$levelProgressPercent%",
                    fontSize = 11.sp,
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color(0xFF4A4458))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0.0f, 1.0f))
                        .background(Color(0xFFD0BCFF))
                )
            }
        }

        // Center Clicker Area (With floating items/particles)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow backdrop
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1AD0BCFF), Color.Transparent),
                            center = Offset.Unspecified
                        )
                    )
            )

            // Dynamic rotation animation for background geometric effect
            val infiniteTransition = rememberInfiniteTransition(label = "rotation")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(25000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation_angle"
            )

            // Floating Clicker Item
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(scale)
                    .rotate(angle * 0.05f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.performClick()
                        isClicked = true
                        
                        // Spawn floating text
                        val randomX = Random.nextInt(-60, 60).toFloat()
                        val randomY = Random.nextInt(-40, 40).toFloat()
                        val textId = nextTextId++
                        val profit = profile.clickUpgradeLevel * 1.5
                        val newFloatingText = FloatingText(
                            id = textId,
                            text = "+${String.format("%.1f", profit)} S",
                            offset = Offset(randomX, randomY)
                        )
                        floatingTexts = floatingTexts + newFloatingText

                        coroutineScope.launch {
                            delay(800)
                            floatingTexts = floatingTexts.filter { it.id != textId }
                        }

                        coroutineScope.launch {
                            delay(80)
                            isClicked = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Diagonal accent lines (Geometric style)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color(0x1AD0BCFF),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0x1AD0BCFF),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5f
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GOLDEN",
                        color = Color(0x1F9C27B0),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "M9 BAYONET",
                        color = Color(0xFFD0BCFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "КЛИКНИ ДЛЯ КРАФТА",
                        color = Color(0xFFCAC4D0),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                }

                // Inner hint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Клик: +${String.format("%.1f", profile.clickUpgradeLevel * 1.5)} S",
                        color = Color(0xFFD0BCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Render floating texts
            floatingTexts.forEach { ft ->
                key(ft.id) {
                    FloatingTextAnimation(floatingText = ft)
                }
            }
        }

        // Sub Navigation Tabs: Cases & Upgrades
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { activeSubTab = 0 },
                modifier = Modifier.weight(1.0f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 0) Color(0xFF4A4458) else Color(0xFF2B2930),
                    contentColor = if (activeSubTab == 0) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4A4458))
            ) {
                Text(
                    text = "📦 КЕЙСЫ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Button(
                onClick = { activeSubTab = 1 },
                modifier = Modifier.weight(1.0f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == 1) Color(0xFF4A4458) else Color(0xFF2B2930),
                    contentColor = if (activeSubTab == 1) Color(0xFFD0BCFF) else Color(0xFFCAC4D0)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4A4458))
            ) {
                Text(
                    text = "⛏️ УЛУЧШЕНИЯ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area (Grid or Lists)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .background(Color(0xFF151417))
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(16.dp)
        ) {
            if (activeSubTab == 0) {
                // Cases list
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(CaseData.cases) { case ->
                        CaseCard(case = case, onBuyClick = {
                            viewModel.openCase(case.id)
                        })
                    }
                }
            } else {
                // Upgrades list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Click Multiplier
                    item {
                        val clickCost = 100.0 * Math.pow(1.6, (profile.clickUpgradeLevel - 1).toDouble())
                        UpgradeItemRow(
                            title = "Усилитель клика",
                            desc = "Увеличивает доход за каждый клик на +1.5 S",
                            currentLevel = "Ур. ${profile.clickUpgradeLevel}",
                            costText = "${String.format("%.0f", clickCost)} S",
                            costColor = Color(0xFFC0C0C0),
                            onUpgradeClick = { viewModel.buyClickUpgrade() }
                        )
                    }

                    // Auto clicker
                    item {
                        val autoCost = 10.0 * Math.pow(1.8, profile.autoClickUpgradeLevel.toDouble())
                        UpgradeItemRow(
                            title = "Авто-добытчик",
                            desc = "Пассивный заработок: +0.5 S в секунду",
                            currentLevel = "Ур. ${profile.autoClickUpgradeLevel}",
                            costText = "${String.format("%.1f", autoCost)} G",
                            costColor = Color(0xFFFFD700),
                            onUpgradeClick = { viewModel.buyAutoClickUpgrade() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingTextAnimation(floatingText: FloatingText) {
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            animY.animateTo(
                targetValue = -120f,
                animationSpec = tween(700, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            delay(400)
            animAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .offset(
                x = floatingText.offset.x.dp,
                y = (floatingText.offset.y + animY.value).dp
            )
    ) {
        Text(
            text = floatingText.text,
            color = floatingText.color.copy(alpha = animAlpha.value),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            style = LocalTextStyle.current.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    offset = Offset(1f, 1f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
fun CaseCard(
    case: Case,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val costText = if (case.costGold > 0) "${case.costGold} G" else "${case.costSilver.toInt()} S"
    val costColor = if (case.costGold > 0) Color(0xFFFFD700) else Color(0xFFC0C0C0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
            .clickable { onBuyClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Case Box Drawing
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF1C1B1F), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Small geometry graphic
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFD0BCFF), Color(0xFF381E72))
                        ),
                        size = size
                    )
                }
                Text(
                    text = "📦",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = case.name,
                color = Color(0xFFE6E1E5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF1C1B1F), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = costText,
                    color = costColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun UpgradeItemRow(
    title: String,
    desc: String,
    currentLevel: String,
    costText: String,
    costColor: Color,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = Color(0xFFE6E1E5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF4A4458), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = currentLevel,
                            color = Color(0xFFD0BCFF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = Color(0xFFCAC4D0),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onUpgradeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = costText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF381E72),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
