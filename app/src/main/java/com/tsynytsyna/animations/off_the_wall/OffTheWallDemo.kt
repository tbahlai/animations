package com.tsynytsyna.animations.off_the_wall

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

// Refined Luxury Hi-Fi Color Palette
private val BgDark = Color(0xFF06080E)
private val CardSurface = Color(0xFF0C101A)
private val BorderSubtle = Color(0xFF1B2338)
private val TextWhite = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF64748B)
private val AccentAmber = Color(0xFFFF9E0B)
private val AccentCoral = Color(0xFFFF5722)
private val EmeraldActive = Color(0xFF10B981)

private data class TrackInfo(
    val index: String,
    val title: String,
    val artist: String,
    val length: String,
    val totalSeconds: Int,
)

private val QueueTracks = listOf(
    TrackInfo("01", "Solaris Mirage", "Komorebi & Astral Ensemble", "05:12", 312),
    TrackInfo("02", "Paper Weather", "Oyster Club • Ganymede", "03:38", 218),
    TrackInfo("03", "Thread Count", "Marlow • Analog Sessions", "05:01", 301),
    TrackInfo("04", "Undercurrent", "Vera Lune • Horizon", "03:55", 235),
)

@Composable
fun OffTheWallDemo(modifier: Modifier = Modifier) {
    val state = rememberOffTheWallState()

    // Real-time Procedural Audio Synthesizer
    val synth = remember { LofiAudioSynth() }
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(204) }

    // Start procedural synth when entering screen, clean up on dispose
    DisposableEffect(Unit) {
        synth.start(coroutineScope)
        onDispose {
            synth.stop()
        }
    }

    // React to play/pause and mute state
    LaunchedEffect(isPlaying, isMuted) {
        if (isPlaying && !isMuted) {
            synth.resume()
        } else {
            synth.pause()
        }
    }

    // Ticking seconds counter while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Infinite Transitions for Live Vinyl Rotation & EQ Dancing Visualizer
    val infiniteTransition = rememberInfiniteTransition(label = "musicVisualizer")

    // Smooth continuous vinyl rotation at 33⅓ RPM
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing)
        ),
        label = "vinylRotation"
    )

    // Dynamic wave phase for dancing audio spectrum bars
    val spectrumPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "spectrumPhase"
    )

    // Pulsing bass glow synced to tempo
    val bassPulse by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bassPulse"
    )

    val currentTrack = QueueTracks[currentTrackIndex]

    Box(modifier = modifier.fillMaxSize().background(BgDark)) {
        // Revealed when cloth screen is pulled or torn off the wall
        BehindTheScreen(
            vinylRotation = if (isPlaying) vinylRotation else 0f,
            isPlaying = isPlaying,
            onReset = { state.reset() }
        )

        // The tearable physical cloth solver surface
        OffTheWall(
            state = state,
            modifier = Modifier.fillMaxSize(),
            columns = 22,
        ) {
            NowPlayingScreen(
                track = currentTrack,
                isPlaying = isPlaying,
                isMuted = isMuted,
                vinylRotation = if (isPlaying) vinylRotation else 0f,
                spectrumPhase = spectrumPhase,
                bassPulse = bassPulse,
                elapsedSeconds = elapsedSeconds,
                onTogglePlay = { isPlaying = !isPlaying },
                onToggleMute = { isMuted = !isMuted },
                onNextTrack = {
                    currentTrackIndex = (currentTrackIndex + 1) % QueueTracks.size
                    elapsedSeconds = 0
                },
                onPrevTrack = {
                    currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else QueueTracks.size - 1
                    elapsedSeconds = 0
                },
            )
        }

        // Bottom floating glass control bar
        Controls(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(10f)
                .padding(bottom = 20.dp),
        )
    }
}

/**
 * Exposed Analog Turntable & Vacuum Tube Core revealed behind the wall.
 */
