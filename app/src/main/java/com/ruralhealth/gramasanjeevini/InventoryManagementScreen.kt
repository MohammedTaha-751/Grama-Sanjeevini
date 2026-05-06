@file:OptIn(ExperimentalAnimationApi::class)

package com.ruralhealth.gramasanjeevini

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. DATA LAYER ---
data class InventoryMed(
    val id: Int,
    val name: String,
    val chemical: String,
    val usage: String,
    val expiryDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

object InventoryRepo {
    var nextId = 2
    var isRecentSortRead = mutableStateOf(false)
    var isRecentSortUpdate = mutableStateOf(false)
    var isRecentSortDelete = mutableStateOf(false)

    val stock = mutableStateListOf(
        InventoryMed(1, "Dolo 650", "Paracetamol", "Fever Relief", "12102026", timestamp = 0L)
    )
}

fun formatPharmaDate(raw: String): String {
    if (raw.length != 8) return raw
    return "${raw.take(2)}/${raw.substring(2, 4)}/${raw.takeLast(4)}"
}

class PharmaDateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text.filter { it.isDigit() }.take(8)
        var out = ""
        for (i in input.indices) {
            out += input[i]
            if (i == 1 || i == 3) out += "/"
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 1) offset else if (offset <= 3) offset + 1 else if (offset <= 8) offset + 2 else out.length
            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else if (offset <= 5) offset - 1 else if (offset <= 10) offset - 2 else input.length
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

// --- 2. MASTER SCREEN SYSTEM ---
@Composable
fun InventoryManagementScreen(onBack: () -> Unit) {
    var currentSubScreen by remember { mutableStateOf("main") }
    var selectedMed by remember { mutableStateOf<InventoryMed?>(null) }
    var showActionModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        BackHandler {
            if (currentSubScreen != "main") currentSubScreen = "main" else onBack()
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFBFBFE)) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFFC2185B), Color(0xFF880E4F))))
                        .statusBarsPadding()
                        .padding(top = 15.dp, bottom = 25.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { if (currentSubScreen == "main") onBack() else currentSubScreen = "main" },
                            color = Color.White.copy(0.2f), shape = CircleShape, modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "PHARMA COMMAND",
                                color = Color.White.copy(0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            AnimatedContent(targetState = currentSubScreen, label = "Title") { label ->
                                Text(
                                    label.uppercase(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            if (currentSubScreen == "read" || currentSubScreen == "update" || currentSubScreen == "delete") {
                                val isRecent = when(currentSubScreen) {
                                    "read" -> InventoryRepo.isRecentSortRead
                                    "update" -> InventoryRepo.isRecentSortUpdate
                                    else -> InventoryRepo.isRecentSortDelete
                                }
                                Surface(
                                    onClick = { isRecent.value = !isRecent.value },
                                    color = if (isRecent.value) Color.White else Color.White.copy(0.15f),
                                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.List, null, tint = if (isRecent.value) Color(0xFF880E4F) else Color.White, modifier = Modifier.padding(10.dp))
                                }
                            } else {
                                Surface(
                                    onClick = { Toast.makeText(context, "System Online & Secure", Toast.LENGTH_SHORT).show() },
                                    color = Color.White.copy(0.2f), shape = CircleShape, modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentSubScreen,
                        label = "BodyTransition",
                        transitionSpec = {
                            if (targetState != "main") {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }
                        }
                    ) { screen ->
                        when (screen) {
                            "main" -> MainDashboardCentered { currentSubScreen = it }
                            "create" -> RegistrationSilkForm { med ->
                                InventoryRepo.stock.add(med.copy(timestamp = System.currentTimeMillis()))
                                InventoryRepo.nextId++
                                scope.launch {
                                    Toast.makeText(context, "Medicine Added Successfully", Toast.LENGTH_SHORT).show()
                                    currentSubScreen = "main"
                                }
                            }
                            "read", "update", "delete" -> DirectoryListView(screen) { med ->
                                selectedMed = med
                                showActionModal = true
                            }
                        }
                    }
                }
            }

            if (showActionModal && selectedMed != null) {
                EliteMasterModal(
                    med = selectedMed!!,
                    mode = currentSubScreen,
                    onDismiss = { showActionModal = false },
                    onDelete = {
                        InventoryRepo.stock.removeIf { it.id == selectedMed?.id }
                        Toast.makeText(context, "Medicine removed from directory", Toast.LENGTH_SHORT).show()
                        showActionModal = false
                    },
                    onUpdate = { updated ->
                        val idx = InventoryRepo.stock.indexOfFirst { it.id == updated.id }
                        if (idx != -1) InventoryRepo.stock[idx] = updated.copy(timestamp = System.currentTimeMillis())
                    }
                )
            }
        }
    }
}

