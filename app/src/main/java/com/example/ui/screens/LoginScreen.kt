package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.46f
        val strokeWidth = w * 0.22f

        // Red top arch
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth)
        )

        // Yellow bottom-left arch
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 110f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth)
        )

        // Green bottom-right arch
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth)
        )

        // Blue right & center bar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 270f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth)
        )

        // Blue horizontal crossbar
        val barPath = Path().apply {
            moveTo(cx, cy - (strokeWidth / 2f))
            lineTo(cx + radius, cy - (strokeWidth / 2f))
            lineTo(cx + radius, cy + (strokeWidth / 2f))
            lineTo(cx, cy + (strokeWidth / 2f))
            close()
        }
        drawPath(path = barPath, color = Color(0xFF4285F4), style = Fill)
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("vincentfranky50@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }

    val primaryRed = Color(0xFFDC2626)
    val slate800 = Color(0xFF1E293B)
    val slate600 = Color(0xFF475569)
    val slate400 = Color(0xFF94A3B8)
    val slate100 = Color(0xFFF1F5F9)
    val slate50 = Color(0xFFF8FAFC)
    val borderStrokeColor = Color(0xFFE2E8F0)

    val googleAccounts = listOf(
        Pair("Vincent Franky", "vincentfranky50@gmail.com"),
        Pair("Flotte Admin Manager", "fleet.admin@thara-track.com")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Header (Google Stitch / Figma Inspired)
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bienvenue",
                        fontSize = 14.sp,
                        color = slate600,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, primaryRed.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Portail Sécurisé",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "THARA TRACK",
                    fontSize = 28.sp,
                    color = slate800,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = if (isSignUpMode) "Créez votre compte pour superviser la télématique" else "Connectez-vous à votre console de gestion de flotte",
                    fontSize = 13.sp,
                    color = slate600,
                    lineHeight = 18.sp
                )
            }

            // Google Sign-In Official Card Button (Google One-Tap / OAuth SSO)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable(enabled = !isGoogleSigningIn) {
                        isGoogleSigningIn = true
                        errorMessage = null
                        coroutineScope.launch {
                            delay(600)
                            isGoogleSigningIn = false
                            onLoginSuccess(email.ifBlank { "vincentfranky50@gmail.com" })
                        }
                    }
                    .testTag("google_signin_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, borderStrokeColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Connexion Google en cours...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = slate800
                        )
                    } else {
                        GoogleLogoIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Se connecter avec Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = slate800
                        )
                    }
                }
            }

            // Quick Account Switcher Pill if email matches Google
            if (email.contains("@gmail.com")) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = slate50,
                    border = BorderStroke(1.dp, borderStrokeColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4285F4).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontWeight = FontWeight.Black, color = Color(0xFF4285F4), fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Compte Google détecté", fontSize = 11.sp, color = slate400, fontWeight = FontWeight.Medium)
                                Text(email, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate800)
                            }
                        }

                        TextButton(
                            onClick = {
                                isGoogleSigningIn = true
                                coroutineScope.launch {
                                    delay(400)
                                    isGoogleSigningIn = false
                                    onLoginSuccess(email)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Continuer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                        }
                    }
                }
            }

            // Divider with text "OU PAR IDENTIFIANTS"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = borderStrokeColor)
                Text(
                    text = "OU AVEC EMAIL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = slate400,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = borderStrokeColor)
            }

            // Input Fields Card Container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Email Field
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = slate50),
                    border = BorderStroke(1.dp, borderStrokeColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = slate400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            placeholder = { Text("Adresse email ou compte Google", color = slate400, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (email.isNotEmpty()) {
                            IconButton(onClick = { email = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Effacer", tint = slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (isSignUpMode) {
                    // Verification Code Field
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = slate50),
                        border = BorderStroke(1.dp, borderStrokeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                placeholder = { Text("Code de validation reçu", color = slate400, fontSize = 13.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { /* Send code */ }) {
                                Text("Recevoir le code", color = primaryRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Password Field
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = slate50),
                    border = BorderStroke(1.dp, borderStrokeColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = slate400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            placeholder = {
                                Text(
                                    if (isSignUpMode) "Mot de passe (min. 6 caractères)" else "Mot de passe",
                                    color = slate400,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                                tint = slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (isSignUpMode) {
                    // Confirm Password Field
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = slate50),
                        border = BorderStroke(1.dp, borderStrokeColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = { Text("Confirmer le mot de passe", color = slate400, fontSize = 13.sp) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (!isSignUpMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { /* Forgot password */ },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Mot de passe oublié ?", color = slate600, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = primaryRed.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, primaryRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = primaryRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Primary Action Button (Connexion or S'inscrire)
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Veuillez renseigner votre email et mot de passe."
                        } else {
                            onLoginSuccess(email)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Créer un compte" else "Se connecter",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Secondary Action Toggle Button
                OutlinedButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        errorMessage = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = slate100),
                    border = BorderStroke(1.dp, borderStrokeColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Vous avez déjà un compte ? Connexion" else "Pas de compte ? Créer un accès",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = slate800
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Protection des données de flotte • Conforme RGPD & Télématique",
                    fontSize = 10.sp,
                    color = slate400
                )
            }
        }
    }
}
