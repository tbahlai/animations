package com.tsynytsyna.animations.happy_birthday

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** The two-tier cake, its candles, their flames and the smoke left when they are blown out. */

internal const val CANDLE_COUNT = 5

/** Overall size of the cake, candles and all. */
private const val CAKE_SCALE = 1.18f

/** Puffs released by a wick the moment its flame dies. */
private const val SMOKE_PUFFS = 12

/** Gap between the candle top and the base of the flame. */
private const val FLAME_GAP = 7f

/** Fraction of the way out a flame travels per second once it has been blown. */
private const val DYING_RATE = 4f

/** Wind speed a flame cannot survive. */
internal const val BLOW_OUT_WIND = 240f

/** Wind speed one pixel of horizontal drag stirs up, and the strongest gust a swipe can raise. */
private const val WIND_PER_DRAG_PIXEL = 3.2f
private const val MAX_WIND = 900f

/** Time constant of the gust dying away, in reciprocal seconds. */
private const val WIND_DECAY = 4f

/** How far the flame tip is pushed sideways per unit of wind, in design px. */
private const val FLAME_LEAN_PER_WIND = 0.05f
private const val MAX_FLAME_LEAN = 50f

private val FrostingLight = Color(0xFFFFA8C4)
private val FrostingDark = Color(0xFFFF5C8A)
private val SpongeLight = Color(0xFF7A4FD0)
private val SpongeDark = Color(0xFF3A1F6E)
private val PlateColor = Color(0xFF57C7FF)
private val SmokeColor = Color(0xFFB9AEC9)

private val SprinkleColors = listOf(
    Color(0xFFFF5C8A), Color(0xFFFFC246), Color(0xFF57C7FF), Color(0xFF8B6BFF),
)

private val CandleColors = listOf(
    Color(0xFFFF5C8A), Color(0xFF57C7FF), Color(0xFFFFC246),
    Color(0xFF8B6BFF), Color(0xFF5AD8A6),
)

/** Cake proportions, derived from the canvas so everything scales with the screen. */
internal class CakeGeometry(val width: Float, val height: Float) {
    /** Scale for everything sized in design px — candles, flames, smoke, sprinkles. */
    val scale = width / 1080f * CAKE_SCALE
    val centerX = width / 2f
    val plateY = height * 0.88f
    val lowerTierWidth = width * 0.60f * CAKE_SCALE
    val lowerTierHeight = height * 0.082f * CAKE_SCALE
    val upperTierWidth = lowerTierWidth * 0.70f
    val upperTierHeight = lowerTierHeight * 0.80f
    val upperTierTop = plateY - lowerTierHeight - upperTierHeight
    val candleHeight = 52f * scale

    /** Height of the wicks — where the flames sit and where the greeting is born. */
    val wickY = upperTierTop - candleHeight

    val candleXs = List(CANDLE_COUNT) { index ->
        val span = upperTierWidth * 0.74f
        centerX - span / 2f + span / (CANDLE_COUNT - 1) * index
    }
}

internal class Candle(val x: Float, val color: Color) {
    var lit = true

    /** 0 = burning, 1 = fully out. */
    var dying = 0f
    val flickerPhase = Random.nextFloat() * 6.28f
}

internal class SmokePuff(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val scale: Float,
) {
    var age = 0f
    val lifespan = 1.6f + Random.nextFloat() * 1.4f
    val wanderPhase = Random.nextFloat() * 6.28f
    val radius = (5f + Random.nextFloat() * 7f) * scale
}

/**
 * The air a swipe stirs up: a gust builds while the finger moves and bleeds away once it stops.
 * Flames lean by it, and a gust past [BLOW_OUT_WIND] puts them out.
 */
internal class Wind {
    var speed by mutableFloatStateOf(0f)
        private set

    val blowsOutFlames: Boolean get() = abs(speed) >= BLOW_OUT_WIND

    fun gust(dragX: Float) {
        speed = (speed + dragX * WIND_PER_DRAG_PIXEL).coerceIn(-MAX_WIND, MAX_WIND)
    }