// --- 3. MAIN DASHBOARD ---
@Composable
fun MainDashboardCentered(onNavigate: (String) -> Unit) {
    var showGuidePopup by remember { mutableStateOf(false) }
    var showExamplePopup by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
    ) {
        item {
            FrontPremiumCard("Inventory Guide", "System Capabilities", Icons.Default.Info, iconBg = Color(0xFF1976D2)) {
                showGuidePopup = true
            }
        }

        val items = listOf(
            Triple("CREATE", Icons.Default.Add, "New Entry"),
            Triple("READ", Icons.Default.Search, "View All"),
            Triple("UPDATE", Icons.Default.Edit, "Edit Data"),
            Triple("DELETE", Icons.Default.Delete, "Remove Stock")
        )

        items(items) { (t, i, d) ->
            Surface(
                onClick = { onNavigate(t.lowercase()) },
                shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().height(90.dp)
            ) {
                Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(Color(0xFFFCE4EC), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(i, null, tint = Color(0xFFC2185B), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF2C3E50))
                        Text(d, fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                }
            }
        }

        item {
            FrontPremiumCard("Medicine Examples", "Registration Samples", Icons.Default.Star, iconBg = Color(0xFFFFD700)) {
                showExamplePopup = true
            }
        }
    }

    if (showGuidePopup) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
            MissionCardSnapEffect(
                title = "SYSTEM GUIDE",
                isFanRotation = true,
                lines = listOf("📦 CREATE: Add new medicine to stock",
                    "🔍 READ: Search and view details",
                    "✏️ UPDATE: Edit existing records",
                    "🗑️ DELETE: Remove expired stock",
                    "📅 FORMAT: Use DD/MM/YYYY for dates",
                ),
                onDismiss = { showGuidePopup = false }
            )
        }
    }

    if (showExamplePopup) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
            MissionCardSnapEffect(
                title = "NEW ENTRY EXAMPLE",
                isFanRotation = false,
                lines = listOf("📝 Step 1: Enter Name (e.g., Dolo)",
                    "🧪 Step 2: Add Composition (e.g., Paracetamol)",
                    "🏥 Step 3: Add Indication (e.g., Fever)",
                    "📅 Step 4: Enter Expiry (e.g., 12102026)",
                    "✅ Step 5: Click FINALIZE to save"),
                onDismiss = { showExamplePopup = false }
            )
        }
    }
}

@Composable
fun FrontPremiumCard(title: String, subTitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color(0xFFC2185B), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if(iconBg == Color(0xFFFFD700)) Color(0xFFC2185B) else Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text(subTitle, color = Color.White.copy(0.8f), fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f)); Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(0.5f))
        }
    }
}

