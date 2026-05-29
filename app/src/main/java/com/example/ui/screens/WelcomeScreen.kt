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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
import com.example.BuildConfig
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    onEnterTerminal: (email: String, name: String, profilePicUrl: String) -> Unit,
    onCheckEmailRegistered: suspend (String) -> Boolean
) {
    var isSigningIn by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Pristine state variables for Login & Register Tabs
    var activeTab by remember { mutableStateOf(0) } // 0 = Sign In, 1 = Register
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val handleGoogleSuccess = { email: String, name: String, photoUrl: String, method: String ->
        coroutineScope.launch {
            val isReg = onCheckEmailRegistered(email)
            isSigningIn = false
            if (isReg) {
                onEnterTerminal(email, name, photoUrl)
                Toast.makeText(context, "Welcome $name ($method)!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Account not registered. Please register.", Toast.LENGTH_LONG).show()
                activeTab = 1
                registerEmail = email
                registerName = name
            }
        }
    }

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

            // Integrated Custom Login, Register, and Normal Google Authentication Tab Interface
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF2E2E33)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TAB SWITCHER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090A), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Tab 0: SIGN IN
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeTab == 0) OrangeFlame else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = 0 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SECURE LOGIN",
                                    color = if (activeTab == 0) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Tab 1: REGISTER
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (activeTab == 1) OrangeFlame else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = 1 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "REGISTER NODE",
                                    color = if (activeTab == 1) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // TAB CONTENT
                    if (activeTab == 0) {
                        // SIGN IN FORM
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Email Address", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeFlameBright,
                                unfocusedBorderColor = Color(0xFF26262B),
                                focusedLabelColor = OrangeFlameBright,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = if (loginPasswordVisible) "HIDE" else "SHOW",
                                    color = OrangeFlameBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { loginPasswordVisible = !loginPasswordVisible }
                                        .padding(8.dp)
                                )
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeFlameBright,
                                unfocusedBorderColor = Color(0xFF26262B),
                                focusedLabelColor = OrangeFlameBright,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (loginEmail.isNotBlank()) {
                                    val email = loginEmail.trim()
                                    val fallbackName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                                    
                                    coroutineScope.launch {
                                        val isReg = onCheckEmailRegistered(email)
                                        if (isReg) {
                                            onEnterTerminal(email, fallbackName, "")
                                            Toast.makeText(context, "Welcome back, $fallbackName!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Account not registered. Please register.", Toast.LENGTH_LONG).show()
                                            activeTab = 1
                                            registerEmail = email
                                            registerName = fallbackName
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeFlame),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "DECRYPT & ENTER NODE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                    } else {
                        // REGISTER FORM
                        OutlinedTextField(
                            value = registerName,
                            onValueChange = { registerName = it },
                            label = { Text("Display Name", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeFlameBright,
                                unfocusedBorderColor = Color(0xFF26262B),
                                focusedLabelColor = OrangeFlameBright,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = registerEmail,
                            onValueChange = { registerEmail = it },
                            label = { Text("Email Address", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeFlameBright,
                                unfocusedBorderColor = Color(0xFF26262B),
                                focusedLabelColor = OrangeFlameBright,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = registerPassword,
                            onValueChange = { registerPassword = it },
                            label = { Text("Password", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Icon",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = if (registerPasswordVisible) "HIDE" else "SHOW",
                                    color = OrangeFlameBright,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { registerPasswordVisible = !registerPasswordVisible }
                                        .padding(8.dp)
                                )
                            },
                            visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangeFlameBright,
                                unfocusedBorderColor = Color(0xFF26262B),
                                focusedLabelColor = OrangeFlameBright,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (registerEmail.isNotBlank() && registerName.isNotBlank()) {
                                    val email = registerEmail.trim()
                                    val name = registerName.trim()
                                    onEnterTerminal(email, name, "")
                                    Toast.makeText(context, "Node Profile registered for $name!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeFlame),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "INITIALIZE PROTOCOL NODE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // STANDARD "OR" DIVIDER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF2E2E33)))
                        Text(
                            text = "OR CONTINUE WITH",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF2E2E33)))
                    }

                    // DIRECT, NORMAL PROCESS FOR GOOGLE LOGIN
                    if (isSigningIn) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = OrangeFlameBright,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = Translations.get("connecting", selectedLanguage),
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                isSigningIn = true
                                val credentialManager = CredentialManager.create(context)
                                val clientId = com.example.util.FirebaseConfigLoader.getGoogleClientId(context)

                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(clientId)
                                    .setAutoSelectEnabled(false)
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
                                            val profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                                            val idToken = googleIdTokenCredential.idToken
                                            
                                            if (!idToken.isNullOrBlank()) {
                                                isSigningIn = true
                                                try {
                                                    val firebaseAuth = FirebaseAuth.getInstance()
                                                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                                    firebaseAuth.signInWithCredential(firebaseCredential)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                val firebaseUser = task.result?.user
                                                                val email = firebaseUser?.email ?: signedEmail
                                                                val name = firebaseUser?.displayName ?: signedName
                                                                val photoUrl = firebaseUser?.photoUrl?.toString() ?: profilePictureUri
                                                                handleGoogleSuccess(email, name, photoUrl, "Firebase Connected")
                                                            } else {
                                                                val err = task.exception?.localizedMessage ?: "Firebase login failed"
                                                                Toast.makeText(context, "Firebase Sign-In failed: $err. Falling back to offline...", Toast.LENGTH_LONG).show()
                                                                handleGoogleSuccess(signedEmail, signedName, profilePictureUri, "Offline")
                                                            }
                                                        }
                                                } catch (exc: Exception) {
                                                    handleGoogleSuccess(signedEmail, signedName, profilePictureUri, "Offline (Firebase Uninitialized)")
                                                }
                                            } else {
                                                handleGoogleSuccess(signedEmail, signedName, profilePictureUri, "Offline")
                                            }
                                        } else {
                                            isSigningIn = false
                                            Toast.makeText(context, "Unsupported credential format.", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: GetCredentialException) {
                                        try {
                                            val firebaseAuth = FirebaseAuth.getInstance()
                                            val provider = com.google.firebase.auth.OAuthProvider.newBuilder("google.com").build()
                                            val activity = context as? android.app.Activity
                                            if (activity != null) {
                                                firebaseAuth.startActivityForSignInWithProvider(activity, provider)
                                                    .addOnSuccessListener { authResult ->
                                                        val user = authResult.user
                                                        if (user != null) {
                                                            handleGoogleSuccess(
                                                                user.email ?: "",
                                                                user.displayName ?: "Google User",
                                                                user.photoUrl?.toString() ?: "",
                                                                "Firebase Web"
                                                            )
                                                        } else {
                                                            isSigningIn = false
                                                        }
                                                    }
                                                    .addOnFailureListener { fallbackErr ->
                                                        isSigningIn = false
                                                        val msg = fallbackErr.localizedMessage ?: ""
                                                        val cause = if (msg.contains("get your package")) {
                                                            "App not authorized in Firebase. You must add the SHA-1 to the Firebase Console: CE:01:66:70:14:16:4D:B3:36:FC:4A:DF:0D:05:DF:2E:70:73:2A:EC for package com.aistudio.luncburner.vuxjqp."
                                                        } else {
                                                            msg
                                                        }
                                                        Toast.makeText(context, "Web Sign-In failed: $cause", Toast.LENGTH_LONG).show()
                                                    }
                                            } else {
                                                isSigningIn = false
                                                val errString = e.localizedMessage ?: e.message ?: "Unknown error"
                                                Toast.makeText(context, "Native Sign-In failed: $errString", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (fallbackEx: Exception) {
                                            isSigningIn = false
                                            val errString = e.localizedMessage ?: e.message ?: "Unknown error"
                                            Toast.makeText(context, "Google Sign-In failed: $errString", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        isSigningIn = false
                                        val errString = e.localizedMessage ?: e.message ?: "Unknown error"
                                        Toast.makeText(context, "Google Sign-In failed: $errString", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E2E33)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF09090A),
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
                                    fontSize = 14.sp,
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
                                        .size(18.dp)
                                        .wrapContentSize(Alignment.Center)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Translations.get("sign_in", selectedLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
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
