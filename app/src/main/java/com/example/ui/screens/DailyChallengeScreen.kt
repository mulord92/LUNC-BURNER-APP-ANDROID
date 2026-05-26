package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.ui.Translations
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark
import com.example.ui.theme.SurfaceGraphite

@Composable
fun DailyChallengeScreen(
    user: UserEntity,
    onWatchAdClicked: () -> Unit
) {
    val dailyProgressFraction = (user.dailyPointsClaimed.toFloat() / user.dailyPointsTarget.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = dailyProgressFraction,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Topic Title:
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = Translations.get("daily_challenges", user.language),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "GLOBAL MARKET PULSE",
                color = OrangeFlameBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        // Center Mission Card (Matches Screen 5)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("daily_challenge_card"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Translations.get("watch_ads_mission_desc", user.language),
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Circular Progress Indicator box with Percentage
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    // Circular track bg
                    CircularProgressIndicator(
                        progress = { 1.0f },
                        color = Color(0xFF1E1E24),
                        strokeWidth = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Active slice
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        color = OrangeFlame,
                        strokeWidth = 12.dp,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Percentage inside
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(dailyProgressFraction * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "CLAIMED",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Under-details twin grid cards (Screen 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Points remaining
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "POINTS REMAINING",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(user.dailyPointsTarget - user.dailyPointsClaimed).coerceAtLeast(0)}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Ads Watched
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ADS WATCHED",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${user.adsWatched}",
                                color = OrangeFlameBright,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Sponsored Channel Card at bottom (Matches Screen 5)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("sponsored_ad_bottom_bar"),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(OrangeFlame.copy(alpha = 0.5f), Color.Transparent)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWatchAdClicked() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.get("watched", user.language),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = Translations.get("watched_desc", user.language),
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeFlame),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch Ad",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live Google Ads / AdSense Banner Ad Space at Bottom
        AdMobBannerAd(modifier = Modifier.padding(bottom = 16.dp))
    }
}
