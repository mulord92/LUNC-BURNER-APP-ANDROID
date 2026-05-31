package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
    var shields by remember { mutableStateOf(3) }

    // Game variables per game type
    val gameId = game.gameId

    // Game 1: Crypto Runner
    var playerX by remember { mutableStateOf(0.5f) } // 0.0 to 1.0
    var coins by remember { mutableStateOf(listOf<Offset>()) } // normalized x, y (0 to 1)
    var candlesticks by remember { mutableStateOf(listOf<Offset>()) } // normalized x, y (0 to 1)

    // Game 2: Space Burner
    var spacePlayerX by remember { mutableStateOf(0.5f) }
    var asteroids by remember { mutableStateOf(listOf<Offset>()) }
    var lasers by remember { mutableStateOf(listOf<Offset>()) }
    var spaceExplosions by remember { mutableStateOf(listOf<Pair<Offset, Int>>()) } // Pair of offset & active ticks remaining

    // Game 3: Chain Linker
    var linkerNodes by remember { mutableStateOf(listOf<Triple<Float, Float, Boolean>>()) } // x, y, isValid
    var lastLaneTapped by remember { mutableStateOf(-1) }
    var tapActiveTicks by remember { mutableStateOf(0) }

    // Game 4: Plane Flyer
    var planeY by remember { mutableStateOf(0.5f) }
    var planeVelocity by remember { mutableStateOf(0.0f) }
    var planePillars by remember { mutableStateOf(listOf<Triple<Float, Float, Boolean>>()) } // x, gapCenterY, passedYet

    // Popups and Particle System for playability feedback and visual juice
    var scorePopups by remember { mutableStateOf(listOf<PopupEffect>()) }
    var particles by remember { mutableStateOf(listOf<ParticleEffect>()) }

    // Main Game Loop timer
    LaunchedEffect(isPlaying, crashed) {
        if (!isPlaying || crashed) return@LaunchedEffect

        var tickCount = 0
        while (isPlaying && !crashed) {
            delay(30) // ~33 FPS
            tickCount++

            when (gameId) {
                "crypto_runner" -> {
                    // move items
                    coins = coins.map { Offset(it.x, it.y + 0.016f) }.filter { it.y < 1.0f }
                    candlesticks = candlesticks.map { Offset(it.x, it.y + 0.022f) }.filter { it.y < 1.0f }

                    // spawn items
                    if (tickCount % 22 == 0) {
                        coins = coins + Offset(Random.nextFloat().coerceIn(0.12f, 0.88f), 0.0f)
                    }
                    if (tickCount % 17 == 0) {
                        candlesticks = candlesticks + Offset(Random.nextFloat().coerceIn(0.12f, 0.88f), 0.0f)
                    }

                    // collision check with player collector rocket around y ~ 0.85
                    val playerMinX = (playerX - 0.11f).coerceAtLeast(0.0f)
                    val playerMaxX = (playerX + 0.11f).coerceAtMost(1.0f)

                    val (hitCoins, remainingCoins) = coins.partition {
                        it.y >= 0.81f && it.y <= 0.89f && it.x >= playerMinX && it.x <= playerMaxX
                    }
                    if (hitCoins.isNotEmpty()) {
                        score += hitCoins.size * 10
                        coins = remainingCoins
                    }

                    val (hitCandles, remainingCandles) = candlesticks.partition {
                        it.y >= 0.81f && it.y <= 0.89f && it.x >= playerMinX && it.x <= playerMaxX
                    }
                    if (hitCandles.isNotEmpty()) {
                        shields -= hitCandles.size
                        candlesticks = remainingCandles
                        if (shields <= 0) {
                            crashed = true
                            isPlaying = false
                        }
                    }
                }
                "space_burner" -> {
                    // move items
                    asteroids = asteroids.map { Offset(it.x, it.y + 0.014f) }.filter { it.y < 1.0f }
                    lasers = lasers.map { Offset(it.x, it.y - 0.040f) }.filter { it.y > 0.0f }
                    spaceExplosions = spaceExplosions.map { Pair(it.first, it.second - 1) }.filter { it.second > 0 }

                    // spawn asteroids
                    if (tickCount % 25 == 0) {
                        asteroids = asteroids + Offset(Random.nextFloat().coerceIn(0.12f, 0.88f), 0.0f)
                    }

                    // check laser vs asteroid hits
                    val remainingAsteroids = mutableListOf<Offset>()
                    val activeLasers = lasers.toMutableList()

                    for (asteroid in asteroids) {
                        var isDestroyed = false
                        val laserIterator = activeLasers.iterator()
                        while (laserIterator.hasNext()) {
                            val laser = laserIterator.next()
                            // calculate distance
                            val dx = laser.x - asteroid.x
                            val dy = laser.y - asteroid.y
                            val dist = kotlin.math.sqrt(dx*dx + dy*dy)
                            if (dist < 0.08f) {
                                isDestroyed = true
                                laserIterator.remove()
                                spaceExplosions = spaceExplosions + Pair(asteroid, 10)
                                score += 10
                                break
                            }
                        }
                        if (!isDestroyed) {
                            remainingAsteroids.add(asteroid)
                        }
                    }
                    asteroids = remainingAsteroids
                    lasers = activeLasers

                    // asteroid hit spaceship
                    val shipMinX = (spacePlayerX - 0.12f).coerceAtLeast(0.0f)
                    val shipMaxX = (spacePlayerX + 0.12f).coerceAtMost(1.0f)
                    val (hittingAsteroids, remainingAsteroidsAfterShip) = asteroids.partition {
                        it.y >= 0.81f && it.y <= 0.89f && it.x >= shipMinX && it.x <= shipMaxX
                    }
                    if (hittingAsteroids.isNotEmpty()) {
                        shields -= hittingAsteroids.size
                        asteroids = remainingAsteroidsAfterShip
                        if (shields <= 0) {
                            crashed = true
                            isPlaying = false
                        }
                    }
                }
                "chain_linker" -> {
                    // move nodes at interactive and fun dynamic speed
                    val currentSpeed = 0.012f + (score.toFloat() / 2000f) * 0.005f
                    linkerNodes = linkerNodes.map { Triple(it.first, it.second + currentSpeed.coerceAtMost(0.018f), it.third) }

                    // miss check: valid node past standard height
                    val missedNodes = linkerNodes.filter { it.second >= 0.92f }
                    val remainingNodes = linkerNodes.filter { it.second < 0.92f }

                    val validMissedCount = missedNodes.count { it.third }
                    if (validMissedCount > 0) {
                        shields -= validMissedCount
                        missedNodes.forEach { node ->
                            if (node.third) {
                                // Add popup for miss!
                                scorePopups = scorePopups + PopupEffect(node.first, 0.85f, "MISS!", Color(0xFFEF5350), 20)
                                // Particle burst on miss
                                particles = particles + (1..6).map {
                                    ParticleEffect(
                                        x = node.first,
                                        y = 0.85f,
                                        vx = (Random.nextFloat() - 0.5f) * 0.015f,
                                        vy = (Random.nextFloat() - 0.5f) * 0.015f,
                                        color = Color(0xFFEF5350),
                                        size = Random.nextFloat() * 6f + 3f,
                                        maxTicks = 12,
                                        ticks = 12
                                    )
                                }
                            }
                        }
                        if (shields <= 0) {
                            crashed = true
                            isPlaying = false
                        }
                    }
                    linkerNodes = remainingNodes

                    // spawn nodes down Left (0.25f), Center (0.50f), Right (0.75f) lanes
                    if (tickCount % 24 == 0) {
                        val laneIndex = Random.nextInt(3)
                        val laneX = when (laneIndex) {
                            0 -> 0.25f
                            1 -> 0.50f
                            else -> 0.75f
                        }
                        val isValid = Random.nextFloat() < 0.75f // 75% connection nodes, 25% virus (more playable)
                        linkerNodes = linkerNodes + Triple(laneX, 0.0f, isValid)
                    }

                    // feedback countdowns
                    if (tapActiveTicks > 0) {
                        tapActiveTicks--
                        if (tapActiveTicks == 0) {
                            lastLaneTapped = -1
                        }
                    }
                }
                "plane" -> {
                    // smoother gravity pull
                    planeVelocity += 0.0013f
                    planeY = (planeY + planeVelocity).coerceIn(-0.02f, 1.02f)

                    if (planeY <= 0.01f || planeY >= 0.95f) {
                        shields = 0
                        crashed = true
                        isPlaying = false
                    }

                    // speed scroll candlesticks (green/red candles)
                    planePillars = planePillars.map { Triple(it.first - 0.008f, it.second, it.third) }

                    // plane vs obstacle collision
                    val updatedPillars = mutableListOf<Triple<Float, Float, Boolean>>()
                    for (pillar in planePillars) {
                        val px = pillar.first
                        val gapCenter = pillar.second
                        val passed = pillar.third

                        // player coordinate centered at X = 0.25f
                        if (px >= 0.20f && px <= 0.30f) {
                            val halfGap = 0.15f // slightly larger gap for playability
                            val topPillarLimit = gapCenter - halfGap
                            val bottomPillarLimit = gapCenter + halfGap
                            
                            // 1.8% grace margin buffer representing the visual body of the plane
                            val graceMargin = 0.018f
                            if (planeY < (topPillarLimit + graceMargin) || planeY > (bottomPillarLimit - graceMargin)) {
                                shields = 0
                                crashed = true
                                isPlaying = false
                            }
                        }

                        if (px < 0.21f && !passed) {
                            score += 20
                            updatedPillars.add(Triple(px, gapCenter, true))
                            // Successful pass feedback popups & glowing green bubble particles
                            scorePopups = scorePopups + PopupEffect(0.25f, planeY, "+20", Color(0xFF66BB6A), 20)
                            particles = particles + (1..10).map {
                                ParticleEffect(
                                    x = 0.25f,
                                    y = planeY,
                                    vx = -(Random.nextFloat() * 0.015f + 0.005f),
                                    vy = (Random.nextFloat() - 0.5f) * 0.015f,
                                    color = Color(0xFF66BB6A),
                                    size = Random.nextFloat() * 8f + 3f,
                                    maxTicks = 12,
                                    ticks = 12
                                )
                            }
                        } else {
                            updatedPillars.add(pillar)
                        }
                    }
                    planePillars = updatedPillars.filter { it.first > -0.1f }

                    // spawn pillars at interactive distances
                    if (tickCount == 1 || tickCount % 84 == 0) {
                        planePillars = planePillars + Triple(1.0f, Random.nextFloat().coerceIn(0.25f, 0.75f), false)
                    }

                    // Rocket Plane smoke/flame engine particle stream!
                    if (tickCount % 2 == 0) {
                        val offsetColor = if (Random.nextBoolean()) OrangeFlameBright else OrangeFlame
                        particles = particles + ParticleEffect(
                            x = 0.23f,
                            y = planeY + (Random.nextFloat() - 0.5f) * 0.02f,
                            vx = -(Random.nextFloat() * 0.012f + 0.006f),
                            vy = (Random.nextFloat() - 0.5f) * 0.004f,
                            color = offsetColor.copy(alpha = 0.82f),
                            size = Random.nextFloat() * 7f + 3f,
                            maxTicks = 10,
                            ticks = 10
                        )
                    }
                }
            }

            // General effects update loop
            scorePopups = scorePopups.map { it.copy(y = it.y - 0.006f, ticks = it.ticks - 1) }.filter { it.ticks > 0 }
            particles = particles.map { it.copy(x = it.x + it.vx, y = it.y + it.vy, ticks = it.ticks - 1) }.filter { it.ticks > 0 }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090A))
            .systemBarsPadding()
    ) {
        if (!crashed) {
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
                        val subSubtitle = when (gameId) {
                            "crypto_runner" -> "COLLECT GREEN, DODGE RED SHORTS"
                            "space_burner" -> "BLAST AWAY INCOMING FUD UNITS"
                            "chain_linker" -> "SECURE BLOCKCHAIN LINK CHANNELS"
                            "plane" -> "DRIVE IN BULLISH GREEN CANDLES"
                            else -> "DODGE SHORTS, BUY THE DIP"
                        }
                        Text(
                            text = subSubtitle,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shields Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E24),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(Color(0xFFEF5350), Color.Transparent))
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHIELDS: ",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "⚡".repeat(shields.coerceAtLeast(0)),
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Score Badge
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
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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

                // Interactive Canvas Container
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F0F11))
                        .pointerInput(gameId) {
                            detectTapGestures { offset ->
                                when (gameId) {
                                    "space_burner" -> {
                                        lasers = lasers + Offset(spacePlayerX, 0.81f)
                                    }
                                    "chain_linker" -> {
                                        val tapX = offset.x / size.width
                                        val laneIndex = when {
                                            tapX < 0.38f -> 0
                                            tapX < 0.62f -> 1
                                            else -> 2
                                        }
                                        lastLaneTapped = laneIndex
                                        tapActiveTicks = 8

                                        // trigger hit check immediately on lane click
                                        val targetX = when (laneIndex) {
                                            0 -> 0.25f
                                            1 -> 0.50f
                                            else -> 0.75f
                                        }
                                        // slightly more generous target alignment height for comfortable tapping
                                        val hitTarget = linkerNodes.firstOrNull {
                                            it.first == targetX && it.second >= 0.64f && it.second <= 0.94f
                                        }
                                        if (hitTarget != null) {
                                            linkerNodes = linkerNodes - hitTarget
                                            if (hitTarget.third) {
                                                score += 15
                                                scorePopups = scorePopups + PopupEffect(targetX, 0.83f, "+15 SUCCESS", Color(0xFF4FC3F7), 20)
                                                particles = particles + (1..10).map {
                                                    ParticleEffect(
                                                        x = targetX,
                                                        y = 0.83f,
                                                        vx = (Random.nextFloat() - 0.5f) * 0.024f,
                                                        vy = (Random.nextFloat() - 0.5f) * 0.024f,
                                                        color = Color(0xFF4FC3F7),
                                                        size = Random.nextFloat() * 10f + 5f,
                                                        maxTicks = 14,
                                                        ticks = 14
                                                    )
                                                }
                                            } else {
                                                shields -= 1
                                                scorePopups = scorePopups + PopupEffect(targetX, 0.83f, "-1 SHIELD", Color(0xFFEF5350), 20)
                                                particles = particles + (1..12).map {
                                                    ParticleEffect(
                                                        x = targetX,
                                                        y = 0.83f,
                                                        vx = (Random.nextFloat() - 0.5f) * 0.03f,
                                                        vy = (Random.nextFloat() - 0.5f) * 0.03f,
                                                        color = Color(0xFFEF5350),
                                                        size = Random.nextFloat() * 11f + 5f,
                                                        maxTicks = 15,
                                                        ticks = 15
                                                    )
                                                }
                                                if (shields <= 0) {
                                                    crashed = true
                                                    isPlaying = false
                                                }
                                            }
                                        } else {
                                            // Empty tap visual feedback core shimmer
                                            particles = particles + (1..4).map {
                                                ParticleEffect(
                                                    x = targetX,
                                                    y = 0.83f,
                                                    vx = (Random.nextFloat() - 0.5f) * 0.015f,
                                                    vy = (Random.nextFloat() - 0.5f) * 0.015f,
                                                    color = Color.LightGray.copy(alpha = 0.4f),
                                                    size = Random.nextFloat() * 6f + 3f,
                                                    maxTicks = 8,
                                                    ticks = 8
                                                )
                                            }
                                        }
                                    }
                                    "plane" -> {
                                        // smoother thrust impulse physics
                                        planeVelocity = -0.028f
                                        // jump puff visual effects
                                        particles = particles + (1..5).map {
                                            ParticleEffect(
                                                x = 0.25f,
                                                y = planeY,
                                                vx = (Random.nextFloat() - 0.5f) * 0.005f - 0.005f,
                                                vy = (Random.nextFloat() - 0.5f) * 0.005f + 0.004f,
                                                color = Color.White.copy(alpha = 0.5f),
                                                size = Random.nextFloat() * 5f + 2f,
                                                maxTicks = 8,
                                                ticks = 8
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        .pointerInput(gameId) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                when (gameId) {
                                    "crypto_runner" -> {
                                        playerX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                    }
                                    "space_burner" -> {
                                        spacePlayerX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                        // add exciting auto shoot laser with 15% probability per drag step
                                        if (Random.nextFloat() < 0.18f) {
                                            lasers = lasers + Offset(spacePlayerX, 0.81f)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        when (gameId) {
                            "crypto_runner" -> {
                                val colGrid = Color(0xFF1D1D22)
                                drawLine(colGrid, Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), 1f)
                                drawLine(colGrid, Offset(0f, size.height * 0.4f), Offset(size.width, size.height * 0.4f), 1f)
                                drawLine(colGrid, Offset(0f, size.height * 0.6f), Offset(size.width, size.height * 0.6f), 1f)
                                drawLine(colGrid, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), 1f)

                                // Gold coins
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

                                // Bearish Red Candlesticks (Shorts)
                                for (candle in candlesticks) {
                                    val cx = candle.x * size.width
                                    val cy = candle.y * size.height
                                    val rWidth = 24f
                                    val rHeight = 84f
                                    drawRect(
                                        color = Color(0xFFEF5350),
                                        topLeft = Offset(cx - rWidth / 2, cy - rHeight / 2),
                                        size = Size(rWidth, rHeight)
                                    )
                                    drawLine(
                                        color = Color(0xFFEF5350),
                                        start = Offset(cx, cy - rHeight / 2 - 20f),
                                        end = Offset(cx, cy + rHeight / 2 + 20f),
                                        strokeWidth = 3f
                                    )
                                }

                                // Player Crypto Flame Rocket
                                val px = playerX * size.width
                                val py = size.height * 0.85f

                                drawCircle(
                                    color = Color.White,
                                    radius = 16f,
                                    center = Offset(px, py)
                                )
                                drawCircle(
                                    color = OrangeFlame,
                                    radius = 32f,
                                    center = Offset(px, py),
                                    alpha = 0.8f
                                )
                                drawCircle(
                                    color = OrangeFlameBright,
                                    radius = 48f,
                                    center = Offset(px, py),
                                    alpha = 0.3f
                                )
                            }
                            "space_burner" -> {
                                // Background Stars
                                val starCol = Color.White.copy(alpha = 0.35f)
                                drawCircle(starCol, 4f, Offset(size.width * 0.15f, size.height * 0.20f))
                                drawCircle(starCol, 3f, Offset(size.width * 0.80f, size.height * 0.12f))
                                drawCircle(starCol, 5f, Offset(size.width * 0.40f, size.height * 0.42f))
                                drawCircle(starCol, 3f, Offset(size.width * 0.88f, size.height * 0.60f))
                                drawCircle(starCol, 4f, Offset(size.width * 0.22f, size.height * 0.72f))
                                drawCircle(starCol, 5f, Offset(size.width * 0.62f, size.height * 0.82f))

                                // Lasers
                                for (laser in lasers) {
                                    val lx = laser.x * size.width
                                    val ly = laser.y * size.height
                                    drawRect(
                                        color = Color(0xFF29B6F6),
                                        topLeft = Offset(lx - 4f, ly - 30f),
                                        size = Size(8f, 30f)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 8f,
                                        center = Offset(lx, ly - 30f)
                                    )
                                }

                                // FUD Asteroids
                                for (ast in asteroids) {
                                    val ax = ast.x * size.width
                                    val ay = ast.y * size.height
                                    drawCircle(
                                        color = Color(0xFF53535C),
                                        radius = 28f,
                                        center = Offset(ax, ay)
                                    )
                                    drawCircle(
                                        color = Color(0xFFAB47BC), // glowing space edge
                                        radius = 30f,
                                        center = Offset(ax, ay),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                    )
                                    drawCircle(
                                        color = Color(0xFF38383D),
                                        radius = 8f,
                                        center = Offset(ax - 8f, ay - 8f)
                                    )
                                }

                                // Particles
                                for (exp in spaceExplosions) {
                                    val ex = exp.first.x * size.width
                                    val ey = exp.first.y * size.height
                                    val ratio = (10 - exp.second) / 10f
                                    drawCircle(
                                        color = Color(0xFFFF7043),
                                        radius = 10f + ratio * 60f,
                                        center = Offset(ex, ey),
                                        alpha = 1f - ratio
                                    )
                                    drawCircle(
                                        color = Color(0xFFFFCA28),
                                        radius = 5f + ratio * 40f,
                                        center = Offset(ex, ey),
                                        alpha = (1f - ratio) * 0.8f
                                    )
                                }

                                // Flying Space Vessel
                                val sx = spacePlayerX * size.width
                                val sy = size.height * 0.85f

                                drawCircle(
                                    color = OrangeFlame,
                                    radius = 20f + (Random.nextFloat() * 10f),
                                    center = Offset(sx, sy + 30f),
                                    alpha = 0.7f
                                )

                                val vesselPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(sx, sy - 34f)
                                    lineTo(sx - 24f, sy + 24f)
                                    lineTo(sx + 24f, sy + 24f)
                                    close()
                                }
                                drawPath(
                                    path = vesselPath,
                                    color = Color.White
                                )
                                drawCircle(
                                    color = Color(0xFF29B6F6),
                                    radius = 26f,
                                    center = Offset(sx, sy),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                )
                            }
                            "chain_linker" -> {
                                val colLane = Color(0xFF1E293B)
                                val litLane = Color(0xFF38BDF8)

                                drawLine(colLane, Offset(size.width * 0.38f, 0f), Offset(size.width * 0.38f, size.height), 2f)
                                drawLine(colLane, Offset(size.width * 0.62f, 0f), Offset(size.width * 0.62f, size.height), 2f)

                                val lPos = size.width * 0.25f
                                val cPos = size.width * 0.50f
                                val rPos = size.width * 0.75f

                                val socketY = size.height * 0.83f
                                val diameter = 45f

                                val lHasTarget = linkerNodes.any { it.first == 0.25f && it.second >= 0.64f && it.second <= 0.94f }
                                val cHasTarget = linkerNodes.any { it.first == 0.50f && it.second >= 0.64f && it.second <= 0.94f }
                                val rHasTarget = linkerNodes.any { it.first == 0.75f && it.second >= 0.64f && it.second <= 0.94f }

                                // Left channel target ring with helper pulse
                                drawCircle(
                                    color = if (lastLaneTapped == 0) litLane else colLane,
                                    radius = diameter,
                                    center = Offset(lPos, socketY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (lastLaneTapped == 0) 6f else 2f)
                                )
                                if (lHasTarget) {
                                    drawCircle(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        radius = diameter + 12f,
                                        center = Offset(lPos, socketY),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                }

                                // Center channel target ring with helper pulse
                                drawCircle(
                                    color = if (lastLaneTapped == 1) litLane else colLane,
                                    radius = diameter,
                                    center = Offset(cPos, socketY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (lastLaneTapped == 1) 6f else 2f)
                                )
                                if (cHasTarget) {
                                    drawCircle(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        radius = diameter + 12f,
                                        center = Offset(cPos, socketY),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                }

                                // Right channel target ring with helper pulse
                                drawCircle(
                                    color = if (lastLaneTapped == 2) litLane else colLane,
                                    radius = diameter,
                                    center = Offset(rPos, socketY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (lastLaneTapped == 2) 6f else 2f)
                                )
                                if (rHasTarget) {
                                    drawCircle(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                        radius = diameter + 12f,
                                        center = Offset(rPos, socketY),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                    )
                                }

                                // Traveling nodes
                                for (node in linkerNodes) {
                                    val nx = node.first * size.width
                                    val ny = node.second * size.height
                                    val isCorrect = node.third

                                    if (isCorrect) {
                                        // Transaction core data with glowing ring
                                        drawCircle(
                                            color = Color(0xFF4FC3F7),
                                            radius = 22f,
                                            center = Offset(nx, ny)
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 10f,
                                            center = Offset(nx, ny)
                                        )
                                    } else {
                                        // Red Malware Packet
                                        val s = 40f
                                        drawRect(
                                            color = Color(0xFFEF5350),
                                            topLeft = Offset(nx - s / 2, ny - s / 2),
                                            size = Size(s, s)
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(nx - 11f, ny - 11f),
                                            end = Offset(nx + 11f, ny + 11f),
                                            strokeWidth = 3f
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(nx + 11f, ny - 11f),
                                            end = Offset(nx - 11f, ny + 11f),
                                            strokeWidth = 3f
                                        )
                                    }
                                }
                            }
                            "plane" -> {
                                val pX = 0.25f * size.width
                                val pY = planeY * size.height

                                // Draw cloud shadows
                                val cloudCol = Color.White.copy(alpha = 0.08f)
                                drawCircle(cloudCol, 120f, Offset(size.width * 0.8f, size.height * 0.3f))
                                drawCircle(cloudCol, 90f, Offset(size.width * 0.2f, size.height * 0.6f))

                                // Draw financial candlesticks hurdles
                                for (pillar in planePillars) {
                                    val pilX = pillar.first * size.width
                                    val gapCenter = pillar.second * size.height
                                    val pillarWidth = 48f
                                    val halfGapSize = 0.15f * size.height

                                    // Top candlestick (Bullish Green Body)
                                    val topBottom = gapCenter - halfGapSize
                                    drawRect(
                                        color = Color(0xFF2E7D32),
                                        topLeft = Offset(pilX - pillarWidth / 2, 0f),
                                        size = Size(pillarWidth, topBottom)
                                    )
                                    // green wick
                                    drawLine(
                                        color = Color(0xFF81C784),
                                        start = Offset(pilX, 0f),
                                        end = Offset(pilX, topBottom + 20f),
                                        strokeWidth = 4f
                                    )

                                    // Bottom candlestick (Bearish Red Body)
                                    val bottomTop = gapCenter + halfGapSize
                                    drawRect(
                                        color = Color(0xFFC62828),
                                        topLeft = Offset(pilX - pillarWidth / 2, bottomTop),
                                        size = Size(pillarWidth, size.height - bottomTop)
                                    )
                                    // red wick
                                    drawLine(
                                        color = Color(0xFFE57373),
                                        start = Offset(pilX, bottomTop - 20f),
                                        end = Offset(pilX, size.height),
                                        strokeWidth = 4f
                                    )
                                }

                                // Player Flappy Jet
                                drawCircle(
                                    color = OrangeFlame,
                                    radius = 14f + (Random.nextFloat() * 10f),
                                    center = Offset(pX - 25f, pY + 2f),
                                    alpha = 0.7f
                                )
                                drawCircle(
                                    color = OrangeFlameBright,
                                    radius = 24f + (Random.nextFloat() * 12f),
                                    center = Offset(pX - 35f, pY + 2f),
                                    alpha = 0.3f
                                )

                                val jetPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(pX + 25f, pY) // tip
                                    lineTo(pX - 20f, pY - 18f)
                                    lineTo(pX - 10f, pY)
                                    lineTo(pX - 20f, pY + 18f)
                                    close()
                                }
                                drawPath(
                                    path = jetPath,
                                    color = Color.White
                                )
                                drawCircle(
                                    color = OrangeFlameBright,
                                    radius = 12f,
                                    center = Offset(pX - 5f, pY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                )
                            }
                        }

                        // Draw visual particles on top of standard elements
                        for (p in particles) {
                            val px = p.x * size.width
                            val py = p.y * size.height
                            val alpha = p.ticks.toFloat() / p.maxTicks.toFloat()
                            drawCircle(
                                color = p.color,
                                radius = p.size,
                                center = Offset(px, py),
                                alpha = alpha
                            )
                        }
                    }

                    // Huge, beautiful semitransparent arcade score HUD overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LIVE HUD SCORE",
                                color = Color.Gray.copy(alpha = 0.45f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = score.toString().padStart(5, '0'),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp
                            )
                        }
                    }

                    // Floating Score / Damage Popups Overlay
                    scorePopups.forEach { popup ->
                        val alpha = popup.ticks.toFloat() / 20f
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = popup.text,
                                color = popup.color.copy(alpha = alpha),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(
                                        x = (popup.x * this@BoxWithConstraints.maxWidth.value).dp - 40.dp,
                                        y = (popup.y * this@BoxWithConstraints.maxHeight.value).dp
                                    )
                            )
                        }
                    }

                    // Touch side control hints
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val controlsText = when (gameId) {
                            "crypto_runner" -> "← DRAG ROCKET LEFT TO RIGHT →\nCollect Gold Coins. Avoid Red Short Candlesticks."
                            "space_burner" -> "← DRAG VEHICLE LEFT/RIGHT | TAP TO SHOOT →\nBlast away falling FUD Asteroids to protect shields."
                            "chain_linker" -> "TAP THE 3 neon RING CHANNELS ON ALIGNMENT\nConnect valid blue transaction nodes. Skip red infected malware."
                            "plane" -> "TAP ANYWHERE TO THRUST/BOOST FLIGHT\nGravity pulls downwards. Fly smoothly through the candle gates."
                            else -> "← SWIPE OR TAP TO PLAY →"
                        }
                        Text(
                            text = controlsText,
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
            // Market Crashed screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                            .background(Color(0xFFEF5350).copy(alpha = 0.30f)),
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

                        HorizontalDivider(color = Color(0xFF26262B))

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

                val gradient = Brush.linearGradient(
                    colors = listOf(OrangeFlame, OrangeFlameBright, OrangeFlameDark)
                )

                // RETRY RUN button
                Button(
                    onClick = {
                        // Reset and replay
                        score = 0
                        shields = 3
                        crashed = false
                        isPlaying = true
                        coins = emptyList()
                        candlesticks = emptyList()
                        asteroids = emptyList()
                        lasers = emptyList()
                        spaceExplosions = emptyList()
                        linkerNodes = emptyList()
                        lastLaneTapped = -1
                        tapActiveTicks = 0
                        planeY = 0.5f
                        planeVelocity = 0.0f
                        planePillars = emptyList()
                        scorePopups = emptyList()
                        particles = emptyList()
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

data class PopupEffect(
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color,
    val ticks: Int
)

data class ParticleEffect(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val maxTicks: Int,
    val ticks: Int
)
