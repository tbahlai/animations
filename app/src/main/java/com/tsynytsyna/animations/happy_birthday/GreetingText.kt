package com.tsynytsyna.animations.happy_birthday

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas

/**
 * The greeting: a swarm of dots that rises from the wicks, settles onto the letterforms and
 * finally dissolves into solid lettering.
 *
 * Where the letters are is found by rasterising the text offscreen and sampling its opaque
 * pixels, so the dots land exactly on the glyphs they later turn into.
 */

private const val GREETING = "Happy\nBirthday!"

/** Text is rasterised at half size for sampling — plenty of detail, a quarter of the pixels. */
private const val RASTER_SCALE = 0.5f

/** Sampling stride over the raster, in raster px. Smaller = denser swarm, more to draw. */
private const val SAMPLE_STEP = 3

/**
 * Radius every dot settles at, in screen px, against the 6px spacing the stride works out to.
 * Just under half the spacing: the landed dots reach each other without overlapping, which is the
 * densest an even halftone can get before it turns blobby.
 */
private const val REST_RADIUS = 2.9f

/** Point in the reveal where the settled dots start dissolving into solid lettering. */
private const val FUSE_START = 0.85f

/** Fraction of the canvas height the greeting is centred on. */
private const val VERTICAL_ANCHOR = 0.30f

/**
 * A warm-to-cool sweep across the width, gold through pink and violet into aqua. The ramp walks
 * the stops at equal intervals, so the two gold entries at the front hold the warm end across the
 * first third of the word.
 */
private val Palette = listOf(
    Color(0xFFFFEE6B),
    Color(0xFFFFC93C),
    Color(0xFFFF6BC1),
    Color(0xFFA45DFF),
    Color(0xFF3AC9FF),
    Color(0xFF3BF5D0),
)

/** One dot, flying from a wick at [startX], [startY] to its pixel of a letter at [x], [y]. */
internal class GreetingDot(
    val x: Float,
    val y: Float,
    val startX: Float,
    val startY: Float,
    val delay: Float,
    val color: Color,
    val radius: Float,
) {
    /** Sideways bulge at the peak of the flight, so the swarm avoids straight lines. */
    val bulge = (Random.nextFloat() - 0.5f) * 90f
}

internal class Greeting(
    val dots: List<GreetingDot>,
    val letters: TextLayoutResult,
    val lettersTopLeft: Offset,
)

/**
 * Builds the swarm for a canvas of [width] x [height]. Dots are born at [sourceY] on one of
 * [sourceXs] — the candle wicks — and [scale] sizes them to the screen.
 */
internal fun buildGreeting(
    width: Float,
    height: Float,
    sourceXs: List<Float>,
    sourceY: Float,
    scale: Float,
    density: Density,
    measurer: TextMeasurer,
): Greeting {
    val rasterWidth = (width * RASTER_SCALE).toInt().coerceAtLeast(1)
    val rasterHeight = (height * RASTER_SCALE).toInt().coerceAtLeast(1)

    // Every dimension of the layout is a fraction of its width, so the half-size raster used for
    // sampling and the full-size letters drawn at the end land on exactly the same pixels.
    fun layout(layoutWidth: Float, brush: Brush) = measurer.measure(
        text = GREETING,
        style = TextStyle(
            fontSize = with(density) { (layoutWidth / 5.0f).toSp() },
            fontWeight = FontWeight.Black,
            brush = brush,
            textAlign = TextAlign.Center,
            lineHeight = with(density) { (layoutWidth / 4.4f).toSp() },
        ),
        constraints = Constraints.fixedWidth(layoutWidth.toInt()),
        density = density,
        layoutDirection = LayoutDirection.Ltr,
    )

    val rasterLayout = layout(rasterWidth.toFloat(), SolidColor(Color.White))
    val rasterTop = rasterHeight * VERTICAL_ANCHOR - rasterLayout.size.height / 2f
    val pixels = rasterize(rasterWidth, rasterHeight, density) {
        drawText(rasterLayout, topLeft = Offset(0f, rasterTop))
    }

    val letters = layout(
        layoutWidth = width,
        brush = Brush.linearGradient(Palette, start = Offset.Zero, end = Offset(width, 0f)),
    )

    val dots = buildList {
        // Only the band the text occupies is worth scanning; the rest of the raster is empty.
        forEachOpaquePixel(
            pixels = pixels,
            width = rasterWidth,
            fromY = rasterTop.toInt().coerceAtLeast(0),
            toY = (rasterTop + rasterLayout.size.height).toInt().coerceAtMost(rasterHeight),
        ) { rasterX, rasterY ->
            val x = rasterX / RASTER_SCALE
            add(
                GreetingDot(
                    x = x,
                    y = rasterY / RASTER_SCALE,
                    startX = sourceXs.random() + (Random.nextFloat() - 0.5f) * 30f * scale,
                    startY = sourceY - Random.nextFloat() * 20f * scale,
                    delay = (x / width).coerceIn(0f, 1f) * 0.4f + Random.nextFloat() * 0.18f,
                    color = colorAt(x / width),
                    radius = (1.1f + Random.nextFloat() * 1.1f) * scale,
                )
            )
        }
    }

    return Greeting(
        dots = dots,
        letters = letters,
        lettersTopLeft = Offset(0f, height * VERTICAL_ANCHOR - letters.size.height / 2f),
    )
}

