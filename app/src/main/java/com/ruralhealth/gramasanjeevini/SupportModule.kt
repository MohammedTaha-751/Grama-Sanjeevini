package com.ruralhealth.gramasanjeevini

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import kotlinx.coroutines.launch

@Composable
fun SupportModuleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Logic States
    var activePopup by remember { mutableStateOf<String?>(null) }
    var suggestionText by remember { mutableStateOf("") }

    // Confirmation States
    var showCallConfirm by remember { mutableStateOf(false) }
    var showChatConfirm by remember { mutableStateOf(false) }
    var showEmailConfirm by remember { mutableStateOf(false) }
    var showSuggestionConfirm by remember { mutableStateOf(false) }

    // Unified Premium Purple Theme
    val purpleTheme = Color(0xFF673AB7)
    val headerGradient = Brush.verticalGradient(listOf(purpleTheme, Color(0xFF512DA8)))

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                Box(Modifier.fillMaxWidth().background(headerGradient).statusBarsPadding().padding(vertical = 15.dp)) {
                    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(onClick = onBack, color = Color.White.copy(0.2f), shape = CircleShape) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                        Text("HELP & CONTACT", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 18.sp)
                        Spacer(Modifier.width(44.dp))
                    }
                }
            },
            containerColor = Color(0xFFF4F7FA)
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Stable Operator Guide Card
                    SpecialGuideCard(
                        title = "Operator Guide",
                        desc = "Learn how to navigate the portal",
                        backgroundBrush = headerGradient,
                        onClick = { activePopup = "guide" }
                    )

                    ContactCommandCard("OFFICE LOCATION", "Visit our regional headquarters", Icons.Default.LocationOn, Color(0xFF5C6BC0)) { activePopup = "address" }
                    ContactCommandCard("PHONE CALL", "Speak with us directly", Icons.Default.Call, Color(0xFF43A047)) { activePopup = "phone" }
                    ContactCommandCard("WHATSAPP CHAT", "Quick Chat Support", Icons.AutoMirrored.Filled.Send, Color(0xFF25D366)) { activePopup = "whatsapp" }
                    ContactCommandCard("EMAIL SUPPORT", "Send us a detailed inquiry", Icons.Default.Email, Color(0xFFFB8C00)) { activePopup = "email" }

                    ContactCommandCard("IMPROVE PORTAL", "Feedback & Suggestions", Icons.Default.Edit, purpleTheme) { activePopup = "suggest" }
                    ContactCommandCard("PROJECT INTEL", "Our rural health mission", Icons.Default.Star, Color(0xFFFF9800)) { activePopup = "mission" }

                    Spacer(Modifier.height(20.dp))
                }

                // --- OVERLAYS ---

                if (activePopup == "guide") {
                    val rotation = remember { Animatable(180f) }

                    LaunchedEffect(Unit) {
                        rotation.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
                    }

                    SupportDetailOverlay(
                        onDismiss = {
                            scope.launch {
                                rotation.animateTo(180f, tween(1000))
                                activePopup = null
                            }
                        },
                        enterTransition = fadeIn()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .graphicsLayer {
                                    rotationY = rotation.value
                                    cameraDistance = 12f * density
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(12.dp),
                            border = BorderStroke(1.dp, purpleTheme.copy(0.2f))
                        ) {
                            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, null, tint = purpleTheme, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("SYSTEM MANUAL", fontWeight = FontWeight.Black, color = purpleTheme, fontSize = 18.sp)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "1. Log medicine daily\n2. Use Connect for regional sharing\n3. Contact support for system issues",
                                    fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center, lineHeight = 22.sp
                                )
                                Spacer(Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            rotation.animateTo(-180f, tween(600)) // Rotates away
                                            activePopup = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = purpleTheme),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("UNDERSTOOD", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // --- REMAINING OVERLAYS (Keep same as original) ---
                if (activePopup == "address") {
                    SupportDetailOverlay(onDismiss = { activePopup = null }, enterTransition = slideInVertically { it } + fadeIn()) {
                        SupportDetailContent("OUR OFFICE", Icons.Default.Home, Color(0xFF5C6BC0), "Bengaluru, Karnataka\nOpen: 9 AM - 6 PM", "CLOSE") { activePopup = null }
                    }
                }

                if (activePopup == "phone") {
                    SupportDetailOverlay(onDismiss = { activePopup = null }, enterTransition = scaleIn(initialScale = 0.5f) + fadeIn()) {
                        SupportDetailContent("VOICE CONTACT", Icons.Default.Phone, Color(0xFF43A047), "Verified Number:\n+91 7899207313", "START CALL") { showCallConfirm = true }
                    }
                }

                if (activePopup == "whatsapp") {
                    SupportDetailOverlay(onDismiss = { activePopup = null }, enterTransition = slideInHorizontally { it } + fadeIn()) {
                        SupportDetailContent("CHAT LINK", Icons.AutoMirrored.Filled.Send, Color(0xFF25D366), "Instant WhatsApp support link.", "OPEN WHATSAPP") { showChatConfirm = true }
                    }
                }

                if (activePopup == "email") {
                    SupportDetailOverlay(onDismiss = { activePopup = null }, enterTransition = fadeIn(animationSpec = tween(600))) {
                        SupportDetailContent("MAILBOX", Icons.Default.Email, Color(0xFFFB8C00), "mohammedtahaahamed@gmail.com", "WRITE EMAIL") { showEmailConfirm = true }
                    }
                }

                if (activePopup == "suggest") {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable { activePopup = null }, contentAlignment = Alignment.BottomCenter) {
                        AnimatedVisibility(visible = true, enter = slideInVertically { it } + fadeIn()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable(enabled = false) {},
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(Modifier.padding(24.dp).navigationBarsPadding()) {
                                    Text("Suggest a Feature", fontWeight = FontWeight.Black, fontSize = 20.sp, color = purpleTheme)
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = suggestionText,
                                        onValueChange = { suggestionText = it },
                                        placeholder = { Text("What is missing?") },
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    Button(onClick = { showSuggestionConfirm = true }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = purpleTheme), shape = RoundedCornerShape(12.dp)) {
                                        Text("SUBMIT IDEA", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (activePopup == "mission") {
                    val rotMission = remember { Animatable(0f) }
                    LaunchedEffect(Unit) { rotMission.animateTo(360f, tween(800)) }
                    SupportDetailOverlay(onDismiss = { activePopup = null }, enterTransition = fadeIn()) {
                        Card(modifier = Modifier.fillMaxWidth(0.85f).graphicsLayer { rotationX = rotMission.value }, shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(50.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("OUR MISSION", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFFF9800))
                                Text("Empowering rural health by digitizing medicine access.", textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.DarkGray)
                                Spacer(Modifier.height(24.dp))
                                TextButton(onClick = { activePopup = null }) { Text("ACKNOWLEDGE", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }

                // --- CONFIRMATIONS ---
                if (showCallConfirm) {
                    SupportConfirmDialog(title = "Initialize Call", text = "Would you like to dial our support line now?", color = Color(0xFF43A047), onDismiss = { showCallConfirm = false; activePopup = null }, onConfirm = { activePopup = null; context.startActivity(Intent(Intent.ACTION_DIAL, "tel:7899207313".toUri())) })
                }
                if (showChatConfirm) {
                    SupportConfirmDialog(title = "Connect on WhatsApp", text = "Open WhatsApp to start a secure chat with support", color = Color(0xFF25D366), onDismiss = { showChatConfirm = false; activePopup = null }, onConfirm = { activePopup = null; context.startActivity(Intent(Intent.ACTION_VIEW, "https://api.whatsapp.com/send?phone=917899207313".toUri())) })
                }
                if (showEmailConfirm) {
                    SupportConfirmDialog(title = "Write an Email", text = "Would you like to open your mail app to contact the developer?", color = Color(0xFFFB8C00), onDismiss = { showEmailConfirm = false; activePopup = null }, onConfirm = { activePopup = null; context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:mohammedtahaahamed@gmail.com".toUri())) })
                }
                if (showSuggestionConfirm) {
                    SupportConfirmDialog(title = "Submit Feedback", text = "Transmit your suggestion via SMS to our team?", color = purpleTheme, onDismiss = { showSuggestionConfirm = false; activePopup = null }, onConfirm = { activePopup = null; context.startActivity(Intent(Intent.ACTION_VIEW, "sms:7899207313?body=Suggestion: $suggestionText".toUri())) })
                }
            }
        }
    }
}

@Composable
fun SpecialGuideCard(title: String, desc: String, backgroundBrush: Brush, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleFactor by animateFloatAsState(if (isPressed) 0.96f else 1f)

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().scale(scaleFactor),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(Modifier.background(backgroundBrush).padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text(desc, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun SupportConfirmDialog(title: String, text: String, color: Color, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.Bold) }, text = { Text(text) }, confirmButton = { Button(onClick = { onConfirm(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = color)) { Text("YES") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("NO", color = Color.Red) } }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable
fun SupportDetailOverlay(onDismiss: () -> Unit, enterTransition: EnterTransition, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = true, enter = enterTransition) { Box(Modifier.clickable(enabled = false) {}) { content() } }
    }
}

@Composable
fun SupportDetailContent(title: String, icon: ImageVector, color: Color, info: String, buttonText: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(0.85f), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(15.dp)) {
        Column(Modifier.padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(60.dp).padding(bottom = 10.dp))
            Spacer(Modifier.height(5.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
            Text(info, textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 10.dp))
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(buttonText, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ContactCommandCard(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleValue by animateFloatAsState(if (isPressed) 0.96f else 1f)
    Card(onClick = onClick, interactionSource = interactionSource, modifier = Modifier.fillMaxWidth().scale(scaleValue), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Gray)
                Text(desc, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}