@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package com.ruralhealth.gramasanjeevini

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- INDUSTRIAL DATA MODEL ---
data class HubProcurementItem(
    val id: Long,
    val medName: String,
    var qty: String,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

@Composable
fun ConnectModuleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var medName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }
    var showGuidePopup by remember { mutableStateOf(false) }
    var showWelcomePopup by remember { mutableStateOf(false) }
    var showLimitError by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf<HubProcurementItem?>(null) }
    var itemToDelete by remember { mutableStateOf<HubProcurementItem?>(null) }
    var duplicateFound by remember { mutableStateOf<HubProcurementItem?>(null) }

    val draftOrder = remember { mutableStateListOf<HubProcurementItem>() }

    val allMedicines = remember {
        try { MedicineProvider.fullList } catch (_: Exception) { listOf("Paracetamol", "Amoxicillin") }
    }

    val filteredSuggestions = remember(medName) {
        if (medName.isEmpty()) emptyList()
        else allMedicines.filter { it.contains(medName, ignoreCase = true) }
    }

    val premiumBlue = Color(0xFF0D47A1)
    val lightPremiumBlue = Color(0xFF1976D2)
    val p3Gradient = Brush.verticalGradient(listOf(premiumBlue, lightPremiumBlue))

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.fillMaxWidth().background(p3Gradient).statusBarsPadding().padding(top = 15.dp, bottom = 20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(onClick = { focusManager.clearFocus(); onBack() }, color = Color.White.copy(0.2f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                        Text("CONNECT HUB", modifier = Modifier.weight(1f), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)

                        IconButton(onClick = { showWelcomePopup = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                        }
                    }
                }
            },
            bottomBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 15.dp) {
                    Box(Modifier.padding(20.dp).navigationBarsPadding()) {
                        Button(
                            onClick = { focusManager.clearFocus(); if (draftOrder.isNotEmpty()) isSyncing = true },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isSyncing && draftOrder.isNotEmpty()
                        ) {
                            if (isSyncing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else {
                                Icon(Icons.AutoMirrored.Filled.Send, null)
                                Spacer(Modifier.width(12.dp))
                                Text("SEND OFFICIAL REQUEST", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFFF4F7FA)
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)
                        .clickable(remember { MutableInteractionSource() }, null) { focusManager.clearFocus() },
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        Surface(
                            onClick = { showGuidePopup = true },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(modifier = Modifier.background(p3Gradient).padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("Requisition Guide", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        Text("Review ordering procedures", fontSize = 12.sp, color = Color.White.copy(0.8f))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("REQUISITION ENTRY", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)

                                Column {
                                    OutlinedTextField(
                                        value = medName,
                                        onValueChange = { medName = it },
                                        label = { Text("Search Medicine Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        trailingIcon = { if (medName.isNotEmpty()) IconButton(onClick = { medName = "" }) { Icon(Icons.Default.Close, null) } }
                                    )

                                    if (filteredSuggestions.isNotEmpty() && medName != filteredSuggestions.firstOrNull()) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color.LightGray.copy(0.3f)),
                                            color = Color.White,
                                            shadowElevation = 8.dp
                                        ) {
                                            LazyColumn {
                                                items(filteredSuggestions) { suggestion ->
                                                    Text(
                                                        text = suggestion,
                                                        modifier = Modifier.fillMaxWidth().clickable { medName = suggestion; focusManager.clearFocus() }.padding(16.dp),
                                                        fontSize = 14.sp, color = Color.Black
                                                    )
                                                    Divider(color = Color(0xFFEEEEEE))
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = quantity,
                                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                                        label = { Text("Quantity") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Button(
                                        onClick = {
                                            val qtyInt = quantity.toIntOrNull() ?: 0
                                            if (qtyInt > 500) { showLimitError = true }
                                            else if (medName.isNotBlank() && quantity.isNotBlank()) {
                                                val existing = draftOrder.find { it.medName.equals(medName, true) }
                                                if (existing != null) { duplicateFound = existing }
                                                else {
                                                    draftOrder.add(HubProcurementItem(System.currentTimeMillis(), medName, quantity))
                                                    medName = ""; quantity = ""; focusManager.clearFocus()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp).weight(1.2f),
                                        colors = ButtonDefaults.buttonColors(containerColor = premiumBlue)
                                    ) { Icon(Icons.Default.Add, null); Text(" ADD", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }

                    item { Text("REQUISITION SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) }

                    items(draftOrder) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { selectedItem = item },
                            colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.List, null, tint = premiumBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.medName.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF263238))
                                    Text("Payload: ${item.qty} Standard Units", fontSize = 12.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { itemToDelete = item }) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFD32F2F))
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }

                // Popup logic
                if (showWelcomePopup) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable { showWelcomePopup = false }, contentAlignment = Alignment.Center) {
                        MissionCardSnapEffect(
                            title = "SYSTEM INITIALIZED",
                            lines = listOf("⦿ Accessing Connect Interface", "⦿ Authorization tokens active", "⦿ Database sync complete"),
                            buttonText = "PROCEED TO CONSOLE",
                            onDismiss = { showWelcomePopup = false }
                        )
                    }
                }

                if (showGuidePopup) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable { showGuidePopup = false }, contentAlignment = Alignment.Center) {
                        MissionCardSnapEffect(
                            title = "REQUISITION PROTOCOL",
                            lines = listOf("📦 Quantities : Enter count as strips/sheets.",
                                "📋 Accuracy : Verify the Medicine.",
                                "✏️ Updates : Duplicate entries gets merged.",
                                "✅ Finalize : Transmit Via Official Request."
                                ),
                            buttonText = "CLOSE & CONTINUE",
                            onDismiss = { showGuidePopup = false }
                        )
                    }
                }

                // Industrial Dialogs
                if (showLimitError) {
                    AlertDialog(onDismissRequest = { showLimitError = false }, title = { Text("VIOLATION HIT !") }, text = { Text("Entry exceeds maximum safety threshold of 500 units per line item.") }, confirmButton = { TextButton(onClick = { showLimitError = false }) { Text("CALIBRATE") } })
                }

                if (duplicateFound != null) {
                    AlertDialog(
                        onDismissRequest = { duplicateFound = null },
                        title = { Text("Already in list!") },
                        text = { Text("Proceed with Adding the Quantity for ${duplicateFound?.medName}?") },
                        confirmButton = {
                            TextButton(onClick = {
                                val idx = draftOrder.indexOf(duplicateFound!!)
                                if (idx != -1) {
                                    val current = draftOrder[idx].qty.toIntOrNull() ?: 0
                                    draftOrder[idx] = draftOrder[idx].copy(qty = (current + (quantity.toIntOrNull() ?: 0)).toString())
                                }
                                duplicateFound = null; medName = ""; quantity = ""
                            }) { Text("UPDATE", color = premiumBlue, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = { TextButton(onClick = { duplicateFound = null }) { Text("CANCEL") } }
                    )
                }

                if (itemToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { itemToDelete = null },
                        title = { Text("CONFIRM REMOVAL") },
                        text = { Text("Remove ${itemToDelete?.medName} from requisition log?") },
                        confirmButton = { TextButton(onClick = { draftOrder.remove(itemToDelete); itemToDelete = null }) { Text("REMOVE", color = Color.Red, fontWeight = FontWeight.Bold) } },
                        dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("CANCEL") } }
                    )
                }

                if (selectedItem != null) {
                    AlertDialog(
                        onDismissRequest = { selectedItem = null },
                        confirmButton = { Button(onClick = { selectedItem = null }, colors = ButtonDefaults.buttonColors(containerColor = premiumBlue)) { Text("ACKNOWLEDGE") } },
                        title = { Text(selectedItem!!.medName.uppercase(), fontWeight = FontWeight.Black) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                DetailRowPopup("Quantity", "${selectedItem!!.qty} Units")
                                DetailRowPopup("Added at", selectedItem!!.timestamp)
                                DetailRowPopup("Status", "In Draft (Not sent yet)")
                            }
                        }
                    )
                }
            }
        }
    }

    if (isSyncing) {
        LaunchedEffect(Unit) {
            delay(1500)
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val orderMessage = StringBuilder()
            orderMessage.append("🏛️ *GRAMA SANJEEVINI OFFICIAL REQUISITION*\n------------------------------------------\n📅 DATE: $date\n🆔 REQ ID: #GS-${(1000..9999).random()}\n------------------------------------------\n\n")
            draftOrder.forEachIndexed { index, item -> orderMessage.append("${index + 1}. *${item.medName.uppercase()}*\n   Requirement: ${item.qty} Units\n\n") }
            orderMessage.append("------------------------------------------\n✅ *STATUS: AUTHORIZED REQUEST*")
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, orderMessage.toString()) }
            context.startActivity(Intent.createChooser(intent, "TRANSMIT:"))
            isSyncing = false; draftOrder.clear()
        }
    }
}

@Composable
fun MissionCardSnapEffect(title: String, lines: List<String>, buttonText: String, onDismiss: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isVanishing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { alpha.animateTo(1f, tween(400)) }
        launch { rotation.animateTo(360f, tween(800, easing = FastOutSlowInEasing)) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight().graphicsLayer {
            rotationY = rotation.value
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            cameraDistance = 12f * density
        }.clickable(enabled = false) {},
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(15.dp)
    ) {
        Column(Modifier.padding(28.dp).graphicsLayer { if (rotation.value > 90f && rotation.value < 270f) rotationY = 180f }) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0D47A1), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            lines.forEach { line -> Text(line, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E), modifier = Modifier.padding(vertical = 4.dp)) }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (!isVanishing) {
                        isVanishing = true
                        scope.launch {
                            launch { alpha.animateTo(0f, tween(400)) }
                            launch { scale.animateTo(0.7f, tween(400)) }
                            delay(400)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
            ) { Text(buttonText, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
fun DetailRowPopup(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}