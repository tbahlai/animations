package com.tsynytsyna.animations.kinetic_typography

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KineticTypographyDemo(modifier: Modifier = Modifier) {
    val engine = remember { TypographyPhysicsEngine() }
    var tick by remember { mutableIntStateOf(0) }
    var showInspector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Visual shockwave state
    var shockwaveRadius by remember { mutableFloatStateOf(0f) }
    var shockwaveAlpha by remember { mutableFloatStateOf(0f) }
    var shockwaveCenter by remember { mutableStateOf(Offset.Zero) }

    // 120 FPS continuous physics simulation loop
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                    engine.step(dt)
                    if (shockwaveAlpha > 0.01f) {
                        shockwaveRadius += 1400f * dt
                        shockwaveAlpha = (shockwaveAlpha - dt * 2.2f).coerceAtLeast(0f)
                    }
                    tick++
                }
                lastNanos = now
            }
        }
    }

    val activeTheme = engine.activeTheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activeTheme.backgroundColor)
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    engine.initialize(size.width.toFloat(), size.height.toFloat())
                }
            }
    ) {
        // Atmospheric Ambient Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeTheme.glowColor,
                            activeTheme.glowColor.copy(alpha = 0.08f),
                            activeTheme.backgroundColor
                        ),
                        center = Offset(engine.width * 0.5f, engine.height * 0.45f),
                        radius = 1200f
                    )
                )
        )

        // Physics Splines, Anchors, Spring Chords, and Waves Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            engine.onTouchStart(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            engine.onTouchMove(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            engine.onTouchEnd()
                        },
                        onDragCancel = {
                            engine.onTouchEnd()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            shockwaveCenter = offset
                            shockwaveRadius = 20f
                            shockwaveAlpha = 0.85f
                            engine.triggerShockwave(offset.x, offset.y)
                        },
                        onTap = { offset ->
                            // If tapped on empty space, send pulse
                            shockwaveCenter = offset
                            shockwaveRadius = 15f
                            shockwaveAlpha = 0.65f
                            engine.triggerShockwave(offset.x, offset.y)
                        }
                    )
                }
        ) {
            val unused = tick
            val letters = engine.letters
            val n = letters.size
            if (n == 0) return@Canvas

            // 1. Draw Shockwave Expansion Ring
            if (shockwaveAlpha > 0.01f) {
                drawCircle(
                    color = activeTheme.accentColor.copy(alpha = shockwaveAlpha),
                    radius = shockwaveRadius,
                    center = shockwaveCenter,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = activeTheme.accentColor.copy(alpha = shockwaveAlpha * 0.35f),
                    radius = (shockwaveRadius - 20f).coerceAtLeast(0f),
                    center = shockwaveCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 2. Draw Equilibrium Baseline & Anchor Points
            val firstX = letters.first().eqX
            val lastX = letters.last().eqX
            val eqY = letters.first().eqY

            // Subtle dashed equilibrium line
            drawLine(
                color = activeTheme.ribbonColor.copy(alpha = 0.45f),
                start = Offset(firstX - 40f, eqY),
                end = Offset(lastX + 40f, eqY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )

            // 3. Draw Spring Tension Chords (Hooke Springs from eq to current)
            for (letter in letters) {
                val dx = letter.currentX - letter.eqX
                val dy = letter.currentY - letter.eqY
                val dist = sqrt(dx * dx + dy * dy)
                val strainAlpha = (dist / 200f).coerceIn(0.12f, 0.85f)

                // Spring tension line
                drawLine(
                    color = if (dist > 30f) activeTheme.accentColor.copy(alpha = strainAlpha)
                    else activeTheme.ribbonColor.copy(alpha = 0.3f),
                    start = Offset(letter.eqX, letter.eqY),
                    end = Offset(letter.currentX, letter.currentY),
                    strokeWidth = (1f + (dist / 120f).coerceIn(0f, 3f)).dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Equilibrium anchor dot
                drawCircle(
                    color = activeTheme.accentColor.copy(alpha = 0.45f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(letter.eqX, letter.eqY)
                )
            }

            // 4. Draw Elastic Inter-Glyph Baseline Ribbon
            if (n >= 2) {
                val ribbonPath = Path()
                ribbonPath.moveTo(letters[0].currentX, letters[0].currentY)

                for (i in 0 until n - 1) {
                    val p0 = letters[i]
                    val p1 = letters[i + 1]
                    val midX = (p0.currentX + p1.currentX) * 0.5f
                    val midY = (p0.currentY + p1.currentY) * 0.5f

                    // Smooth quadratic curve through midpoints
                    ribbonPath.quadraticTo(p0.currentX, p0.currentY, midX, midY)
                }
                ribbonPath.lineTo(letters.last().currentX, letters.last().currentY)

                // Ribbon glow stroke
                drawPath(
                    path = ribbonPath,
                    color = activeTheme.accentColor.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Ribbon core wire
                drawPath(
                    path = ribbonPath,
                    color = activeTheme.accentColor.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Kinetic Characters Layer
        val letters = engine.letters
        val unused = tick
        letters.forEach { letter ->
            val speed = sqrt(letter.vx * letter.vx + letter.vy * letter.vy)
            val isHighVelocity = speed > 45f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // Center glyph on physics coordinates
                        translationX = letter.currentX - 50f
                        translationY = letter.currentY - 60f
                        scaleX = letter.scaleX
                        scaleY = letter.scaleY
                        rotationZ = letter.rotationDeg
                    }
            ) {
                // Chromatic Aberration / Motion Trail (When fast)
                if (isHighVelocity) {
                    val trailOffsetX = (-letter.vx * 0.018f).coerceIn(-18f, 18f)
                    val trailOffsetY = (-letter.vy * 0.018f).coerceIn(-18f, 18f)
                    Text(
                        text = letter.char.toString(),
                        color = activeTheme.accentColor.copy(alpha = 0.45f),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.graphicsLayer {
                            translationX = trailOffsetX
                            translationY = trailOffsetY
                        }
                    )
                }

                // Primary Kinetic Character
                Text(
                    text = letter.char.toString(),
                    color = if (letter.isPinned) activeTheme.accentColor else activeTheme.textColor,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Top Floating Diagnostics Glass Pill
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF101018).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    .clickable { showInspector = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activeTheme.accentColor)
                )
                Text(
                    text = "${activeTheme.title.uppercase()} • ${engine.letters.size} GLYPHS • 120 FPS",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Kinetic Info",
                    tint = activeTheme.accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Drag letters to stretch Hooke spring • Double-tap shockwave",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Glassmorphic Control HUD
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0C0B14).copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.14f)
            ),
            shadowElevation = 20.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Word Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val words = listOf("KINETIC", "MOMENTUM", "ELASTIC", "GRAVITY", "PULSE")
                    words.forEach { word ->
                        val isSelected = engine.currentWord == word
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) activeTheme.accentColor
                                    else Color.White.copy(alpha = 0.07f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) activeTheme.accentColor
                                    else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    engine.rebuildWord(word)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = word,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Motion Mode Toggles & Shockwave Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MotionMode.entries.forEach { mode ->
                        val isSelected = engine.motionMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) activeTheme.accentColor.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) activeTheme.accentColor
                                    else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { engine.motionMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) activeTheme.accentColor else Color.White.copy(alpha = 0.6f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Shockwave Pulse Trigger Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeTheme.accentColor)
                            .clickable {
                                shockwaveCenter = Offset(engine.width * 0.5f, engine.height * 0.45f)
                                shockwaveRadius = 20f
                                shockwaveAlpha = 0.95f
                                engine.triggerShockwave()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Pulse",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "PULSE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Theme Presets Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypographyTheme.entries.forEach { theme ->
                        val isSelected = engine.activeTheme == theme
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.04f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) theme.accentColor
                                    else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { engine.activeTheme = theme }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentColor)
                            )
                            Text(
                                text = theme.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Technical Physics Inspector Modal BottomSheet
        if (showInspector) {
            ModalBottomSheet(
                onDismissRequest = { showInspector = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0F0E17),
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KINETIC PHYSICS ENGINE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = activeTheme.accentColor
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Springs",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    engine.stiffness = 180f
                                    engine.damping = 12f
                                    engine.rebuildWord(engine.currentWord)
                                }
                        )
                    }

                    Text(
                        text = "Real-time Hooke spring-mass lattice with neighbor coupling, dynamic velocity squash & stretch (ΔW × ΔH = const), and chromatic trail aberration.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        lineHeight = 17.sp
                    )

                    // Spring Stiffness Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spring Stiffness (k)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${engine.stiffness.roundToInt()} N/m",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = activeTheme.accentColor
                            )
                        }
                        Slider(
                            value = engine.stiffness,
                            onValueChange = { engine.stiffness = it },
                            valueRange = 50f..400f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeTheme.accentColor,
                                activeTrackColor = activeTheme.accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    // Friction Damping Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Friction Damping (c)",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${engine.damping.roundToInt()} N·s/m",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = activeTheme.accentColor
                            )
                        }
                        Slider(
                            value = engine.damping,
                            onValueChange = { engine.damping = it },
                            valueRange = 3f..35f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeTheme.accentColor,
                                activeTrackColor = activeTheme.accentColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
