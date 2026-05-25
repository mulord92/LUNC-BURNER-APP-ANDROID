package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BurnLogEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark

@Composable
fun ProofScreen(
    burnLogs: List<BurnLogEntity>
) {
    val context = LocalContext.current
    var showAchievementsModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Main central glowing card exactly as Screen 6:
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("burn_station_card"),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Flame icon in orange background ring
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(OrangeFlame.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Burn Station",
                            tint = OrangeFlameBright,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "BURN STATION",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You can see everything on the blockchain.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val gradient = Brush.linearGradient(
                        colors = listOf(OrangeFlame, OrangeFlameBright, OrangeFlameDark)
                    )

                    // achievements check button
                    Button(
                        onClick = {
                            showAchievementsModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("check_achievements_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "CHECK OUT OUR ACHIEVEMENTS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Subtitle header: MONTHLY BURN LOG
        item {
            Text(
                text = "MONTHLY BURN LOG",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // List of Month logs
        items(burnLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("burn_log_item_${log.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = log.monthText.uppercase(),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = log.txId,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = log.destroyedText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "DESTROYED",
                            color = OrangeFlameBright,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }

    // Achievements Dialog box modal
    if (showAchievementsModal) {
        AlertDialog(
            onDismissRequest = { showAchievementsModal = false },
            confirmButton = {
                TextButton(onClick = { showAchievementsModal = false }) {
                    Text("SECURE LOCK", color = OrangeFlameBright)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Achievements",
                        tint = OrangeFlameBright
                    )
                    Text("GLOBAL ACHIEVEMENTS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "LUNC burning stats are certified on-chain with decentralized logs.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Divider(color = Color(0xFF2E2E33))

                    val stats = listOf(
                        Pair("Total LUNC Burned", "25.4 Billion"),
                        Pair("Active Node Workers", "482,900 Nodes"),
                        Pair("Market Peak Destroys", "4.2 Billion/mo"),
                        Pair("Decentralized Rank", "Global Class A")
                    )

                    stats.forEach { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stat.first, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(stat.second, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = Color(0xFF18181B),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}
