package com.tsynytsyna.animations.glass_shatter

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tsynytsyna.animations.swipe_card.SwipeCard
import com.tsynytsyna.animations.swipe_card.rememberSwipeCardState

private val photos = listOf(
    "https://picsum.photos/id/29/800/600",
    "https://picsum.photos/id/180/800/600",
    "https://picsum.photos/id/169/800/600",
    "https://picsum.photos/id/40/800/600",
    "https://picsum.photos/id/15/800/600",
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlassShatterEffectDemo(modifier: Modifier = Modifier) {
    val deck = rememberSwipeCardState(itemCount = photos.size)
    val shatter = rememberGlassShatterState(durationMs = 1500)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            SwipeCard(deck, modifier = Modifier.fillMaxSize()) {
                GlassShatter(
                    state = shatter,
                    modifier = Modifier.fillMaxSize(),
                    tapEnabled = false,
                ) {
                    GlideImage(
                        model = photos[deck.currentIndex],
                        contentDescription = "Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundActionButton(
                symbol = "✕",
                container = Color(0xFFFF5252),
                onClick = {
                    deck.dismiss { onComplete ->
                        shatter.shatter(
                            deck.impactCenter,
                            onComplete
                        )
                    }
                },
                enabled = !deck.busy
            )
            RoundActionButton(
                symbol = "♥",
                container = Color(0xFF1DE9B6),
                onClick = { deck.swipe(toRight = true) },
                enabled = !deck.busy
            )
        }
    }
}

@Composable
private fun RoundActionButton(
    symbol: String,
    container: Color,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(68.dp),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container)
    ) {
        Text(symbol, fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}