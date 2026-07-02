package com.tsynytsyna.animations.rift_toggle

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_COUNT = 100
private const val CIRCLE_SEGMENTS = 64
private const val TWO_PI = 2f * PI.toFloat()


private class RiftParticle(
    val angle: Float,
    val distance: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val trailLength: Int,
)


@Composable
fun RiftToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    riftColor: Color = Color(0xFFFF1744),
    durationMs: Int = 2200,
    shakeStrength: Float = 8f,
    lightningEnabled: Boolean = true,
    lightContent: @Composable () -> Unit,
    darkContent: @Composable () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var animating by remember { mutableStateOf(false) }
    var showingReverse by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val particles = remember { buildRiftParticles() }

    val timeState = remember { mutableFloatStateOf(0f) }
    val progressState = remember { mutableFloatStateOf(0f) }

    val mainPath = remember { Path() }
    val innerPath = remember { Path() }
    val clipPathObj = remember { Path() }
    val boltPath = remember { Path() }
    val branchPath = remember { Path() }
    val subBranchPath = remember { Path() }

    val frameColor = if (isDark) Color.Black else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .offset {
                calculateShake(
                    animating = animating,
                    progress = progressState.floatValue,
                    time = timeState.floatValue,
                    strength = shakeStrength
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (animating) return@detectTapGestures
                    tapOffset = offset
                    scope.launch {
                        animating = true
                        showingReverse = true
                        val start = withFrameNanos { it }
                        while (true) {
                            val elapsedMs = (withFrameNanos { it } - start) / 1_000_000f
                            val raw = (elapsedMs / durationMs).coerceAtMost(1f)
                            progressState.floatValue = FastOutSlowInEasing.transform(raw)
                            timeState.floatValue = elapsedMs / 2000f * TWO_PI
                            if (raw >= 1f) break
                        }
                        onToggle()
                        showingReverse = false
                        progressState.floatValue = 0f
                        timeState.floatValue = 0f
                        animating = false
                    }
                }
            }
    ) {
        if (isDark) darkContent() else lightContent()

        if (showingReverse) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val progress = progressState.floatValue
                        val time = timeState.floatValue
                        val currentRadius = maxReveal(tapOffset, size) * progress
                        fillWobblyPath(clipPathObj, tapOffset, currentRadius, time, 1f)
                        clipPath(clipPathObj) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                if (isDark) lightContent() else darkContent()
            }

            Canvas(Modifier.fillMaxSize()) {
                val progress = progressState.floatValue
                val time = timeState.floatValue
                val currentRadius = maxReveal(tapOffset, size) * progress
                drawDarkness(progress)
                drawGlow(tapOffset, currentRadius, progress, time, riftColor)
                drawEdgeRings(
                    tapOffset,
                    currentRadius,
                    progress,
                    time,
                    riftColor,
                    mainPath,
                    innerPath
                )
                drawParticles(tapOffset, currentRadius, progress, time, particles)
                if (lightningEnabled) {
                    drawLightning(
                        tapOffset,
                        currentRadius,
                        progress,
                        time,
                        riftColor,
                        boltPath,
                        branchPath,
                        subBranchPath
                    )
                }
                drawRiftFrame(progress, time, frameColor, shakeStrength)
            }
        }
    }
}


private fun maxReveal(center: Offset, size: IntSize): Float = hypot(
    maxOf(center.x, size.width - center.x),
    maxOf(center.y, size.height - center.y)
)


private fun calculateShake(
    animating: Boolean,
    progress: Float,
    time: Float,
    strength: Float
): IntOffset {
    if (!animating) return IntOffset.Zero
    val intensity = when {
        progress < 0.3f -> progress * 3f
        progress < 0.7f -> 1f
        else -> (1f - progress) * 3f + 0.5f
    }
    val x = (sin(time * 15f) * strength * intensity).toInt()
    val y = (cos(time * 12f) * strength * 0.75f * intensity).toInt()
    return IntOffset(x, y)
}


private fun DrawScope.drawDarkness(progress: Float) {
    if (progress < 0.85f) {
        drawRect(color = Color.Black.copy(alpha = 0.7f * (1f - progress)))
    }
}


