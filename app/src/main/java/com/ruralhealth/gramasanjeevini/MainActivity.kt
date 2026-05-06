package com.ruralhealth.gramasanjeevini

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- ASSETS & CONSTANTS ---

val DescendingSortIcon: ImageVector
    get() = ImageVector.Builder(
        name = "DescendingSort",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
        moveTo(3f, 18f); horizontalLineToRelative(6f); verticalLineToRelative(-2f); lineTo(3f, 16f); verticalLineToRelative(2f); close()
        moveTo(3f, 6f); verticalLineToRelative(2f); horizontalLineToRelative(18f); lineTo(21f, 6f); lineTo(3f, 6f); close()
        moveTo(3f, 13f); horizontalLineToRelative(12f); verticalLineToRelative(-2f); lineTo(3f, 11f); verticalLineToRelative(2f); close()
    }.build()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainNavigation()
            }
        }
    }
}

// --- DATA MODELS ---

data class UserProfile(
    val name: String,
    val dob: String,
    val city: String,
    val phone: String,
    val isPharma: Boolean
)

data class ServiceItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val id: String
)

// --- SHARED PREFERENCES & UTILS ---

fun saveUser(context: Context, user: UserProfile) {
    val prefs = context.getSharedPreferences("GramaPrefs", Context.MODE_PRIVATE)
    val prefix = if (user.isPharma) "pharma_" else "citizen_"
    prefs.edit()
        .putString("${prefix}name", user.name)
        .putString("${prefix}dob", user.dob)
        .putString("${prefix}city", user.city)
        .putString("${prefix}phone", user.phone)
        .putBoolean("${prefix}isReg", true)
        .putBoolean("last_was_pharma", user.isPharma)
        .apply()
}

fun getUser(context: Context, isPharma: Boolean): UserProfile? {
    val prefs = context.getSharedPreferences("GramaPrefs", Context.MODE_PRIVATE)
    val prefix = if (isPharma) "pharma_" else "citizen_"
    if (!prefs.getBoolean("${prefix}isReg", false)) return null
    return try {
        UserProfile(
            prefs.getString("${prefix}name", "") ?: "",
            prefs.getString("${prefix}dob", "") ?: "",
            prefs.getString("${prefix}city", "") ?: "",
            prefs.getString("${prefix}phone", "") ?: "",
            isPharma
        )
    } catch (e: Exception) { null }
}

