package com.tsynytsyna.animations.wave_reassembly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val photos = listOf(
    "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=1080&q=85", // Antelope Sunbeam
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1080&q=85", // Deep Space Nebula
    "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1080&q=85", // Emerald Mountain Lake
    "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=1080&q=85", // Tokyo Neon Rain
    "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1080&q=85", // Alpine Peak
    "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1080&q=85", // Sahara Sand Dunes
    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=1080&q=85", // Prismatic Crystal
    "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=1080&q=85", // Northern Lights Aurora
)

/** Length of the fake inference pass before the reconstruction starts. */
private const val PROCESSING_MS = 700L

/** When the second and third status lines appear during the reconstruction. */
private const val SECOND_MESSAGE_MS = 550L
private const val THIRD_MESSAGE_MS = 550L

private val statusMessages = listOf(
    "Removing noise…",
    "Restoring details…",
    "Enhancing colors…",
)

private enum class Stage { Idle, Processing, Reconstructing, Done }

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WaveReassemblyDemo(modifier: Modifier = Modifier) {
    val wave = rememberWaveReassemblyState()
    val scope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(Stage.Idle) }
    var enhanced by remember { mutableStateOf(false) }
    var message by remember { mutableIntStateOf(0) }
    var photoIndex by remember { mutableIntStateOf(0) }

    val busy = stage == Stage.Processing || stage == Stage.Reconstructing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Header(
            onNextPhoto = {
                photoIndex = (photoIndex + 1) % photos.size
                // A new photo arrives un-enhanced, badge and all.
                enhanced = false
                message = 0
                stage = Stage.Idle
            },
            nextEnabled = !busy,
        )

        Spacer(Modifier.height(24.dp))

        PhotoCard(
            wave = wave,
            stage = stage,
            enhanced = enhanced,
            photo = photos[photoIndex],
            nextPhoto = photos[(photoIndex + 1) % photos.size],
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Spacer(Modifier.height(20.dp))

        StatusLine(busy = busy, message = statusMessages[message])

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    enhanced = false
                    message = 0
                    stage = Stage.Processing
                    delay(PROCESSING_MS.milliseconds)

                    enhanced = true
                    message = 1
                    stage = Stage.Reconstructing
                    wave.assemble { stage = Stage.Done }

                    delay(SECOND_MESSAGE_MS.milliseconds)
                    if (stage == Stage.Reconstructing) message = 2
                    delay(THIRD_MESSAGE_MS.milliseconds)
                }
            },
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = if (busy) "Enhancing…" else "Enhance Photo",
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun PhotoCard(
    wave: WaveReassemblyState,
    stage: Stage,
    enhanced: Boolean,
    photo: String,
    nextPhoto: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        PhotoStack(wave, stage, enhanced, photo, nextPhoto)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PhotoStack(
    wave: WaveReassemblyState,
    stage: Stage,
    enhanced: Boolean,
    photo: String,
    nextPhoto: String,
) {
    Box(Modifier.fillMaxSize()) {
        GlideImage(
            model = nextPhoto,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0f),
            contentScale = ContentScale.Crop,
        )

        WaveReassembly(wave, Modifier.fillMaxSize(), captureFrames = 3) {
            GlideImage(
                model = photo,
                contentDescription = "Photo being enhanced",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = if (enhanced) EnhancedFilter else null,
            )
        }

        AnimatedVisibility(
            visible = stage == Stage.Processing,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        }

        AnimatedVisibility(
            visible = stage == Stage.Done,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
        ) {
            EnhancedBadge()
        }
    }
}

@Composable
private fun Header(onNextPhoto: () -> Unit, nextEnabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Photo Enhancer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }

        FilledTonalIconButton(
            onClick = onNextPhoto,
            enabled = nextEnabled,
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Next photo",
            )
        }
    }
}

/** Fixed height, so the layout never jumps as the status appears and clears. */
@Composable
private fun StatusLine(busy: Boolean, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = if (busy) message else null, label = "status") { text ->
            if (text != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedBadge() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = "Enhanced",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

/**
 * The "enhanced" look: more saturation, a firmer contrast curve and a touch of lift.
 *
 * Contrast has to pivot around mid-grey, hence the `(1 - contrast) * 128` offset — scaling
 * the channels alone would just brighten the image.
 */
private val EnhancedFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply {
        setToSaturation(1.35f)
        timesAssign(contrastMatrix(contrast = 1.18f, lift = 4f))
    }
)

private fun contrastMatrix(contrast: Float, lift: Float): ColorMatrix {
    val offset = lift + (1f - contrast) * 128f
    return ColorMatrix(
        floatArrayOf(
            contrast, 0f, 0f, 0f, offset,
            0f, contrast, 0f, 0f, offset,
            0f, 0f, contrast, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        )
    )
}