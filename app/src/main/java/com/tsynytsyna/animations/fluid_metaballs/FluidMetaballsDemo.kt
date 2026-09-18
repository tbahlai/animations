package com.tsynytsyna.animations.fluid_metaballs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF07090E)
private val CardDark = Color(0xFF0F1420).copy(alpha = 0.85f)
private val BorderStroke = Color(0xFF263238).copy(alpha = 0.65f)

@Composable
fun FluidMetaballsDemo(modifier: Modifier = Modifier) {
    val engine = remember { MetaballEngine() }
    var draggingDropletId by remember { mutableStateOf<Long?>(null) }
    val velocityTracker = remember { VelocityTracker() }

    var tick by remember { mutableIntStateOf(0) }
    var canvasWidth by remember { mutableStateOf(1080f) }
    var canvasHeight by remember { mutableStateOf(2400f) }

    // Physical simulation loop running at 60/120 FPS
    LaunchedEffect(canvasWidth, canvasHeight) {
        if (canvasWidth > 100f && canvasHeight > 100f && engine.droplets.isEmpty()) {
            engine.initialize(canvasWidth, canvasHeight)
        }
        var previousFrameTime = withFrameNanos { it }
        while (true) {
            val frameTime = withFrameNanos { it }
            val dt = ((frameTime - previousFrameTime) / 1e9f).coerceIn(0.001f, 0.033f)
            previousFrameTime = frameTime

            engine.step(dt, canvasWidth, canvasHeight)
            tick++
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(300f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(600f)

        if (canvasWidth != widthPx || canvasHeight != heightPx) {
            canvasWidth = widthPx
            canvasHeight = heightPx
        }

        // 1. Interactive Fluid Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val grabbed = engine.grabDroplet(offset.x, offset.y)
                            if (grabbed != null) {
                                val d = engine.droplets.find { it.id == grabbed }
                                d?.triggerSnapWobble(0f, 1f, 0.9f)
                                d?.isDragging = false
                            } else {
                                engine.spawnDroplet(offset.x, offset.y)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            velocityTracker.resetTracking()
                            val id = engine.grabDroplet(offset.x, offset.y)
                            draggingDropletId = id
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            val id = draggingDropletId
                            if (id != null) {
                                engine.dragDroplet(
                                    id = id,
                                    x = change.position.x,
                                    y = change.position.y,
                                    vx = dragAmount.x * 60f,
                                    vy = dragAmount.y * 60f
                                )
                            }
                        },
                        onDragEnd = {
                            val id = draggingDropletId
                            if (id != null) {
                                val velocity = velocityTracker.calculateVelocity()
                                engine.releaseDroplet(id, velocity.x * 0.75f, velocity.y * 0.75f)
                            }
                            draggingDropletId = null
                        },
                        onDragCancel = {
                            val id = draggingDropletId
                            if (id != null) {
                                engine.releaseDroplet(id, 0f, 0f)
                            }
                            draggingDropletId = null
                        }
                    )
                }
        ) {
            // Subtle chamber grid backdrop
            val gridSize = 60.dp.toPx()
            val cols = (size.width / gridSize).toInt() + 1
            val rows = (size.height / gridSize).toInt() + 1

            for (c in 0..cols) {
                val x = c * gridSize
                drawLine(
                    color = Color.White.copy(alpha = 0.025f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            for (r in 0..rows) {
                val y = r * gridSize
                drawLine(
                    color = Color.White.copy(alpha = 0.025f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // High-Performance Spline & Specular Metaball Renderer
            @Suppress("UNUSED_EXPRESSION") tick
            MetaballSplineRenderer.drawFluidSystem(
                scope = this,
                engine = engine,
                theme = engine.theme,
                surfaceTension = engine.surfaceTension
            )
        }

        // 2. Top Header & Diagnostics Glass Pill
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(CardDark)
                    .border(1.dp, BorderStroke, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(engine.theme.primaryColorHex))
                )
                Text(
                    text = "${engine.theme.title.uppercase()} • ${engine.droplets.size} ORBS • ${engine.activeConnectionsCount} BRIDGES",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap empty space to spawn drops • Drag & pull to stretch & snap",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 3. Floating Bottom Fluid Controls Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Theme Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                FluidTheme.values().forEach { themeItem ->
                    val isSelected = engine.theme == themeItem
                    val themeColor = Color(themeItem.primaryColorHex)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (isSelected) themeColor.copy(alpha = 0.22f)
                                else CardDark
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) themeColor else BorderStroke,
                                shape = RoundedCornerShape(100.dp)
                            )
                            .clickable { engine.theme = themeItem }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                        )
                        Text(
                            text = themeItem.title,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Action Buttons Row: Gravity Toggle, Spawn, Clear
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardDark)
                    .border(1.dp, BorderStroke, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gravity Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (engine.gravityEnabled) Color(0xFFFF5722).copy(alpha = 0.25f)
                            else Color(0xFF00E5FF).copy(alpha = 0.20f)
                        )
                        .border(
                            1.dp,
                            if (engine.gravityEnabled) Color(0xFFFF5722) else Color(0xFF00E5FF),
                            RoundedCornerShape(100.dp)
                        )
                        .clickable { engine.gravityEnabled = !engine.gravityEnabled }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (engine.gravityEnabled) "⚡ 1G GRAVITY" else "🌌 0G FLOAT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Add Droplet Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .clickable {
                            engine.spawnDroplet(
                                canvasWidth * 0.5f + (kotlin.random.Random.nextFloat() - 0.5f) * 160f,
                                canvasHeight * 0.4f
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ DROP",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Reset Chamber Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(100.dp))
                        .clickable { engine.initialize(canvasWidth, canvasHeight) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "RESET",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
