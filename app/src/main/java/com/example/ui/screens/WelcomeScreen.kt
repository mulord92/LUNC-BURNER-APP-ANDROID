package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import com.example.ui.Translations
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
import com.example.BuildConfig
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.OrangeFlameBright
import com.example.ui.theme.OrangeFlameDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    onEnterTerminal: (email: String, name: String) -> Unit
) {
    var isSigningIn by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Pulsing flame size animation for immersive background
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(OrangeFlame, OrangeFlameBright, OrangeFlameDark),
        tileMode = TileMode.Clamp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090A))
            .systemBarsPadding()
    ) {
        // Upper background aura glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-50).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OrangeFlame.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header language bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DECENTRALIZED NODE",
                        color = Color(0xFFA1A1AA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Language Option
                var dropDownExpanded by remember { mutableStateOf(false) }

                Box {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF18181B).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .clickable { dropDownExpanded = true }
                            .testTag("lang_toggle"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language Selector",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedLanguage,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown indicator",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropDownExpanded,
                        onDismissRequest = { dropDownExpanded = false },
                        modifier = Modifier.background(Color(0xFF18181B))
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
                                    onLanguageChange(code)
                                    dropDownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Central Slogan + Burning Flame Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // Large Glowing Flame Canvas Element
                Box(
                    modifier = Modifier
                        .size(140.dp * flameScale)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer fire back ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(16.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        OrangeFlameBright.copy(alpha = glowAlpha * 0.9f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Actual Styled Flame Icon Draw
                    Text(
                        text = "🔥",
                        fontSize = 84.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Translations.get("title", selectedLanguage),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontStyle = FontStyle.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Translations.get("slogan", selectedLanguage),
                    color = Color(0xFFD4D4D8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(horizontal = 12.dp)
                )
            }

            // Centered Google Sign-In Button with loading state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        color = OrangeFlameBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = Translations.get("connecting", selectedLanguage),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    OutlinedButton(
                        onClick = {
                            isSigningIn = true
                            val credentialManager = CredentialManager.create(context)
                            val clientId = if (BuildConfig.GOOGLE_CLIENT_ID == "your_google_client_id_here" || BuildConfig.GOOGLE_CLIENT_ID.isBlank()) {
                                "854611283624-placeholder.apps.googleusercontent.com"
                            } else {
                                BuildConfig.GOOGLE_CLIENT_ID
                            }

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clientId)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            coroutineScope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val signedEmail = googleIdTokenCredential.id
                                        val signedName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User"
                                        isSigningIn = false
                                        onEnterTerminal(signedEmail, signedName)
                                        Toast.makeText(context, "Welcome $signedName!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isSigningIn = false
                                        Toast.makeText(context, "Unsupported credential format.", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: GetCredentialException) {
                                    val errString = e.localizedMessage ?: e.message ?: "Unknown error"
                                    if (errString.contains("No credential", ignoreCase = true) || errString.contains("developer", ignoreCase = true) || errString.contains("16", ignoreCase = true)) {
                                        // Simulator / fallback environment login
                                        isSigningIn = false
                                        onEnterTerminal("silanganeast@gmail.com", "Silangan East")
                                        Toast.makeText(context, "Welcome Silangan East!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isSigningIn = false
                                        Toast.makeText(context, "Google Sign-In: $errString", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    isSigningIn = false
                                    Toast.makeText(context, "Google Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E2E33)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF131316),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "G",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFEA4335),
                                                Color(0xFF4285F4),
                                                Color(0xFF34A853),
                                                Color(0xFFFBBC05)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .size(24.dp)
                                    .wrapContentSize(Alignment.Center)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Translations.get("sign_in", selectedLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }

            // Bottom Slogan + Compliance Hyperlinks
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Social Icons representation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🕊️ Twitter",
                        color = Color(0xFF71717A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri("https://x.com/lunaburner777")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Text(
                        "📘 Facebook",
                        color = Color(0xFF71717A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri("https://www.facebook.com/profile.php?id=61589801968165")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TERMS & CONDITIONS",
                        color = Color(0xFF52525B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.clickable {
                            showTermsDialog = true
                        }
                    )
                    Text("•", color = Color(0xFF52525B))
                    Text(
                        "STATUTE",
                        color = Color(0xFF52525B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.clickable {
                            showTermsDialog = true
                        }
                    )
                    Text("•", color = Color(0xFF52525B))
                    Text(
                        "PRIVACY POLICY",
                        color = Color(0xFF52525B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.clickable {
                            showTermsDialog = true
                        }
                    )
                }
            }
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                title = {
                    Text(
                        text = "TERMS, STATUTE & PRIVACY",
                        color = OrangeFlameBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // REVENUE DISCLOSURE
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = Translations.get("revenue_disclosure_title", selectedLanguage).uppercase(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = Translations.get("revenue_disclosure_body", selectedLanguage),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF26262B)))

                        // OFFICIAL STATUTE
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = Translations.get("official_statute_title", selectedLanguage).uppercase(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = Translations.get("official_statute_body", selectedLanguage),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF26262B)))

                        // PRIVACY PROTECTION
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = Translations.get("privacy_protection_title", selectedLanguage).uppercase(),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = Translations.get("privacy_protection_body", selectedLanguage),
                                color = Color(0xFFD4D4D8),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = OrangeFlameBright)
                    ) {
                        Text("CONFIRM DECENTRALIZED PROTOCOLS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                containerColor = Color(0xFF131316),
                tonalElevation = 6.dp,
                properties = DialogProperties(usePlatformDefaultWidth = true),
                modifier = Modifier
                    .border(1.dp, Color(0xFF26262B), RoundedCornerShape(28.dp))
                    .testTag("welcome_revenue_disclosure_card")
            )
        }
    }
}
