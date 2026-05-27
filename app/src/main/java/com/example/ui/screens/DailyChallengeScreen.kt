package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.Translations
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark
import com.example.ui.theme.SurfaceGraphite

data class LuncNewsItem(
    val id: Int,
    val title: String,
    val source: String,
    val timeAgo: String,
    val summary: String,
    val category: String,
    val categoryColor: Color
)

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
    val context = LocalContext.current

    // Internal sub-tabs: 0 = Challenges, 1 = Token News
    var activeSubTab by remember { mutableStateOf(0) }

    // Fully interactive state for likes and expand/collapse
    val newsLikesMap = remember {
        mutableStateMapOf(
            1 to 245,
            2 to 189,
            3 to 312,
            4 to 415
        )
    }
    val newsLikedStatusMap = remember {
        mutableStateMapOf(
            1 to false,
            2 to false,
            3 to false,
            4 to false
        )
    }
    val expandedNewsItemMap = remember {
        mutableStateMapOf(
            1 to false,
            2 to false,
            3 to false,
            4 to false
        )
    }

    val newsList = remember {
        listOf(
            LuncNewsItem(
                id = 1,
                title = "LUNC Weekly Burn Rate Surges over 40% Driven by Exchange Activity",
                source = "Binance Analytics Terminal",
                timeAgo = "2h ago",
                summary = "This week witnessed a substantial surge in LUNC tokens destroyed, driven by elevated decentralized exchange swap volumes and secondary-market validator commission burns. Total cumulative community burns now track higher than first quarter averages, signaling bullish ongoing commitment towards reducing total supply.",
                category = "BURN RATE",
                categoryColor = OrangeFlameBright
            ),
            LuncNewsItem(
                id = 2,
                title = "Joint L1 Task Force Announces Security Upgrade V3.1.5",
                source = "Terra Classic Core Dev Group",
                timeAgo = "1d ago",
                summary = "Core developers successfully planned and deployed a non-disruptive blockchain upgrade on the node validator mainnet. This upgrade fixes security patches, optimizes execution gas modules, and improves oracle stability to better align with upcoming USTC peg community experiments.",
                category = "CORE DEV",
                categoryColor = Color(0xFF3891E6)
            ),
            LuncNewsItem(
                id = 3,
                title = "Community Pool Tops 5 Billion LUNC with Validator Tax Commission Proposal",
                source = "Ecosystem Governance Proposal #12093",
                timeAgo = "2d ago",
                summary = "A new on-chain governance proposal aiming to automatically redirect a small percentage of validator commission and tax rewards back into primary community developer funds has passed the quorum threshold with 92% yes votes, establishing resources for the remainder of the year.",
                category = "GOVERNANCE",
                categoryColor = Color(0xFFA23FE6)
            ),
            LuncNewsItem(
                id = 4,
                title = "Ecosystem Lock: Staking Ratio Tops 15.6% Active Staked Milestones",
                source = "Classic Station Explorer Node",
                timeAgo = "3d ago",
                summary = "LUNC locked staking ratio has reached an all-time monthly high of 15.6% with over 1.08 Trillion LUNC tokens secure on validating nodes. Staked locking reduces the fluid spot circulating supply of tokens, increasing positive market depth and enhancing security of consensus checks.",
                category = "METRICS",
                categoryColor = Color(0xFF32D74B)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Title section:
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp)
        ) {
            Text(
                text = Translations.get("daily_challenges", user.language),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = if (activeSubTab == 0) "GLOBAL MARKET PULSE" else "TERRA CLASSIC FEED",
                color = OrangeFlameBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // Beautiful custom sub-tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131316), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 0: CHALLENGES
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (activeSubTab == 0) OrangeFlame else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = 0 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CHALLENGES",
                        color = if (activeSubTab == 0) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Tab 1: TOKEN NEWS
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (activeSubTab == 1) OrangeFlame else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = 1 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TOKEN NEWS",
                        color = if (activeSubTab == 1) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        if (activeSubTab == 0) {
            // Challenges screen UI elements (Center Circular Progress card + Sponsored Ads)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Circular Progress Indicator box with Percentage
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
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
                                  color = OrangeFlameBright,
                                  fontSize = 11.sp,
                                  fontWeight = FontWeight.Black,
                                  letterSpacing = 1.sp
                              )
                          }
                      }

                      Spacer(modifier = Modifier.height(24.dp))

                      // Counter text matching the design rules
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center
                      ) {
                          Text(
                              text = "${user.dailyPointsClaimed}",
                              color = OrangeFlameBright,
                              fontSize = 24.sp,
                              fontWeight = FontWeight.Black
                          )
                          Text(
                              text = " / ${user.dailyPointsTarget} LUNC",
                              color = Color.LightGray,
                              fontSize = 16.sp,
                              fontWeight = FontWeight.Medium
                          )
                      }
                      Text(
                          text = "DAILY ENERGY TARGET REQUIRED",
                          color = Color.Gray,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          letterSpacing = 1.sp,
                          modifier = Modifier.padding(top = 4.dp)
                      )
                  }
              }

              // Sponsored Channel Card at bottom
              Card(
                  colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                  modifier = Modifier
                      .fillMaxWidth()
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
          } else {
              // Token News List UI elements (using weight to avoid clipping with the banner ad)
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .weight(1f)
              ) {
                  LazyColumn(
                      modifier = Modifier.fillMaxSize(),
                      verticalArrangement = Arrangement.spacedBy(10.dp),
                      contentPadding = PaddingValues(bottom = 8.dp)
                  ) {
                      items(newsList) { item ->
                          val isLiked = newsLikedStatusMap[item.id] ?: false
                          val likesVal = newsLikesMap[item.id] ?: 0
                          val isExpanded = expandedNewsItemMap[item.id] ?: false

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF2E2E33), Color(0xFF0F0F11))
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("news_item_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Category badge and Time ago
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(item.categoryColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = item.categoryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                    }

                                    Text(
                                        text = item.timeAgo,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Title
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Expandable Content
                                AnimatedVisibility(visible = isExpanded) {
                                    Text(
                                        text = item.summary,
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }

                                // Interactive Info footer: Click card to expand/collapse
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.source,
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = if (isExpanded) "SHOW LESS" else "READ FULL ARTICLE",
                                        color = OrangeFlameBright,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier
                                            .clickable { expandedNewsItemMap[item.id] = !isExpanded }
                                            .padding(4.dp)
                                    )
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = Color(0xFF222226)
                                )

                                // Social footprint interactions: Like, Share, Reward
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Like Action Component
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isLiked) {
                                                    newsLikedStatusMap[item.id] = false
                                                    newsLikesMap[item.id] = likesVal - 1
                                                } else {
                                                    newsLikedStatusMap[item.id] = true
                                                    newsLikesMap[item.id] = likesVal + 1
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Like News",
                                            tint = if (isLiked) Color.Red else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$likesVal Likes",
                                            color = if (isLiked) Color.LightGray else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Share Action Component
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                Toast.makeText(
                                                    context,
                                                    "Article link copied to clipboard!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Share",
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Google Ads / AdSense Banner Ad Space at Bottom (Always shown and budgeted correctly)
        AdMobBannerAd(modifier = Modifier.padding(bottom = 16.dp))
    }
}
