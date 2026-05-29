package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LuncRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    private val userDao = db.userDao()
    private val gameDao = db.gameDao()
    private val burnLogDao = db.burnLogDao()
    private val analyticsDao = db.analyticsDao()
    private val notificationDao = db.notificationDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Sync status Flow
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Synced)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Network Simulated State
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Active user flow (cached/persisted)
    val currentUser: Flow<UserEntity?> = userDao.getUserFlow("local_user")

    // Games and progress flow
    val allGames: Flow<List<GameProgressEntity>> = gameDao.getAllGamesFlow()

    // Burn logs flow
    val allBurnLogs: Flow<List<BurnLogEntity>> = burnLogDao.getAllBurnLogsFlow()

    // Analytics flow
    val allAnalytics: Flow<List<AnalyticsMetricEntity>> = analyticsDao.getAllAnalyticsFlow()

    // Notification center flow
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()

    init {
        // Run seed on startup if required
        repositoryScope.launch {
            seedDatabaseIfEmpty()
        }
    }

    fun setNetworkStatus(online: Boolean) {
        _isOnline.value = online
        if (online) {
            triggerSync()
        }
    }

    fun triggerSync() {
        if (!_isOnline.value) return
        repositoryScope.launch {
            _syncState.value = SyncState.Syncing
            // Simulate cloud server handshakes with active Firebase replica real-time sync
            delay(1500)
            _syncState.value = SyncState.Synced
            
            // Push notification for successful sync if logged in
            val user = userDao.getUser()
            if (user != null && user.isLoggedIn) {
                sendLocalNotification(
                    title = "Database Synchronized",
                    body = "Your progress has been secured to the live decentralized LUNC terminal."
                )
            }
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        // Seed standard games
        val gameCount = gameDao.getAllGamesFlow().first().size
        if (gameCount == 0) {
            val defaultGames = listOf(
                GameProgressEntity("crypto_runner", "Crypto Runner", 50, progress = 0.65f),
                GameProgressEntity("space_burner", "Space Burner", 100, progress = 0.12f),
                GameProgressEntity("chain_linker", "Chain Linker", 150, progress = 0.0f),
                GameProgressEntity("plane", "Plane", 200, progress = 0.0f)
            )
            gameDao.insertAllGames(defaultGames)
        }

        // Seed burn logs
        val burnCount = burnLogDao.getAllBurnLogsFlow().first().size
        if (burnCount == 0) {
            val defaultBurnLogs = listOf(
                BurnLogEntity(monthText = "APR 2026", destroyedText = "1.2B DESTROYED", txId = "TX: 8F2E...4A1B", timestamp = System.currentTimeMillis() - 1000 * 3600 * 24 * 5),
                BurnLogEntity(monthText = "MAR 2026", destroyedText = "980M DESTROYED", txId = "TX: 3D9A...2C7F", timestamp = System.currentTimeMillis() - 1000 * 3600 * 24 * 35),
                BurnLogEntity(monthText = "FEB 2026", destroyedText = "1.5B DESTROYED", txId = "TX: 7B6E...9D2A", timestamp = System.currentTimeMillis() - 1000 * 3600 * 24 * 65),
                BurnLogEntity(monthText = "JAN 2026", destroyedText = "2.1B DESTROYED", txId = "TX: E24B...109C", timestamp = System.currentTimeMillis() - 1000 * 3600 * 24 * 95)
            )
            burnLogDao.insertAllBurnLogs(defaultBurnLogs)
        }

        // Seed Analytics
        val defaultMetrics = listOf(
            AnalyticsMetricEntity("app_open", "App Launched", 0),
            AnalyticsMetricEntity("game_played", "Games Played", 0),
            AnalyticsMetricEntity("ad_watched", "Sponsored Ads Viewed", 0),
            AnalyticsMetricEntity("points_claimed", "Points Claims Processed", 0),
            AnalyticsMetricEntity("network_offline", "Offline Activity Logged", 0)
        )
        for (metric in defaultMetrics) {
            if (analyticsDao.getMetric(metric.metricId) == null) {
                analyticsDao.insertOrUpdateMetric(metric)
            }
        }
        
        // Populate first notification if empty
        val notificationCount = notificationDao.getAllNotificationsFlow().first().size
        if (notificationCount == 0) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Terminal initialized",
                    body = "Welcome to the LUNC Burning application node. Track achievements, play games & destroy supply."
                )
            )
        }
    }

    suspend fun login(email: String, name: String, profilePicUrl: String = ""): Boolean {
        // Secure Login / Simulation Cache
        val existingUser = userDao.getUser()
        val user = existingUser?.copy(
            email = email,
            name = name,
            profilePicUrl = profilePicUrl,
            isLoggedIn = true
        ) ?: UserEntity(
            email = email,
            name = name,
            profilePicUrl = profilePicUrl,
            isLoggedIn = true
        )
        
        userDao.insertOrUpdateUser(user)
        incrementMetric("app_open")
        
        sendLocalNotification(
            title = "Node Secured",
            body = "Session verified as $name"
        )
        triggerSync()
        return true
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        val user = userDao.getUser()
        return user != null && user.email.isNotBlank() && user.email.trim().equals(email.trim(), ignoreCase = true)
    }

    suspend fun logout() {
        val user = userDao.getUser() ?: return
        userDao.insertOrUpdateUser(user.copy(isLoggedIn = false))
        sendLocalNotification(
            title = "Logged Out",
            body = "Session terminated. Your keys and balance are cached securely offline."
        )
    }

    suspend fun claimPoints(gameId: String): Boolean {
        val user = userDao.getUser() ?: return false
        val game = gameDao.getGame(gameId) ?: return false

        if (game.claimed) return false

        // Mark game as claimed, progress full (1.0f)
        val updatedGame = game.copy(claimed = true, progress = 1.0f)
        gameDao.insertOrUpdateGame(updatedGame)

        // Increment user points
        val newPoints = user.points + game.rewardPoints
        // Engagement rank dynamically updates (e.g. better score leads to higher rank!)
        val newRank = (42 - (newPoints / 100)).coerceAtLeast(1)
        
        // Log to daily progress
        val newDailyClaimed = (user.dailyPointsClaimed + game.rewardPoints).coerceAtMost(user.dailyPointsTarget)

        userDao.insertOrUpdateUser(user.copy(
            points = newPoints,
            engagementRank = newRank,
            dailyPointsClaimed = newDailyClaimed
        ))

        incrementMetric("points_claimed")

        // Sync to Decentralized blockchain simulation
        addChainBurnLog(game.title, game.rewardPoints)

        sendLocalNotification(
            title = "Tokens Claimed!",
            body = "Successfully processed +${game.rewardPoints} points. Total: $newPoints"
        )

        triggerSync()
        return true
    }

    suspend fun addChainBurnLog(actionTitle: String, amount: Int) {
        val chars = "0123456789ABCDEF"
        val txId = "TX: " + (1..8).map { chars.random() }.joinToString("") + "..." + (1..4).map { chars.random() }.joinToString("")
        val amountInM = (amount * 10).toFloat() / 10f
        val destroyedText = "${amountInM}M DESTROYED"
        burnLogDao.insertBurnLog(
            BurnLogEntity(
                monthText = "RECENT: $actionTitle",
                destroyedText = destroyedText,
                txId = txId
            )
        )
    }

    suspend fun watchAdAndCompleteMission(): Boolean {
        val user = userDao.getUser() ?: return false
        val pointsReward = 1000 // Each ad awards 1,000 points towards completion of daily pulse

        val updatedAdsWatched = user.adsWatched + 1
        val updatedDailyClaimed = (user.dailyPointsClaimed + pointsReward).coerceAtMost(user.dailyPointsTarget)
        val newPoints = user.points + pointsReward
        val newRank = (42 - (newPoints / 100)).coerceAtLeast(1)

        userDao.insertOrUpdateUser(user.copy(
            adsWatched = updatedAdsWatched,
            dailyPointsClaimed = updatedDailyClaimed,
            points = newPoints,
            engagementRank = newRank
        ))

        incrementMetric("ad_watched")

        sendLocalNotification(
            title = "Daily Pulse Contribution",
            body = "Earned +$pointsReward points from Sponsored Channel! Progress: $updatedDailyClaimed/${user.dailyPointsTarget}"
        )

        if (updatedDailyClaimed >= user.dailyPointsTarget) {
            sendLocalNotification(
                title = "Mission Accomplished! 🎉",
                body = "You have met the daily 5,000 LUNC BURN token goal!"
            )
        }

        triggerSync()
        return true
    }

    suspend fun submitGameScore(gameId: String, score: Int) {
        val game = gameDao.getGame(gameId) ?: return
        val currentHighScore = game.highScore
        val newHighScore = if (score > currentHighScore) score else currentHighScore
        
        // Simulating the incremental game completion/progress up to 1.0 based on score
        val newProgress = (score.toFloat() / 100f).coerceIn(0.0f, 1.0f)

        gameDao.insertOrUpdateGame(
            game.copy(
                highScore = newHighScore,
                lastScore = score,
                progress = newProgress,
                claimed = if (score >= 100) true else game.claimed,
                lastUpdated = System.currentTimeMillis()
            )
        )

        // If score is high enough, automatic claim triggered
        if (score > 0) {
            incrementMetric("game_played")
            val user = userDao.getUser()
            if (user != null) {
                // Earn points proportionately to score if it wasn't claimed
                val awardedValue = (score * 2)
                val newUserTotalPoints = user.points + awardedValue
                val newDailyClaimed = (user.dailyPointsClaimed + awardedValue).coerceAtMost(user.dailyPointsTarget)
                userDao.insertOrUpdateUser(user.copy(
                    points = newUserTotalPoints,
                    dailyPointsClaimed = newDailyClaimed
                ))

                sendLocalNotification(
                    title = "Game Run Saved (${game.title})",
                    body = "Scored $score! Extracted +$awardedValue points."
                )
            }
        }
        
        triggerSync()
    }

    suspend fun toggleDarkMode(enabled: Boolean) {
        val user = userDao.getUser() ?: return
        userDao.insertOrUpdateUser(user.copy(darkModeEnabled = enabled))
    }

    suspend fun awardQuizPoints(points: Int): Boolean {
        val user = userDao.getUser() ?: return false
        val newPoints = user.points + points
        val newDailyClaimed = (user.dailyPointsClaimed + points).coerceAtMost(user.dailyPointsTarget)
        val newRank = (42 - (newPoints / 100)).coerceAtLeast(1)
        
        userDao.insertOrUpdateUser(user.copy(
            points = newPoints,
            dailyPointsClaimed = newDailyClaimed,
            engagementRank = newRank
        ))
        
        addChainBurnLog("Burning Quiz", points)
        
        sendLocalNotification(
            title = "Quiz Correct Answer! 🎉",
            body = "Earned +$points points towards LUNC Burn! Total: $newPoints"
        )
        
        triggerSync()
        return true
    }

    suspend fun toggleNotifications(enabled: Boolean) {
        val user = userDao.getUser() ?: return
        userDao.insertOrUpdateUser(user.copy(notificationsEnabled = enabled))
    }

    suspend fun updateLanguage(lang: String) {
        val user = userDao.getUser() ?: return
        userDao.insertOrUpdateUser(user.copy(language = lang))
    }

    suspend fun incrementMetric(metricId: String) {
        analyticsDao.incrementMetric(metricId)
        if (!_isOnline.value) {
            analyticsDao.incrementMetric("network_offline")
        }
    }

    suspend fun sendLocalNotification(title: String, body: String) {
        val user = userDao.getUser()
        if (user != null && !user.notificationsEnabled) {
            // User disabled notifications in preferences
            return
        }
        
        notificationDao.insertNotification(
            NotificationEntity(
                title = title,
                body = body
            )
        )
    }

    suspend fun clearNotifications() {
        // clear old notifications
        val all = notificationDao.getAllNotificationsFlow().first()
        for (item in all) {
            notificationDao.deleteNotification(item.id)
        }
        notificationDao.insertNotification(
            NotificationEntity(
                title = "History Cleared",
                body = "System notifications have been successfully recycled."
            )
        )
    }
}

sealed interface SyncState {
    object Synced : SyncState
    object Syncing : SyncState
}
