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
import androidx.compose.ui.text.style.TextAlign
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
    onClaimPoints: (String) -> Unit,
    onAwardPoints: (Int) -> Unit
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SCORE: ${game.lastScore}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color.Gray.copy(alpha = 0.6f))
                                    )
                                    Text(
                                        text = "HIGHSCORE: ${game.highScore}",
                                        color = OrangeFlameBright,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
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

        // SECTION HEADER: Community Focus & Hub
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "COMMUNITY HUB",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "LUNC COMMUNITY-DRIVEN FOCUS",
                    color = OrangeFlameBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // 1. Community Focus Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("community_focus_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OrangeFlame.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Community Focus",
                                tint = OrangeFlameBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Community Focus",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Core Supply Destroy Target",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Goal statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BURNED IN TOTAL",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "124.52B / 150B (83%)",
                            color = OrangeFlameBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1C1C22))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.83f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(OrangeFlame, OrangeFlameBright)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini milestone grids
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stake ratio
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C22)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("LUNC STAKED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("1.08T (15.6%)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Repeg progress
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C22)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("USTC REPEG", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("34.2% Stable", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Community Poll Card
        item {
            var selectedPollOption by remember { mutableStateOf<Int?>(null) }
            val hasVoted = selectedPollOption != null
            val totalVotesBase = 14520
            val totalVotes = if (hasVoted) totalVotesBase + 1 else totalVotesBase

            val voteOpt1 = if (selectedPollOption == 1) 61 else 60
            val voteOpt2 = if (selectedPollOption == 2) 26 else 25
            val voteOpt3 = if (selectedPollOption == 3) 14 else 15

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("community_poll_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OrangeFlame.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Leaderboard,
                                    contentDescription = "Poll",
                                    tint = OrangeFlameBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Community Poll",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Your voice matters",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "$totalVotes Votes",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Which mechanism should be prioritized as the next community-funded burn effort?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option cards
                    val options = listOf(
                        Triple(1, "Continuous on-chain tax burn expansion", voteOpt1),
                        Triple(2, "Interactive dApp and game integration rewards", voteOpt2),
                        Triple(3, "Decentralized validation fees destruction", voteOpt3)
                    )

                    options.forEach { opt ->
                        val activeSelection = selectedPollOption == opt.first
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeSelection) OrangeFlame.copy(alpha = 0.08f) else Color(0xFF1E1E24)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (activeSelection) OrangeFlame.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPollOption = opt.first }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Animated percentage indicator in background of card
                                if (hasVoted) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(opt.third / 100f)
                                            .background(OrangeFlame.copy(alpha = 0.15f))
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = opt.second,
                                        color = if (activeSelection) OrangeFlameBright else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = if (activeSelection) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (hasVoted) {
                                        Text(
                                            text = "${opt.third}%",
                                            color = if (activeSelection) OrangeFlameBright else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (hasVoted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thanks for voting! You can click any option to change your vote.",
                            color = OrangeFlameBright,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 3. Burning Quiz Card
        item {
            var selectedQuizAnswer by remember { mutableStateOf<Int?>(null) }
            var isQuizSubmitted by remember { mutableStateOf(false) }
            var pointsEarnedFromQuiz by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("burning_quiz_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OrangeFlame.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = "Quiz",
                                tint = OrangeFlameBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Burning Quiz",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Correct answers award +150 LUNC BURN",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "What was the peak circulating supply of Terra Classic (LUNC) following the historical de-pegging event in May 2022?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quiz Options
                    val quizOptions = listOf(
                        Pair(1, "A) 100 Billion LUNC"),
                        Pair(2, "B) 1 Trillion LUNC"),
                        Pair(3, "C) 6.9 Trillion LUNC"), // Correct
                        Pair(4, "D) 15 Trillion LUNC")
                    )

                    quizOptions.forEach { option ->
                        val isSelected = selectedQuizAnswer == option.first
                        val clickableEnabled = !isQuizSubmitted

                        val cardBg = when {
                            isQuizSubmitted && option.first == 3 -> Color(0xFF1B3A2C)
                            isQuizSubmitted && isSelected && option.first != 3 -> Color(0xFF4C1D1D)
                            isSelected -> OrangeFlame.copy(alpha = 0.08f)
                            else -> Color(0xFF1E1E24)
                        }

                        val cardBorderColor = when {
                            isQuizSubmitted && option.first == 3 -> Color(0xFF3CD070).copy(alpha = 0.5f)
                            isQuizSubmitted && isSelected && option.first != 3 -> Color(0xFFFF5F5F).copy(alpha = 0.5f)
                            isSelected -> OrangeFlame.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorderColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = clickableEnabled) { selectedQuizAnswer = option.first }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option.second,
                                    color = when {
                                        isQuizSubmitted && option.first == 3 -> Color(0xFF3CD070)
                                        isQuizSubmitted && isSelected && option.first != 3 -> Color(0xFFFF5F5F)
                                        isSelected -> OrangeFlameBright
                                        else -> Color.LightGray
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )

                                if (isQuizSubmitted && option.first == 3) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF3CD070),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isQuizSubmitted) {
                        Button(
                            onClick = {
                                if (selectedQuizAnswer != null) {
                                    isQuizSubmitted = true
                                    if (selectedQuizAnswer == 3) {
                                        pointsEarnedFromQuiz = true
                                        onAwardPoints(150)
                                    }
                                }
                            },
                            enabled = selectedQuizAnswer != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeFlame,
                                disabledContainerColor = Color(0xFF1E1E24)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("quiz_submit_button")
                        ) {
                            Text(
                                "SUBMIT ANSWER",
                                color = if (selectedQuizAnswer != null) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Explanation state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (pointsEarnedFromQuiz) "Quiz Completed! Correct! 🎉" else "Incorrect choice. Correct is C.",
                                color = if (pointsEarnedFromQuiz) Color(0xFF3CD070) else Color(0xFFFF5F5F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "LUNC circulating supply reached approximately 6.9 trillion tokens at its peak in late May 2022 after automated printing logic ran to stabilize the UST peg. You have unlocked +150 LUNC BURN.",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    selectedQuizAnswer = null
                                    isQuizSubmitted = false
                                    pointsEarnedFromQuiz = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131316)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("TRY ANOTHER ATTEMPT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
