package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.UserEntity
import com.example.ui.theme.OrangeFlame
import com.example.ui.Translations
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.GoldPoints

import com.example.data.network.LuncMarketData

@Composable
fun DashboardScreen(
    user: UserEntity,
    marketData: LuncMarketData,
    onRefreshMarketData: () -> Unit,
    onWatchAdClicked: () -> Unit,
    syncStatusContent: @Composable () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // Sync & Connectivity pill injected
        item {
            syncStatusContent()
        }

        // 1. Personal Node Card (Matches Screen 4)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personal_node_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5722).copy(alpha = 0.08f))
                            .border(1.dp, OrangeFlame.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = OrangeFlameBright,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = Translations.get("personal_node", user.language),
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = user.name.uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Grid-like dynamic scores row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Points
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "BURNLUNC POINTS",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${user.points}",
                                    color = GoldPoints,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Engagement Rank
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = Translations.get("rank", user.language),
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "#${user.engagementRank}",
                                    color = OrangeFlameBright,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Service duration label
                    Text(
                        text = Translations.get("days_active", user.language),
                        color = OrangeFlameBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${user.daysWithUs}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Real-Time LUNC Market Pulse Segment (Manual Refresh Mode)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131316)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_pulse_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header row: Title + Manual Refresh Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REAL-TIME MARKET PULSE",
                                color = OrangeFlameBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Manual Refresh Mode",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Refresh Button with indicator loading state
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1E24))
                                .clickable(enabled = !marketData.isFetching) { onRefreshMarketData() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (marketData.isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = OrangeFlameBright,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Price",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // LUNC Coin Name Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(OrangeFlame.copy(alpha = 0.15f))
                                .border(1.dp, OrangeFlameBright.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ł",
                                color = OrangeFlameBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Terra Luna Classic (LUNC)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price and Percentage Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = marketData.formatPrice(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )

                        // 24H Price Change Status
                        val positive = marketData.change24h >= 0
                        val statusBg = if (positive) Color(0xFF1B3A2C) else Color(0xFF4C1D1D)
                        val statusColor = if (positive) Color(0xFF3CD070) else Color(0xFFFF5F5F)
                        val statusIcon = if (positive) Icons.Default.TrendingUp else Icons.Default.TrendingDown

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = if (positive) "Up" else "Down",
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = marketData.formatChange(),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Grid stats: Market Cap and 24H Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    text = "MARKET CAP",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = marketData.formatMarketCap(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    text = "24H VOLUME",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = marketData.formatVolume(),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    if (marketData.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: Live ticker paused. Showing cached values (*)",
                            color = Color.Yellow.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2. Sponsored Channel Box (SUPPORT THE MISSION)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sponsored_ads_card"),
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
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Translations.get("watched", user.language),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Translations.get("watched_desc", user.language),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Play Circular Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(OrangeFlameBright, OrangeFlame)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Ad",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Live Google Ads / AdSense Banner Ad Space
        item {
            AdMobBannerAd()
        }

        // 3. BURNING LEADERS (Leaderboard panel)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "Leaderboard",
                        tint = OrangeFlameBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = Translations.get("leaderboard", user.language),
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of top players
                val leaderboard = listOf(
                    Triple("DEV_BURNER", "142,500 pts", "🏆 1"),
                    Triple("SATOSHI_LUNC", "98,200 pts", "⚡ 2"),
                    Triple(user.name, "${user.points} pts", "#${user.engagementRank}"),
                    Triple("COMPRESSOR_NODE", "410 pts", "4"),
                    Triple("RECYCLER_SUPPLY", "390 pts", "5")
                ).sortedByDescending { 
                    it.second.replace(",", "").replace(" pts", "").toIntOrNull() ?: 0 
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        leaderboard.forEachIndexed { index, item ->
                            val isMe = item.first == user.name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isMe) OrangeFlame.copy(alpha = 0.08f) else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (index < 3) GoldPoints else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(16.dp)
                                    )
                                    Text(
                                        text = item.first.uppercase(),
                                        color = if (isMe) OrangeFlameBright else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = item.second,
                                    color = if (isMe) GoldPoints else Color.LightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (index < leaderboard.size - 1) {
                                Divider(
                                    color = Color(0xFF1F1F23),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
