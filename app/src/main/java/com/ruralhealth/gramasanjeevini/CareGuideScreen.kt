package com.ruralhealth.gramasanjeevini

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full screen Care Guide with instructions on medication safety.
 */
@Composable
fun CareGuideScreen(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        // --- TOP HEADER BAR ---
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D47A1))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = "HEALTH CARE GUIDE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        // --- SCROLLABLE CONTENT ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            item {
                Text(
                    text = "Safety Instructions",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D47A1)
                )
                Text(
                    text = "Guidelines for responsible medication and recovery.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(30.dp))

                // SECTION 1: WHAT NOT TO DO (TOP - RED)
                CareGuideSection(
                    title = "❌ WHAT NOT TO DO",
                    color = Color(0xFFC62828),
                    items = listOf(
                        "Do not self-medicate for chronic or severe pain.",
                        "Avoid taking expired medicines from home kits.",
                        "Do not stop medication halfway because you feel better.",
                        "Avoid spicy and oily foods during stomach upset.",
                        "Do not share your prescription medicines with others."
                    )
                )

                Spacer(Modifier.height(32.dp))

                // SECTION 2: WHAT TO DO (BOTTOM - GREEN)
                CareGuideSection(
                    title = "✅ WHAT TO DO",
                    color = Color(0xFF2E7D32),
                    items = listOf(
                        "Read medication labels carefully before use.",
                        "Complete the full course of prescribed medication.",
                        "Keep yourself hydrated with clean water and ORS.",
                        "Record your body temperature and symptoms daily.",
                        "Consult a doctor if symptoms persist beyond 3 days."
                    )
                )

                Spacer(Modifier.height(40.dp))

                // --- EMERGENCY ALERT CARD ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFF57F17)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Emergency Note: If you face breathing difficulty or severe rash, call for an ambulance immediately.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Reusable layout for Dos and Don'ts sections
 */
@Composable
fun CareGuideSection(title: String, color: Color, items: List<String>) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = color
        )
        Spacer(Modifier.height(12.dp))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(
                    text = "•",
                    color = color,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(20.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = item,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}