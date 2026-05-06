package com.ruralhealth.gramasanjeevini

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. DATA MODEL ---
data class HealthTip(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val color: Color,
    val steps: List<String>
)

object TipsProvider {
    val categories = listOf("All", "Hygiene", "Nutrition", "Seasonal", "Emergency", "Lifestyle")

    val allTips = (1..100).map { i ->
        when (i % 10) {
            0 -> HealthTip(i, "Emergency ORS Prep", "Essential for dehydration and heatstroke.", "Emergency", Icons.Default.Warning, Color(0xFFE64A19), listOf("Wash hands.", "1L clean water.", "6 tsp sugar.", "1/2 tsp salt.", "Stir well.", "Sip slowly over 24 hrs."))
            1 -> HealthTip(i, "Snake Bite Action", "Immediate first aid before the hospital.", "Emergency", Icons.Default.Warning, Color(0xFFB71C1C), listOf("Keep victim calm.", "Immobilize the limb.", "Keep bite below heart.", "Do not cut wound.", "Rush to the ER."))
            2 -> HealthTip(i, "Moringa Nutrition", "Nature's multivitamin from the backyard.", "Nutrition", Icons.Default.Favorite, Color(0xFF2E7D32), listOf("Pluck green leaves.", "Wash thoroughly.", "Add to Dal or Soup.", "Avoid overcooking."))
            3 -> HealthTip(i, "Safe Handwashing", "Prevent 80% of common infections.", "Hygiene", Icons.Default.Face, Color(0xFF0097A7), listOf("Wet hands with water.", "Lather soap for 20s.", "Scrub under nails.", "Rinse well.", "Air dry completely."))
            4 -> HealthTip(i, "Back Injury Safety", "Lifting techniques for farm loads.", "Lifestyle", Icons.Default.Build, Color(0xFF4527A0), listOf("Bend at the knees.", "Keep back straight.", "Lift with your legs.", "Hold load close to body."))
            5 -> HealthTip(i, "Heatstroke Safety", "Protection while working in the sun.", "Seasonal", Icons.Default.Info, Color(0xFFEF6C00), listOf("Work early or late.", "Rest in deep shade.", "Cover head with cloth.", "Eat raw onions daily."))
            6 -> HealthTip(i, "Pure Water Storage", "Keeping stored water safe to drink.", "Hygiene", Icons.Default.CheckCircle, Color(0xFF0288D1), listOf("Filter using cloth.", "Boil for 10 minutes.", "Keep pot covered.", "Never dip dirty hands."))
            7 -> HealthTip(i, "Iron Deficiency", "Tips to prevent weakness and Anaemia.", "Nutrition", Icons.Default.Favorite, Color(0xFFD32F2F), listOf("Eat dark green leaves.", "Add lemon (Vitamin C).", "No tea after meals.", "Cook in iron vessels."))
            8 -> HealthTip(i, "Cardiac Health", "Daily walking for heart fitness.", "Lifestyle", Icons.Default.AccountCircle, Color(0xFF00796B), listOf("30 min brisk walk.", "Deep rhythmic breathing.", "Early morning air.", "Consistent daily routine."))
            else -> HealthTip(i, "Mosquito Control", "Stop Malaria and Dengue at home.", "Seasonal", Icons.Default.Warning, Color(0xFFFF9800), listOf("Check stagnant water.", "Use nets while sleeping.", "Apply Neem oil.", "Keep drains flowing."))
        }
    }
}

// Renamed function to match common calling patterns from MainActivity
@Composable
fun GramSanjeeviniWellnessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F8FB)) {
            HealthTipsContent(onBack = onDismiss)
        }
    }
}

