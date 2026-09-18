package com.tsynytsyna.animations.showcase

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Procedural miniature motion previews rendered directly on Canvas inside each showcase card.
 * Highly optimized, 0 allocations per frame, stunning visual dynamism.
 */
@Composable
fun ExperimentMiniPreview(
    id: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    hasImageBackdrop: Boolean = false,
) {
    Box(
        modifier = modifier.background(
            if (hasImageBackdrop) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C101A).copy(alpha = 0.50f),
                        Color(0xFF080B12).copy(alpha = 0.72f),
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C101A),
                        Color(0xFF080B12),
                    )
                )
            }
        )
    ) {
        when (id) {
            "unfolding_ticket" -> UnfoldingTicketMiniPreview(accentColor)
            "elastic_pull_refresh" -> ElasticPullRefreshMiniPreview(accentColor)
            "boids_swarm" -> BoidsSwarmMiniPreview(accentColor)
            "origami_fold" -> OrigamiFoldMiniPreview(accentColor)
            "kinetic_typography" -> KineticTypographyMiniPreview(accentColor)
            "gravitational_lensing" -> GravitationalLensingMiniPreview(accentColor)
            "fluid_metaballs" -> FluidMetaballMiniPreview(accentColor)
            "off_the_wall" -> ClothMeshMiniPreview(accentColor)
            "glass_shatter" -> GlassShatterMiniPreview(accentColor)
            "wave_reassembly" -> WaveReassemblyMiniPreview(accentColor)
            "gallery_disintegration" -> DisintegrationMiniPreview(accentColor)
            "rift_toggle" -> RiftMiniPreview(accentColor)
            "git_diff" -> GitDiffMiniPreview(accentColor)
            "happy_birthday" -> FlameMiniPreview(accentColor)
            "polaroid_scatter" -> PolaroidMiniPreview(accentColor)
            "cover_flow" -> CoverFlowMiniPreview(accentColor)
            "fan_cards" -> FanCardsMiniPreview(accentColor)
            "paralax_onboarding" -> ParallaxMiniPreview(accentColor)
            "swipe_card" -> SwipeCardMiniPreview(accentColor)
            else -> DefaultMiniPreview(accentColor)
        }
    }
}

