package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameProgressEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(
    game: GameProgressEntity,
    onGameFinished: (score: Int) -> Unit,
    onExit: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var score by remember { mutableStateOf(0) }
    var crashed by remember { mutableStateOf(false) }

    // Game variables
    var playerX by remember { mutableStateOf(0.5f) } // 0.0 to 1.0
    var coins by remember { mutableStateOf(listOf<Offset>()) } // normalized x, y (0 to 1)
    var candlesticks by remember { mutableStateOf(listOf<Offset>()) } // normalized x, y (0 to 1)

    // Game loop
    LaunchedEffect(isPlaying, crashed) {
        if (!isPlaying || crashed) return@LaunchedEffect
        
        // Spawn loop
        var tickCount = 0
        while (isPlaying && !crashed) {
            delay(30) // ~33 FPS
            tickCount++

            // Move existing coins & candlesticks down
            coins = coins.map { Offset(it.x, it.y + 0.025f) }.filter { it.y < 1.0f }
            candlesticks = candlesticks.map { Offset(it.x, it.y + 0.03f) }.filter { it.y < 1.0f }

            // Spawn new coins
            if (tickCount % 20 == 0) {
                coins = coins + Offset(Random.nextFloat().coerceIn(0.1f, 0.9f), 0.0f)
            }
            // Spawn new red candlesticks
            if (tickCount % 15 == 0) {
                candlesticks = candlesticks + Offset(Random.nextFloat().coerceIn(0.1f, 0.9f), 0.0f)
            }

            // Check collision with player Rocket / Flame at y ~ 0.85
            val playerMinX = (playerX - 0.12f).coerceAtLeast(0.0f)
            val playerMaxX = (playerX + 0.12f).coerceAtMost(1.0f)

            // Hit Coin
            val (hitCoins, remainingCoins) = coins.partition { 
                it.y >= 0.80f && it.y <= 0.90f && it.x >= playerMinX && it.x <= playerMaxX
            }
            if (hitCoins.isNotEmpty()) {
                score += hitCoins.size * 10
                coins = remainingCoins
            }

            // Hit Candlestick -> MARKET CRASH!
            val hitCandles = candlesticks.filter { 
                it.y >= 0.80f && it.y <= 0.90f && it.x >= playerMinX && it.x <= playerMaxX
            }
            if (hitCandles.isNotEmpty()) {
                crashed = true
                isPlaying = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090A))
            .systemBarsPadding()
    ) {
        if (!crashed) {
            // Live active game view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = game.title.uppercase(),
                            color = OrangeFlameBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "DODGE SHORTS, BUY THE DIP",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E24),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(OrangeFlame, Color.Transparent))
                            )
                        ) {
                            Text(
                                text = "SCORE: $score",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = onExit,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E24))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit",
                                tint = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Canvas Field
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F0F11))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                playerX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                            }
                        }
                ) {
                    val w = maxWidth
                    val h = maxHeight

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing Guidelines / Grid Lines
                        val col1 = Color(0xFF1D1D22)
                        drawLine(col1, Offset(0f, size.height*0.2f), Offset(size.width, size.height*0.2f), 1f)
                        drawLine(col1, Offset(0f, size.height*0.4f), Offset(size.width, size.height*0.4f), 1f)
                        drawLine(col1, Offset(0f, size.height*0.6f), Offset(size.width, size.height*0.6f), 1f)
                        drawLine(col1, Offset(0f, size.height*0.8f), Offset(size.width, size.height*0.8f), 1f)

                        // Draw golden coins
                        for (coin in coins) {
                            val cx = coin.x * size.width
                            val cy = coin.y * size.height
                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 24f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = 12f,
                                center = Offset(cx, cy)
                            )
                        }

                        // Draw falling red candlesticks
                        for (candle in candlesticks) {
                            val cx = candle.x * size.width
                            val cy = candle.y * size.height
                            val rWidth = 24f
                            val rHeight = 80f
                            // Body of candlestick
                            drawRect(
                                color = Color(0xFFEF5350),
                                topLeft = Offset(cx - rWidth / 2, cy - rHeight / 2),
                                size = Size(rWidth, rHeight)
                            )
                            // Wick line
                            drawLine(
                                color = Color(0xFFEF5350),
                                start = Offset(cx, cy - rHeight / 2 - 20f),
                                end = Offset(cx, cy + rHeight / 2 + 20f),
                                strokeWidth = 3f
                            )
                        }

                        // Draw Player rocket / terminal green flame
                        val px = playerX * size.width
                        val py = size.height * 0.85f

                        // Inner white flame center
                        drawCircle(
                            color = Color.White,
                            radius = 16f,
                            center = Offset(px, py)
                        )
                        // Outer glowing orange flame
                        drawCircle(
                            color = OrangeFlame,
                            radius = 32f,
                            center = Offset(px, py),
                            alpha = 0.8f
                        )
                        // Extra outer glowing radial indicator
                        drawCircle(
                            color = OrangeFlameBright,
                            radius = 48f,
                            center = Offset(px, py),
                            alpha = 0.3f
                        )
                    }

                    // Touch side control hints
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = "← DRAG/SLIDE SCREEN LEFT TO RIGHT →\nCollect Yellow Coins. Avoid Red Short Candlesticks.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // Market Crashed screen exactly as in Screen 3 image:
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large red rounded trophy badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF5350).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF5350).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy icon",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MARKET CRASHED!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Normal,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Score metrics panel
                Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "YOUR SCORE",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "$score",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Divider(color = Color(0xFF26262B))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "HIGH SCORE",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${if (score > game.highScore) score else game.highScore}",
                                color = Color(0xFFFFA500),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                val gradient = Brush.linearGradient(
                    colors = listOf(OrangeFlame, OrangeFlameBright, OrangeFlameDark)
                )

                // RETRY RUN button
                Button(
                    onClick = {
                        // Reset and replay
                        score = 0
                        crashed = false
                        isPlaying = true
                        coins = emptyList()
                        candlesticks = emptyList()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("retry_run_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = Color.White
                            )
                            Text(
                                "RETRY RUN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // EXIT TO DASH button
                OutlinedButton(
                    onClick = {
                        onGameFinished(score)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("exit_to_dash_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(Color(0xFF2E2E33), Color(0xFF1E1E24)))
                    )
                ) {
                    Text(
                        "EXIT TO DASH",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}
