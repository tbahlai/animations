package com.tsynytsyna.animations.rift_toggle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RiftDemo(modifier: Modifier = Modifier) {
    var isDark by remember { mutableStateOf(false) }

    RiftToggle(
        isDark = isDark,
        onToggle = { isDark = !isDark },
        modifier = modifier,
        riftColor = Color(0xFFFF0000),
        lightContent = { ThemedScreen(dark = false) },
        darkContent = { ThemedScreen(dark = true) }
    )
}

@Composable
private fun ThemedScreen(dark: Boolean) {
    val bg = if (dark) Color(0xFF121212) else Color(0xFFF5F5F5)
    val textColor = if (dark) Color.White else Color(0xFF1a1a1a)
    val cardBg = if (dark) Color(0xFF1E1E1E) else Color.White
    val accent = if (dark) Color(0xFFBB86FC) else Color(0xFF6200EE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (dark) "🌙" else "☀️",
            fontSize = 56.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (dark) "Dark Mode" else "Light Mode",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap anywhere to toggle",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 16.sp
        )
        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("John Doe", fontWeight = FontWeight.Bold, color = textColor)
                        Text("Premium Member", color = textColor.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Welcome to the Upside Down 🔦",
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Statistics", fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Posts", "128", textColor, accent)
                    StatItem("Followers", "4.2K", textColor, accent)
                    StatItem("Following", "312", textColor, accent)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, textColor: Color, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = accent, fontSize = 20.sp)
        Text(label, color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}
