package com.tsynytsyna.animations.thanksgiving

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * "Happy Thanksgiving" as a swarm of dots. The text is rasterised offscreen and its opaque
 * pixels are sampled, so each dot lands on a real glyph pixel before the dots fade into the text.
 */

private const val GREETING = "Happy\nThanksgiving"
private const val RASTER_SCALE = 0.5f
private const val SAMPLE_STEP = 3
private const val REST_RADIUS = 2.9f
private const val FUSE_START = 0.85f
private const val VERTICAL_ANCHOR = 0.22f

private val Palette = listOf(
    Color(0xFFF6DDB0),
    Color(0xFFF2B33D),
    Color(0xFFE8943B),
    Color(0xFFE0662F),
    Color(0xFFF2B33D),
    Color(0xFFF6DDB0),
)

internal class GreetingDot(
    val x: Float,
    val y: Float,
    val startX: Float,
    val startY: Float,
    val delay: Float,
    val color: Color,
    val radius: Float,
    val bulge: Float,
)

internal class Greeting(
    val dots: List<GreetingDot>,
    val letters: TextLayoutResult,
    val lettersTopLeft: Offset,
)

internal fun buildGreeting(
    width: Float,
    height: Float,
    sources: List<Offset>,
    scale: Float,
    density: Density,
    measurer: TextMeasurer,
): Greeting {
    val rasterWidth = (width * RASTER_SCALE).toInt().coerceAtLeast(1)
    val rasterHeight = (height * RASTER_SCALE).toInt().coerceAtLeast(1)
    val random = Random(1126)

    // Sized from the layout width, so the half-size raster and the final text line up exactly.
    fun layout(layoutWidth: Float, brush: Brush) = measurer.measure(
        text = GREETING,
        style = TextStyle(
            fontSize = with(density) { (layoutWidth * 0.125f).toSp() },
            lineHeight = with(density) { (layoutWidth * 0.15f).toSp() },
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            brush = brush,
            textAlign = TextAlign.Center,
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

    val letters = layout(width, Brush.linearGradient(Palette, start = Offset.Zero, end = Offset(width, 0f)))

    val dots = buildList {
        forEachOpaquePixel(
            pixels = pixels,
            width = rasterWidth,
            fromY = rasterTop.toInt().coerceAtLeast(0),
            toY = (rasterTop + rasterLayout.size.height).toInt().coerceAtMost(rasterHeight),
        ) { rasterX, rasterY ->
            val x = rasterX / RASTER_SCALE
            val source = sources[random.nextInt(sources.size)]
            add(
                GreetingDot(
                    x = x,
                    y = rasterY / RASTER_SCALE,
                    startX = source.x + (random.nextFloat() - 0.5f) * 30f * scale,
                    startY = source.y + (random.nextFloat() - 0.5f) * 30f * scale,
                    delay = (x / width).coerceIn(0f, 1f) * 0.4f + random.nextFloat() * 0.18f,
                    color = colorAt(x / width),
                    radius = (1.1f + random.nextFloat() * 1.1f) * scale,
                    bulge = (random.nextFloat() - 0.5f) * 90f * scale,
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

internal fun DrawScope.drawGreeting(greeting: Greeting, progress: Float) {
    if (progress <= 0f) return

    val fuse = ((progress - FUSE_START) / (1f - FUSE_START)).coerceIn(0f, 1f)
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

        val halo = 0.22f * flight * (1f - flight)
        if (halo > 0.01f) {
            drawCircle(dot.color.copy(alpha = halo), radius = dot.radius * 3.4f, center = center, blendMode = BlendMode.Plus)
        }
        drawCircle(
            color = dot.color.copy(alpha = (0.55f + 0.45f * flight) * (1f - fuse)),
            radius = dot.radius + (REST_RADIUS - dot.radius) * flight,
            center = center,
        )
    }

    if (fuse > 0f) {
        drawText(greeting.letters, topLeft = greeting.lettersTopLeft, alpha = fuse)
    }
}

private fun rasterize(width: Int, height: Int, density: Density, block: DrawScope.() -> Unit): PixelMap {
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
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
