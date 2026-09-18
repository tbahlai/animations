package com.tsynytsyna.animations.gravitational_lensing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GravitationalLensingDemo(modifier: Modifier = Modifier) {
    val engine = remember { LensingEngine() }

    var tick by remember { mutableIntStateOf(0) }
    var showInspector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 120 FPS continuous physics & rendering step
    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos != 0L) {
                    val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                    engine.step(dt)
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
            .background(Color(0xFF040308))
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    engine.initialize(size.width.toFloat(), size.height.toFloat())
                }
            }
    ) {
        // Celestial Void & Nebula Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeTheme.secondaryColor.copy(alpha = 0.12f),
                            activeTheme.primaryColor.copy(alpha = 0.05f),
                            Color(0xFF040308)
                        ),
                        radius = 1600f
                    )
                )
        )

        // Main Relativistic Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            engine.onDragStart(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            engine.onDrag(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            engine.onDragEnd()
                        },
                        onDragCancel = {
                            engine.onDragEnd()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            engine.triggerGravitationalWave()
                        },
                        onTap = { offset ->
                            val dx = offset.x - engine.singularityX
                            val dy = offset.y - engine.singularityY
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist < engine.currentEinsteinRadius * 1.5f) {
                                engine.triggerGravitationalWave()
                            }
                        }
                    )
                }
        ) {
            // Read tick to trigger redraw
            val unused = tick
            LensingRenderer.render(this, engine)
        }

        // Top Floating Diagnostics Glass Pill
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rsVal = engine.currentSchwarzschildRadius.roundToInt()
            val reVal = engine.currentEinsteinRadius.roundToInt()

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
                        .background(activeTheme.primaryColor)
                )
                Text(
                    text = "${activeTheme.title.uppercase()} • Rs: ${rsVal}px • Re: ${reVal}px • 120 FPS",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Relativistic Info",
                    tint = activeTheme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Drag black hole across space • Double-tap for gravitational wave",
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
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Theme Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LensingTheme.values().forEach { theme ->
                        val isSelected = engine.activeTheme == theme
                        val chipBg = if (isSelected) {
                            theme.primaryColor.copy(alpha = 0.22f)
                        } else {
                            Color.White.copy(alpha = 0.05f)
                        }
                        val chipBorder = if (isSelected) {
                            theme.primaryColor
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                .clickable { engine.activeTheme = theme }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = theme.title,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Singularity Mass Slider & Orbit Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MASS ${String.format("%.1f", engine.massMultiplier)} M☉",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Slider(
                        value = engine.massMultiplier,
                        onValueChange = { engine.massMultiplier = it },
                        valueRange = 0.5f..2.2f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = activeTheme.primaryColor,
                            activeTrackColor = activeTheme.primaryColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Grid Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (engine.showSpacetimeGrid) activeTheme.primaryColor.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (engine.showSpacetimeGrid) activeTheme.primaryColor else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { engine.showSpacetimeGrid = !engine.showSpacetimeGrid }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (engine.showSpacetimeGrid) "GRID: ON" else "GRID: OFF",
                            color = if (engine.showSpacetimeGrid) Color.White else Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Accretion Disk Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (engine.showAccretionDisk) activeTheme.primaryColor.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (engine.showAccretionDisk) activeTheme.primaryColor else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { engine.showAccretionDisk = !engine.showAccretionDisk }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (engine.showAccretionDisk) "DISK: ON" else "DISK: OFF",
                            color = if (engine.showAccretionDisk) Color.White else Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Gravitational Wave Pulse Button
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeTheme.primaryColor)
                            .clickable { engine.triggerGravitationalWave() }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ GRAV-WAVE",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Reset Singularity Button
                    IconButton(
                        onClick = { engine.resetSingularity() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Singularity",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Relativistic Info & Physics Inspector Bottom Sheet
        if (showInspector) {
            ModalBottomSheet(
                onDismissRequest = { showInspector = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0E0C18),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "General Relativistic Lensing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = activeTheme.primaryColor
                    )
                    Text(
                        text = "Astrophysical Physics & Mathematics",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PhysicsInfoCard(
                        title = "1. Schwarzschild Event Horizon (Rs)",
                        formula = "Rs = 2GM / c²",
                        description = "The boundary of no escape where escape velocity equals the speed of light. Inside this radius, all light paths are trapped, rendering the core pitch-black."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PhysicsInfoCard(
                        title = "2. Einstein Ring & Gravitational Deflection",
                        formula = "θ_E = √(4GM / c² · D_ds / D_d D_s)",
                        description = "Light rays from background stars bend around the spacetime curvature funnel. When a source aligns with the singularity, it is magnified and sheared into a brilliant tangential circular arc."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PhysicsInfoCard(
                        title = "3. Relativistic Doppler Beaming",
                        formula = "I_obs = I_emit · δ⁴,  δ = 1 / [γ(1 - β cos θ)]",
                        description = "Plasma in the accretion disk orbits at relativistic fractions of c. The approaching limb (moving toward the viewer) is dramatically blueshifted and beamed brighter, while the receding limb is redshifted and dimmer."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PhysicsInfoCard(
                        title = "4. Gravitational Top Halo (Gargantua Effect)",
                        formula = "Light bending angle α = 4GM / (c² b)",
                        description = "Light emitted from the backside of the accretion disk is curved upward over the top of the event horizon, producing the iconic Interstellar double-ring optical signature."
                    )
                }
            }
        }
    }
}

@Composable
private fun PhysicsInfoCard(
    title: String,
    formula: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formula,
                color = Color(0xFF80D8FF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}