/** Draws the greeting at [progress], 0 = still on the wicks, 1 = solid lettering. */
internal fun DrawScope.drawGreeting(greeting: Greeting, progress: Float) {
    if (progress <= 0f) return

    val fuse = ((progress - FUSE_START) / (1f - FUSE_START)).coerceIn(0f, 1f)

    // Past the crossfade the dots are fully transparent — thousands of invisible circles.
    if (fuse >= 1f) {
        drawText(greeting.letters, topLeft = greeting.lettersTopLeft)
        return
    }

    greeting.dots.forEach { dot ->
        val dotProgress = ((progress - dot.delay) / (1f - dot.delay)).coerceIn(0f, 1f)
        if (dotProgress <= 0f) return@forEach

        val flight = EaseOutCubic.transform(dotProgress)
        val center = Offset(
            dot.startX + (dot.x - dot.startX) * flight + sin(PI.toFloat() * flight) * dot.bulge,
            dot.startY + (dot.y - dot.startY) * flight,
        )

        // The halo belongs to the flight only: at rest its variation reads as speckle.
        val halo = 0.22f * flight * (1f - flight)
        if (halo > 0.01f) {
            drawCircle(
                color = dot.color.copy(alpha = halo),
                radius = dot.radius * 3.4f,
                center = center,
                blendMode = BlendMode.Plus,
            )
        }

        drawCircle(
            color = dot.color.copy(alpha = (0.55f + 0.45f * flight) * (1f - fuse)),
            radius = dot.radius + (REST_RADIUS - dot.radius) * flight,
            center = center,
        )
    }

    if (fuse > 0f) {
        drawText(
            textLayoutResult = greeting.letters,
            topLeft = greeting.lettersTopLeft,
            alpha = fuse,
        )
    }
}

private fun rasterize(
    width: Int,
    height: Int,
    density: Density,
    block: DrawScope.() -> Unit,
): PixelMap {
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = GraphicsCanvas(bitmap),
        size = Size(width.toFloat(), height.toFloat()),
        block = block,
    )
    return bitmap.toPixelMap()
}

private inline fun forEachOpaquePixel(
    pixels: PixelMap,
    width: Int,
    fromY: Int,
    toY: Int,
    onPixel: (x: Float, y: Float) -> Unit,
) {
    var y = fromY
    while (y < toY) {
        var x = 0
        while (x < width) {
            if (pixels[x, y].alpha > 0.55f) onPixel(x.toFloat(), y.toFloat())
            x += SAMPLE_STEP
        }
        y += SAMPLE_STEP
    }
}

/** Samples [Palette] as a continuous left-to-right ramp. */
private fun colorAt(position: Float): Color {
    val ramp = position.coerceIn(0f, 1f) * (Palette.size - 1)
    val index = ramp.toInt().coerceAtMost(Palette.size - 2)
    val fraction = ramp - index
    val from = Palette[index]
    val to = Palette[index + 1]
    return Color(
        from.red + (to.red - from.red) * fraction,
        from.green + (to.green - from.green) * fraction,
        from.blue + (to.blue - from.blue) * fraction,
    )
}