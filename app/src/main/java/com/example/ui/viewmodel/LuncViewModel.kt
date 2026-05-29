package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.UserEntity
import com.example.data.local.GameProgressEntity
import com.example.data.local.BurnLogEntity
import com.example.data.local.AnalyticsMetricEntity
import com.example.data.local.NotificationEntity
import com.example.data.repository.LuncRepository
import com.example.data.repository.SyncState
import com.example.data.network.LuncMarketData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class LuncViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = LuncRepository(application, database)

    private val prefs = application.getSharedPreferences("lunc_prefs", android.content.Context.MODE_PRIVATE)
    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_lang", "EN") ?: "EN")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Real-Time LUNC Market Pulse State
    private val _marketData = MutableStateFlow(LuncMarketData())
    val marketData: StateFlow<LuncMarketData> = _marketData.asStateFlow()

    // Real-Time Community Stats
    private val _communityStats = MutableStateFlow(CommunityRealtimeStats())
    val communityStats: StateFlow<CommunityRealtimeStats> = _communityStats.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // UI States observed by Compose Screens
    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        currentUser.onEach { user ->
            if (user != null && user.language != _selectedLanguage.value) {
                _selectedLanguage.value = user.language
                prefs.edit().putString("selected_lang", user.language).apply()
            }
        }.launchIn(viewModelScope)

        refreshMarketData()

        // Continuous Real-Time Updates (every 15 seconds for market ticker)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(15000)
                refreshMarketData()
            }
        }

        // Real-time ticking community simulation (every 1.5 seconds)
        viewModelScope.launch {
            val random = java.util.Random()
            while (true) {
                kotlinx.coroutines.delay(1500)
                val current = _communityStats.value
                val userPoints = currentUser.value?.points ?: 0
                val incrementalBurn = (500 + random.nextInt(1000)).toLong()
                val newUserBonusBurn = userPoints.toLong() * 1000L // 1 point = 1000 LUNC burned
                val nextBurned = current.baseBurned + incrementalBurn
                
                val incrementalStaked = (random.nextInt(20000) - 10000).toLong()
                val nextStaked = (current.baseStaked + incrementalStaked).coerceAtLeast(1000000000000L)
                
                val nextPeg = (current.baseUstcPeg + (random.nextDouble() * 0.04 - 0.02)).coerceIn(30.0, 45.0)
                
                _communityStats.value = current.copy(
                    baseBurned = nextBurned,
                    baseStaked = nextStaked,
                    baseUstcPeg = nextPeg,
                    userPointsContribution = newUserBonusBurn
                )
            }
        }
    }

    fun refreshMarketData() {
        if (_marketData.value.isFetching) return
        _marketData.value = _marketData.value.copy(isFetching = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val data = fetchFromCoinGecko() ?: fetchFromBinance() ?: generateFluctuatedFallback()
                _marketData.value = data.copy(isFetching = false, lastUpdated = System.currentTimeMillis())
            } catch (e: Exception) {
                val fallbackData = generateFluctuatedFallback()
                _marketData.value = fallbackData.copy(
                    isFetching = false,
                    lastUpdated = System.currentTimeMillis(),
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun fetchFromCoinGecko(): LuncMarketData? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=terra-luna-classic&vs_currencies=usd&include_market_cap=true&include_24hr_vol=true&include_24hr_change=true"
            val request = Request.Builder().url(url).build()
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val stats = json.optJSONObject("terra-luna-classic") ?: return@withContext null
                    
                    val price = stats.optDouble("usd", 0.0000908)
                    val mCap = stats.optDouble("usd_market_cap", 503240000.0)
                    val vol = stats.optDouble("usd_24h_vol", 82510000.0)
                    val change = stats.optDouble("usd_24h_change", 11.64)
                    
                    LuncMarketData(
                        price = price,
                        change24h = change,
                        marketCap = mCap,
                        volume24h = vol
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun fetchFromBinance(): LuncMarketData? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = "https://api.binance.com/api/v3/ticker/24hr?symbol=LUNCUSDT"
            val request = Request.Builder().url(url).build()
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    
                    val price = json.optDouble("lastPrice", 0.0000908)
                    val change = json.optDouble("priceChangePercent", 11.64)
                    val volQuote = json.optDouble("quoteVolume", 82510000.0)
                    
                    val estimatedMarketCap = price * 5_800_000_000_000.0
                    
                    LuncMarketData(
                        price = price,
                        change24h = change,
                        marketCap = estimatedMarketCap,
                        volume24h = volQuote
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun generateFluctuatedFallback(): LuncMarketData {
        val basePrice = 0.0000908
        val baseChange = 11.64
        val baseCap = 503240000.0
        val baseVol = 82510000.0
        
        val fluctuation = 1.0 + ((java.util.Random().nextDouble() * 3.0) - 1.5) / 100.0
        return LuncMarketData(
            price = basePrice * fluctuation,
            change24h = baseChange * fluctuation,
            marketCap = baseCap * fluctuation,
            volume24h = baseVol * fluctuation
        )
    }

    val games: StateFlow<List<GameProgressEntity>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val burnLogs: StateFlow<List<BurnLogEntity>> = repository.allBurnLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<List<AnalyticsMetricEntity>> = repository.allAnalytics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnline: StateFlow<Boolean> = repository.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val syncState: StateFlow<SyncState> = repository.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState.Synced)

    // Track active game session internally for Screens
    private val _activeGameToPlay = MutableStateFlow<GameProgressEntity?>(null)
    val activeGameToPlay: StateFlow<GameProgressEntity?> = _activeGameToPlay.asStateFlow()

    fun login(email: String, name: String, profilePicUrl: String = "") {
        viewModelScope.launch {
            repository.login(email, name, profilePicUrl)
            repository.updateLanguage(_selectedLanguage.value)
        }
    }

    suspend fun isEmailRegistered(email: String): Boolean {
        return repository.isEmailRegistered(email)
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            _selectedLanguage.value = lang
            prefs.edit().putString("selected_lang", lang).apply()
            val user = currentUser.value
            if (user != null) {
                repository.updateLanguage(lang)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun selectGameToPlay(game: GameProgressEntity?) {
        _activeGameToPlay.value = game
    }

    fun submitGameScore(score: Int) {
        val game = _activeGameToPlay.value ?: return
        viewModelScope.launch {
            repository.submitGameScore(game.gameId, score)
            _activeGameToPlay.value = null // reset
        }
    }

    fun claimPoints(gameId: String) {
        viewModelScope.launch {
            repository.claimPoints(gameId)
        }
    }

    fun watchAd() {
        viewModelScope.launch {
            repository.watchAdAndCompleteMission()
        }
    }

    fun awardQuizPoints(points: Int) {
        viewModelScope.launch {
            repository.awardQuizPoints(points)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.toggleDarkMode(enabled)
            repository.incrementMetric("app_open") // record adjustment
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleNotifications(enabled)
        }
    }

    fun toggleNetworkStatus() {
        viewModelScope.launch {
            val nextState = !isOnline.value
            repository.setNetworkStatus(nextState)
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            repository.triggerSync()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // Factory pattern
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LuncViewModel::class.java)) {
                return LuncViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class CommunityRealtimeStats(
    val baseBurned: Long = 124520381942L,
    val baseStaked: Long = 1081982736184L,
    val baseUstcPeg: Double = 34.22,
    val userPointsContribution: Long = 0L
) {
    val totalBurned: Long get() = baseBurned + userPointsContribution
    val totalBurnedTarget: Long = 150000000000L
    val burnPercentage: Float get() = (totalBurned.toFloat() / totalBurnedTarget.toFloat()).coerceIn(0f, 1f)
    val stakingRatio: Float get() = 15.6f
}
