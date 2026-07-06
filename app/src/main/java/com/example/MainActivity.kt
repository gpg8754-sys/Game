package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GameViewModel
import com.example.ui.components.GameHeader
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF1C1B1F)
                ) { innerPadding ->
                    StandoffClickerApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun StandoffClickerApp(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val ownedSkins by viewModel.ownedSkins.collectAsState()
    val marketListings by viewModel.marketListings.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val caseOpeningResult by viewModel.caseOpeningResult.collectAsState()

    // Automatic toast auto-clearing
    LaunchedEffect(uiMessage) {
        if (uiMessage != null) {
            delay(2500)
            viewModel.clearUiMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Common Header
            GameHeader(profile = profile)

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF4A4458))
            )

            // 2. Active Screen with smooth crossfade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            ) {
                when (currentTab) {
                    0 -> InventoryScreen(
                        viewModel = viewModel,
                        ownedSkins = ownedSkins
                    )
                    1 -> MarketScreen(
                        viewModel = viewModel,
                        marketListings = marketListings
                    )
                    2 -> profile?.let {
                        ClickerScreen(
                            viewModel = viewModel,
                            profile = it
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                    }
                    3 -> profile?.let {
                        GamesScreen(
                            viewModel = viewModel,
                            profile = it
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                    }
                    4 -> profile?.let {
                        StatsScreen(
                            viewModel = viewModel,
                            profile = it
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                    }
                }
            }

            // Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF4A4458))
            )

            NavigationBar(
                containerColor = Color(0xFF2B2930),
                modifier = Modifier.height(72.dp)
            ) {
                // Tab 0: Inventory
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Text("📦", fontSize = 20.sp) },
                    label = { Text("Инвентарь", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedTextColor = Color(0xFFCAC4D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )

                // Tab 1: Market
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Text("🛒", fontSize = 20.sp) },
                    label = { Text("Рынок", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedTextColor = Color(0xFFCAC4D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )

                // Tab 2: Clicker
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Text("⛏️", fontSize = 20.sp) },
                    label = { Text("Кликер", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedTextColor = Color(0xFFCAC4D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )

                // Tab 3: Games
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Text("🎮", fontSize = 20.sp) },
                    label = { Text("Игры", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedTextColor = Color(0xFFCAC4D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )

                // Tab 4: Stats/Settings
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Text("⚙️", fontSize = 20.sp) },
                    label = { Text("Профиль", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFD0BCFF),
                        unselectedIconColor = Color(0xFFCAC4D0),
                        selectedTextColor = Color(0xFFD0BCFF),
                        unselectedTextColor = Color(0xFFCAC4D0),
                        indicatorColor = Color(0xFF4A4458)
                    )
                )
            }
        }

        // 3. Floating Notification Toast Alert
        AnimatedVisibility(
            visible = uiMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
        ) {
            uiMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }

        // 4. Case Opening Roulette Dialog Overlay
        caseOpeningResult?.let { result ->
            CaseOpeningScreen(
                result = result,
                onClose = { viewModel.closeCaseOpeningDialog() },
                onQuickSell = { id -> viewModel.quickSellSkin(id) }
            )
        }
    }
}
