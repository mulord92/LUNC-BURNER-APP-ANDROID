package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.activity.viewModels
import com.example.ui.screens.MainContainer
import com.example.ui.screens.LuncAdManager
import com.example.ui.viewmodel.LuncViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  private val viewModel: LuncViewModel by viewModels {
    LuncViewModel.Factory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Google Mobile Ads SDK for AdSense / AdMob
    try {
      MobileAds.initialize(this) {
        try {
          LuncAdManager.loadInterstitial(this)
        } catch (t: Throwable) {
          android.util.Log.e("MainActivity", "Error preloading initial interstitial", t)
        }
      }
    } catch (t: Throwable) {
      android.util.Log.e("MainActivity", "Error initializing MobileAds SDK", t)
    }

    setContent {
      MainContainer(viewModel = viewModel)
    }
  }
}