private fun DrawScope.drawGlow(
    center: Offset,
    radius: Float,
    progress: Float,
    time: Float,
    color: Color
) {
    val flicker = 0.7f + sin(time * 8f) * 0.3f
    val colorMid =
        color.copy(red = color.red * 0.8f, green = color.green * 0.6f, blue = color.blue * 0.6f)
    val colorDark =
        color.copy(red = color.red * 0.3f, green = color.green * 0.1f, blue = color.blue * 0.1f)

    val glowRadius = (radius * 1.2f).coerceAtLeast(0.1f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.0f),
                color.copy(alpha = 0.9f * flicker * (1f - progress * 0.6f)),
                colorMid.copy(alpha = 0.6f * flicker * (1f - progress * 0.6f)),
                colorDark.copy(alpha = 0.3f * (1f - progress)),
                Color.Transparent,
            ),
            center = center,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = center
    )

    val outerGlow = (radius * 1.6f).coerceAtLeast(0.1f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Transparent,
                colorMid.copy(alpha = 0.3f * flicker * (1f - progress)),
                colorDark.copy(alpha = 0.15f * (1f - progress)),
                Color.Transparent,
            ),
            center = center,
            radius = outerGlow
        ),
        radius = outerGlow,
        center = center
    )
}


private fun DrawScope.drawEdgeRings(
    center: Offset, radius: Float, progress: Float, time: Float,
    color: Color, mainPath: Path, innerPath: Path,
) {
    val flicker = 0.7f + sin(time * 8f) * 0.3f

    fillWobblyPath(mainPath, center, radius, time, 1f)
    drawPath(
        path = mainPath,
        color = color.copy(alpha = flicker * (1f - progress * 0.4f)),
        style = Stroke(width = 6f)
    )

    fillWobblyPath(innerPath, center, radius * 0.94f, time * 1.3f + 1f, 1.5f)
    drawPath(
        path = innerPath,
        color = color.copy(alpha = 0.6f * flicker * (1f - progress)),
        style = Stroke(width = 3f)
    )

    fillWobblyPath(innerPath, center, radius * 1.06f, time * 1.8f + 0.5f, 0.8f)
    drawPath(
        path = innerPath,
        color = color.copy(alpha = 0.5f * flicker * (1f - progress)),
        style = Stroke(width = 2.5f)
    )
}


private fun DrawScope.drawParticles(
    center: Offset, radius: Float, progress: Float, time: Float,
    particles: List<RiftParticle>,
) {
    if (progress >= 0.9f) return
    val invProgress = 1f - progress

    for (i in particles.indices) {
        val p = particles[i]
        val particleRadius = radius * (1.4f + p.distance * 0.6f)
        val rawProgress = ((time * p.speed + p.angle) % TWO_PI) / TWO_PI
        val currentDist = particleRadius * (1f - rawProgress)

        if (currentDist <= radius * 0.1f) continue

        val baseAngle = p.angle + time * 0.5f
        val baseAlpha = (1f - rawProgress) * invProgress * 0.7f
        val maxTrail = minOf(p.trailLength, 3)

        for (t in 0 until maxTrail) {
            val trailP = rawProgress - t * 0.04f
            if (trailP < 0f) break
            val tDist = particleRadius * (1f - trailP)
            val spiralAngle = baseAngle + trailP * 2f
            val px = center.x + cos(spiralAngle) * tDist
            val py = center.y + sin(spiralAngle) * tDist
            val tAlpha = baseAlpha * (1f - t * 0.35f)

            drawCircle(
                color = p.color,
                radius = p.size * (1f - t * 0.25f),
                center = Offset(px, py),
                alpha = tAlpha
            )
        }
    }
}


private fun DrawScope.drawLightning(
    center: Offset, radius: Float, progress: Float, time: Float,
    color: Color, boltPath: Path, branchPath: Path, subBranchPath: Path,
) {
    if (progress >= 0.75f) return
    val invProgress = 1f - progress

    for (i in 0 until 12) {
        val boltFlicker = sin(time * 7f + i * 2.1f)
        if (boltFlicker < -0.2f) continue

        val boltAngle = (i / 12f) * TWO_PI + time * 0.6f
        val startR = radius * 0.99f
        var bx = center.x + cos(boltAngle) * startR
        var by = center.y + sin(boltAngle) * startR

        boltPath.reset()
        boltPath.moveTo(bx, by)
        val segments = 16 + (sin(time + i.toFloat()) * 5).toInt()
        for (seg in 1..segments) {
            val segAngle = boltAngle + sin(time * 3f + seg * 1.7f + i * 0.9f) * 1.2f
            val segLen = 50f + sin(time * 4f + seg * 2.3f) * 25f
            bx += cos(segAngle) * segLen
            by += sin(segAngle) * segLen
            boltPath.lineTo(bx, by)
        }
        drawPath(
            path = boltPath,
            color = color
                .copy(alpha = (0.9f * invProgress * (0.5f + boltFlicker * 0.5f)).coerceIn(0f, 1f)),
            style = Stroke(width = 4.5f)
        )

        if (boltFlicker > 0.1f) {
            drawBranch(
                center = center,
                boltAngle = boltAngle,
                startR = startR,
                time = time,
                invProgress = invProgress,
                color = color,
                branchPath = branchPath,
                subBranchPath = subBranchPath,
                flicker = boltFlicker
            )
        }
    }
}

