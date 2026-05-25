package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton manager to securely load and present Google Mobile Ads Interstitial ads.
 */
object LuncAdManager {
    private const val TAG = "LuncAdManager"
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    // Using Google standard interstitial test unit ID for testing
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun loadInterstitial(context: Context) {
        if (mInterstitialAd != null || isAdLoading) return
        isAdLoading = true
        
        Log.d(TAG, "Requesting AdSense/AdMob interstitial ad...")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad failed to load: ${adError.message}")
                    mInterstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad successfully loaded and cached!")
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed by user.")
                    mInterstitialAd = null
                    loadInterstitial(activity) // pre-cache next active instance
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Ad failed to present: ${error.message}")
                    mInterstitialAd = null
                    loadInterstitial(activity) // reload key callback
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            Log.w(TAG, "No Ad cached. Executing standard flow immediately.")
            // Trigger prefetch for subsequent user runs
            loadInterstitial(activity)
            onAdClosed()
        }
    }
}

/**
 * A beautiful, elegant banner ad widget that utilizes Google Ads / AdSense dynamically,
 * with an integrated loading skeleton and custom branding labels.
 */
@Composable
fun AdMobBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(Color(0xFF2E2E33), Color(0xFF18181B))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: AdSense branding label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPONSORED BY GOOGLE ADSENSE",
                        color = Color(0xFF3B82F6),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2E2E33),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Ad",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (hasError) {
                // Return a graceful matching styled internal self-sponsored banner
                SelfSponsoredBanner()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF1E1E22), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                // Using Google standard banner test unit ID
                                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        super.onAdLoaded()
                                        isAdLoaded = true
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        Log.e("AdMobBanner", "Failed to load banner ad: ${error.message}")
                                        hasError = true
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isAdLoaded) {
                        CircularProgressIndicator(
                            color = Color(0xFF3B82F6),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelfSponsoredBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔥 BURN LUNC TO REDUCE TOTAL SUPPLY 🔥",
            color = Color(0xFFFF5722),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Participate in daily challenge nodes to accelerate burns.",
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
