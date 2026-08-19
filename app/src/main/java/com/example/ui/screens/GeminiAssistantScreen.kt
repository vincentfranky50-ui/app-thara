package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiChatMessage
import com.example.model.MessageSender
import com.example.model.MessageStatus
import com.example.model.VehicleStatus
import com.example.model.VehicleSummaryCard
import com.example.ui.theme.TharaCardBorder
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextPrimary
import com.example.ui.theme.TharaTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeminiAssistantScreen(
    chatMessages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onNavigateToVehicle: ((String) -> Unit)? = null,
    onOpenEngineMaintenance: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Quels camions sont au ralenti (idling) ?",
        "Y a-t-il des véhicules en réserve de carburant ?",
        "Qui est en excès de vitesse ?",
        "Synthèse générale de la flotte",
        "Véhicules à l'arrêt ou hors ligne"
    )

    // Automatically scroll to bottom when a new message arrives or loading state changes
    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val topBarBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("gemini_chat_screen"),
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(TharaRed, Color(0xFFFF5252))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Thara AI Copilot",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else TharaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TharaRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GEMINI 3.5",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TharaRed
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Télématique en temps réel connectée",
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = if (isDarkTheme) Color.White else TharaTextPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onClearChat()
                            Toast.makeText(context, "Conversation réinitialisée", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Effacer la discussion",
                            tint = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBg
                )
            )
        },
        bottomBar = {
            // Chat Input Bottom Bar
            Surface(
                color = topBarBg,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Quick Prompts Chips Carousel
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        items(quickQuestions) { query ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                                    .border(
                                        1.dp,
                                        if (isDarkTheme) Color(0xFF475569) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        inputText = query
                                        onSendMessage(query)
                                        inputText = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = query,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkTheme) Color(0xFFE2E8F0) else TharaTextPrimary
                                )
                            }
                        }
                    }

                    // Input Field & Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "Posez une question sur la flotte...",
                                    fontSize = 13.sp,
                                    color = if (isDarkTheme) Color(0xFF64748B) else TharaTextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank() && !isLoading) {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TharaRed,
                                unfocusedBorderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                                focusedContainerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                unfocusedContainerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                focusedTextColor = if (isDarkTheme) Color.White else TharaTextPrimary,
                                unfocusedTextColor = if (isDarkTheme) Color.White else TharaTextPrimary
                            ),
                            trailingIcon = {
                                if (inputText.isNotBlank()) {
                                    IconButton(onClick = { inputText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Effacer",
                                            tint = TharaTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank() && !isLoading) TharaRed else if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                                .testTag("chat_send_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer",
                                    tint = if (inputText.isNotBlank()) Color.White else TharaTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    when (message.sender) {
                        MessageSender.USER -> UserMessageBubble(
                            message = message,
                            isDarkTheme = isDarkTheme
                        )
                        MessageSender.ASSISTANT -> AssistantMessageBubble(
                            message = message,
                            onNavigateToVehicle = onNavigateToVehicle,
                            onOpenEngineMaintenance = onOpenEngineMaintenance,
                            onSelectSuggestedAction = { query ->
                                onSendMessage(query)
                            },
                            isDarkTheme = isDarkTheme
                        )
                        MessageSender.SYSTEM -> {}
                    }
                }

                if (isLoading) {
                    item {
                        ThinkingIndicator(isDarkTheme = isDarkTheme)
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(
    message: AiChatMessage,
    isDarkTheme: Boolean
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_message_row"),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(TharaRed)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeStr,
                fontSize = 10.sp,
                color = if (isDarkTheme) Color(0xFF64748B) else TharaTextMuted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantMessageBubble(
    message: AiChatMessage,
    onNavigateToVehicle: ((String) -> Unit)?,
    onOpenEngineMaintenance: (() -> Unit)?,
    onSelectSuggestedAction: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    val cardBg = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assistant_message_row"),
        horizontalArrangement = Arrangement.Start
    ) {
        // AI Avatar
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.5.dp, TharaRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = TharaRed,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.fillMaxWidth(0.92f)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Badge & Copy Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Thara AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else TharaTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "RÉPONSE TÉLÉMATIQUE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Thara AI Response", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier le message",
                                tint = TharaTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message text formatting
                    Text(
                        text = message.text,
                        fontSize = 13.5.sp,
                        color = if (isDarkTheme) Color(0xFFE2E8F0) else TharaTextPrimary,
                        lineHeight = 20.sp
                    )

                    // Referenced Vehicles Mini-Cards (if query mentioned specific vehicles)
                    if (message.referencedVehicles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Véhicules concernés :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            message.referencedVehicles.forEach { card ->
                                VehicleMiniCard(
                                    card = card,
                                    onNavigateToVehicle = onNavigateToVehicle,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    }

                    // Suggested Follow-up Actions
                    if (message.suggestedActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            message.suggestedActions.forEach { action ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(TharaRed.copy(alpha = 0.08f))
                                        .border(1.dp, TharaRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .clickable { onSelectSuggestedAction(action) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "💬 $action",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TharaRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeStr,
                fontSize = 10.sp,
                color = if (isDarkTheme) Color(0xFF64748B) else TharaTextMuted
            )
        }
    }
}

@Composable
fun VehicleMiniCard(
    card: VehicleSummaryCard,
    onNavigateToVehicle: ((String) -> Unit)?,
    isDarkTheme: Boolean
) {
    val statusColor = when (card.status) {
        VehicleStatus.MOVING -> Color(0xFF10B981) // Green
        VehicleStatus.IDLE -> Color(0xFFF59E0B)   // Amber/Orange
        VehicleStatus.STOPPED -> Color(0xFF6B7280) // Gray
        VehicleStatus.OFFLINE -> Color(0xFFDC2626) // Red
        VehicleStatus.ALERT_GEOFENCE -> Color(0xFFEF4444)
    }

    val fuelUrgencyColor = if (card.fuelLevelPct <= 20) Color(0xFFEF4444) else if (card.fuelLevelPct <= 35) Color(0xFFF59E0B) else Color(0xFF10B981)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToVehicle?.invoke(card.vehicleId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = card.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else TharaTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = card.licensePlate,
                            fontSize = 11.sp,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "Chauffeur : ${card.driverName} • ${card.speedKmH.toInt()} km/h",
                        fontSize = 10.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Fuel Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(fuelUrgencyColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = fuelUrgencyColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${card.fuelLevelPct}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = fuelUrgencyColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TharaRed)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Voir ➔",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("thinking_indicator"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .border(1.5.dp, TharaRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = TharaRed,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
            modifier = Modifier.alpha(alpha)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TharaRed,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Thara AI analyse l'état télématique en direct...",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else TharaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