    /** Exponential decay, so the gust dies at the same rate whatever the frame length. */
    fun settle(deltaSeconds: Float) {
        speed *= exp(-WIND_DECAY * deltaSeconds)
    }
}

internal fun CakeGeometry.buildCandles(): List<Candle> = List(CANDLE_COUNT) { index ->
    Candle(x = candleXs[index], color = CandleColors[index % CandleColors.size])
}

/** Puts out [candle] and leaves a burst of smoke on its wick. */
internal fun CakeGeometry.blowOut(candle: Candle, smoke: MutableList<SmokePuff>) {
    candle.lit = false
    repeat(SMOKE_PUFFS) {
        smoke += SmokePuff(
            x = candle.x + (Random.nextFloat() - 0.5f) * 8f * scale,
            y = wickY - 6f * scale,
            velocityX = (Random.nextFloat() - 0.5f) * 40f * scale,
            velocityY = (-30f - Random.nextFloat() * 50f) * scale,
            scale = scale,
        )
    }
}

internal fun advanceCandles(candles: List<Candle>, deltaSeconds: Float) {
    candles.forEach { candle ->
        if (!candle.lit && candle.dying < 1f) {
            candle.dying = (candle.dying + deltaSeconds * DYING_RATE).coerceAtMost(1f)
        }
    }
}

internal fun advanceSmoke(smoke: MutableList<SmokePuff>, deltaSeconds: Float) {
    smoke.forEach { puff ->
        // Smoke accelerates upward as it thins, and wanders sideways as it goes.
        puff.velocityY += -18f * deltaSeconds * puff.scale
        puff.velocityX += sin(puff.age * 2.4f + puff.wanderPhase) * 22f * deltaSeconds * puff.scale
        puff.x += puff.velocityX * deltaSeconds
        puff.y += puff.velocityY * deltaSeconds
        puff.age += deltaSeconds
    }
    smoke.removeAll { it.age > it.lifespan }
}

/** Cake, candles and the warm light the flames cast, with [glow] as the fraction still lit. */
internal fun DrawScope.drawCake(geometry: CakeGeometry, candles: List<Candle>, glow: Float) {
    if (glow > 0f) {
        val center = Offset(geometry.centerX, geometry.wickY)
        val radius = geometry.width * 0.9f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFB74D).copy(alpha = 0.22f * glow), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
            blendMode = BlendMode.Plus,
        )
    }

    drawPlate(geometry)
    drawTier(
        geometry,
        geometry.plateY,
        geometry.lowerTierWidth,
        geometry.lowerTierHeight,
        drips = 7
    )
    drawTier(
        geometry,
        geometry.plateY - geometry.lowerTierHeight,
        geometry.upperTierWidth,
        geometry.upperTierHeight,
        drips = 5,
    )
    candles.forEach { drawCandle(geometry, it) }
}

/** Flames of every candle still burning, leaning by [wind] and flickering on [time]. */
internal fun DrawScope.drawFlames(
    geometry: CakeGeometry,
    candles: List<Candle>,
    time: Float,
    wind: Wind,
) {
    candles.forEach { candle ->
        val life = if (candle.lit) 1f else 1f - candle.dying
        if (life <= 0.01f) return@forEach

        drawFlame(
            x = candle.x,
            baseY = geometry.wickY - FLAME_GAP * geometry.scale,
            scale = geometry.scale,
            life = life,
            lean = (wind.speed * FLAME_LEAN_PER_WIND)
                .coerceIn(-MAX_FLAME_LEAN, MAX_FLAME_LEAN) * geometry.scale,
            // Two out-of-step waves, so the flicker never settles into a visible rhythm.
            flicker = sin(time * 11f + candle.flickerPhase) * 0.5f +
                    sin(time * 23f + candle.flickerPhase * 2f) * 0.25f,
        )
    }
}