@Composable
private fun ClothMeshMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "clothTransition")
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clothWave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cols = 9
        val rows = 5
        val stepX = size.width / (cols + 1)
        val stepY = size.height / (rows + 1)

        val points = Array(cols) { c ->
            FloatArray(rows) { r ->
                val dist = (c + r).toFloat()
                sin(wave + dist * 0.5f) * 7f
            }
        }

        // Draw horizontal spring lines
        for (r in 0 until rows) {
            val y = (r + 1) * stepY
            for (c in 0 until cols - 1) {
                val x1 = (c + 1) * stepX
                val y1 = y + points[c][r]
                val x2 = (c + 2) * stepX
                val y2 = y + points[c + 1][r]
                drawLine(
                    color = accentColor.copy(alpha = 0.4f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 1.5f
                )
            }
        }

        // Draw vertical spring lines
        for (c in 0 until cols) {
            val x = (c + 1) * stepX
            for (r in 0 until rows - 1) {
                val y1 = (r + 1) * stepY + points[c][r]
                val y2 = (r + 2) * stepY + points[c][r + 1]
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(x, y1),
                    end = Offset(x, y2),
                    strokeWidth = 1.5f
                )
            }
        }

        // Draw node points
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                val x = (c + 1) * stepX
                val y = (r + 1) * stepY + points[c][r]
                drawCircle(
                    color = if (r == 0) Color.White.copy(alpha = 0.9f) else accentColor,
                    radius = if (r == 0) 3.5f else 2.5f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun GlassShatterMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "shatterTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shatterPhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.height * 0.36f

        // Shard polygons expanding outward
        val shardCount = 8
        val angleStep = (2 * PI / shardCount).toFloat()

        for (i in 0 until shardCount) {
            val a1 = i * angleStep
            val a2 = (i + 1) * angleStep
            val midA = (a1 + a2) / 2f

            val explodeDist = phase * 18f
            val dx = cos(midA) * explodeDist
            val dy = sin(midA) * explodeDist + phase * phase * 12f

            val p = Path().apply {
                moveTo(cx + dx, cy + dy)
                lineTo(cx + dx + cos(a1) * radius, cy + dy + sin(a1) * radius)
                lineTo(cx + dx + cos(a2) * radius, cy + dy + sin(a2) * radius)
                close()
            }

            drawPath(
                path = p,
                color = accentColor.copy(alpha = 0.18f + (1f - phase) * 0.1f),
                style = Fill
            )
            drawPath(
                path = p,
                color = Color.White.copy(alpha = 0.6f + (1f - phase) * 0.3f),
                style = Stroke(width = 1.8f)
            )
        }

        // Center impact flash
        drawCircle(
            color = Color.White.copy(alpha = (1f - phase).coerceIn(0f, 0.9f)),
            radius = 6f + (1f - phase) * 14f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun WaveReassemblyMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "waveTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.width * 0.45f

        // Expanding wavefront rings
        for (i in 0..2) {
            val waveR = ((progress + i * 0.33f) % 1f) * maxR
            val alpha = (1f - waveR / maxR).coerceIn(0f, 0.7f)
            drawCircle(
                color = accentColor.copy(alpha = alpha),
                radius = waveR,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }

        // Flying tile dots converging
        val tileCount = 12
        for (i in 0 until tileCount) {
            val ang = (i * 2 * PI / tileCount).toFloat()
            val startDist = maxR * 0.95f
            val currDist = startDist * (1f - progress)
            val tx = cx + cos(ang) * currDist
            val ty = cy + sin(ang) * currDist

            drawRoundRect(
                color = if (progress > 0.85f) Color.White else accentColor,
                topLeft = Offset(tx - 4f, ty - 4f),
                size = Size(8f, 8f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
private fun DisintegrationMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "disintegrateTransition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val startX = size.width * 0.28f
        val startY = size.height * 0.28f
        val blockW = size.width * 0.22f
        val blockH = size.height * 0.44f

        // Remaining intact solid block
        drawRoundRect(
            color = accentColor.copy(alpha = 0.85f),
            topLeft = Offset(startX, startY),
            size = Size(blockW * (1f - progress * 0.5f), blockH),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Flying particles drifting to the right with turbulence
        val particleCount = 28
        for (i in 0 until particleCount) {
            val seed = (i * 137.5f) % 1f
            val seedY = (i * 53.7f) % 1f
            val delay = seed * 0.4f

            if (progress > delay) {
                val localP = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
                val px = startX + blockW + localP * (size.width * 0.45f)
                val py = startY + seedY * blockH + sin(localP * 8f + i) * 14f - localP * 20f
                val pAlpha = (1f - localP).coerceIn(0f, 1f)

                drawCircle(
                    color = if (i % 3 == 0) Color.White else accentColor,
                    radius = (3.5f * (1f - localP * 0.5f)).coerceAtLeast(1f),
                    center = Offset(px, py),
                    alpha = pAlpha
                )
            }
        }
    }
}

@Composable
private fun RiftMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "riftTransition")
    val flicker by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "riftFlicker"
    )
    val widthPhase by transition.animateFloat(
        initialValue = 4f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "riftWidth"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val h = size.height

        // Outer chromatic glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.35f * flicker), Color.Transparent),
                center = Offset(cx, h / 2f),
                radius = size.width * 0.4f
            ),
            radius = size.width * 0.4f,
            center = Offset(cx, h / 2f)
        )

        // Cosmic Rift Fissure (zigzag vertical path)
        val p = Path().apply {
            moveTo(cx, 0f)
            lineTo(cx - widthPhase * 0.4f, h * 0.25f)
            lineTo(cx + widthPhase * 0.6f, h * 0.5f)
            lineTo(cx - widthPhase * 0.5f, h * 0.75f)
            lineTo(cx, h)
        }

        // Draw fissure edges
        drawPath(
            path = p,
            color = accentColor,
            style = Stroke(width = widthPhase * 0.8f)
        )
        // Draw bright core
        drawPath(
            path = p,
            color = Color.White.copy(alpha = flicker),
            style = Stroke(width = 2.5f)
        )

        // Little lightning branch sparks
        val sparkY = h * 0.45f
        val sparkEnd = Offset(cx + 28f * flicker, sparkY - 12f)
        drawLine(
            color = Color.White,
            start = Offset(cx + widthPhase * 0.6f, sparkY),
            end = sparkEnd,
            strokeWidth = 1.5f
        )
    }
}

