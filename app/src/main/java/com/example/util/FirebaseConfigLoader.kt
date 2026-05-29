package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.json.JSONObject
import java.io.InputStream

object FirebaseConfigLoader {
    private const val TAG = "FirebaseConfigLoader"
    private const val CONFIG_FILE_NAME = "firebase-applet-config.json"

    data class FirebaseConfig(
        val projectId: String?,
        val appId: String?,
        val apiKey: String?,
        val storageBucket: String?,
        val messagingSenderId: String?,
        val googleClientId: String?
    )

    fun loadFromAssets(context: Context): FirebaseConfig? {
        return try {
            val inputStream: InputStream = context.assets.open(CONFIG_FILE_NAME)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val projectId = jsonObject.optString("projectId").takeIf { it.isNotBlank() }
            val appId = jsonObject.optString("appId").takeIf { it.isNotBlank() }
            val apiKey = jsonObject.optString("apiKey").takeIf { it.isNotBlank() }
            val storageBucket = jsonObject.optString("storageBucket").takeIf { it.isNotBlank() }
            val messagingSenderId = (jsonObject.optString("messagingSenderId").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("gcmSenderId").takeIf { it.isNotBlank() })

            // Dynamic support for Client ID matching in the same json config
            val googleClientId = (jsonObject.optString("clientId").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("client_id").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("webClientId").takeIf { it.isNotBlank() }
                ?: jsonObject.optString("web_client_id").takeIf { it.isNotBlank() })

            Log.i(TAG, "Successfully loaded $CONFIG_FILE_NAME from assets.")
            FirebaseConfig(projectId, appId, apiKey, storageBucket, messagingSenderId, googleClientId)
        } catch (e: java.io.FileNotFoundException) {
            Log.w(TAG, "$CONFIG_FILE_NAME not found in assets. Fallback to default manual configuration.")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading $CONFIG_FILE_NAME from assets", e)
            null
        }
    }

    fun initializeFirebase(context: Context) {
        try {
            val assetConfig = loadFromAssets(context)
            
            val builder = FirebaseOptions.Builder()
            var hasConfig = false

            // Priority 1: Check if the assets config file contains required keys
            if (assetConfig != null && !assetConfig.apiKey.isNullOrBlank() && !assetConfig.appId.isNullOrBlank()) {
                builder.setApiKey(assetConfig.apiKey)
                builder.setApplicationId(assetConfig.appId)
                assetConfig.projectId?.let { builder.setProjectId(it) }
                assetConfig.storageBucket?.let { builder.setStorageBucket(it) }
                assetConfig.messagingSenderId?.let { builder.setGcmSenderId(it) }
                hasConfig = true
                Log.i(TAG, "Configuring Firebase from $CONFIG_FILE_NAME assets file.")
            } 
            // Priority 2: Check if Secrets panel/BuildConfig variables are populated
            else if (BuildConfig.FIREBASE_API_KEY.isNotBlank() && 
                     BuildConfig.FIREBASE_API_KEY != "your_firebase_api_key_here" && 
                     BuildConfig.FIREBASE_APPLICATION_ID.isNotBlank() && 
                     BuildConfig.FIREBASE_APPLICATION_ID != "your_firebase_application_id_here") {
                builder.setApiKey(BuildConfig.FIREBASE_API_KEY)
                builder.setApplicationId(BuildConfig.FIREBASE_APPLICATION_ID)
                builder.setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                builder.setStorageBucket(BuildConfig.FIREBASE_PROJECT_ID + ".firebasestorage.app")
                builder.setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                hasConfig = true
                Log.i(TAG, "Configuring Firebase from system environment variables / Secrets panel.")
            }

            if (hasConfig) {
                val options = builder.build()
                val apps = FirebaseApp.getApps(context)
                if (apps.isNotEmpty()) {
                    FirebaseApp.getInstance().delete()
                }
                FirebaseApp.initializeApp(context, options)
                Log.i(TAG, "Firebase initialized successfully with custom settings: ${options.projectId}")
            } else {
                Log.i(TAG, "Defaulting to auto-compiled google-services.json settings.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase dynamic configuration and initialization failed", e)
        }
    }

    fun getGoogleClientId(context: Context): String {
        // Priority 1: Check Secrets panel / BuildConfig
        if (BuildConfig.GOOGLE_CLIENT_ID.isNotBlank() && 
            BuildConfig.GOOGLE_CLIENT_ID != "your_google_client_id_here") {
            return BuildConfig.GOOGLE_CLIENT_ID
        }

        // Priority 2: Check inside firebase-applet-config.json
        val assetConfig = loadFromAssets(context)
        if (assetConfig?.googleClientId != null) {
            return assetConfig.googleClientId
        }

        // Priority 3: Resolve default_web_client_id from string resources (generated by google-services.json compiler helper)
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val resolvedId = context.getString(resId)
                if (resolvedId.isNotBlank() && !resolvedId.contains("placeholder")) {
                    return resolvedId
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up dynamic default_web_client_id resource", e)
        }

        // Priority 4: Default generated placeholder key utilizing current project number if possible, or overall fallback
        val projectNum = assetConfig?.messagingSenderId ?: "854611283624"
        return "${projectNum}-pb4v6un9gnd8vj9l0eub2d9j12k1pveo.apps.googleusercontent.com"
    }
}