internal fun DrawScope.drawSmoke(smoke: List<SmokePuff>) {
    smoke.forEach { puff ->
        val fade = puff.age / puff.lifespan
        drawCircle(
            color = SmokeColor.copy(alpha = (1f - fade) * 0.28f),
            radius = puff.radius * (1f + fade * 3f),
            center = Offset(puff.x, puff.y),
        )
    }
}

private fun DrawScope.drawPlate(geometry: CakeGeometry) {
    drawOval(
        color = Color(0xFF2A2150),
        topLeft = Offset(
            geometry.centerX - geometry.lowerTierWidth * 0.62f,
            geometry.plateY - 14f * geometry.scale,
        ),
        size = Size(geometry.lowerTierWidth * 1.24f, 30f * geometry.scale),
    )
    drawLine(
        color = PlateColor.copy(alpha = 0.55f),
        start = Offset(geometry.centerX - geometry.lowerTierWidth * 0.58f, geometry.plateY),
        end = Offset(geometry.centerX + geometry.lowerTierWidth * 0.58f, geometry.plateY),
        strokeWidth = 3f * geometry.scale,
        cap = StrokeCap.Round,
        blendMode = BlendMode.Plus,
    )
}

private fun DrawScope.drawTier(
    geometry: CakeGeometry,
    bottomY: Float,
    width: Float,
    height: Float,
    drips: Int,
) {
    val left = geometry.centerX - width / 2f
    val right = geometry.centerX + width / 2f
    val top = bottomY - height

    val body = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(left, top, right, bottomY),
                topLeft = CornerRadius(height * 0.30f),
                topRight = CornerRadius(height * 0.30f),
                bottomLeft = CornerRadius(height * 0.42f),
                bottomRight = CornerRadius(height * 0.42f),
            )
        )
    }
    drawPath(
        path = body,
        brush = Brush.verticalGradient(
            listOf(SpongeLight, SpongeDark),
            startY = top,
            endY = bottomY
        ),
    )

    clipPath(body) {
        for (seam in 1..2) {
            val y = top + height * (0.42f + 0.24f * seam)
            drawLine(
                color = Color.Black.copy(alpha = 0.16f),
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 2.5f * geometry.scale,
            )
        }
    }

    val slabHeight = height * 0.36f
    val slabTop = top - slabHeight * 0.34f
    drawPath(
        path = frostingPath(left, right, slabTop, slabHeight, drips),
        brush = Brush.verticalGradient(
            listOf(FrostingLight, FrostingDark),
            startY = slabTop,
            endY = slabTop + slabHeight * 2.2f,
        ),
    )
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(left + width * 0.04f, slabTop + slabHeight * 0.16f),
        end = Offset(right - width * 0.04f, slabTop + slabHeight * 0.16f),
        strokeWidth = 3f * geometry.scale,
        cap = StrokeCap.Round,
    )

    drawSprinkles(geometry, left, width, slabTop, slabHeight, drips)
}

/** Seeded on the tier's own size, so the sprinkles hold still between frames. */
private fun DrawScope.drawSprinkles(
    geometry: CakeGeometry,
    left: Float,
    width: Float,
    slabTop: Float,
    slabHeight: Float,
    drips: Int,
) {
    val random = Random(width.toInt() * 31 + drips)
    repeat(drips * 3) {
        val x = left + width * (0.06f + random.nextFloat() * 0.88f)
        val y = slabTop + slabHeight * (0.3f + random.nextFloat() * 0.55f)
        rotate(random.nextFloat() * 180f, Offset(x, y)) {
            drawRoundRect(
                color = SprinkleColors[random.nextInt(SprinkleColors.size)],
                topLeft = Offset(x - 6f * geometry.scale, y - 2f * geometry.scale),
                size = Size(12f * geometry.scale, 4f * geometry.scale),
                cornerRadius = CornerRadius(2f * geometry.scale),
            )
        }
    }
}