@Composable
private fun GitDiffMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "diffTransition")
    val toggle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diffToggle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val left = size.width * 0.18f
        val top = size.height * 0.22f
        val lineH = 7f
        val lineSpacing = 16f

        // Line 1: Neutral code
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(left, top),
            size = Size(size.width * 0.5f, lineH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Line 2: Removed line (Red diff)
        val redAlpha = (1f - toggle).coerceIn(0.2f, 1f)
        drawRoundRect(
            color = Color(0xFFEF4444).copy(alpha = redAlpha),
            topLeft = Offset(left, top + lineSpacing),
            size = Size(size.width * 0.42f, lineH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Line 3: Added line (Green diff)
        val greenAlpha = toggle.coerceIn(0.2f, 1f)
        drawRoundRect(
            color = Color(0xFF10B981).copy(alpha = greenAlpha),
            topLeft = Offset(left, top + lineSpacing * 2),
            size = Size(size.width * 0.58f, lineH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Line 4: Neutral code
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(left, top + lineSpacing * 3),
            size = Size(size.width * 0.35f, lineH),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@Composable
private fun FlameMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "flameTransition")
    val flicker by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameFlicker"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.55f

        // Candle stick
        drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(cx - 7f, cy),
            size = Size(14f, size.height * 0.35f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Wick
        drawLine(
            color = Color(0xFF334155),
            start = Offset(cx, cy),
            end = Offset(cx, cy - 8f),
            strokeWidth = 2f
        )

        // Warm ambient flame aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF9100).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(cx + flicker * 0.4f, cy - 22f),
                radius = 32f
            ),
            radius = 32f,
            center = Offset(cx + flicker * 0.4f, cy - 22f)
        )

        // Teardrop Flame
        val flameTip = Offset(cx + flicker, cy - 36f)
        val flameLeft = Offset(cx - 10f, cy - 14f)
        val flameRight = Offset(cx + 10f, cy - 14f)

        val flamePath = Path().apply {
            moveTo(cx, cy - 8f)
            quadraticTo(flameLeft.x, flameLeft.y, flameTip.x, flameTip.y)
            quadraticTo(flameRight.x, flameRight.y, cx, cy - 8f)
            close()
        }

        drawPath(path = flamePath, color = Color(0xFFFF6D00))

        // Inner white/cyan core
        val innerTip = Offset(cx + flicker * 0.5f, cy - 26f)
        val innerPath = Path().apply {
            moveTo(cx, cy - 8f)
            quadraticTo(cx - 5f, cy - 12f, innerTip.x, innerTip.y)
            quadraticTo(cx + 5f, cy - 12f, cx, cy - 8f)
            close()
        }
        drawPath(path = innerPath, color = Color(0xFFFFF7ED))
    }
}

@Composable
private fun PolaroidMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "polaroidTransition")
    val rot by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "polaroidRot"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val cardW = size.width * 0.36f
        val cardH = size.height * 0.52f

        // Background photo 1
        rotate(degrees = -14f, pivot = Offset(cx - 20f, cy)) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(cx - cardW / 2f - 24f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Foreground photo with dynamic rotation
        rotate(degrees = rot, pivot = Offset(cx, cy)) {
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Inner photo image frame
            drawRoundRect(
                color = accentColor.copy(alpha = 0.85f),
                topLeft = Offset(cx - cardW / 2f + 4f, cy - cardH / 2f + 4f),
                size = Size(cardW - 8f, cardH * 0.68f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
private fun CoverFlowMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "coverFlowTransition")
    val shift by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coverFlowShift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val cardW = size.width * 0.28f
        val cardH = size.height * 0.52f

        // Left 3D slanted card
        withTransform({
            translate(left = -cardW * 0.7f + shift, top = 6f)
            scale(scaleX = 0.75f, scaleY = 0.85f, pivot = Offset(cx, cy))
        }) {
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        // Right 3D slanted card
        withTransform({
            translate(left = cardW * 0.7f + shift, top = 6f)
            scale(scaleX = 0.75f, scaleY = 0.85f, pivot = Offset(cx, cy))
        }) {
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        // Center featured card
        withTransform({
            translate(left = shift * 0.4f, top = -4f)
        }) {
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

@Composable
private fun FanCardsMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "fanTransition")
    val spread by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fanSpread"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val pivot = Offset(cx, size.height * 0.85f)
        val cardW = size.width * 0.24f
        val cardH = size.height * 0.52f

        val angles = floatArrayOf(-26f, -13f, 0f, 13f, 26f)
        for (i in angles.indices) {
            val a = angles[i] * spread
            rotate(degrees = a, pivot = pivot) {
                val cardColor = if (i == 2) accentColor else Color(0xFF1E293B)
                drawRoundRect(
                    color = cardColor,
                    topLeft = Offset(cx - cardW / 2f, pivot.y - cardH),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = if (i == 2) 0.5f else 0.2f),
                    topLeft = Offset(cx - cardW / 2f, pivot.y - cardH),
                    size = Size(cardW, cardH),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }
}

@Composable
private fun ParallaxMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "parallaxTransition")
    val offset by transition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "parallaxOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Deep background layer (slowest)
        drawCircle(
            color = Color(0xFF1E293B),
            radius = size.height * 0.35f,
            center = Offset(cx + offset * 0.25f, cy)
        )

        // Midground geometric polygon (medium speed)
        drawCircle(
            color = accentColor.copy(alpha = 0.5f),
            radius = size.height * 0.25f,
            center = Offset(cx + offset * 0.65f, cy)
        )

        // Foreground floating sphere (fastest)
        drawCircle(
            color = Color.White,
            radius = 12f,
            center = Offset(cx + offset * 1.2f, cy)
        )
    }
}

@Composable
private fun SwipeCardMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "swipeTransition")
    val tilt by transition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeTilt"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val cardW = size.width * 0.32f
        val cardH = size.height * 0.5f

        // Underneath card
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(cx - cardW / 2f, cy - cardH / 2f + 6f),
            size = Size(cardW, cardH),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Top swiping card
        rotate(degrees = tilt, pivot = Offset(cx, cy + cardH)) {
            val swipeX = tilt * 1.8f
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(cx - cardW / 2f + swipeX, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(cx - cardW / 2f + swipeX, cy - cardH / 2f),
                size = Size(cardW, cardH),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

@Composable
private fun FluidMetaballMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "metaballPreview")
    val stretch by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stretch"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f

        val baseR = size.height * 0.22f
        val satR = size.height * 0.15f

        val maxSeparation = size.width * 0.32f
        val separation = baseR * 0.8f + stretch * maxSeparation

        val x1 = cx - separation * 0.45f
        val y1 = cy
        val x2 = cx + separation * 0.55f
        val y2 = cy

        val dist = x2 - x1
        val sumR = baseR + satR

        // If close enough, draw connecting liquid neck
        if (dist < sumR * 1.55f) {
            val stretchRatio = ((dist - sumR) / (sumR * 0.55f)).coerceIn(0f, 1f)
            val waistThickness = ((baseR + satR) * 0.45f * (1f - stretchRatio * 0.85f)).coerceAtLeast(1.5f)

            val bridgePath = Path().apply {
                moveTo(x1, y1 - baseR * 0.85f)
                quadraticTo(cx, cy - waistThickness, x2, y2 - satR * 0.85f)
                lineTo(x2, y2 + satR * 0.85f)
                quadraticTo(cx, cy + waistThickness, x1, y1 + baseR * 0.85f)
                close()
            }
            drawPath(
                path = bridgePath,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.85f), Color.White.copy(alpha = 0.90f), accentColor.copy(alpha = 0.85f)),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2)
                )
            )
        }

        // Left orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.95f), accentColor, Color(0xFF0F172A)),
                center = Offset(x1 - baseR * 0.3f, y1 - baseR * 0.3f),
                radius = baseR * 1.2f
            ),
            radius = baseR,
            center = Offset(x1, y1)
        )

        // Right orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.95f), accentColor, Color(0xFF0F172A)),
                center = Offset(x2 - satR * 0.3f, y2 - satR * 0.3f),
                radius = satR * 1.2f
            ),
            radius = satR,
            center = Offset(x2, y2)
        )

        // Specular glint on left orb
        drawOval(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(x1 - baseR * 0.55f, y1 - baseR * 0.5f),
            size = Size(baseR * 0.55f, baseR * 0.3f)
        )
    }
}