@Composable
private fun BehindTheScreen(
    vinylRotation: Float,
    isPlaying: Boolean,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF080B12), Color(0xFF040508))
                )
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF1A1208), RoundedCornerShape(100.dp))
                .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.size(7.dp).background(if (isPlaying) EmeraldActive else AccentAmber, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPlaying) "ANALOG CORE LIVE • 33⅓ RPM" else "ANALOG CORE IDLE",
                color = AccentAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Illustrated Analog Turntable Platter & Tonearm
        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(30.dp, CircleShape, spotColor = AccentAmber.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            // Rotating turntable vinyl platter
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(vinylRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Outer metallic chassis ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E2538), Color(0xFF0F1420), Color(0xFF080A10)),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                )
                drawCircle(color = Color(0xFF334155), radius = radius, style = Stroke(2.dp.toPx()))

                // Vinyl Grooves
                val grooveRadii = listOf(0.88f, 0.80f, 0.72f, 0.64f, 0.56f, 0.48f)
                grooveRadii.forEach { fraction ->
                    drawCircle(
                        color = Color(0xFF1E293B).copy(alpha = 0.6f),
                        radius = radius * fraction,
                        style = Stroke(1.dp.toPx()),
                    )
                }

                // Rotating Light Reflection Streak
                drawLine(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        center = center,
                        radius = radius * 0.85f,
                    ),
                    start = Offset(center.x - radius * 0.7f, center.y - radius * 0.7f),
                    end = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
                    strokeWidth = 36.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center Golden Brass Label
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFB45309)),
                    ),
                    radius = radius * 0.35f,
                )
                // Center Brass Spindle Pin
                drawCircle(color = Color(0xFF1E293B), radius = radius * 0.08f)
                drawCircle(color = Color(0xFFFDE68A), radius = radius * 0.04f)
            }

            // Stationary Tonearm positioned on top of the rotating record
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Tonearm line
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(size.width * 0.88f, size.height * 0.12f),
                    end = Offset(size.width * 0.55f, size.height * 0.60f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Pivot base
                drawCircle(
                    color = Color(0xFF475569),
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                    radius = 8.dp.toPx(),
                )
                // Tonearm Cartridge head
                drawCircle(
                    color = AccentCoral,
                    center = Offset(size.width * 0.55f, size.height * 0.60f),
                    radius = 5.dp.toPx(),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Headline & Description
        Text(
            text = "Display Dismounted.",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You tore the front digital screen off the wall.\nThe analog vacuum-tube preamp and mechanical belt-drive turntable are now running live.",
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Re-Anchor CTA Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AccentCoral, AccentAmber)
                    )
                )
                .clickable(onClick = onReset)
                .padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            Text(
                text = "📌  RE-ANCHOR SCREEN TO WALL",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
    }
}

/**
 * Front Hi-Fi Music Player Screen (rendered directly on the tearable cloth mesh).
 */