private fun DrawScope.drawCandle(geometry: CakeGeometry, candle: Candle) {
    val scale = geometry.scale
    val width = 15f * scale
    val height = 58f * scale
    val bottom = geometry.upperTierTop + 6f * scale
    val top = bottom - height
    val body = Path().apply {
        addRoundRect(
            RoundRect(
                Rect(candle.x - width / 2f, top, candle.x + width / 2f, bottom),
                CornerRadius(5f * scale),
            )
        )
    }
    drawPath(body, candle.color)

    clipPath(body) {
        for (stripe in -2..6) {
            val y = top + stripe * 14f * scale
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(candle.x - width, y),
                end = Offset(candle.x + width, y - 14f * scale),
                strokeWidth = 5f * scale,
            )
        }
    }

    drawLine(
        color = Color(0xFF2B2B2B),
        start = Offset(candle.x, top),
        end = Offset(candle.x, top - 8f * scale),
        strokeWidth = 3f * scale,
    )
}

/** A frosting slab with rounded shoulders and a row of hanging drips along its bottom edge. */
private fun frostingPath(
    left: Float,
    right: Float,
    top: Float,
    slabHeight: Float,
    drips: Int,
): Path = Path().apply {
    val step = (right - left) / drips
    val shoulder = slabHeight * 0.55f

    moveTo(left, top + shoulder)
    quadraticTo(left, top, left + shoulder, top)
    lineTo(right - shoulder, top)
    quadraticTo(right, top, right, top + shoulder)
    lineTo(right, top + slabHeight)
    for (drip in drips - 1 downTo 0) {
        val dripLeft = left + step * drip
        // Alternating depths, so the drips do not read as a regular comb.
        val depth = slabHeight * (0.55f + (drip % 3) * 0.45f)
        quadraticTo(
            dripLeft + step * 0.5f,
            top + slabHeight + depth,
            dripLeft,
            top + slabHeight,
        )
    }
    lineTo(left, top + shoulder)
    close()
}

/**
 * Teardrop flame, hotter core and halo ring, all blended additively so the flame reads as a light
 * source rather than a shape. [life] fades it out as the candle dies.
 */
private fun DrawScope.drawFlame(
    x: Float,
    baseY: Float,
    scale: Float,
    life: Float,
    lean: Float,
    flicker: Float,
) {
    val height = (44f + flicker * 7f) * scale * life
    val width = (11f + flicker * 1.5f) * scale * life
    val glowCenter = Offset(x + lean * 0.4f, baseY - height * 0.5f)
    val glowRadius = 70f * scale * life

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFC947).copy(alpha = 0.55f * life), Color.Transparent),
            center = glowCenter,
            radius = glowRadius,
        ),
        radius = glowRadius,
        center = glowCenter,
        blendMode = BlendMode.Plus,
    )

    fun teardrop(size: Float): Path = Path().apply {
        val flameHeight = height * size
        val flameWidth = width * size
        val tipX = x + lean * size
        moveTo(x - flameWidth, baseY)
        cubicTo(
            x - flameWidth, baseY - flameHeight * 0.55f,
            tipX - flameWidth * 0.45f, baseY - flameHeight * 0.82f,
            tipX, baseY - flameHeight,
        )
        cubicTo(
            tipX + flameWidth * 0.45f, baseY - flameHeight * 0.82f,
            x + flameWidth, baseY - flameHeight * 0.55f,
            x + flameWidth, baseY,
        )
        close()
    }

    drawPath(teardrop(1f), Color(0xFFFF7A18).copy(alpha = 0.85f * life), blendMode = BlendMode.Plus)
    drawPath(
        teardrop(0.62f),
        Color(0xFFFFE082).copy(alpha = 0.95f * life),
        blendMode = BlendMode.Plus
    )
    drawPath(teardrop(0.3f), Color.White.copy(alpha = 0.9f * life), blendMode = BlendMode.Plus)

    drawCircle(
        color = Color(0xFFFFA726).copy(alpha = 0.15f * life),
        radius = 26f * scale,
        center = Offset(x, baseY - height * 0.4f),
        style = Stroke(width = 2f * scale),
        blendMode = BlendMode.Plus,
    )
}