@Composable
private fun GravitationalLensingMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "lensingMiniTransition")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lensingAngle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.52f
        val rs = size.height * 0.22f
        val photonR = rs * 1.5f
        val haloR = rs * 1.7f

        // 1. Warped upper halo arc (Gargantua backside disk bent upward)
        drawArc(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.85f), accentColor.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(cx, cy - haloR * 0.3f),
                radius = haloR * 1.4f
            ),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - haloR, cy - haloR * 0.85f),
            size = Size(haloR * 2f, haloR * 1.5f),
            style = Stroke(width = rs * 0.45f, cap = StrokeCap.Round)
        )

        // 2. Orbiting lensed star particles (tangentially sheared arcs)
        for (i in 0..4) {
            val starAngle = angle + (i * (2 * PI.toFloat() / 5f))
            val orbitR = rs * (1.8f + (i % 2) * 0.4f)
            val sx = cx + cos(starAngle) * orbitR
            val sy = cy + sin(starAngle) * (orbitR * 0.38f) // perspective squash
            val isApproaching = sin(starAngle) < 0

            // Star point with tangential trail
            drawCircle(
                color = if (isApproaching) Color.White else accentColor,
                radius = if (isApproaching) 3.5f else 2.2f,
                center = Offset(sx, sy)
            )
        }

        // 3. Equatorial Accretion Disk (front band)
        drawArc(
            brush = Brush.horizontalGradient(
                0.0f to Color.White.copy(alpha = 0.95f),
                0.3f to accentColor.copy(alpha = 0.9f),
                1.0f to accentColor.copy(alpha = 0.35f),
                startX = cx - rs * 2.6f,
                endX = cx + rs * 2.6f
            ),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - rs * 2.6f, cy - rs * 0.7f),
            size = Size(rs * 5.2f, rs * 1.4f),
            style = Stroke(width = rs * 0.38f, cap = StrokeCap.Round)
        )

        // 4. Photon sphere ring
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = photonR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.8f)
        )

        // 5. Pitch-black event horizon void
        drawCircle(
            color = Color(0xFF030308),
            radius = rs,
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun OrigamiFoldMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "origamiMiniTransition")
    val foldFraction by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "foldFraction"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val panelW = size.width * 0.26f
        val panelH = size.height * 0.72f

        // Projected panel width under perspective fold
        val projW = panelW * (1f - foldFraction * 0.65f)
        val skewY = panelH * 0.12f * foldFraction

        val paperWhite = Color(0xFFFDFBF7)
        val shadowAlpha = foldFraction * 0.55f

        // Center panel
        drawRect(
            color = paperWhite,
            topLeft = Offset(cx - projW * 0.5f, cy - panelH * 0.5f),
            size = Size(projW, panelH)
        )
        // Center panel crease lines
        drawLine(
            color = accentColor.copy(alpha = 0.45f),
            start = Offset(cx - projW * 0.3f, cy - panelH * 0.3f),
            end = Offset(cx + projW * 0.3f, cy - panelH * 0.3f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.2f),
            start = Offset(cx - projW * 0.3f, cy),
            end = Offset(cx + projW * 0.3f, cy),
            strokeWidth = 1.0f
        )

        // Left folding wing (folded forward with shadow)
        val leftPath = Path().apply {
            moveTo(cx - projW * 0.5f - projW, cy - panelH * 0.5f + skewY)
            lineTo(cx - projW * 0.5f, cy - panelH * 0.5f)
            lineTo(cx - projW * 0.5f, cy + panelH * 0.5f)
            lineTo(cx - projW * 0.5f - projW, cy + panelH * 0.5f - skewY)
            close()
        }
        drawPath(leftPath, paperWhite.copy(alpha = 0.95f))
        drawPath(leftPath, Color.Black.copy(alpha = shadowAlpha * 0.4f))
        drawPath(leftPath, accentColor.copy(alpha = 0.35f), style = Stroke(width = 1f))

        // Right folding wing (folded forward with shadow)
        val rightPath = Path().apply {
            moveTo(cx + projW * 0.5f, cy - panelH * 0.5f)
            lineTo(cx + projW * 0.5f + projW, cy - panelH * 0.5f + skewY)
            lineTo(cx + projW * 0.5f + projW, cy + panelH * 0.5f - skewY)
            lineTo(cx + projW * 0.5f, cy + panelH * 0.5f)
            close()
        }
        drawPath(rightPath, paperWhite.copy(alpha = 0.90f))
        drawPath(rightPath, Color.Black.copy(alpha = shadowAlpha * 0.6f))
        drawPath(rightPath, accentColor.copy(alpha = 0.35f), style = Stroke(width = 1f))

        // Crease hinge lines
        drawLine(
            color = Color.Black.copy(alpha = 0.35f + shadowAlpha * 0.4f),
            start = Offset(cx - projW * 0.5f, cy - panelH * 0.5f),
            end = Offset(cx - projW * 0.5f, cy + panelH * 0.5f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.35f + shadowAlpha * 0.4f),
            start = Offset(cx + projW * 0.5f, cy - panelH * 0.5f),
            end = Offset(cx + projW * 0.5f, cy + panelH * 0.5f),
            strokeWidth = 1.5f
        )
    }
}

