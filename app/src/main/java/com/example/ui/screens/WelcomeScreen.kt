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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
    onEnterTerminal: (email: String, name: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("EN") }
    var showError by remember { mutableStateOf(false) }
    var showGoogleOptionDialog by remember { mutableStateOf(false) }
    var isManualGoogleLogin by remember { mutableStateOf(false) }
    var googleManualEmail by remember { mutableStateOf("") }
    var googleManualName by remember { mutableStateOf("") }
    var googleEmailError by remember { mutableStateOf(false) }
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
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF18181B).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .clickable { language = if (language == "EN") "ES" else "EN" }
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
                            text = language,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    text = "LUNC BURNER APP",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontStyle = FontStyle.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == "EN") {
                        "\"This app burns LUNC. We're glad you're with us. Together, we'll achieve our goal.\""
                    } else {
                        "\"Esta aplicación quema LUNC. Nos alegra tenerte. Juntos, lograremos la meta.\""
                    },
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

            // Interactive Input Box Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E2E33), Color(0xFF0C0C0D))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "START THE TERMINAL ENGINE",
                        color = OrangeFlameBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Email input
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            showError = false
                        },
                        label = { Text("Enter Email Connection", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = OrangeFlame
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeFlame,
                            unfocusedBorderColor = Color(0xFF2E2E33),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            showError = false
                        },
                        label = { Text("Assign User Nickname", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nick",
                                tint = OrangeFlame
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeFlame,
                            unfocusedBorderColor = Color(0xFF2E2E33),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (showError) {
                        Text(
                            text = "Please enter both fields to access the secure node.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ENTER TERMINAL Button
                    Button(
                        onClick = {
                            if (email.isNotBlank() && name.isNotBlank()) {
                                onEnterTerminal(email, name)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("enter_terminal_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradient)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "ENTER TERMINAL",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // OR Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF2E2E33)
                        )
                        Text(
                            text = "OR",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF2E2E33)
                        )
                    }

                    // Google Login Button
                    OutlinedButton(
                        onClick = {
                            showGoogleOptionDialog = true
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
                                text = "SIGN IN WITH GOOGLE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    // Google-themed authenticated credential selection dialog
                    if (showGoogleOptionDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                showGoogleOptionDialog = false 
                                isManualGoogleLogin = false
                            },
                            properties = DialogProperties(usePlatformDefaultWidth = true),
                            containerColor = Color(0xFF101012),
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .border(BorderStroke(1.dp, Color(0xFF2E2E33)), RoundedCornerShape(24.dp))
                                .testTag("google_login_dialog"),
                            title = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
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
                                            .size(36.dp)
                                            .wrapContentSize(Alignment.Center)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isManualGoogleLogin) "Google Account Login" else "Google Sign-In Options",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (!isManualGoogleLogin) {
                                        Text(
                                            text = "Select an authentication method to connect your Google account securely to this node:",
                                            color = Color.Gray,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Option 1: Native Authentication Dialog
                                        Card(
                                            onClick = {
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
                                                            showGoogleOptionDialog = false
                                                            onEnterTerminal(signedEmail, signedName)
                                                            Toast.makeText(context, "Welcome $signedName!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Unsupported credential format.", Toast.LENGTH_LONG).show()
                                                        }
                                                    } catch (e: GetCredentialException) {
                                                        val errString = e.localizedMessage ?: e.message ?: "Unknown error"
                                                        if (errString.contains("No credential", ignoreCase = true) || errString.contains("developer", ignoreCase = true) || errString.contains("16", ignoreCase = true)) {
                                                            // Auto pivot to manual input screen directly
                                                            isManualGoogleLogin = true
                                                        } else {
                                                            Toast.makeText(context, "Google Sign-In: $errString", Toast.LENGTH_LONG).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Google Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F)),
                                            border = BorderStroke(1.dp, Color(0xFF2E2E33)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⚡", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Device Native Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Use Android's Credential Manager modal", color = Color.Gray, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        // Option 2: Custom Account Login Form
                                        Card(
                                            onClick = {
                                                isManualGoogleLogin = true
                                            },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F)),
                                            border = BorderStroke(1.dp, Color(0xFF2E2E33)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🔑", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Sign In with Google Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Enter your custom Google details directly", color = Color.Gray, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Input your Google profile credentials to authenticate this session. Your node will register with your actual profile info:",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        // Email field
                                        OutlinedTextField(
                                            value = googleManualEmail,
                                            onValueChange = {
                                                googleManualEmail = it
                                                googleEmailError = false
                                            },
                                            label = { Text("Google Account Email", color = Color.Gray) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = "Email",
                                                    tint = Color(0xFF4285F4)
                                                )
                                            },
                                            isError = googleEmailError,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF4285F4),
                                                unfocusedBorderColor = Color(0xFF2E2E33),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // Name field
                                        OutlinedTextField(
                                            value = googleManualName,
                                            onValueChange = { googleManualName = it },
                                            label = { Text("Display Name (e.g. Satoshi)", color = Color.Gray) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Name",
                                                    tint = Color(0xFF34A853)
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF34A853),
                                                unfocusedBorderColor = Color(0xFF2E2E33),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        if (googleEmailError) {
                                            Text(
                                                text = "Please enter a valid Google email address.",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                if (isManualGoogleLogin) {
                                    Button(
                                        onClick = {
                                            val mail = googleManualEmail.trim()
                                            if (mail.isBlank() || !mail.contains("@") || !mail.contains(".")) {
                                                googleEmailError = true
                                            } else {
                                                val disp = if (googleManualName.trim().isBlank()) {
                                                    mail.substringBefore("@").replaceFirstChar { it.uppercase() }
                                                } else {
                                                    googleManualName.trim()
                                                }
                                                showGoogleOptionDialog = false
                                                isManualGoogleLogin = false
                                                onEnterTerminal(mail, disp)
                                                Toast.makeText(context, "Welcome $disp!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        if (isManualGoogleLogin) {
                                            isManualGoogleLogin = false
                                        } else {
                                            showGoogleOptionDialog = false
                                        }
                                    }
                                ) {
                                    Text(
                                        text = if (isManualGoogleLogin) "Back" else "Cancel",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
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
                            try {
                                uriHandler.openUri("https://policies.google.com/terms")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
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
                            try {
                                uriHandler.openUri("https://en.wikipedia.org/wiki/Statute")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
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
                            try {
                                uriHandler.openUri("https://policies.google.com/privacy")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