@Composable
fun HealthTipsContent(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCard by remember { mutableStateOf<HealthTip?>(null) }
    var showAboutPopup by remember { mutableStateOf(false) }

    val filteredTips = remember(selectedCategory, searchQuery) {
        TipsProvider.allTips.filter { tip ->
            val matchesCategory = if (selectedCategory == "All") true else tip.category == selectedCategory
            val matchesSearch = tip.title.contains(searchQuery, ignoreCase = true) ||
                    tip.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // HEADER & SEARCH
            Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF7B1FA2)))).statusBarsPadding().padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    Text("Health & Wellness", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(54.dp),
                    placeholder = { Text("Search 100 useful health tips...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF4A148C)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // STATIC LOCALITY FINDER CARD
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(110.dp).clickable { showAboutPopup = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A)),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(65.dp), shape = CircleShape, color = Color(0xFFFFD54F)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE65100), modifier = Modifier.size(32.dp)) }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Section Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Tap to learn about this section", color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                }
            }

            // GLASS CATEGORY BAR
            Box(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(TipsProvider.categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF4A148C) else Color(0xFF4A148C).copy(0.05f),
                            border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFF4A148C).copy(0.3f)),
                        ) {
                            Text(cat, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = if (isSelected) Color.White else Color(0xFF4A148C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // MAIN LIST
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items = filteredTips, key = { it.id }) { tip ->
                    Card(
                        onClick = { selectedCard = tip },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(45.dp).background(tip.color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(tip.icon, null, tint = tip.color, modifier = Modifier.size(20.dp)) }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(tip.category.uppercase(), fontWeight = FontWeight.Black, fontSize = 10.sp, color = tip.color, letterSpacing = 1.5.sp)
                                Text(tip.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))
                                Text("Tap for instructions →", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // ROTATING MISSION POPUP
        if (showAboutPopup) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)), contentAlignment = Alignment.Center) {
                MissionCardSnapEffect(onDismiss = { showAboutPopup = false })
            }
        }

        // STEP-BY-STEP OVERLAY
        selectedCard?.let { tip ->
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable { selectedCard = null }, contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight().clickable(enabled = false) {}, shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(24.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(Modifier.size(40.dp).background(tip.color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(tip.icon, null, tint = tip.color) }
                            IconButton(onClick = { selectedCard = null }) { Icon(Icons.Default.Close, null) }
                        }
                        Text(tip.title, fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF1A1A2E))
                        Spacer(Modifier.height(16.dp))
                        tip.steps.forEachIndexed { index, step ->
                            Row(Modifier.padding(vertical = 6.dp)) {
                                Text("${index + 1}.", color = tip.color, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                                Text(step, fontSize = 15.sp, color = Color(0xFF1A1A2E))
                            }
                        }
                        Button(onClick = { selectedCard = null }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp), colors = ButtonDefaults.buttonColors(containerColor = tip.color)) { Text("GOT IT") }
                    }
                }
            }
        }
    }
}

@Composable
fun MissionCardSnapEffect(onDismiss: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var isVanishing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { rotation.animateTo(360f, animationSpec = tween(1200, easing = FastOutSlowInEasing)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .wrapContentHeight()
            .graphicsLayer {
                rotationY = rotation.value
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
                cameraDistance = 12f * density
            }
            .clickable(enabled = false) {},
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(20.dp)
    ) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.Start) {
            Text("MISSION & CARE", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF4A148C), letterSpacing = 2.sp)
            Spacer(Modifier.height(16.dp))

            MissionRow("🚨", "Emergency instructions you can trust")
            MissionRow("🥗", "Smart nutrition tips for better health")
            MissionRow("🧼", "Daily hygiene made simple")
            MissionRow("👨‍👩‍👧‍👦", "Care designed for your whole family")

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.4f)) // Fixed: HorizontalDivider instead of Divider
            Spacer(Modifier.height(20.dp))

            Text("👉 Tap any card to learn more", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF4A148C), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!isVanishing) {
                        isVanishing = true
                        scope.launch {
                            launch { alpha.animateTo(0f, animationSpec = tween(1000)) }
                            launch { scale.animateTo(2.5f, animationSpec = tween(1000)) }
                            delay(1000)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) {
                Text("CLOSE GUIDE", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MissionRow(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text(emoji, fontSize = 24.sp)
        Spacer(Modifier.width(16.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E), lineHeight = 20.sp)
    }
}