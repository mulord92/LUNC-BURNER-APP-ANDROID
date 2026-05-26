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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LuncViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = LuncRepository(application, database)

    private val prefs = application.getSharedPreferences("lunc_prefs", android.content.Context.MODE_PRIVATE)
    private val _selectedLanguage = MutableStateFlow(prefs.getString("selected_lang", "EN") ?: "EN")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

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

    fun login(email: String, name: String) {
        viewModelScope.launch {
            repository.login(email, name)
            repository.updateLanguage(_selectedLanguage.value)
        }
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