@Composable
private fun NowPlayingScreen(
    track: TrackInfo,
    isPlaying: Boolean,
    isMuted: Boolean,
    vinylRotation: Float,
    spectrumPhase: Float,
    bassPulse: Float,
    elapsedSeconds: Int,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF090D18), BgDark)
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // Top Mounting Pins & Hi-Res Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left Wall Mounting Brass Pin
            MountingPin()

            // Hi-Res Audio / Sound Status Pill (clickable to mute/unmute audio)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF101626))
                    .border(1.dp, if (!isMuted && isPlaying) EmeraldActive.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(100.dp))
                    .clickable(onClick = onToggleMute)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (!isMuted && isPlaying) EmeraldActive else AccentAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMuted) "🔈 SOUND MUTED • TAP FOR AUDIO" else if (isPlaying) "🔊 LIVE LO-FI AUDIO • 24-BIT / 192kHz" else "⏸ PAUSED • 24-BIT / 192kHz",
                    color = if (!isMuted && isPlaying) EmeraldActive else AccentAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                )
            }

            // Right Wall Mounting Brass Pin
            MountingPin()
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Vinyl & Jacket Showcase Centerpiece
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Sliding Black Vinyl Record Disk (Peeking from behind the sleeve and rotating!)
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
                    .rotate(vinylRotation)
                    .shadow(16.dp, CircleShape),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    // Black vinyl disc base
                    drawCircle(color = Color(0xFF10131A), radius = radius)

                    // Concentric micro-grooves
                    val grooves = listOf(0.92f, 0.85f, 0.78f, 0.71f, 0.64f, 0.57f, 0.50f)
                    grooves.forEach { frac ->
                        drawCircle(
                            color = Color(0xFF232938),
                            radius = radius * frac,
                            style = Stroke(1.dp.toPx()),
                        )
                    }

                    // Rotating Sheen Highlight
                    drawLine(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                            center = center,
                            radius = radius * 0.9f,
                        ),
                        start = Offset(center.x - radius * 0.7f, center.y - radius * 0.7f),
                        end = Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
                        strokeWidth = 28.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Golden Center Label
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
                        ),
                        radius = radius * 0.35f,
                    )
                    // Spindle Hole
                    drawCircle(color = Color(0xFF080B10), radius = radius * 0.08f)
                }
            }

            // Front Album Sleeve Jacket (with subtle bass pulse scale)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterStart)
                    .scale(if (isPlaying) bassPulse else 1f)
                    .shadow(18.dp, RoundedCornerShape(18.dp), spotColor = AccentCoral.copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B1D54),
                                Color(0xFFFF5722),
                                Color(0xFFFFB300),
                                Color(0xFF131A2D)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(400f, 400f)
                        )
                    )
                    .border(1.2.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ASTRANOVA",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = "SIDE A",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Column {
                        Text(
                            text = "SOLARIS",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "MIRAGE",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 3.sp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Track Title & Artist Metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Favorite Heart Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF141A29), CircleShape)
                    .border(1.dp, BorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("♥", color = AccentCoral, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Dancing Waveform Audio Scrubber
        AudioWaveformScrubber(
            isPlaying = isPlaying,
            spectrumPhase = spectrumPhase,
            elapsedSeconds = elapsedSeconds,
            totalSeconds = track.totalSeconds,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hi-Fi Transport Playback Controls
        PlaybackControlsRow(
            isPlaying = isPlaying,
            onTogglePlay = onTogglePlay,
            onNext = onNextTrack,
            onPrev = onPrevTrack,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Up Next Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "UP NEXT IN QUEUE",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
            )
            Text(
                text = if (isPlaying) "Autoplay Active" else "Playback Paused",
                color = if (isPlaying) EmeraldActive else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Up Next Track List
        QueueTracks.filter { it.title != track.title }.take(2).forEach { nextTrack ->
            QueueTrackRow(nextTrack)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Wall mounting brass push-pin graphics anchored at top corners.
 */
@Composable
private fun MountingPin() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .shadow(4.dp, CircleShape, spotColor = AccentAmber)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF78350F))
                ),
                shape = CircleShape
            )
            .border(1.dp, Color(0xFFFFFBEB).copy(alpha = 0.6f), CircleShape)
    )
}

/**
 * Dynamic 32-bar dancing audio spectrum waveform visualizer synced to playback.
 */