fun formatDisplayDate(rawDate: String): String {
    if (rawDate.length != 8) return rawDate
    return "${rawDate.substring(0, 2)}/${rawDate.substring(2, 4)}/${rawDate.substring(4, 8)}"
}

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text.filter { it.isDigit() }.take(8)
        var out = ""
        for (i in input.indices) {
            out += input[i]
            if (i == 1 || i == 3) out += "/"
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return out.length
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return input.length
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF0D47A1),
    isOutlined: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "btnScale")

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(20.dp),
        color = if (isOutlined) Color.Transparent else containerColor,
        border = if (isOutlined) BorderStroke(2.dp, Color.White) else null
    ) {
        Box(Modifier.padding(vertical = 14.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
            Text(text, color = if (isOutlined) Color.White else contentColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// --- NAVIGATION & SCREENS ---

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("GramaPrefs", Context.MODE_PRIVATE) }
    val lastRoleWasPharma = prefs.getBoolean("last_was_pharma", false)

    var currentScreen by remember { mutableStateOf("splash") }
    var userProfile by remember { mutableStateOf(getUser(context, lastRoleWasPharma)) }
    var tempPharmaRole by remember { mutableStateOf(false) }

    val isAnyUserRegistered = getUser(context, false) != null || getUser(context, true) != null

    AnimatedContent(targetState = currentScreen, label = "screen_transition") { target ->
        when (target) {
            "splash" -> SplashScreen {
                currentScreen = if (userProfile != null) "dashboard" else "role_choice"
            }
            "role_choice" -> RoleSelectionScreen(
                isExistingUser = isAnyUserRegistered,
                onBackToDashboard = { if (userProfile != null) currentScreen = "dashboard" },
                onRoleSelected = { isPharma ->
                    val existing = getUser(context, isPharma)
                    if (existing != null) {
                        userProfile = existing
                        prefs.edit().putBoolean("last_was_pharma", isPharma).apply()
                        currentScreen = "dashboard"
                    } else {
                        tempPharmaRole = isPharma
                        currentScreen = "onboarding"
                    }
                }
            )
            "onboarding" -> OnboardingFlow(
                isPharma = tempPharmaRole,
                onBack = { currentScreen = "role_choice" }
            ) { profile ->
                saveUser(context, profile)
                userProfile = profile
                currentScreen = "dashboard"
            }
            "dashboard" -> userProfile?.let {
                GramaSanjeeviniDashboard(
                    user = it,
                    onUpdate = { updated -> saveUser(context, updated); userProfile = updated },
                    onSwitch = { currentScreen = "role_choice" }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1.2f, tween(1000, easing = FastOutSlowInEasing))
        delay(800)
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF0D47A1)), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(120.dp).scale(scale.value))
            Text("GRAMA SANJEEVINI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
        }
    }
}

@Composable
fun RoleSelectionScreen(isExistingUser: Boolean, onBackToDashboard: () -> Unit, onRoleSelected: (Boolean) -> Unit) {
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (isExistingUser) onBackToDashboard()
        else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) (context as? ComponentActivity)?.finish()
            else {
                Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D47A1)).padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose Your Role", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(50.dp))
        AnimatedButton("Citizen Account", { onRoleSelected(false) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        AnimatedButton("Registered Pharmacist", { onRoleSelected(true) }, Modifier.fillMaxWidth(), isOutlined = true)
        if (isExistingUser) {
            TextButton(onClick = onBackToDashboard, modifier = Modifier.padding(top = 20.dp)) {
                Text("CANCEL", color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingFlow(isPharma: Boolean, onBack: () -> Unit, onComplete: (UserProfile) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val questions = if (isPharma) listOf("Pharmacist Name", "Drug License Number", "Enter Location", "Contact Number")
    else listOf("Full Name", "Date of Birth", "Enter Your Location", "Phone Number")

    val hints = if (isPharma) listOf("Ex: Arun Kumar", "KA-12-X-1234", "Ex: Bengaluru", "Ex: 9876543210")
    else listOf("Ex: Arun Kumar", "DD/MM/YYYY", "Ex: Bengaluru", "Ex: 9876543210")

    BackHandler { if (step > 0) { step--; showError = false } else onBack() }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))), Alignment.Center) {
        Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(0.2f))
            Spacer(modifier = Modifier.height(50.dp))
            AnimatedContent(targetState = step, label = "onboarding_step") { s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STEP ${s + 1} OF 4", color = Color.White.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(questions[s], color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))

                    val curVal = when (s) { 0 -> name 1 -> dob 2 -> city else -> phone }

                    OutlinedTextField(
                        value = curVal,
                        onValueChange = {
                            showError = false
                            val filtered = if (s == 1 && !isPharma) it.filter { c -> c.isDigit() }.take(8)
                            else if (s == 3) it.filter { c -> c.isDigit() }.take(10)
                            else it
                            when (s) { 0 -> name = filtered; 1 -> dob = filtered; 2 -> city = filtered; 3 -> phone = filtered }
                        },
                        singleLine = true,
                        placeholder = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text(hints[s], textAlign = TextAlign.Center, color = Color.White.copy(0.4f)) } },
                        visualTransformation = if (s == 1 && !isPharma) DateTransformation() else VisualTransformation.None,
                        keyboardOptions = if ((s == 1 && !isPharma) || s == 3) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                        textStyle = TextStyle(color = Color.White, fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(0.4f), cursorColor = Color.White)
                    )

                    if (showError) {
                        val errMsg = when {
                            s == 3 && curVal.length < 10 -> "⚠️ Please enter 10 digits"
                            s == 1 && isPharma && curVal.isBlank() -> "⚠️ Drug License Required"
                            else -> "⚠️ Please fill the field"
                        }
                        Text(errMsg, color = Color.Yellow, modifier = Modifier.padding(top = 10.dp), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(35.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AnimatedButton("BACK", { if (step > 0) { step--; showError = false } else onBack() }, Modifier.weight(1f), isOutlined = true)
                        Spacer(Modifier.width(16.dp))
                        AnimatedButton(if (step == 3) "FINISH" else "NEXT", {
                            val valid = if (s == 3) curVal.length == 10 else curVal.isNotBlank()
                            if (valid) { if (step < 3) step++ else onComplete(UserProfile(name, dob, city, phone, isPharma)) } else showError = true
                        }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// --- DASHBOARD ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GramaSanjeeviniDashboard(user: UserProfile, onUpdate: (UserProfile) -> Unit, onSwitch: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isDirectoryOpen by remember { mutableStateOf(false) }
    var isFindMedsOpen by remember { mutableStateOf(false) }
    var isMapDiscoveryOpen by remember { mutableStateOf(false) }
    var isSymptomGuideOpen by remember { mutableStateOf(false) }
    var isAlertsOpen by remember { mutableStateOf(false) }
    var isWellnessTipsOpen by remember { mutableStateOf(false) }
    var isInventoryOpen by remember { mutableStateOf(false) }
    var isExpiryOpen by remember { mutableStateOf(false) }

    var symptomSearchQuery by remember { mutableStateOf("") }
    var showCareInstructions by remember { mutableStateOf(false) }
    var showAddSymptomDialog by remember { mutableStateOf(false) }
    var newSymptomName by remember { mutableStateOf("") }
    var newSymptomMeds by remember { mutableStateOf("") }

    var isSearchingMode by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var showSwitchConfirm by remember { mutableStateOf(false) }
    var showCriticalFirst by remember { mutableStateOf(false) }
    var showSafetyWarning by remember { mutableStateOf(false) }
    var selectedMedForWarning by remember { mutableStateOf("") }
    var isConnectOpen by remember { mutableStateOf(false) }
    var isSupportOpen by remember { mutableStateOf(false) }

    val medicineList = remember { MedicineProvider.fullList }

    var backPressedTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (isSupportOpen) isSupportOpen = false
        else if (showCareInstructions) showCareInstructions = false
        else if (showAddSymptomDialog) showAddSymptomDialog = false
        else if (isAlertsOpen) isAlertsOpen = false
        else if (isDirectoryOpen) {
            isDirectoryOpen = false; searchQuery = ""; isSearchingMode = false; showCriticalFirst = false
        }
        else if (isFindMedsOpen) isFindMedsOpen = false
        else if (isMapDiscoveryOpen) isMapDiscoveryOpen = false
        else if (isSymptomGuideOpen) { isSymptomGuideOpen = false; symptomSearchQuery = "" }
        else if (isWellnessTipsOpen) isWellnessTipsOpen = false
        else if (isInventoryOpen) isInventoryOpen = false
        else if (isConnectOpen) isConnectOpen = false
        else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) (context as ComponentActivity).finish()
            else { Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show(); backPressedTime = System.currentTimeMillis() }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GRAMA SANJEEVINI", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { showProfileModal = true }) {
                        Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(45.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { showSwitchConfirm = true }) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color.White, modifier = Modifier.size(35.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        }
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p).background(Color(0xFFF8FAFF))) {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2))), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(bottom = 60.dp, top = 10.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(if (user.isPharma) "PHARMACY ACCOUNT" else "Verified Citizen", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("Welcome,", color = Color.White.copy(0.9f), fontSize = 16.sp)
                            Text(user.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center, lineHeight = 32.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-20).dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; if(it.isNotEmpty()){ isSearchingMode=true; isDirectoryOpen=true } },
                            placeholder = { Text("Search medicines or syrups...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF0D47A1)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                        )
                    }

                    Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(80.dp).clickable { isSearchingMode = false; isDirectoryOpen = true }, shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))), Alignment.CenterStart) {
                            Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color(0xFF0D47A1), shape = CircleShape, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.List, null, tint = Color.White, modifier = Modifier.padding(12.dp)) }
                                Spacer(Modifier.width(15.dp)); Text("Medicine List (A-Z)", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D47A1), fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(80.dp).clickable { isSymptomGuideOpen = true }, shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))), Alignment.CenterStart) {
                            Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color(0xFF00897B), shape = CircleShape, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Info, null, tint = Color(0xFFE8F5E9), modifier = Modifier.padding(12.dp)) }
                                Spacer(Modifier.width(15.dp))
                                Column {
                                    Text("Symptom Guide", fontWeight = FontWeight.ExtraBold, color = Color(0xFF00897B), fontSize = 16.sp)
                                    Text("Find meds by problem", fontSize = 11.sp, color = Color(0xFF00897B).copy(0.7f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(35.dp)) }

                val srv = if (user.isPharma) listOf(
                    ServiceItem("INVENTORY", Icons.Default.Add, Color(0xFFFFC1CC), "P1"),
                    ServiceItem("EXPIRY", Icons.Default.Refresh, Color(0xFFC1FFC1), "P2"),
                    ServiceItem("CONNECT", Icons.Default.Share, Color(0xFFC1E1FF), "P3"),
                    ServiceItem("SUPPORT", Icons.Default.Face, Color(0xFFE1C1FF), "P4")
                )
                else listOf(
                    ServiceItem("FIND MEDS", Icons.Default.Search, Color(0xFFFFC1CC), "C1"),
                    ServiceItem("MAP", Icons.Default.LocationOn, Color(0xFFC1FFC1), "C2"),
                    ServiceItem("ALERTS", Icons.Default.Warning, Color(0xFFC1E1FF), "C3"),
                    ServiceItem("TIPS", Icons.Default.Info, Color(0xFFE1C1FF), "C4")
                )

                items(srv.chunked(2)) { pair ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        pair.forEach { itm ->
                            Column(modifier = Modifier.weight(1f).clickable {
                                when(itm.id) {
                                    "P1" -> isInventoryOpen = true
                                    "P2" -> isExpiryOpen = true
                                    "P3" -> isConnectOpen = true
                                    "P4" -> isSupportOpen = true
                                    "C1" -> isFindMedsOpen = true
                                    "C2" -> isMapDiscoveryOpen = true
                                    "C3" -> isAlertsOpen = true
                                    "C4" -> isWellnessTipsOpen = true
                                }
                            }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(modifier = Modifier.fillMaxWidth().aspectRatio(1.3f), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = itm.color)) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(itm.icon, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(34.dp)) }
                                }
                                Text(itm.title, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D47A1), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isDirectoryOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                Column(Modifier.fillMaxSize().background(Color(0xFFF3F7FA))) {
                    Box(Modifier.fillMaxWidth().background(Color(0xFF0D47A1).copy(0.9f)).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isDirectoryOpen = false; searchQuery = ""; isSearchingMode = false; showCriticalFirst = false }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                            Text(if (isSearchingMode) "Search Results" else "A-Z DIRECTORY", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showCriticalFirst = !showCriticalFirst }) {
                                Icon(imageVector = if (showCriticalFirst) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Show Critical", tint = if (showCriticalFirst) Color.Red else Color.White)
                            }
                        }
                    }
                    if (isSearchingMode) {
                        OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Search medicines...") }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Search, null) }, colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                    }
                    val filteredMeds = remember(searchQuery, showCriticalFirst) {
                        val base = medicineList.filter { it.contains(searchQuery, ignoreCase = true) }
                        if (showCriticalFirst) base.sortedByDescending { EmergencyProvider.isCritical(it) } else base
                    }
                    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(filteredMeds) { med ->
                            val isCritical = EmergencyProvider.isCritical(med)
                            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { selectedMedForWarning = med; showSafetyWarning = true }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(if(isCritical) Color.Red else Color(0xFF1976D2), CircleShape))
                                    Spacer(Modifier.width(15.dp)); Text(med, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                                    if (isCritical) {
                                        Surface(color = Color(0xFFB71C1C), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(start = 8.dp)) {
                                            Text(text = "CRITICAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isSymptomGuideOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                Column(Modifier.fillMaxSize().background(Color(0xFFF9FBF9))) {
                    Box(Modifier.fillMaxWidth().background(Color(0xFF00897B)).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSymptomGuideOpen = false; symptomSearchQuery = "" }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                            Text("SYMPTOM ASSISTANT", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAddSymptomDialog = true }) { Icon(Icons.Default.Add, "Request Symptom", tint = Color.White) }
                            Surface(color = Color.White.copy(0.2f), shape = CircleShape, modifier = Modifier.size(40.dp).clickable { showCareInstructions = true }) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Care Guide", tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = symptomSearchQuery,
                        onValueChange = { symptomSearchQuery = it },
                        placeholder = { Text("What is the health problem?") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF00897B)) },
                        trailingIcon = { if (symptomSearchQuery.isNotEmpty()) { IconButton(onClick = { symptomSearchQuery = "" }) { Icon(Icons.Default.Close, null) } } },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00897B), unfocusedBorderColor = Color(0xFF00897B).copy(0.3f), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )

                    val filteredSymptoms = remember(symptomSearchQuery) {
                        SymptomProvider.commonSymptoms.filter {
                            it.problem.contains(symptomSearchQuery, ignoreCase = true) || it.description.contains(symptomSearchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        if (filteredSymptoms.isEmpty()) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Search, null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                                    Text("No matching symptoms found", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                                }
                            }
                        } else {
                            items(filteredSymptoms) { symptom ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFF00897B).copy(0.1f)), elevation = CardDefaults.cardElevation(2.dp)) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text(symptom.problem, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF00897B))
                                        Text(symptom.description, fontSize = 14.sp, color = Color.Gray)
                                        Spacer(Modifier.height(12.dp))
                                        Text("Recommended Meds:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                        Row(Modifier.padding(top = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            symptom.relatedMeds.forEach { med ->
                                                AssistChip(onClick = { selectedMedForWarning = med; showSafetyWarning = true }, label = { Text(med, fontWeight = FontWeight.Bold) }, colors = AssistChipDefaults.assistChipColors(labelColor = Color(0xFF0D47A1), containerColor = Color(0xFFE8F5E9).copy(0.5f)), border = BorderStroke(1.dp, Color(0xFF00897B).copy(0.2f)))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isAlertsOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                AlertsScreen(onBack = { isAlertsOpen = false })
            }

            AnimatedVisibility(visible = showCareInstructions, enter = slideInHorizontally { it } + fadeIn(), exit = slideOutHorizontally { it } + fadeOut()) {
                CareGuideScreen(onClose = { showCareInstructions = false })
            }

            AnimatedVisibility(visible = isFindMedsOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                FindMedsScreen(onBack = { isFindMedsOpen = false })
            }

            AnimatedVisibility(visible = isMapDiscoveryOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                MapDiscoveryScreen(onBack = { isMapDiscoveryOpen = false })
            }
            AnimatedVisibility(
                visible = isInventoryOpen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                InventoryManagementScreen(onBack = { isInventoryOpen = false })
            }

