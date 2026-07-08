package com.tsynytsyna.animations.fan_cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val photos = listOf(
    "https://picsum.photos/id/1080/600/600",
    "https://picsum.photos/id/1074/600/600",
    "https://picsum.photos/id/1062/600/600",
    "https://picsum.photos/id/1060/600/600",
    "https://picsum.photos/id/1059/600/600",
    "https://picsum.photos/id/1050/600/600",
    "https://picsum.photos/id/1044/600/600",
)

@Composable
fun FanCardsDemo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Fan Cards",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap the stack to fan • tap a card to pick",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            FanCards(photos = photos)
        }
    }
}