// --- 4. REGISTRATION FORM ---
@Composable
fun RegistrationSilkForm(onSave: (InventoryMed) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var chem by remember { mutableStateOf("") }
    var use by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }

    val suggestions = MedicineProvider.fullList.filter { it.contains(name, ignoreCase = true) }

    Column(Modifier.fillMaxSize().padding(20.dp).shadow(10.dp, RoundedCornerShape(28.dp)).background(Color.White).padding(24.dp)) {
        Text("Registration", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2C3E50), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(20.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        if (name.isNotEmpty() && suggestions.isNotEmpty() && name != suggestions.firstOrNull()) {
            Surface(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp), color = Color(0xFFFBFBFE), border = BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(8.dp)) {
                LazyColumn { items(suggestions) { s -> Text(s, modifier = Modifier.fillMaxWidth().clickable { name = s }.padding(12.dp), fontSize = 14.sp) } }
            }
        }

        Spacer(Modifier.height(12.dp)); OutlinedTextField(value = chem, onValueChange = { chem = it }, label = { Text("Composition") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp)); OutlinedTextField(value = use, onValueChange = { use = it }, label = { Text("Indication") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(Modifier.height(12.dp)); OutlinedTextField(
        value = exp,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            if(filtered.length <= 8) exp = filtered
        },
        label = { Text("Expiry (DDMMYYYY)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = PharmaDateTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

        Spacer(Modifier.weight(1f))
        Surface(onClick = {
            if(name.isNotBlank() && exp.length == 8) onSave(InventoryMed(InventoryRepo.nextId, name, chem, use, exp))
            else Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1A1A1A)) {
            Box(contentAlignment = Alignment.Center) { Text("FINALIZE ENTRY", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
        }
    }
}

// --- 5. DIRECTORY LIST ---
@Composable
fun DirectoryListView(mode: String, onClick: (InventoryMed) -> Unit) {
    var query by remember { mutableStateOf("") }
    val isRecent = when(mode) {
        "read" -> InventoryRepo.isRecentSortRead.value
        "update" -> InventoryRepo.isRecentSortUpdate.value
        else -> InventoryRepo.isRecentSortDelete.value
    }

    val displayList = remember(query, isRecent, InventoryRepo.stock.size) {
        val repoItems = InventoryRepo.stock.toList()
        val databaseItems = MedicineProvider.fullList.map { medName ->
            repoItems.find { it.name.equals(medName, true) } ?: InventoryMed(0, medName, "System Record", "Standard Medical", "00000000", timestamp = 0L)
        }
        val combinedList = (repoItems + databaseItems).distinctBy { it.name }
        val filtered = combinedList.filter { it.name.contains(query, true) }

        if (isRecent) filtered.sortedByDescending { it.timestamp } else filtered.sortedBy { it.name }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Search stocks...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) }
        )

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(displayList) { med ->
                Surface(onClick = { onClick(med) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp) {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(if(med.id == 0) Color(0xFFBDC3C7) else Color(0xFFC2185B), CircleShape))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(med.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF2C3E50))
                            Text(if(med.id == 0) "System Database" else "Inventory Stock", fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

// --- 6. POPUPS & UNIQUE MODALS ---
@Composable
fun MissionCardSnapEffect(title: String, lines: List<String>, isFanRotation: Boolean, onDismiss: () -> Unit) {
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
        modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight().graphicsLayer {
            if (isFanRotation) rotationX = rotation.value else rotationZ = rotation.value
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            cameraDistance = 12f * density
        }.clickable(enabled = false) {},
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(15.dp)
    ) {
        Column(Modifier.padding(28.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFFC2185B), letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            lines.forEach { line ->
                Text(line, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E), modifier = Modifier.padding(vertical = 6.dp))
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (!isVanishing) {
                        isVanishing = true
                        scope.launch {
                            launch { alpha.animateTo(0f, animationSpec = tween(700, easing = FastOutSlowInEasing)) }
                            launch { scale.animateTo(1.5f, animationSpec = tween(700, easing = FastOutSlowInEasing)) }
                            delay(700)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B))
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EliteMasterModal(med: InventoryMed, mode: String, onDismiss: () -> Unit, onDelete: () -> Unit, onUpdate: (InventoryMed) -> Unit) {
    var isRotated by remember { mutableStateOf(false) }
    var showSurePrompt by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isVanishing by remember { mutableStateOf(false) }

    val flipRotation by animateFloatAsState(targetValue = if (isRotated) 180f else 0f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy), label = "MasterFlip")
    val vanishScale by animateFloatAsState(targetValue = if (isVanishing) 1.5f else 1f, animationSpec = tween(700), label = "VanishScale")
    val vanishAlpha by animateFloatAsState(targetValue = if (isVanishing) 0f else 1f, animationSpec = tween(700), label = "VanishAlpha")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {

            val cardShape = if(mode == "delete") CutCornerShape(topStart = 40.dp, bottomEnd = 40.dp) else RoundedCornerShape(40.dp)
            val cardColor = if(mode == "delete") Color(0xFFE3F2FD) else Color.White

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(if(mode == "read") 480.dp else if(mode == "delete") 480.dp else 550.dp)
                    .graphicsLayer {
                        rotationY = flipRotation
                        scaleX = vanishScale
                        scaleY = vanishScale
                        alpha = vanishAlpha
                        cameraDistance = 15f * density
                    }
                    .clickable(enabled = false) {},
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                if (flipRotation <= 90f) {
                    Column(Modifier.padding(32.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(mode.uppercase(), fontWeight = FontWeight.Black, color = if(mode == "delete") Color(0xFF1976D2) else Color(0xFFC2185B), letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        Text(med.name, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50), textAlign = TextAlign.Center)
                        HorizontalDivider(Modifier.padding(vertical = 20.dp), color = if(mode == "delete") Color(0xFF1976D2).copy(0.2f) else Color.LightGray.copy(0.5f))

                        DetailItem("COMPOSITION", med.chemical, true)
                        DetailItem("USAGE", med.usage, true)
                        DetailItem("EXPIRY", if(med.expiryDate == "00000000") "N/A" else formatPharmaDate(med.expiryDate), true)

                        Spacer(Modifier.weight(1f))

                        if (showSurePrompt && mode == "delete") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("SECURE REMOVAL?", color = Color(0xFF1976D2), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Spacer(Modifier.height(10.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Button(onClick = { onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("CONFIRM") }
                                    TextButton(onClick = {
                                        scope.launch {
                                            isVanishing = true
                                            delay(700)
                                            onDismiss()
                                        }
                                    }) { Text("CANCEL", color = Color.Gray) }
                                }
                            }
                        } else if (mode == "read") {
                            Box(Modifier.fillMaxWidth().padding(bottom = 8.dp), contentAlignment = Alignment.Center) {
                                Text("CLOSE", color = Color(0xFFC2185B), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onDismiss() }.padding(12.dp))
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if(mode=="update") "EDIT" else "SECURE REMOVE", color = if(mode == "delete") Color(0xFF1976D2) else Color(0xFF1976D2), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { if(mode=="update") isRotated = true else if(mode=="delete") showSurePrompt = true }.padding(12.dp))
                                Text("CLOSE", color = Color(0xFFC2185B), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onDismiss() }.padding(12.dp))
                            }
                        }
                    }
                } else {
                    Column(Modifier.padding(32.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                            Text("MODIFY DATA", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(20.dp))
                            var eName by remember { mutableStateOf(med.name) }
                            var eChem by remember { mutableStateOf(med.chemical) }
                            var eUse by remember { mutableStateOf(med.usage) }

                            OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = eChem, onValueChange = { eChem = it }, label = { Text("Composition") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = eUse, onValueChange = { eUse = it }, label = { Text("Indication") }, modifier = Modifier.fillMaxWidth())

                            Spacer(Modifier.weight(1f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("BACK", color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { isRotated = false }.padding(12.dp))
                                Text("SAVE", color = Color(0xFF43A047), fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                                    isRotated = false // Flip back to front
                                    scope.launch {
                                        delay(400)
                                        onUpdate(med.copy(name = eName, chemical = eChem, usage = eUse))
                                    }
                                }.padding(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, isCentered: Boolean = false) {
    Column(
        Modifier.padding(vertical = 8.dp),
        horizontalAlignment = if (isCentered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = if(isCentered) Modifier.fillMaxWidth() else Modifier, textAlign = if(isCentered) TextAlign.Center else TextAlign.Start)
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34495E), modifier = if(isCentered) Modifier.fillMaxWidth() else Modifier, textAlign = if(isCentered) TextAlign.Center else TextAlign.Start)
    }
}