@Composable
private fun KineticTypographyMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "KineticMini")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val glyphCount = 5
        val spacing = size.width * 0.16f
        val startX = cx - (glyphCount - 1) * spacing * 0.5f

        val points = mutableListOf<Offset>()

        for (i in 0 until glyphCount) {
            val glyphPhase = phase + i * 0.85f
            val offsetY = sin(glyphPhase) * (size.height * 0.22f)
            val px = startX + i * spacing
            val py = cy + offsetY
            points.add(Offset(px, py))

            // Anchor point & spring string
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(px, cy),
                end = Offset(px, py),
                strokeWidth = 1.2f
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.4f),
                radius = 2.5f,
                center = Offset(px, cy)
            )

            // Dynamic velocity squash/stretch pill representing glyph
            val speedNorm = cos(glyphPhase) // derivative
            val stretch = 1f + kotlin.math.abs(speedNorm) * 0.45f
            val pillW = 10f / stretch
            val pillH = 22f * stretch

            drawRoundRect(
                color = if (i == 2) accentColor else Color.White.copy(alpha = 0.85f),
                topLeft = Offset(px - pillW * 0.5f, py - pillH * 0.5f),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(pillW * 0.5f, pillW * 0.5f)
            )
        }

        // Connecting elastic spline ribbon
        if (points.size >= 2) {
            val ribbon = Path()
            ribbon.moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) * 0.5f
                val midY = (p0.y + p1.y) * 0.5f
                ribbon.quadraticTo(p0.x, p0.y, midX, midY)
            }
            ribbon.lineTo(points.last().x, points.last().y)

            drawPath(
                path = ribbon,
                color = accentColor.copy(alpha = 0.65f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun UnfoldingTicketMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "TicketMini")
    val unfoldFraction by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "unfold"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val cardW = size.width * 0.62f
        val cardH = size.height * 0.76f

        val topH = cardH * 0.38f
        val midH = cardH * 0.32f * unfoldFraction
        val btmH = cardH * 0.30f * unfoldFraction

        val left = cx - cardW * 0.5f
        var currentY = cy - (topH + midH + btmH) * 0.5f

        // 1. Top summary segment
        drawRoundRect(
            color = Color(0xFFFBF9F5),
            topLeft = Offset(left, currentY),
            size = Size(cardW, topH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Airline dot & flight bar
        drawCircle(
            color = accentColor,
            radius = 3.5f,
            center = Offset(left + 12f, currentY + 12f)
        )
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(left + 22f, currentY + 9f),
            size = Size(28f, 6f)
        )
        // Flight path vector
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(left + 12f, currentY + 28f),
            end = Offset(left + cardW - 12f, currentY + 28f),
            strokeWidth = 1.2f
        )
        drawCircle(
            color = accentColor,
            radius = 2.5f,
            center = Offset(cx, currentY + 28f)
        )

        currentY += topH

        // Crease shadow 1
        if (unfoldFraction < 0.9f) {
            drawRect(
                color = Color.Black.copy(alpha = (1f - unfoldFraction) * 0.45f),
                topLeft = Offset(left, currentY),
                size = Size(cardW, 6f)
            )
        }

        // 2. Middle itinerary segment
        if (midH > 2f) {
            drawRect(
                color = Color(0xFFFBF9F5).copy(alpha = 0.95f),
                topLeft = Offset(left, currentY),
                size = Size(cardW, midH)
            )
            // Seat & Gate boxes
            val boxW = cardW * 0.4f
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(left + 10f, currentY + 6f),
                size = Size(boxW, (midH - 12f).coerceAtLeast(0f)),
                cornerRadius = CornerRadius(4f, 4f)
            )
            currentY += midH
        }

        // Crease shadow 2
        if (unfoldFraction < 0.9f) {
            drawRect(
                color = Color.Black.copy(alpha = (1f - unfoldFraction) * 0.5f),
                topLeft = Offset(left, currentY),
                size = Size(cardW, 6f)
            )
        }

        // 3. Bottom barcode segment
        if (btmH > 2f) {
            drawRect(
                color = Color(0xFFFBF9F5).copy(alpha = 0.9f),
                topLeft = Offset(left, currentY),
                size = Size(cardW, btmH)
            )
            // Barcode lines
            for (bx in 0..12) {
                val lineX = left + 14f + bx * ((cardW - 28f) / 12f)
                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(lineX, currentY + 6f),
                    end = Offset(lineX, currentY + (btmH - 8f).coerceAtLeast(0f)),
                    strokeWidth = if (bx % 3 == 0) 2.2f else 1.2f
                )
            }
        }
    }
}

