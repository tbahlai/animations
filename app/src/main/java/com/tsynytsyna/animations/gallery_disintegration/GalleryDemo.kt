package com.tsynytsyna.animations.gallery_disintegration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

private val photos = listOf(
    "https://picsum.photos/id/29/800/600",
    "https://picsum.photos/id/180/800/600",
    "https://picsum.photos/id/169/800/600",
    "https://picsum.photos/id/40/800/600",
    "https://picsum.photos/id/15/800/600",
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryDemo(modifier: Modifier = Modifier) {
    val state = rememberDisintegrationState(durationMs = 1000)
    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val appearScale = remember { Animatable(1f) }
    val appearAlpha = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Photo Gallery",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${currentIndex + 1} / ${photos.size}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Disintegration(state, Modifier.fillMaxSize()) {
                GlideImage(
                    model = photos[currentIndex],
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = appearScale.value
                            scaleY = appearScale.value
                            alpha = appearAlpha.value
                        }
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    state.disintegrate {
                        currentIndex = (currentIndex + 1) % photos.size
                        scope.launch {
                            appearScale.snapTo(0.8f)
                            appearAlpha.snapTo(0f)
                            launch { appearScale.animateTo(1f, tween(500)) }
                            launch { appearAlpha.animateTo(1f, tween(500)) }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DE9B6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next Photo")
            }
        }
    }
}
