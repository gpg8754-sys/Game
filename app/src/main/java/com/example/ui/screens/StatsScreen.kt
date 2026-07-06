package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
fun StatsScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    modifier: Modifier = Modifier
) {
    var nameInput by remember { mutableStateOf(profile.name) }
    var isConfirmResetDialogShowing by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ПРОФИЛЬ И СТАТИСТИКА",
            color = Color(0xFFE6E1E5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        // Rename profile card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2B2930))
                .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ИЗМЕНИТЬ НИКНЕЙМ",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.weight(1.0f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF4A4458)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        placeholder = { Text("Введите никнейм...", color = Color(0xFF4A4458), fontSize = 13.sp) }
                    )

                    Button(
                        onClick = { viewModel.changePlayerName(nameInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ОК", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2B2930))
                .border(1.dp, Color(0xFF4A4458), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ИГРОВАЯ СТАТИСТИКА",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Divider(color = Color(0xFF4A4458))

                StatRow(label = "Всего кликов по экрану", value = profile.totalClicks.toString())
                StatRow(label = "Открыто оружейных кейсов", value = profile.totalCasesOpened.toString())
                StatRow(label = "Сыграно мини-игр", value = profile.totalMiniGamesPlayed.toString())
                
                Divider(color = Color(0xFF4A4458))

                StatRow(
                    label = "Рекорд в тире", 
                    value = if (profile.highestTargetScore == 0) "—" else "${profile.highestTargetScore} миш."
                )
                StatRow(
                    label = "Рекорд скорости реакции", 
                    value = if (profile.highestReactionTimeMs == 9999) "—" else "${profile.highestReactionTimeMs} мс"
                )
                StatRow(
                    label = "Рекорд скорострельности", 
                    value = if (profile.highestTapRushScore == 0) "—" else "${profile.highestTapRushScore} тапов"
                )
            }
        }

        // Dangerous Actions Area (Reset)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1AFF5252))
                .border(1.dp, Color(0x33FF5252), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ОПАСНАЯ ЗОНА",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Сброс сотрет все ваши добытые скины, накопленную голду, серебро, рекорды и уровень.",
                    color = Color(0xFFCAC4D0),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = { isConfirmResetDialogShowing = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("СБРОСИТЬ ПРОГРЕСС", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Confirm dialog
    if (isConfirmResetDialogShowing) {
        AlertDialog(
            onDismissRequest = { isConfirmResetDialogShowing = false },
            title = { Text("Сбросить весь прогресс?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("Вы действительно хотите обнулить всю игровую статистику, скины и балансы? Это действие необратимо.", color = Color(0xFFCAC4D0)) },
            containerColor = Color(0xFF2B2930),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetProgress()
                        isConfirmResetDialogShowing = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("СБРОСИТЬ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { isConfirmResetDialogShowing = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("ОТМЕНА")
                }
            }
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFCAC4D0),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
