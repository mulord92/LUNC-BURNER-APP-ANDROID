package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = "local_user",
    val email: String,
    val name: String,
    val profilePicUrl: String = "",
    val isLoggedIn: Boolean = false,
    val points: Int = 420,
    val engagementRank: Int = 42,
    val daysWithUs: Int = 8,
    val adsWatched: Int = 0,
    val dailyPointsTarget: Int = 5000,
    val dailyPointsClaimed: Int = 0,
    val darkModeEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val language: String = "EN"
)

@Entity(tableName = "games")
data class GameProgressEntity(
    @PrimaryKey val gameId: String,
    val title: String,
    val rewardPoints: Int,
    val claimed: Boolean = false,
    val progress: Float = 0.0f,
    val highScore: Int = 0,
    val lastScore: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "burn_logs")
data class BurnLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monthText: String,
    val destroyedText: String,
    val txId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analytics")
data class AnalyticsMetricEntity(
    @PrimaryKey val metricId: String,
    val description: String,
    val count: Int = 0,
    val lastTriggered: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
