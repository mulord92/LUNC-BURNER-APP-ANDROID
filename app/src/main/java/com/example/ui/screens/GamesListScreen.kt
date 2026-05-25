package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameProgressEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark
import com.example.ui.theme.GoldPoints

@Composable
fun GamesListScreen(
    games: List<GameProgressEntity>,
    onPlayGame: (GameProgressEntity) -> Unit,
    onClaimPoints: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Topic Title exactly as in Screen 2 image:
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "BURN GAMING",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SOME FUN FOR YOU",
                    color = OrangeFlameBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // List of Games mapping
        items(games) { game ->
            val iconVector = when (game.gameId) {
                "crypto_runner" -> Icons.Default.SportsEsports
                "space_burner" -> Icons.Default.FlashOn
                "chain_linker" -> Icons.Default.Link
                "plane" -> Icons.Default.ConnectingAirports
                else -> Icons.Default.Games
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_item_card_${game.gameId}"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title/Icon and Claim status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Rounded game icon box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1B1B1F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = game.title,
                                    tint = OrangeFlameBright,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Title & Highscore info
                            Column {
                                Text(
                                    text = game.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "HIGHSCORE: ${game.highScore}",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Share + Claim Button section (matches Screen 2 look)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Share icon
                            IconButton(
                                onClick = { /* Share Simulation */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1F1F24))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // CLAIM / PLAY Button
                            val gradient = Brush.linearGradient(
                                colors = listOf(OrangeFlame, OrangeFlameBright, OrangeFlameDark)
                            )

                            if (game.claimed) {
                                Button(
                                    onClick = { onPlayGame(game) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1F)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp).testTag("play_game_btn_${game.gameId}"),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text(
                                        "REPLAY",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        // If score is enough, can claim. Otherwise warns client to run game first!
                                        if (game.highScore > 0 || game.progress > 0.5f) {
                                            onClaimPoints(game.gameId)
                                        } else {
                                            onPlayGame(game)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("claim_btn_${game.gameId}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(gradient)
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (game.highScore > 0 || game.progress > 0.5f) "CLAIM" else "PLAY",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Progress bar & Reward indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Styled progress bar on bottom left
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1C1C22))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(game.progress)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(OrangeFlame)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Points Reward Text Label ("+50 LUNC BURN")
                        Text(
                            text = "+ ${game.rewardPoints} LUNC BURN",
                            color = OrangeFlameBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
