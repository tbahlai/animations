package com.tsynytsyna.animations.boids_swarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Phase 3: Magnetic Boids Particle Swarm
 * Craig Reynolds 1987 flocking algorithms (Separation, Alignment, Cohesion)
 * accelerated with O(N) spatial partitioning grid hashing and magnetic touch dynamics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoidsSwarmDemo(
    modifier: Modifier = Modifier
) {
    // Current visual theme and interaction mode
    var currentTheme by remember { mutableStateOf(BoidTheme.STARLING_DUSK) }
    var interactionMode by remember { mutableStateOf(BoidInteractionMode.ATTRACT) }
    var activeBoidCount by remember { mutableIntStateOf(1400) }
    var showInspector by remember { mutableStateOf(false) }

    // Reynolds steering weights
    var separationWeight by remember { mutableFloatStateOf(1.6f) }
    var alignmentWeight by remember { mutableFloatStateOf(1.1f) }
    var cohesionWeight by remember { mutableFloatStateOf(1.0f) }

    // Simulation engine instance
    val engine = remember {
        BoidsEngine(
            maxBoids = 2500,
            config = BoidsConfig(activeBoidsCount = activeBoidCount)
        )
    }

    // Sync UI parameters with engine
    LaunchedEffect(activeBoidCount, separationWeight, alignmentWeight, cohesionWeight, interactionMode) {
        engine.config = engine.config.copy(
            activeBoidsCount = activeBoidCount,
            separationWeight = separationWeight,
            alignmentWeight = alignmentWeight,
            cohesionWeight = cohesionWeight
        )
        engine.interactionMode = interactionMode
    }

    // Continuous 120 FPS render loop
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastNano = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastNano) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                lastNano = now
                engine.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
    ) {
        // High-Performance Interactive Canvas Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    engine.resize(size.width.toFloat(), size.height.toFloat())
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                if (change.pressed) {
                                    engine.isTouchActive = true
                                    engine.touchX = change.position.x
                                    engine.touchY = change.position.y
                                    change.consume()
                                } else {
                                    engine.isTouchActive = false
                                }
                            } else {
                                engine.isTouchActive = false
                            }
                        }
                    }
                }
        ) {
            // Read frameTick to trigger recomposition every frame
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            val boidCount = engine.config.activeBoidsCount.coerceAtMost(engine.maxBoids)
            val pColor = currentTheme.primaryParticleColor
            val sColor = currentTheme.secondaryParticleColor
            val glowColor = currentTheme.accentGlowColor

            // 1. Render Active Magnetic Well Ring
            if (engine.isTouchActive && interactionMode != BoidInteractionMode.FREE_FLOCK) {
                val pulse = (sin(tick * 0.00000001f) * 8f).toFloat()
                val radius = when (interactionMode) {
                    BoidInteractionMode.ATTRACT -> 45f + pulse
                    BoidInteractionMode.VORTEX -> 65f + pulse
                    BoidInteractionMode.REPULSE -> 85f + pulse
                    else -> 40f
                }

                drawCircle(
                    color = glowColor.copy(alpha = 0.25f),
                    radius = radius,
                    center = Offset(engine.touchX, engine.touchY),
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = glowColor.copy(alpha = 0.6f),
                    radius = 8f,
                    center = Offset(engine.touchX, engine.touchY)
                )
            }

            // 2. Render Shockwave Pulse if active
            if (engine.repulseRadius > 0f) {
                drawCircle(
                    color = glowColor.copy(alpha = (engine.repulseRadius / 400f).coerceIn(0f, 0.6f)),
                    radius = 400f - engine.repulseRadius,
                    center = Offset(engine.touchX, engine.touchY),
                    style = Stroke(width = 3.5f)
                )
                engine.repulseRadius -= 8f
            }

            // 3. Render 1,500 - 2,500 Boids with velocity alignment & trailing lines
            for (i in 0 until boidCount) {
                val px = engine.posX[i]
                val py = engine.posY[i]
                val tx = engine.tailX[i]
                val ty = engine.tailY[i]
                val cVar = engine.colorVariation[i]
                val sVar = engine.sizeVariation[i]

                val boidColor = lerp(pColor, sColor, cVar)

                // Aerodynamic Motion Tail Line
                drawLine(
                    color = boidColor.copy(alpha = 0.75f),
                    start = Offset(tx, ty),
                    end = Offset(px, py),
                    strokeWidth = 2.2f * sVar,
                    cap = StrokeCap.Round
                )

                // Leading Particle Head
                drawCircle(
                    color = glowColor.copy(alpha = 0.9f),
                    radius = 1.8f * sVar,
                    center = Offset(px, py)
                )
            }
        }

        // Top Glassmorphic Telemetry & Mode HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC121620),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(currentTheme.accentGlowColor, CircleShape)
                    )

                    Text(
                        text = "${engine.config.activeBoidsCount} BOIDS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "•",
                        color = Color(0x66FFFFFF),
                        fontSize = 12.sp
                    )

                    Text(
                        text = interactionMode.label.uppercase(),
                        color = currentTheme.accentGlowColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0x33FFFFFF), CircleShape)
                            .clip(CircleShape)
                            .clickable { showInspector = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Technical Inspector",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Theme Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoidTheme.values().forEach { theme ->
                    val isSelected = currentTheme == theme
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) theme.accentGlowColor else Color(0x99171C26),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.clickable { currentTheme = theme }
                    ) {
                        Text(
                            text = theme.title,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Interaction Mode Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BoidInteractionMode.values().forEach { mode ->
                    val isSelected = interactionMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { interactionMode = mode },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) currentTheme.accentGlowColor.copy(alpha = 0.25f) else Color(0x88121620),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) currentTheme.accentGlowColor else Color(0x22FFFFFF)
                        )
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) currentTheme.accentGlowColor else Color(0xBBFFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 9.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Quick Shockwave Pulse Button
            Button(
                onClick = {
                    engine.triggerRepulseShockwave(engine.width / 2f, engine.height / 2f)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xCC1A1F2C)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.accentGlowColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Text(
                    text = "💥 TRIGGER SHOCKWAVE DISPERSAL",
                    color = currentTheme.accentGlowColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // Technical Inspector Bottom Sheet
    if (showInspector) {
        ModalBottomSheet(
            onDismissRequest = { showInspector = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF10141E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REYNOLDS BOIDS ARCHITECTURE",
                        color = currentTheme.accentGlowColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = {
                            separationWeight = 1.6f
                            alignmentWeight = 1.1f
                            cohesionWeight = 1.0f
                            activeBoidCount = 1400
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Parameters",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Simulates autonomous artificial life flocking (Craig Reynolds, 1987) using separation, alignment, and cohesion steering rules. Neighborhood queries are accelerated from O(N²) to O(N) using uniform spatial grid hashing with zero object allocations.",
                    color = Color(0xBBFFFFFF),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Swarm Density Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SWARM PARTICLES",
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$activeBoidCount / 2500",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = activeBoidCount.toFloat(),
                        onValueChange = { activeBoidCount = it.toInt() },
                        valueRange = 500f..2500f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.accentGlowColor,
                            activeTrackColor = currentTheme.accentGlowColor
                        )
                    )
                }

                // Separation Weight Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SEPARATION (Fs)",
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%.2f".format(separationWeight),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = separationWeight,
                        onValueChange = { separationWeight = it },
                        valueRange = 0.5f..3.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.accentGlowColor,
                            activeTrackColor = currentTheme.accentGlowColor
                        )
                    )
                }

                // Alignment Weight Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ALIGNMENT (Fa)",
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%.2f".format(alignmentWeight),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = alignmentWeight,
                        onValueChange = { alignmentWeight = it },
                        valueRange = 0.2f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.accentGlowColor,
                            activeTrackColor = currentTheme.accentGlowColor
                        )
                    )
                }

                // Cohesion Weight Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "COHESION (Fc)",
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%.2f".format(cohesionWeight),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = cohesionWeight,
                        onValueChange = { cohesionWeight = it },
                        valueRange = 0.2f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = currentTheme.accentGlowColor,
                            activeTrackColor = currentTheme.accentGlowColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