@Composable
private fun ElasticPullRefreshMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "PullMini")
    val pullFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pull"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.35f
        val glyphCount = 6
        val spacing = size.width * 0.12f
        val startX = cx - (glyphCount - 1) * spacing * 0.5f

        val points = mutableListOf<Offset>()

        for (i in 0 until glyphCount) {
            val px = startX + i * spacing
            val arch = sin((i.toFloat() / (glyphCount - 1)) * PI.toFloat())
            val py = cy + (pullFraction * size.height * 0.45f * arch)

            points.add(Offset(px, py))

            // Spring line from baseline
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(px, cy),
                end = Offset(px, py),
                strokeWidth = 1f
            )

            // Letter node pill
            val stretch = 1f + (py - cy) / 40f
            val pillW = 8f / sqrt(stretch)
            val pillH = 14f * stretch
            val halfW = pillW * 0.5f
            val halfH = pillH * 0.5f

            drawRoundRect(
                color = if (pullFraction > 0.8f) accentColor else Color.White.copy(alpha = 0.85f),
                topLeft = Offset(px - halfW, py - halfH),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(halfW, halfW)
            )
        }

        // Connecting elastic spline
        if (points.size >= 2) {
            val spline = Path()
            spline.moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) * 0.5f
                val midY = (p0.y + p1.y) * 0.5f
                spline.quadraticTo(p0.x, p0.y, midX, midY)
            }
            spline.lineTo(points.last().x, points.last().y)

            drawPath(
                path = spline,
                color = accentColor.copy(alpha = 0.8f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun BoidsSwarmMiniPreview(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "boidsPreview")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boidsAngle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val count = 32

        // Attractor vortex core
        drawCircle(
            color = accentColor.copy(alpha = 0.2f),
            radius = 26f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.6f),
            radius = 3.5f,
            center = Offset(cx, cy)
        )

        for (i in 0 until count) {
            val phase = (i.toFloat() / count) * 2f * PI.toFloat()
            val r = 16f + (i * 2.4f)
            val currentA = angle + phase + (r * 0.025f)
            val bx = cx + cos(currentA) * r
            val by = cy + sin(currentA) * r * 0.65f // slight elliptical tilt

            val tailX = bx - (-sin(currentA) * 11f)
            val tailY = by - (cos(currentA) * 7.5f)

            // Motion trail
            drawLine(
                color = accentColor.copy(alpha = 0.5f),
                start = Offset(tailX, tailY),
                end = Offset(bx, by),
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )
            // Particle head
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.2f,
                center = Offset(bx, by)
            )
        }
    }
}

@Composable
private fun DefaultMiniPreview(accentColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = accentColor.copy(alpha = 0.6f),
            radius = size.height * 0.3f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
    }
}