private fun DrawScope.drawBranch(
    center: Offset,
    boltAngle: Float,
    startR: Float,
    time: Float,
    invProgress: Float,
    color: Color,
    branchPath: Path,
    subBranchPath: Path,
    flicker: Float,
) {
    val midX = center.x + cos(boltAngle) * startR + cos(boltAngle + 0.3f) * 80f
    val midY = center.y + sin(boltAngle) * startR + sin(boltAngle + 0.3f) * 80f

    branchPath.reset()
    branchPath.moveTo(midX, midY)
    var bbx = midX
    var bby = midY
    for (bs in 1..8) {
        val ba = boltAngle + sin(time * 5f + bs) * 1.4f + 0.5f
        val bLen = 35f + sin(time * 3f + bs * 1.5f) * 18f
        bbx += cos(ba) * bLen
        bby += sin(ba) * bLen
        branchPath.lineTo(bbx, bby)
    }
    drawPath(
        path = branchPath,
        color = color.copy(alpha = 0.7f * invProgress),
        style = Stroke(width = 3f)
    )

    if (flicker > 0.3f) {
        subBranchPath.reset()
        subBranchPath.moveTo(bbx, bby)
        var sbx = bbx
        var sby = bby
        for (sb in 1..6) {
            val sa = boltAngle + sin(time * 6f + sb * 2f) * 1.5f + 1f
            sbx += cos(sa) * 30f
            sby += sin(sa) * 30f
            subBranchPath.lineTo(sbx, sby)
        }
        drawPath(
            path = subBranchPath,
            color = color.copy(alpha = 0.5f * invProgress),
            style = Stroke(width = 2f)
        )
    }
}


private fun DrawScope.drawRiftFrame(
    progress: Float,
    time: Float,
    color: Color,
    shakeStrength: Float,
) {
    val intensity = when {
        progress < 0.3f -> progress / 0.3f
        progress < 0.7f -> 1f
        else -> ((1f - progress) / 0.3f)
    }.coerceIn(0f, 1f)
    if (intensity <= 0f) return

    val flicker = 0.6f + sin(time * 9f) * 0.4f

    val jx = sin(time * 23f) * shakeStrength * 0.6f * intensity
    val jy = cos(time * 19f) * shakeStrength * 0.6f * intensity

    val inset = 14f
    val topLeft = Offset(inset + jx, inset + jy)
    val rectSize = Size(size.width - inset * 2f, size.height - inset * 2f)
    val corner = CornerRadius(28f, 28f)

    val highlight = lerp(color, Color.White, 0.6f)

    drawRoundRect(
        color = color.copy(alpha = 0.22f * intensity * flicker),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = corner,
        style = Stroke(width = 30f)
    )
    drawRoundRect(
        color = color.copy(alpha = 0.9f * intensity * flicker),
        topLeft = topLeft,
        size = rectSize,
        cornerRadius = corner,
        style = Stroke(width = 6f)
    )
    drawRoundRect(
        color = highlight.copy(alpha = 0.5f * intensity * flicker),
        topLeft = Offset(topLeft.x + 4f, topLeft.y + 4f),
        size = Size(rectSize.width - 8f, rectSize.height - 8f),
        cornerRadius = corner,
        style = Stroke(width = 2f)
    )
}


private fun fillWobblyPath(
    path: Path,
    center: Offset,
    radius: Float,
    time: Float,
    intensity: Float
) {
    path.reset()
    if (radius <= 0f) return
    for (i in 0..CIRCLE_SEGMENTS) {
        val angle = (i.toFloat() / CIRCLE_SEGMENTS) * TWO_PI
        val wobble = 1f +
                (sin(angle * 7f + time * 1.2f) * 0.03f +
                        cos(angle * 5f - time * 1.9f) * 0.025f +
                        sin(angle * 11f + time * 3f) * 0.015f) * intensity
        val r = radius * wobble
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}


private fun buildRiftParticles(): List<RiftParticle> {
    val colors = listOf(
        Color(0xFFFF1744),
        Color(0xFFD50000),
        Color(0xFFFF5252),
        Color(0xFFFF8A80),
        Color(0xFFFFFFFF),
        Color(0xFFFF6E40),
        Color(0xFFFF3D00),
    )
    return List(PARTICLE_COUNT) {
        val angle = Random.nextFloat() * TWO_PI
        RiftParticle(
            angle = angle,
            distance = Random.nextFloat(),
            speed = 0.15f + Random.nextFloat() * 0.6f,
            size = 1.5f + Random.nextFloat() * 4f,
            color = colors.random(),
            trailLength = 3 + Random.nextInt(3),
        )
    }
}