@Composable
private fun AudioWaveformScrubber(
    isPlaying: Boolean,
    spectrumPhase: Float,
    elapsedSeconds: Int,
    totalSeconds: Int,
) {
    val baseBarHeights = remember {
        listOf(
            0.3f, 0.45f, 0.7f, 0.9f, 0.6f, 0.4f, 0.8f, 1.0f,
            0.75f, 0.5f, 0.65f, 0.85f, 0.95f, 0.7f, 0.4f, 0.6f,
            0.85f, 0.75f, 0.9f, 0.55f, 0.35f, 0.65f, 0.8f, 0.95f,
            0.7f, 0.5f, 0.4f, 0.6f, 0.75f, 0.45f, 0.3f, 0.2f
        )
    }

    val progressFraction = (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)

    val currentMins = elapsedSeconds / 60
    val currentSecs = elapsedSeconds % 60
    val currentTimeString = "%02d:%02d".format(currentMins, currentSecs)

    val totalMins = totalSeconds / 60
    val totalSecs = totalSeconds % 60
    val totalTimeString = "%02d:%02d".format(totalMins, totalSecs)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            baseBarHeights.forEachIndexed { index, baseHeight ->
                val isPlayed = (index.toFloat() / baseBarHeights.size) <= progressFraction

                // Dynamically animated bar height
                val animatedHeightFraction = if (isPlaying) {
                    val wave = sin(spectrumPhase * 2f + index * 0.42f) * 0.35f +
                            sin(spectrumPhase * 3.3f + index * 0.85f) * 0.18f
                    (baseHeight + wave).coerceIn(0.18f, 1.0f)
                } else {
                    (baseHeight * 0.55f).coerceAtLeast(0.15f)
                }

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((26 * animatedHeightFraction).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isPlayed) {
                                Brush.verticalGradient(
                                    colors = listOf(AccentCoral, AccentAmber)
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF26334D), Color(0xFF182030))
                                )
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(currentTimeString, color = AccentAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(totalTimeString, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Normal)
        }
    }
}

/**
 * Hi-Fi Transport Controls Row with functional glowing Play/Pause and Next/Prev buttons.
 */
@Composable
private fun PlaybackControlsRow(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Shuffle
        Text("⇄", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        // Previous Track
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onPrev)
                .padding(6.dp)
        ) {
            Text("⏮", color = TextWhite, fontSize = 18.sp)
        }

        // Glowing Center Play/Pause Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(12.dp, CircleShape, spotColor = AccentCoral.copy(alpha = 0.45f))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AccentCoral, AccentAmber)
                    )
                )
                .clickable(onClick = onTogglePlay),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isPlaying) "❚❚" else "▶",
                color = Color.White,
                fontSize = if (isPlaying) 13.sp else 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Next Track
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onNext)
                .padding(6.dp)
        ) {
            Text("⏭", color = TextWhite, fontSize = 18.sp)
        }

        // Repeat
        Text("🔁", color = TextMuted, fontSize = 14.sp)
    }
}

/**
 * Up Next Queue Track Row with audio visualizer bar.
 */
@Composable
private fun QueueTrackRow(track: TrackInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F1A), RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Track Index Badge
        Text(
            text = track.index,
            color = AccentAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp),
        )

        // Track Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = track.artist,
                color = TextMuted,
                fontSize = 10.sp,
            )
        }

        // Length
        Text(
            text = track.length,
            color = TextMuted,
            fontSize = 11.sp,
        )
    }
}

/**
 * Bottom Floating Action Controls with Tension Status & Spring Physics Triggers.
 */
@Composable
private fun Controls(state: OffTheWallState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF0B101C).copy(alpha = 0.92f), RoundedCornerShape(100.dp))
            .border(1.2.dp, Color(0xFF1E2B45), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tension Status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (state.pinned) EmeraldActive else AccentCoral, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (state.pinned) "2 PINS ANCHORED" else "PINS RELEASED",
                color = if (state.pinned) EmeraldActive else AccentCoral,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        // Pull Pins Action Pill
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (state.pinned) Color(0xFF1F293D) else Color(0xFF111724))
                .clickable(enabled = state.pinned) { state.drop() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "PULL PINS",
                color = if (state.pinned) AccentAmber else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }

        // Reset Action Pill
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (state.awake || state.dismissed) Color(0xFF2A1C38) else Color(0xFF111724))
                .clickable(enabled = state.awake || state.dismissed) { state.reset() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "RESET",
                color = if (state.awake || state.dismissed) Color(0xFFE879F9) else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
    }
}