package com.tsynytsyna.animations.glass_shatter

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
    "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=1080&q=85", // Glass Skyscraper
    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=1080&q=85", // Prismatic Crystal
    "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1080&q=85", // Tokyo Neon
    "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1080&q=85", // Alpine Peak
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1080&q=85", // Cosmic Nebula
    "https://images.unsplash.com/photo-1501594907352-04cda38ebc29?auto=format&fit=crop&w=1080&q=85", // Golden Gate
    "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1080&q=85", // Antelope Light
    "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1080&q=85", // Sahara Sand
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlassShatterEffectDemo(modifier: Modifier = Modifier) {
    val deck = rememberSwipeCardState(itemCount = photos.size)
    val shatter = rememberGlassShatterState(durationMs = 1500)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
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
                    tapEnabled = true,
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