package com.tsynytsyna.animations.swipe_card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

private data class CardProfile(
    val title: String,
    val subtitle: String,
    val tags: List<String>,
    val imageUrl: String,
    val accentColor: Color,
)

private val sampleProfiles = listOf(
    CardProfile(
        title = "Neon Nebula",
        subtitle = "Deep Space Explorer • 4.2 ly",
        tags = listOf("Astronomy", "Cosmos", "Vibrant"),
        imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFF7C4DFF),
    ),
    CardProfile(
        title = "Alpine Crest",
        subtitle = "Mountain Expedition • 3,800m",
        tags = listOf("Nature", "Hiking", "Adventure"),
        imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFF00E5FF),
    ),
    CardProfile(
        title = "Cyber Cityscape",
        subtitle = "Metropolis Night Drive • 02:45 AM",
        tags = listOf("Cyberpunk", "Urban", "Architecture"),
        imageUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFFFF1744),
    ),
    CardProfile(
        title = "Ancient Redwoods",
        subtitle = "Temperate Rainforest • Pacific Trail",
        tags = listOf("Wilderness", "Tranquil", "Greenery"),
        imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFF00E676),
    ),
    CardProfile(
        title = "Sahara Dunes",
        subtitle = "Erg Chebbi Crossing • Golden Hour",
        tags = listOf("Desert", "Warm", "Minimalist"),
        imageUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFFFF9100),
    ),
    CardProfile(
        title = "Aurora Veil",
        subtitle = "Lofoten Islands • Solar Wind",
        tags = listOf("Arctic", "Night Sky", "Ethereal"),
        imageUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFF1DE9B6),
    ),
    CardProfile(
        title = "Antelope Canyon",
        subtitle = "Navajo Sandstone • Solar Radiance",
        tags = listOf("Geology", "Warm", "Monolithic"),
        imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFFFF5252),
    ),
    CardProfile(
        title = "Prismatic Spectrum",
        subtitle = "Optical Diffraction • Kinetic Light",
        tags = listOf("Abstract", "Optics", "Futuristic"),
        imageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=1080&q=85",
        accentColor = Color(0xFFE040FB),
    ),
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SwipeCardDemo(modifier: Modifier = Modifier) {
    val state = rememberSwipeCardState(itemCount = sampleProfiles.size)
    var dragAccumulatorX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Swipe Deck",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Drag card or tap action buttons below",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Background Card (depth illusion)
                val nextProfile = sampleProfiles[(state.currentIndex + 1) % sampleProfiles.size]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(450.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1E293B),
                ) {
                    GlideImage(
                        model = nextProfile.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }

                // Active Swipeable Card
                val profile = sampleProfiles[state.currentIndex % sampleProfiles.size]
                SwipeCard(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(470.dp)
                        .shadow(24.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .pointerInput(state.busy) {
                            if (state.busy) return@pointerInput
                            detectDragGestures(
                                onDragStart = { dragAccumulatorX = 0f },
                                onDragEnd = {
                                    if (dragAccumulatorX > 160f) {
                                        state.swipe(toRight = true)
                                    } else if (dragAccumulatorX < -160f) {
                                        state.swipe(toRight = false)
                                    }
                                    dragAccumulatorX = 0f
                                },
                                onDragCancel = { dragAccumulatorX = 0f },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulatorX += dragAmount.x
                                }
                            )
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GlideImage(
                            model = profile.imageUrl,
                            contentDescription = profile.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )

                        // Bottom gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.85f),
                                        ),
                                        startY = 200f,
                                    )
                                )
                        )

                        // Card Content
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                profile.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color.White.copy(alpha = 0.2f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }

                            Text(
                                text = profile.title,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile.subtitle,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reject Button
                ActionButton(
                    label = "✕",
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                    borderColor = Color(0xFFEF4444),
                    contentColor = Color(0xFFEF4444),
                    size = 58.dp,
                    onClick = { state.swipe(toRight = false) }
                )

                // Superlike / Center action
                ActionButton(
                    label = "★",
                    containerColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    borderColor = Color(0xFF3B82F6),
                    contentColor = Color(0xFF60A5FA),
                    size = 50.dp,
                    onClick = { state.swipe(toRight = true) }
                )

                // Like Button
                ActionButton(
                    label = "♥",
                    containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                    borderColor = Color(0xFF10B981),
                    contentColor = Color(0xFF34D399),
                    size = 58.dp,
                    onClick = { state.swipe(toRight = true) }
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    containerColor: Color,
    borderColor: Color,
    contentColor: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(8.dp, CircleShape)
            .background(containerColor, CircleShape)
            .border(1.5.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
