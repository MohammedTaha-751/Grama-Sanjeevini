@file:OptIn(ExperimentalMaterial3Api::class)

package com.ruralhealth.gramasanjeevini

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun ExpiryManagementScreen(onBack: () -> Unit) {
    var selectedMedicine by remember { mutableStateOf<InventoryMed?>(null) }
    var showExpiryGuide by remember { mutableStateOf(false) }
    var showReturnConfirm by remember { mutableStateOf(false) } // State for Return Yes/No
    var showReturnMessage by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Track medicines that are in the return process
    val returningMedicineIds = remember { mutableStateListOf<Int>() }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Header & Guide Gradient
    val pharmaGradient = Brush.verticalGradient(listOf(Color(0xFF43A047), Color(0xFF2E7D32)))

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var sortByUrgency by remember { mutableStateOf(false) }
        val rawStock = InventoryRepo.stock

        val stockList = remember(searchQuery, sortByUrgency, rawStock.size) {
            var filtered = rawStock.filter { it.name.contains(searchQuery, ignoreCase = true) }
            if (sortByUrgency) {
                filtered = filtered.sortedBy { med ->
                    val clean = med.expiryDate.filter { it.isDigit() }
                    if (clean.length == 8) {
                        clean.takeLast(4) + clean.substring(2, 4) + clean.take(2)
                    } else "99999999"
                }
            }
            filtered
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFBFBFE)) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize()) {
                    // --- HEADER ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(pharmaGradient)
                            .statusBarsPadding()
                            .padding(top = 15.dp, bottom = 20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = onBack,
                                    color = Color.White.copy(0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                                }

                                Text(
                                    "EXPIRY MONITOR",
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )

                                Surface(
                                    onClick = { sortByUrgency = !sortByUrgency },
                                    color = if (sortByUrgency) Color.White else Color.White.copy(0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.Star, null, tint = if (sortByUrgency) Color(0xFF2E7D32) else Color.White, modifier = Modifier.padding(10.dp))
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.9f))
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search stocks...", fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF2E7D32)) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    // --- LIST ---
                    LazyColumn(
                        Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Surface(
                                onClick = { showExpiryGuide = true },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                shadowElevation = 4.dp
                            ) {
                                Box(modifier = Modifier.background(pharmaGradient).padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, null, tint = Color.White)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Expiry Guide", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Understand color & sort logic", fontSize = 12.sp, color = Color.White.copy(0.8f))
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(stockList, key = { _, med -> med.id }) { index, med ->
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(500))
                            ) {
                                ExpiryCardPremium(
                                    med = med,
                                    isReturning = returningMedicineIds.contains(med.id),
                                    onClick = { selectedMedicine = med }
                                )
                            }
                        }
                    }
                }

                // Guide Popup
                if (showExpiryGuide) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable { showExpiryGuide = false }, contentAlignment = Alignment.Center) {
                        ExpiryMissionCard(
                            title = "EXPIRY SYSTEM GUIDE",
                            lines = listOf(
                                "🔴 RED: Medicine is already Expired",
                                "🟡 YELLOW: Expiring in next 30 days",
                                "🟢 GREEN: Stock is Safe for use",
                                "⭐ STAR: Sorts by most urgent first",
                                "📅 UPDATE: Based on live current date"
                            ),
                            onDismiss = { showExpiryGuide = false }
                        )
                    }
                }

                // --- RETURN CONFIRMATION (YES/NO) ---
                if (showReturnConfirm) {
                    AlertDialog(
                        onDismissRequest = { showReturnConfirm = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showReturnConfirm = false
                                showReturnMessage = true
                            }) {
                                Text("YES", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showReturnConfirm = false
                                selectedMedicine = null
                            }) {
                                Text("NO", color = Color.Gray)
                            }
                        },
                        title = { Text("Confirm Return", fontWeight = FontWeight.Bold) },
                        text = { Text("Do you want to start the return process for this medicine?") },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White
                    )
                }

                // Return Success Message
                if (showReturnMessage) {
                    AlertDialog(
                        onDismissRequest = {
                            selectedMedicine?.let { returningMedicineIds.add(it.id) }
                            showReturnMessage = false
                            selectedMedicine = null
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                selectedMedicine?.let { returningMedicineIds.add(it.id) }
                                showReturnMessage = false
                                selectedMedicine = null
                            }) {
                                Text("OK", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        },
                        title = { Text("Processing Return", fontWeight = FontWeight.Bold) },
                        text = { Text("This medicine is now under return to company. It will soon be replaced with a new one.") },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White
                    )
                }

                // Delete Confirmation Popup
                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        confirmButton = {
                            TextButton(onClick = {
                                InventoryRepo.stock.remove(selectedMedicine)
                                selectedMedicine = null
                                showDeleteConfirm = false
                                Toast.makeText(context, "Removed from inventory", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("YES, REMOVE", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDeleteConfirm = false
                                showReturnConfirm = true
                                scope.launch { sheetState.hide() }
                            }) {
                                Text("RETURN INSTEAD", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        },
                        title = { Text("Confirm Removal", fontWeight = FontWeight.Bold) },
                        text = { Text("Are you sure you want to delete this record? You can just return it to the company instead.") },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White
                    )
                }

                if (selectedMedicine != null && !showReturnConfirm && !showReturnMessage && !showDeleteConfirm) {
                    ModalBottomSheet(
                        onDismissRequest = { selectedMedicine = null },
                        sheetState = sheetState,
                        containerColor = Color.White,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) },
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    ) {
                        MedicineDetailPopUp(
                            med = selectedMedicine!!,
                            onReturn = {
                                showReturnConfirm = true
                                scope.launch { sheetState.hide() }
                            },
                            onDeleteTrigger = {
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryMissionCard(title: String, lines: List<String>, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }

    Card(
        modifier = Modifier.fillMaxWidth(0.85f).graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(title, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            lines.forEach { line -> Text(line, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp)) }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) { Text("GOT IT", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ExpiryCardPremium(med: InventoryMed, isReturning: Boolean, onClick: () -> Unit) {
    val today = Calendar.getInstance()
    val todayValue = String.format(Locale.US, "%04d%02d%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH)).toLong()

    val statusColor = remember(med.expiryDate, todayValue) {
        val cleanDate = med.expiryDate.filter { it.isDigit() }
        val expiryValue = if (cleanDate.length == 8) {
            try { (cleanDate.takeLast(4) + cleanDate.substring(2, 4) + cleanDate.take(2)).toLong() } catch (_: Exception) { 0L }
        } else 0L

        when {
            expiryValue == 0L -> Color.Gray
            expiryValue < todayValue -> Color(0xFFD32F2F)
            expiryValue <= todayValue + 100 -> Color(0xFFFBC02D)
            else -> Color(0xFF388E3C)
        }
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(statusColor, CircleShape))
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(med.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C3E50))
                    if (isReturning) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Return in process",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text("Exp: ${formatPharmaDate(med.expiryDate)}", fontSize = 13.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Info, null, tint = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun MedicineDetailPopUp(med: InventoryMed, onReturn: () -> Unit, onDeleteTrigger: () -> Unit) {
    val clean = med.expiryDate.filter { it.isDigit() }
    val today = Calendar.getInstance().let { String.format(Locale.US, "%04d%02d%02d", it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH)).toLong() }
    val expiry = if (clean.length == 8) (clean.takeLast(4) + clean.substring(2, 4) + clean.take(2)).toLong() else 0L

    val statusColor = if (expiry < today && expiry != 0L) Color(0xFFD32F2F) else Color(0xFF388E3C)

    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
        Text(text = med.name, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2C3E50))
        Text(text = "Pharma Audit Trace", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        InfoRow("Batch Expiry", formatPharmaDate(med.expiryDate), statusColor)
        InfoRow("Compliance Status", if (expiry < today && expiry != 0L) "EXPIRED" else "VALID", statusColor)
        InfoRow("Storage Condition", "Dry Storage (25°C)", Color.DarkGray)
        Spacer(Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onReturn,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Return", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onDeleteTrigger,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Remove", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 15.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}