// THIS IS NOW OUTSIDE AND INDEPENDENT
            AnimatedVisibility(
                visible = isExpiryOpen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (isExpiryOpen) ExpiryManagementScreen(onBack = { isExpiryOpen = false })
            }
            AnimatedVisibility(visible = isWellnessTipsOpen, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                GramSanjeeviniWellnessDialog(onDismiss = { isWellnessTipsOpen = false })
            }
        }
        AnimatedVisibility(
            visible = isConnectOpen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            ConnectModuleScreen(onBack = { isConnectOpen = false })
        }
        if (isSupportOpen) {
            SupportModuleScreen(onBack = { isSupportOpen = false })
        }

        if (showAddSymptomDialog) {
            AlertDialog(
                onDismissRequest = { showAddSymptomDialog = false; newSymptomName = ""; newSymptomMeds = "" },
                title = { Text("Request New Symptom", fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        Text("Help us improve. If you couldn't find a problem, tell us here.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value = newSymptomName, onValueChange = { newSymptomName = it }, label = { Text("Health Problem") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = newSymptomMeds, onValueChange = { newSymptomMeds = it }, label = { Text("Medicine required") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ex: Paracetamol") })
                    }
                },
                confirmButton = { Button(onClick = { Toast.makeText(context, "Thank you! Will be Verified soon.", Toast.LENGTH_LONG).show(); showAddSymptomDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))) { Text("SUBMIT") } },
                dismissButton = { TextButton(onClick = { showAddSymptomDialog = false }) { Text("CANCEL") } },
                containerColor = Color.White, shape = RoundedCornerShape(28.dp)
            )
        }

        if (showSafetyWarning) {
            AlertDialog(
                onDismissRequest = { showSafetyWarning = false },
                title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = Color(0xFF0277BD)); Spacer(Modifier.width(8.dp)); Text("Medical Guidance", fontWeight = FontWeight.Bold) } },
                text = { Text("You selected $selectedMedForWarning. \n\nBefore taking this medicine, consult a professional for dosage.") },
                confirmButton = { TextButton(onClick = { showSafetyWarning = false }) { Text("OK") } },
                containerColor = Color.White, shape = RoundedCornerShape(24.dp)
            )
        }

        if (showProfileModal) {
            var isEd by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(targetValue = if (isEd) 180f else 0f, animationSpec = tween(800, easing = FastOutSlowInEasing), label = "flip")
            var eNm by remember { mutableStateOf(user.name) }; var eCt by remember { mutableStateOf(user.city) }; var ePh by remember { mutableStateOf(user.phone) }
            AlertDialog(onDismissRequest = { if (!isEd) showProfileModal = false }, containerColor = Color.Transparent, properties = DialogProperties(usePlatformDefaultWidth = false), confirmButton = {}, text = {
                Card(modifier = Modifier.fillMaxWidth(0.9f).height(480.dp).graphicsLayer { rotationY = rotation; cameraDistance = 30f * density }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Box(Modifier.fillMaxSize()) {
                        if (rotation <= 90f) {
                            Column(Modifier.padding(24.dp).fillMaxSize()) {
                                Text("MY PROFILE", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF0D47A1))
                                ProfileRow(Icons.Default.Person, "Name", user.name)
                                ProfileRow(Icons.Default.DateRange, if (user.isPharma) "License" else "DOB", if (user.isPharma) user.dob else formatDisplayDate(user.dob))
                                ProfileRow(Icons.Default.LocationOn, "City", user.city)
                                ProfileRow(Icons.Default.Phone, "Phone", user.phone)
                                Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { isEd = true }) { Text("EDIT", color = Color(0xFF1976D2), fontWeight = FontWeight.Black) }
                                TextButton(onClick = { showProfileModal = false }) { Text("CLOSE", color = Color(0xFF616161), fontWeight = FontWeight.Black) }
                            }
                            }
                        } else {
                            Column(Modifier.padding(24.dp).fillMaxSize().graphicsLayer { rotationY = 180f }) {
                                Text("UPDATE INFO", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF2E7D32))
                                OutlinedTextField(value = eNm, onValueChange = { eNm = it }, label = { Text("Name") }, singleLine = true)
                                OutlinedTextField(value = eCt, onValueChange = { eCt = it }, label = { Text("City") }, singleLine = true)
                                OutlinedTextField(value = ePh, onValueChange = { ePh = it.filter { it.isDigit() }.take(10) }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                                Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { isEd = false }) { Text("CANCEL", color = Color(0xFFB71C1C), fontWeight = FontWeight.Black) }
                                TextButton(onClick = { if (ePh.length == 10 && eNm.isNotBlank()) { onUpdate(user.copy(name = eNm, city = eCt, phone = ePh)); isEd = false } }) { Text("SAVE", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black) }
                            }
                            }
                        }
                    }
                }
            })
        }
        if (showSwitchConfirm) { AlertDialog(onDismissRequest = { showSwitchConfirm = false }, containerColor = Color.White, title = { Text("Switch Account") }, text = { Text("Would you like to log out?") }, confirmButton = { Button(onClick = { onSwitch(); showSwitchConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("LOGOUT", color = Color.White) } }, dismissButton = { TextButton(onClick = { showSwitchConfirm = false }) { Text("CANCEL") } }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDiscoveryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var mapQuery by remember { mutableStateOf("") }
    var onlyFavorites by remember { mutableStateOf(false) }
    val chainList = remember { MedicalNetworkProvider.fullChainList }
    val starredShops = remember { mutableStateListOf<String>() }
    var selectedShopName by remember { mutableStateOf("") }
    var showCallDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF3F7FA))) {
        Box(Modifier.fillMaxWidth().background(Color(0xFF0D47A1)).padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("PHARMACY MAPS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { onlyFavorites = !onlyFavorites }) {
                    Icon(imageVector = if (onlyFavorites) Icons.Default.Star else Icons.Outlined.Star, contentDescription = "Favs", tint = if (onlyFavorites) Color(0xFFFFD54F) else Color.White)
                }
                IconButton(onClick = { showResetConfirm = true }) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
            }
        }

        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                    TextField(value = mapQuery, onValueChange = { mapQuery = it }, placeholder = { Text("Search shop by name...") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF0D47A1)) }, trailingIcon = { if (mapQuery.isNotEmpty()) IconButton(onClick = { mapQuery = "" }) { Icon(Icons.Default.Close, null) } }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent))
                }
            }
            item {
                LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chips = listOf("24/7", "Ambulance", "Blood Bank", "Lab Tests")
                    items(chips) { chip ->
                        FilterChip(selected = false, onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$chip+near+me"))) }, label = { Text(chip, fontWeight = FontWeight.Bold) }, colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, labelColor = Color(0xFF0D47A1)), shape = CircleShape)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=pharmacy+near+me"))) }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))) {
                    Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFFFD54F), shape = CircleShape, modifier = Modifier.size(60.dp).shadow(6.dp,CircleShape)) { Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE65100), modifier = Modifier.padding(14.dp)) }
                        Spacer(Modifier.width(18.dp))
                        Column { Text("Locality Finder", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black); Text("Find pharmacies near you", color = Color.White.copy(0.8f)) }
                    }
                }
                Spacer(Modifier.height(16.dp)); Text("  DIRECTORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
            }
            val filtered = chainList.filter { it.name.contains(mapQuery, ignoreCase = true) && (!onlyFavorites || starredShops.contains(it.name)) }
            items(filtered) { chain ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if(starredShops.contains(chain.name)) Icons.Default.Star else Icons.Outlined.Star, null, tint = if(starredShops.contains(chain.name)) Color(0xFFFFD54F) else Color.Gray, modifier = Modifier.size(28.dp).clickable { if(starredShops.contains(chain.name)) starredShops.remove(chain.name) else starredShops.add(chain.name) })
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(chain.name, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF0D47A1))
                            Text(chain.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { selectedShopName = chain.name; showCallDialog = true }) { Icon(Icons.Default.Call, null, tint = Color(0xFF43A047), modifier = Modifier.size(26.dp)) }
                        IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${chain.name}+near+me"))) }) { Icon(Icons.Default.KeyboardArrowRight, null, tint = Color(0xFFBBDEFB), modifier = Modifier.size(30.dp)) }
                    }
                }
            }
        }
    }
    if (showResetConfirm) { AlertDialog(onDismissRequest = { showResetConfirm = false }, containerColor = Color.White, shape = RoundedCornerShape(24.dp), title = { Text("Reset Your Favourites? ", fontWeight = FontWeight.Black) }, confirmButton = { Button(onClick = { starredShops.clear(); showResetConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))) { Text("RESET") } }, dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("CANCEL") } }) }
    if (showCallDialog) { AlertDialog(onDismissRequest = { showCallDialog = false }, containerColor = Color.White, shape = RoundedCornerShape(24.dp), title = { Text("Contact Info", fontWeight = FontWeight.Black) }, text = { Text("Open Maps for details for $selectedShopName?") }, confirmButton = { Button(onClick = { showCallDialog = false; context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$selectedShopName+near+me"))) }) { Text("YES") } }) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindMedsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var sortOpt by remember { mutableStateOf("Popularity") }
    var expanded by remember { mutableStateOf(false) }
    var showAddShop by remember { mutableStateOf(false) }
    var addStep by remember { mutableIntStateOf(0) }
    var newShopName by remember { mutableStateOf("") }
    var newShopAddress by remember { mutableStateOf("") }

    val baseList = MedicalNetworkProvider.fullChainList
    val sortedList = when (sortOpt) {
        "A to Z" -> baseList.sortedBy{ it.name }
        "Z to A" -> baseList.sortedByDescending{ it.name }
        "24/7 Service" -> baseList.sortedBy { !it.description.contains("24/7") }
        "Generic Savings" -> baseList.sortedBy { !it.description.contains("Generic") }
        "Hospital Linked" -> baseList.sortedBy { !it.description.contains("Hospital") }
        else -> baseList
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F4F8))) {
        Box(Modifier.fillMaxWidth().background(Color(0xFF0D47A1)).padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("NEAREST MEDICALS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { addStep = 0; newShopName = ""; newShopAddress = ""; showAddShop = true }) { Icon(Icons.Default.Add, "Add", tint = Color.White, modifier = Modifier.size(28.dp)) }
                Box {
                    IconButton(onClick = { expanded = true }) { Icon(imageVector = DescendingSortIcon, contentDescription = "Sort", tint = Color.White) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White).width(210.dp)) {
                        listOf("Popularity", "A to Z", "Z to A", "24/7 Service", "Generic Savings", "Hospital Linked").forEach { option ->
                            DropdownMenuItem(text = { Text(option, fontWeight = FontWeight.Bold, color = if(sortOpt == option) Color(0xFF0D47A1) else Color.Black) }, onClick = { sortOpt = option; expanded = false }, leadingIcon = { Icon(if(sortOpt == option) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight, null, tint = if(sortOpt == option) Color(0xFF0D47A1) else Color.Gray) })
                        }
                    }
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFFFD54F), shape = CircleShape, modifier = Modifier.size(54.dp).shadow(6.dp,CircleShape)) { Icon(Icons.Default.Star, null, tint = Color(0xFFE65100), modifier = Modifier.padding(12.dp)) }
                        Spacer(Modifier.width(16.dp))
                        Column { Text("Health First", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); Text("Verified medicine networks near you.", color = Color.White.copy(0.8f)) }
                    }
                }
            }
            items(sortedList.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { chain ->
                        Card(modifier = Modifier.weight(1f).aspectRatio(1.1f).clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${chain.name}+near+me"))) }, shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Surface(color = Color(0xFFE3F2FD), shape = CircleShape, modifier = Modifier.size(46.dp)) { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF0D47A1), modifier = Modifier.padding(11.dp)) }
                                Spacer(Modifier.height(8.dp)); Text(chain.name, fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    if (showAddShop) {
        AlertDialog(onDismissRequest = { showAddShop = false }, title = { Text(if (addStep == 0) "Add Missing Shop" else "Shop Location", fontWeight = FontWeight.Black) }, text = { Column { AnimatedContent(targetState = addStep, label = "add_shop") { s -> if (s == 0) { Column { Text("Help us grow.", fontSize = 14.sp); Spacer(Modifier.height(10.dp)); OutlinedTextField(value = newShopName, onValueChange = { newShopName = it }, label = { Text("Shop Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) } } else { Column { Text("Provide Maps Link.", fontSize = 14.sp); Spacer(Modifier.height(10.dp)); OutlinedTextField(value = newShopAddress, onValueChange = { newShopAddress = it }, label = { Text("Link") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp)) } } } } }, confirmButton = { Button(onClick = { if (addStep == 0) { if (newShopName.isNotBlank()) addStep = 1 } else { if (newShopAddress.isNotBlank()) { Toast.makeText(context, "Request Sent!", Toast.LENGTH_LONG).show(); showAddShop = false } } }) { Text(if (addStep == 0) "NEXT" else "SUBMIT") } }, dismissButton = { TextButton(onClick = { if (addStep == 1) addStep = 0 else showAddShop = false }) { Text(if (addStep == 1) "BACK" else "CANCEL") } }, containerColor = Color.White, shape = RoundedCornerShape(28.dp) )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- REFRESH / SYNC STATE ---
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val alerts = remember(refreshTrigger) { AlertProvider.simulatedAlerts.shuffled() }
    val timePool = listOf("Just Now", "2 mins ago", "12 mins ago", "1 hour ago", "Today", "45 mins ago")

    // --- REFRESH ANIMATION LOGIC ---
    var rotationAngle by remember { mutableStateOf(0f) }
    val rotationAnim by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "syncRotation"
    )

    // --- FILTER STATE (UI Matched to Sort UI) ---
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All Alerts") }

    val filteredAlerts = remember(selectedFilter, alerts) {
        when (selectedFilter) {
            "🔴 Emergencies" -> alerts.filter { it.category.contains("EMERGENCY") }
            "🟡 Medicine Stock" -> alerts.filter { it.category.contains("STOCK") }
            "🔵 Govt Schemes" -> alerts.filter { it.category.contains("GOVT") }
            "🟢 Health Wellness" -> alerts.filter { it.category.contains("WELLNESS") }
            "🟣 Livestock Care" -> alerts.filter { it.category.contains("LIVESTOCK") }
            else -> alerts
        }
    }

    var selectedAlert by remember { mutableStateOf<HealthAlert?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7F9))) {
        Box(Modifier.fillMaxWidth().background(Color(0xFF0D47A1)).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("LIVE HEALTH ALERTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f))

                // REFRESH AI BUTTON WITH SYNC ANIMATION & MESSAGE
                IconButton(onClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, "Synchronizing with Grama AI... You will receive the latest info.", Toast.LENGTH_SHORT).show()
                        rotationAngle += 360f // Trigger spin animation
                        delay(200)
                        refreshTrigger++
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh AI",
                        tint = Color.White,
                        modifier = Modifier.rotate(rotationAnim)
                    )
                }

                // FILTER DROPDOWN BUTTON (MATCHES SORT UI STYLE)
                Box {
                    IconButton(onClick = { filterMenuExpanded = true }) {
                        Icon(Icons.Default.List, "Filter", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        modifier = Modifier.background(Color.White).width(230.dp)
                    ) {
                        val filterOptions = listOf(
                            "All Alerts",
                            "🔴 Emergencies",
                            "🟡 Medicine Stock",
                            "🔵 Govt Schemes",
                            "🟢 Health Wellness",
                            "🟣 Livestock Care"
                        )
                        filterOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontWeight = FontWeight.Bold, color = if(selectedFilter == option) Color(0xFF0D47A1) else Color.Black) },
                                onClick = { selectedFilter = option; filterMenuExpanded = false },
                                leadingIcon = {
                                    Icon(
                                        if(selectedFilter == option) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                                        null,
                                        tint = if(selectedFilter == option) Color(0xFF0D47A1) else Color.Gray
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }


        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredAlerts) { alert ->
                val simulatedTime = remember(refreshTrigger, alert.id) { timePool.random() }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = alert.color.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(alert.category, color = alert.color, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(Modifier.weight(1f))
                            Text(simulatedTime, fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text(alert.description, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                        Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { selectedAlert = alert; showSheet = true }) {
                                Text(alert.actionText, fontWeight = FontWeight.Black, color = Color(0xFF1976D2))
                            }
                        }
                    }
                }
            }
            if (filteredAlerts.isEmpty()) {
                item {
                    Text("No results for $selectedFilter", modifier = Modifier.fillMaxWidth().padding(40.dp), textAlign = TextAlign.Center, color = Color.Gray)
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showSheet && selectedAlert != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth().padding(bottom = 40.dp)) {
                Surface(color = selectedAlert!!.color.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
                    Text(selectedAlert!!.category, color = selectedAlert!!.color, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(selectedAlert!!.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFF0D47A1))
                        Spacer(Modifier.width(12.dp))
                        Text(selectedAlert!!.aiSummary, fontSize = 14.sp, color = Color(0xFF0D47A1), fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("STEP-BY-STEP PRECAUTIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                selectedAlert!!.precautions.forEach { step ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF43A047), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(step, fontSize = 15.sp, color = Color.Black)
                    }
                }
                Spacer(Modifier.height(30.dp))
                val isCallAction = selectedAlert!!.contactType in listOf("Hospital", "Ambulance", "Panchayat", "Vet Support", "Anganwadi", "Clinic")

                val actionBtnLabel = when(selectedAlert!!.contactType) {
                    "Hospital" -> "CALL EMERGENCY (102)"
                    "Pharmacy" -> "NAVIGATE TO PHARMACY"
                    "Ambulance" -> "CALL AMBULANCE (108)"
                    "Panchayat" -> "CONTACT PANCHAYAT (112)"
                    "Vet Support" -> "CALL VET CLINIC (112)"
                    "Anganwadi" -> "CALL WORKER (102)"
                    "Clinic" -> "CALL CLINIC (112)"
                    else -> "GET SUPPORT"
                }

                Button(
                    onClick = {
                        showSheet = false
                        if (isCallAction) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedAlert!!.phoneNumber}"))
                            context.startActivity(intent)
                        } else {
                            val query = if(selectedAlert!!.contactType == "Pharmacy") "pharmacy near me" else "${selectedAlert!!.contactType} near me"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedAlert!!.color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(actionBtnLabel, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Color(0xFFE3F2FD), shape = CircleShape, modifier = Modifier.size(40.dp)) { Icon(icon, null, tint = Color(0xFF0D47A1), modifier = Modifier.padding(10.dp)) }
        Spacer(modifier = Modifier.width(16.dp)); Column { Text(label, fontSize = 11.sp, color = Color.Gray); Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

