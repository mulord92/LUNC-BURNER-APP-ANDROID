package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserFlow(uid: String = "local_user"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUser(uid: String = "local_user"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET points = :points, engagementRank = :rank WHERE uid = :uid")
    suspend fun updatePoints(uid: String, points: Int, rank: Int)

    @Query("UPDATE users SET adsWatched = :adsWatched, dailyPointsClaimed = :claimed WHERE uid = :uid")
    suspend fun updateDailyChallenge(uid: String, adsWatched: Int, claimed: Int)

    @Query("UPDATE users SET darkModeEnabled = :enabled WHERE uid = :uid")
    suspend fun updateDarkMode(uid: String, enabled: Boolean)

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE uid = :uid")
    suspend fun updateNotificationsEnabled(uid: String, enabled: Boolean)

    @Query("UPDATE users SET language = :language WHERE uid = :uid")
    suspend fun updateLanguage(uid: String, language: String)

    @Query("UPDATE users SET isLoggedIn = :isLoggedIn WHERE uid = :uid")
    suspend fun updateLoginState(uid: String, isLoggedIn: Boolean)
}

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY rewardPoints ASC")
    fun getAllGamesFlow(): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM games WHERE gameId = :gameId LIMIT 1")
    suspend fun getGame(gameId: String): GameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGame(game: GameProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGames(games: List<GameProgressEntity>)

    @Query("UPDATE games SET progress = :progress, claimed = :claimed WHERE gameId = :gameId")
    suspend fun updateGameClaimStatus(gameId: String, progress: Float, claimed: Boolean)

    @Query("UPDATE games SET highScore = :highScore, lastScore = :lastScore WHERE gameId = :gameId")
    suspend fun updateGameScores(gameId: String, highScore: Int, lastScore: Int)
}

@Dao
interface BurnLogDao {
    @Query("SELECT * FROM burn_logs ORDER BY timestamp DESC")
    fun getAllBurnLogsFlow(): Flow<List<BurnLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBurnLog(log: BurnLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBurnLogs(logs: List<BurnLogEntity>)
}

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics")
    fun getAllAnalyticsFlow(): Flow<List<AnalyticsMetricEntity>>

    @Query("SELECT * FROM analytics WHERE metricId = :metricId LIMIT 1")
    suspend fun getMetric(metricId: String): AnalyticsMetricEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetric(metric: AnalyticsMetricEntity)

    @Query("UPDATE analytics SET count = count + 1, lastTriggered = :timestamp WHERE metricId = :metricId")
    suspend fun incrementMetric(metricId: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}
