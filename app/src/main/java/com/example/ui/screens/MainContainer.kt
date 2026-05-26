package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.UserEntity
import com.example.data.repository.SyncState
import com.example.ui.theme.LuncBurnerTheme
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.TerminalCyan
import com.example.ui.viewmodel.LuncViewModel
import com.example.ui.Translations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: LuncViewModel
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val games by viewModel.games.collectAsStateWithLifecycle()
    val burnLogs by viewModel.burnLogs.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val activeGameToPlay by viewModel.activeGameToPlay.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? Activity

    var currentTab by remember { mutableStateOf("DASH") }

    // If active mini-game is playing, show it full-bleed!
    val activeGame = activeGameToPlay
    if (activeGame != null) {
        LuncBurnerTheme(darkTheme = currentUser?.darkModeEnabled ?: true) {
            GameScreen(
                game = activeGame,
                onGameFinished = { score ->
                    viewModel.submitGameScore(score)
                },
                onExit = {
                    viewModel.selectGameToPlay(null)
                }
            )
        }
        return
    }

    val user = currentUser
    if (user == null || !user.isLoggedIn) {
        // Welcome logins terminal
        LuncBurnerTheme(darkTheme = true) {
            WelcomeScreen(
                selectedLanguage = selectedLanguage,
                onLanguageChange = { viewModel.updateLanguage(it) },
                onEnterTerminal = { email, name ->
                    viewModel.login(email, name)
                }
            )
        }
        return
    }

    // Dynamic Centralized Theme based on User preference switch
    LuncBurnerTheme(darkTheme = user.darkModeEnabled) {
        val darkColorBg = if (user.darkModeEnabled) Color(0xFF09090A) else MaterialTheme.colorScheme.background

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(darkColorBg)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            val isDesktop = maxWidth > 650.dp

            Row(modifier = Modifier.fillMaxSize()) {
                // Responsive Desktop Side Rail layout
                if (isDesktop) {
                    NavigationRail(
                        containerColor = if (user.darkModeEnabled) Color(0xFF0F0F12) else MaterialTheme.colorScheme.surface,
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Text("🔥", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "LUNC BURN",
                                    color = OrangeFlameBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        },
                        modifier = Modifier.width(90.dp)
                    ) {
                        val tabs = listOf(
                            Triple("DASH", Icons.Default.Dashboard, "DASH"),
                            Triple("GAMES", Icons.Default.SportsEsports, "GAMES"),
                            Triple("DAILY", Icons.Default.EmojiEvents, "DAILY"),
                            Triple("PROOF", Icons.Default.LocalFireDepartment, "PROOF"),
                            Triple("ANALYTICS", Icons.Default.Analytics, "ANALYTICS"),
                            Triple("SETTINGS", Icons.Default.Settings, "SETTINGS")
                        )

                        tabs.forEach { tab ->
                            val active = currentTab == tab.first
                            NavigationRailItem(
                                selected = active,
                                onClick = { currentTab = tab.first },
                                icon = {
                                    Icon(
                                        imageVector = tab.second,
                                        contentDescription = tab.third,
                                        tint = if (active) OrangeFlameBright else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        tab.third,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color.White else Color.Gray
                                    )
                                },
                                modifier = Modifier.testTag("desktop_tab_${tab.first}"),
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = OrangeFlame.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }

                // Main screen container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // UPPER HEADER MODULE: Custom synced title bar (Exactly matches Look and feel of Screen 4 header)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (user.darkModeEnabled) Color(0xFF0F0F12) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo/Text Left (Matches "LUNC BURN" orange with fire logo)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔥", fontSize = 22.sp)
                                Text(
                                    text = "LUNC BURN",
                                    color = if (user.darkModeEnabled) Color.White else Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Desktop Language indicator pill, LOGOUT button and Avatar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Language button dropdown
                                var isTopLangExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.clickable { isTopLangExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "Language",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = selectedLanguage,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown Indicator",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isTopLangExpanded,
                                        onDismissRequest = { isTopLangExpanded = false },
                                        modifier = Modifier.background(Color(0xFF1E1E24))
                                    ) {
                                        listOf("EN", "PL", "ZH", "FR", "ES").forEach { code ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${Translations.getLanguageFullName(code)} ($code)",
                                                        color = if (selectedLanguage == code) OrangeFlameBright else Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (selectedLanguage == code) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.updateLanguage(code)
                                                    isTopLangExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // LOGOUT Header action button (Matches header standard logout trigger)
                                Text(
                                    text = "LOGOUT",
                                    color = OrangeFlame,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { viewModel.logout() }
                                        .testTag("header_logout_btn")
                                )

                                // Circle user avatar representation
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OrangeFlame.copy(alpha = 0.2f))
                                        .border(1.dp, OrangeFlameBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = OrangeFlameBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Content viewport
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (currentTab) {
                            "DASH" -> DashboardScreen(
                                user = user,
                                onWatchAdClicked = {
                                    if (activity != null) {
                                        LuncAdManager.showInterstitial(activity) {
                                            viewModel.watchAd()
                                        }
                                    } else {
                                        viewModel.watchAd()
                                    }
                                },
                                syncStatusContent = {
                                    // Live Cloud Firebase/Room sync indicator pill
                                    Surface(
                                        color = if (user.darkModeEnabled) Color(0xFF18181C) else Color(0x10000000),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Flashing cyan/green dot for online success, or offline warning dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isOnline) {
                                                                if (syncState == SyncState.Synced) TerminalCyan else Color.Yellow
                                                            } else {
                                                                Color.Red
                                                            }
                                                        )
                                                )
                                                Text(
                                                    text = if (isOnline) {
                                                        if (syncState == SyncState.Synced) "Firebase replica synced (Real-time online)" else "Syncing with live Cloud replica..."
                                                    } else {
                                                        "Offline Database Active (Local caching enabled)"
                                                    },
                                                    color = if (user.darkModeEnabled) Color.LightGray else Color.DarkGray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Sycronize trigger button
                                            if (isOnline) {
                                                Icon(
                                                    imageVector = Icons.Default.Sync,
                                                    contentDescription = "Synch Now",
                                                    tint = OrangeFlameBright,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable { viewModel.triggerManualSync() }
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                            "GAMES" -> GamesListScreen(
                                games = games,
                                onPlayGame = { gameSelected ->
                                    viewModel.selectGameToPlay(gameSelected)
                                },
                                onClaimPoints = { gameId ->
                                    viewModel.claimPoints(gameId)
                                }
                            )
                            "DAILY" -> DailyChallengeScreen(
                                user = user,
                                onWatchAdClicked = {
                                    if (activity != null) {
                                        LuncAdManager.showInterstitial(activity) {
                                            viewModel.watchAd()
                                        }
                                    } else {
                                        viewModel.watchAd()
                                    }
                                }
                            )
                            "PROOF" -> ProofScreen(
                                burnLogs = burnLogs
                            )
                            "ANALYTICS" -> AnalyticsScreen(
                                metrics = analytics
                            )
                            "SETTINGS" -> SettingsScreen(
                                user = user,
                                isOnline = isOnline,
                                notifications = notifications,
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onToggleNotifications = { viewModel.toggleNotifications(it) },
                                onToggleNetwork = { viewModel.toggleNetworkStatus() },
                                onClearNotifications = { viewModel.clearAllNotifications() },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }

                    // Mobile mode navigation bar (Only shown when not desktop width)
                    if (!isDesktop) {
                        NavigationBar(
                            containerColor = if (user.darkModeEnabled) Color(0xFF0F0F12) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            val tabs = listOf(
                                Triple("DASH", Icons.Default.Dashboard, "DASH"),
                                Triple("GAMES", Icons.Default.SportsEsports, "GAMES"),
                                Triple("DAILY", Icons.Default.EmojiEvents, "DAILY"),
                                Triple("PROOF", Icons.Default.LocalFireDepartment, "PROOF"),
                                Triple("SETTINGS", Icons.Default.Settings, "SETTINGS")
                            )

                            tabs.forEach { tab ->
                                val active = currentTab == tab.first
                                NavigationBarItem(
                                    selected = active,
                                    onClick = { currentTab = tab.first },
                                    icon = {
                                        Icon(
                                            imageVector = tab.second,
                                            contentDescription = tab.third,
                                            tint = if (active) OrangeFlameBright else Color.Gray
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.third,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color.White else Color.Gray
                                        )
                                    },
                                    modifier = Modifier.testTag("tab_${tab.first}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = OrangeFlame.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
