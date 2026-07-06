package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PuzzleCard(
    val id: Int,
    val symbol: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun ElementMatchGame(
    onGameFinished: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isStarted by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableStateOf(0) }
    var movesCount by remember { mutableStateOf(0) }
    var cards by remember { mutableStateOf(emptyList<PuzzleCard>()) }
    var firstSelectedCardIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedCardIndex by remember { mutableStateOf<Int?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

    val symbols = listOf("🔪", "🔫", "💣", "🛡️", "📦", "💎", "🏆", "🔥")

    LaunchedEffect(isStarted) {
        if (isStarted) {
            val shuffledSymbols = (symbols + symbols).shuffled()
            cards = shuffledSymbols.mapIndexed { index, symbol ->
                PuzzleCard(id = index, symbol = symbol)
            }
            secondsElapsed = 0
            movesCount = 0
            firstSelectedCardIndex = null
            secondSelectedCardIndex = null
            isProcessing = false
            gameOver = false

            while (!gameOver) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    if (!isStarted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Widgets, contentDescription = "puzzle", tint = Color(0xFFFF9800), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🧩 СОЕДИНЕНИЕ ЭЛЕМЕНТОВ",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Игровое поле содержит 16 плиток (8 пар одинаковых символов оружия и скинов). Открывайте плитки по две штуки за раз и находите пары.\n\nПобедите как можно быстрее! Время прохождения определяет ценность награды.",
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { isStarted = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("НАЧАТЬ ПАЗЛ", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ходов: $movesCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Время: ${secondsElapsed}с",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE082),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cards.size) { index ->
                        val card = cards[index]
                        val showContent = card.isFlipped || card.isMatched || index == firstSelectedCardIndex || index == secondSelectedCardIndex

                        Box(
                            modifier = Modifier
                                .aspectRatio(1.0f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (card.isMatched) Color(0x334CAF50)
                                    else if (showContent) Color(0xFF4A4458)
                                    else Color(0xFF211F26)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (card.isMatched) Color(0xFF4CAF50)
                                            else if (showContent) Color(0xFFD0BCFF)
                                            else Color(0xFF4A4458),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !card.isMatched && !showContent && !isProcessing) {
                                    if (firstSelectedCardIndex == null) {
                                        firstSelectedCardIndex = index
                                    } else {
                                        secondSelectedCardIndex = index
                                        movesCount++
                                        isProcessing = true
                                        
                                        val firstCard = cards[firstSelectedCardIndex!!]
                                        val secondCard = cards[index]
                                        
                                        if (firstCard.symbol == secondCard.symbol) {
                                            cards = cards.toMutableList().apply {
                                                this[firstSelectedCardIndex!!] = firstCard.copy(isMatched = true)
                                                this[index] = secondCard.copy(isMatched = true)
                                            }
                                            firstSelectedCardIndex = null
                                            secondSelectedCardIndex = null
                                            isProcessing = false
                                            
                                            if (cards.all { it.isMatched }) {
                                                gameOver = true
                                                onGameFinished(secondsElapsed)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                delay(800)
                                                firstSelectedCardIndex = null
                                                secondSelectedCardIndex = null
                                                isProcessing = false
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (showContent) {
                                Text(
                                    text = card.symbol,
                                    fontSize = 24.sp
                                )
                            } else {
                                Text(
                                    text = "❓",
                                    fontSize = 18.sp,
                                    color = Color(0xFFCAC4D0)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Соединяйте плитки с одинаковыми рисунками",
                fontSize = 10.sp,
                color = Color(0xFFCAC4D0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GamesScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    modifier: Modifier = Modifier
) {
    var activeGameId by remember { mutableStateOf<String?>(null) } // "target", "reaction", "tap_rush", "puzzle"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(16.dp)
    ) {
        if (activeGameId == null) {
            // Main menu of games
            Text(
                text = "МИНИ-ИГРЫ",
                color = Color(0xFFE6E1E5),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Зарабатывайте золото, серебро и кейсы за победы!",
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Game 1: Shooting Range (Тир)
                GameSelectionCard(
                    title = "🎯 Тренировочный Тир",
                    desc = "Тапайте по мишеням на сетке 3х3 до истечения времени. Развивает точность!",
                    highScoreText = "Рекорд: ${profile.highestTargetScore} очк.",
                    rewardText = "Награда: Silver за мишени. 30+ очков = Origin Кейс. 45+ = Furious Кейс!",
                    accentColor = Color(0xFF2196F3),
                    onClick = { activeGameId = "target" }
                )

                // Game 2: Reaction Time (Тест реакции)
                GameSelectionCard(
                    title = "⚡ Проверка Реакции",
                    desc = "Ждите зеленого экрана и кликайте максимально быстро. Проверьте свой рефлекс!",
                    highScoreText = "Рекорд: ${if (profile.highestReactionTimeMs == 9999) "—" else "${profile.highestReactionTimeMs} мс"}",
                    rewardText = "Награда: <280мс = Silver. <200мс = 20 Gold + Furious Кейс. <150мс = 40 Gold + Sharp Кейс!",
                    accentColor = Color(0xFF4CAF50),
                    onClick = { activeGameId = "reaction" }
                )

                // Game 3: Tap Rush (Скорострельность)
                GameSelectionCard(
                    title = "🖱️ Быстрая Стрельба",
                    desc = "Кликайте по кнопке как можно чаще за 10 секунд. Предел ваших возможностей!",
                    highScoreText = "Рекорд: ${profile.highestTapRushScore} тапов",
                    rewardText = "Награда: Silver за каждый тап. 60+ = 5 Gold. 90+ = 20 Gold + Empire Кейс!",
                    accentColor = Color(0xFFE91E63),
                    onClick = { activeGameId = "tap_rush" }
                )

                // Game 4: Element Match Puzzle (Пазл)
                GameSelectionCard(
                    title = "🧩 Соединение Элементов",
                    desc = "Находите одинаковые пары оружия на поле 4х4. Потренируйте память!",
                    highScoreText = "Пазл",
                    rewardText = "Награда: 150 Silver. <40с = 5 Gold. <25с = 20 Gold + Furious Кейс. <15с = 40 Gold + Sharp Кейс!",
                    accentColor = Color(0xFFFF9800),
                    onClick = { activeGameId = "puzzle" }
                )
            }
        } else {
            // Interactive running game panel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { activeGameId = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (activeGameId) {
                        "target" -> "ТРЕНИРОВОЧНЫЙ ТИР"
                        "reaction" -> "ТЕСТ СКОРОСТИ РЕАКЦИИ"
                        "tap_rush" -> "БЫСТРАЯ СТРЕЛЬБА"
                        else -> "СОЕДИНЕНИЕ ЭЛЕМЕНТОВ"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(24.dp))
            ) {
                when (activeGameId) {
                    "target" -> TargetShootingGame(onGameFinished = { score ->
                        viewModel.rewardMiniGame(score, "target_shooting")
                        activeGameId = null
                    })
                    "reaction" -> ReactionTimeGame(onGameFinished = { timeMs ->
                        viewModel.rewardMiniGame(timeMs, "reaction_check")
                        activeGameId = null
                    })
                    "tap_rush" -> TapRushGame(onGameFinished = { tapsCount ->
                        viewModel.rewardMiniGame(tapsCount, "tap_rush")
                        activeGameId = null
                    })
                    "puzzle" -> ElementMatchGame(onGameFinished = { seconds ->
                        viewModel.rewardMiniGame(seconds, "element_puzzle")
                        activeGameId = null
                    })
                }
            }
        }
    }
}

@Composable
fun GameSelectionCard(
    title: String,
    desc: String,
    highScoreText: String,
    rewardText: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = highScoreText,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = desc,
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C1B1F))
                    .padding(8.dp)
            ) {
                Text(
                    text = rewardText,
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// GAME 1: Target Shooting
@Composable
fun TargetShootingGame(
    onGameFinished: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isStarted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(25) }
    
    // Grid cells state: true if target exists
    var targetsState by remember { mutableStateOf(List(9) { false }) }

    // Start timer & spawner
    LaunchedEffect(isStarted) {
        if (isStarted) {
            // Count down timer
            launch {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                }
                onGameFinished(score)
            }

            // Target spawner
            launch {
                while (timeLeft > 0) {
                    delay(800) // spawn target every 800ms
                    // Select a random empty cell
                    val emptyIndices = targetsState.mapIndexedNotNull { index, hasTarget ->
                        if (!hasTarget) index else null
                    }
                    if (emptyIndices.isNotEmpty()) {
                        val indexToSpawn = emptyIndices.random()
                        targetsState = targetsState.toMutableList().apply {
                            this[indexToSpawn] = true
                        }
                        
                        // Let it expire after 1300ms
                        launch {
                            delay(1300)
                            targetsState = targetsState.toMutableList().apply {
                                this[indexToSpawn] = false
                            }
                        }
                    }
                }
            }
        }
    }

    if (!isStarted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "target", tint = Color(0xFF2196F3), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎯 ПРАВИЛА ИГРЫ",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "На экране в сетке 3х3 будут случайно появляться мишени. Успейте кликнуть по ним до того, как они исчезнут! Каждое попадание приносит очки.\n\nВремя игры: 25 сек.",
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { isStarted = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("НАЧАТЬ ИГРУ", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Очки: $score",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Время: ${timeLeft}с",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeft < 5) Color(0xFFFF5252) else Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3x3 Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1C1B1F))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(9) { index ->
                        val hasTarget = targetsState[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(1.0f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (hasTarget) Color(0x332196F3) else Color(0xFF2B2930))
                                .border(
                                    1.dp,
                                    if (hasTarget) Color(0xFF2196F3) else Color(0xFF4A4458),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (hasTarget) {
                                        score++
                                        targetsState = targetsState
                                            .toMutableList()
                                            .apply {
                                                this[index] = false
                                            }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasTarget) {
                                // Draw target symbol
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5252))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// GAME 2: Reaction Time
enum class ReactionPhase {
    IDLE, WAITING, READY_GREEN, OUTCOME, EARLY_FAIL
}

@Composable
fun ReactionTimeGame(
    onGameFinished: (Int) -> Unit
) {
    var phase by remember { mutableStateOf(ReactionPhase.IDLE) }
    var startTime by remember { mutableStateOf(0L) }
    var reactionTimeMs by remember { mutableStateOf(0) }

    val gameBgColor = when (phase) {
        ReactionPhase.WAITING -> Color(0xFF422132) // deep purple/red warning
        ReactionPhase.READY_GREEN -> Color(0xFF1B5E20) // green
        ReactionPhase.EARLY_FAIL -> Color(0xFFB71C1C) // red
        else -> Color(0xFF2B2930)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gameBgColor)
            .clickable {
                when (phase) {
                    ReactionPhase.WAITING -> {
                        // User clicked too early!
                        phase = ReactionPhase.EARLY_FAIL
                    }
                    ReactionPhase.READY_GREEN -> {
                        val endTime = System.currentTimeMillis()
                        reactionTimeMs = (endTime - startTime).toInt()
                        phase = ReactionPhase.OUTCOME
                    }
                    else -> {}
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            when (phase) {
                ReactionPhase.IDLE -> {
                    Icon(Icons.Default.FlashOn, contentDescription = "reaction", tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "⚡ ТЕСТ РЕАКЦИИ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите старт, дождитесь когда экран загорится зеленым и кликните по экрану максимально быстро! Чем меньше время реакции, тем круче приз.",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            phase = ReactionPhase.WAITING
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("НАЧАТЬ ТЕСТ", fontWeight = FontWeight.Bold)
                    }
                }
                ReactionPhase.WAITING -> {
                    Text(
                        text = "ЖДИТЕ ЗЕЛЕНЫЙ ЭКРАН...",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Тапните при смене цвета на зеленый!",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Spawner reaction timer
                    LaunchedEffect(Unit) {
                        val waitTime = Random.nextLong(2000, 5000)
                        delay(waitTime)
                        if (phase == ReactionPhase.WAITING) {
                            phase = ReactionPhase.READY_GREEN
                            startTime = System.currentTimeMillis()
                        }
                    }
                }
                ReactionPhase.READY_GREEN -> {
                    Text(
                        text = "КЛИКАЙТЕ СЕЙЧАС!!!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
                ReactionPhase.EARLY_FAIL -> {
                    Text(
                        text = "СЛИШКОМ РАНО! ❌",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Вы поспешили. Дождитесь зеленого цвета перед тапом.",
                        color = Color(0xFFCAC4D0),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Button(
                        onClick = { phase = ReactionPhase.WAITING },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ПОПРОБОВАТЬ СНОВА", fontWeight = FontWeight.Bold)
                    }
                }
                ReactionPhase.OUTCOME -> {
                    Text(
                        text = "ВАШ РЕЗУЛЬТАТ:",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$reactionTimeMs мс",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    val rating = when {
                        reactionTimeMs < 150 -> "Нечеловеческая скорость! (Global Elite)"
                        reactionTimeMs < 200 -> "Превосходная реакция! (Legendary Eagle)"
                        reactionTimeMs < 280 -> "Хорошая реакция. (Master Guardian)"
                        else -> "Нужно больше тренироваться! (Silver)"
                    }

                    Text(
                        text = rating,
                        color = Color(0xFFD0BCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onGameFinished(reactionTimeMs) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ПОЛУЧИТЬ НАГРАДУ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// GAME 3: Tap Rush (Скорострельность)
@Composable
fun TapRushGame(
    onGameFinished: (Int) -> Unit
) {
    var isStarted by remember { mutableStateOf(false) }
    var tapsCount by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(10) }

    LaunchedEffect(isStarted) {
        if (isStarted) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            onGameFinished(tapsCount)
        }
    }

    if (!isStarted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Gesture, contentDescription = "rush", tint = Color(0xFFE91E63), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🖱️ СКОРОСТРЕЛЬНОСТЬ",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Простая и захватывающая игра! После старта тапайте по огромной круглой кнопке как можно быстрее. Постарайтесь набрать максимальное количество тапов за 10 секунд!",
                color = Color(0xFFCAC4D0),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { isStarted = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("НАЧАТЬ КЛИКИ", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Кликов: $tapsCount",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Осталось: ${timeLeft}с",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE082),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Big tap button
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFE91E63), Color(0xFF880E4F))
                        )
                    )
                    .clickable {
                        tapsCount++
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "КЛИКАЙ!!!",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Simple rating hint
            Text(
                text = when {
                    tapsCount > 85 -> "Идеальный зажим! (Global)"
                    tapsCount > 60 -> "Отличный спрей! (Legendary)"
                    tapsCount > 30 -> "Хороший темп! (Guardian)"
                    else -> "Тапай быстрее! (Silver)"
                },
                color = Color(0xFFD0BCFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
