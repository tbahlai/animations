package com.tsynytsyna.animations.thanksgiving

import android.graphics.BlurMaskFilter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

private const val COVERT_COUNT = 11
private const val FEATHER_BLUR = 0.03f
private const val COVERT_BLUR = 0.05f

internal class FeatherStyle(
    val color: Color,
    val label: TextLayoutResult,
    val labelRotation: Float,
    val fill: Brush,
    val barbs: Color,
    val edge: Color,
    val shaft: Color,
) {
    lateinit var sprite: Sprite
}

// [origin] is where the quill sits in the bitmap.
internal class Sprite(val image: ImageBitmap, val origin: Offset)

internal fun buildStyles(geometry: TurkeyGeometry, density: Density, measurer: TextMeasurer) =
    Thanks.mapIndexed { index, word ->
        val fromCentre = abs(index - CENTRE).toInt().coerceAtMost(FeatherColors.lastIndex)
        val color = FeatherColors[fromCentre]
        val label = measurer.measure(
            text = word,
            style = TextStyle(
                color = if (color.luminance() > 0.3f) DarkInk else LightInk,
                fontSize = with(density) { (geometry.unit * 0.075f).toSp() },
                fontWeight = FontWeight.Medium,
                letterSpacing = with(density) { (geometry.unit * 0.003f).toSp() },
            ),
        )
        FeatherStyle(
            color = color,
            label = label,
            // Left-hand feathers read tip to quill so no word is upside down.
            labelRotation = if (index < CENTRE) 90f else -90f,
            fill = Brush.linearGradient(
                0f to lerp(color, Color.Black, 0.35f),
                0.45f to color,
                0.8f to lerp(color, Color.White, 0.12f),
                start = Offset.Zero,
                end = Offset(0f, -geometry.unit),
            ),
            barbs = lerp(color, Color.Black, 0.3f).copy(alpha = 0.35f),
            edge = lerp(color, Color.Black, 0.3f).copy(alpha = 0.6f),
            shaft = lerp(color, Color.White, 0.45f),
        ).also { style ->
            style.sprite = renderSprite(
                halfWidth = geometry.featherHalfWidth,
                length = geometry.unit,
                margin = geometry.unit * FEATHER_BLUR * 3f,
            ) { drawFeatherArt(geometry, style) }
        }
    }

internal fun buildCovertSprite(geometry: TurkeyGeometry) = renderSprite(
    halfWidth = geometry.covertHalfWidth,
    length = geometry.covertLength,
    margin = geometry.covertLength * COVERT_BLUR * 3f,
) { drawCovertArt(geometry) }

// Feathers are painted once: the blurred shadows are far too slow to redraw every frame.
private fun renderSprite(
    halfWidth: Float,
    length: Float,
    margin: Float,
    art: DrawScope.() -> Unit
): Sprite {
    val width = ceil(halfWidth * 2f + margin * 2f).toInt()
    val height = ceil(length + margin * 2f).toInt()
    val origin = Offset(width / 2f, length + margin)
    val image = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(image),
        size = Size(width.toFloat(), height.toFloat()),
    ) {
        translate(origin.x, origin.y) { art() }
    }
    return Sprite(image, origin)
}

private fun DrawScope.drawSprite(sprite: Sprite) {
    drawImage(sprite.image, topLeft = -sprite.origin)
}

private fun DrawScope.drawFeatherArt(geometry: TurkeyGeometry, style: FeatherStyle) {
    val length = geometry.unit
    val outline = geometry.featherOutline
    val bounds = Size(geometry.featherHalfWidth * 2f, length)
    val corner = Offset(-geometry.featherHalfWidth, -length)

    drawSoftShadow(outline, blur = length * FEATHER_BLUR, alpha = 0.3f)
    drawPath(outline, style.fill)
    clipPath(outline) {
        drawPath(geometry.featherBarbs, style.barbs, style = Stroke(length * 0.003f))
        drawRect(geometry.featherSheen, topLeft = corner, size = bounds)
        drawRect(geometry.featherTip, topLeft = corner, size = bounds)
    }
    drawLine(style.shaft, Offset.Zero, Offset(0f, -length * 0.9f), length * 0.006f, StrokeCap.Round)
    drawPath(outline, style.edge, style = Stroke(length * 0.004f))
}

private fun DrawScope.drawCovertArt(geometry: TurkeyGeometry) {
    val length = geometry.covertLength
    drawSoftShadow(geometry.covertOutline, blur = length * COVERT_BLUR, alpha = 0.35f)
    drawPath(geometry.covertOutline, geometry.covertFill)
    clipPath(geometry.covertOutline) {
        drawPath(
            geometry.covertBarbs,
            CovertDark.copy(alpha = 0.22f),
            style = Stroke(length * 0.004f)
        )
        drawRect(
            geometry.covertSheen,
            topLeft = Offset(-geometry.covertHalfWidth, -length),
            size = Size(geometry.covertHalfWidth * 2f, length),
        )
    }
    drawLine(
        CovertEdge.copy(alpha = 0.45f),
        Offset.Zero,
        Offset(0f, -length * 0.88f),
        length * 0.01f,
        StrokeCap.Round
    )
}

private fun DrawScope.drawSoftShadow(path: Path, blur: Float, alpha: Float) {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb((alpha * 255).toInt(), 0, 0, 0)
            maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

// Quill at the origin, tip straight up.
private fun DrawScope.drawFeather(
    geometry: TurkeyGeometry,
    style: FeatherStyle,
    labelAlpha: Float
) {
    drawSprite(style.sprite)
    if (labelAlpha <= 0f) return
    val label = style.label
    withTransform({
        translate(0f, -geometry.unit * 0.64f)
        rotate(style.labelRotation, pivot = Offset.Zero)
    }) {
        drawText(
            textLayoutResult = label,
            topLeft = Offset(-label.size.width / 2f, -label.size.height / 2f),
            alpha = labelAlpha,
        )
    }
}

internal fun DrawScope.drawAttachedFeather(
    geometry: TurkeyGeometry,
    feather: Feather,
    style: FeatherStyle,
    time: Float
) {
    val lengthFactor = feather.lengthFactor()
    withTransform({
        translate(geometry.pivot.x, geometry.pivot.y)
        rotate(feather.drawAngle(time), pivot = Offset.Zero)
        translate(0f, -feather.stretch)
        scale(lengthFactor, lengthFactor, pivot = Offset.Zero)
    }) {
        drawFeather(
            geometry,
            style,
            labelAlpha = ((feather.progress - 0.6f) / 0.4f).coerceIn(0f, 1f)
        )
    }
}

internal fun DrawScope.drawPoppedFeather(geometry: TurkeyGeometry, feather: PoppedFeather) {
    val t = feather.progress
    val size = feather.lengthFactor * (1f - 0.65f * t)
    withTransform({
        translate(feather.x, feather.y)
        rotate(feather.angle, pivot = Offset.Zero)
        scale(size, size, pivot = Offset.Zero)
        translate(0f, geometry.unit / 2f) // spin about the middle, not the quill
    }) {
        drawFeather(geometry, feather.style, labelAlpha = 1f - t)
    }
}

// The short feathers over the base of the tail follow the fan's average progress.
internal fun DrawScope.drawCoverts(geometry: TurkeyGeometry, turkey: Turkey, sprite: Sprite) {
    val progress = turkey.feathers.sumOf { it.progress.toDouble() }.toFloat() / FEATHER_COUNT
    val spread = FOLDED_DEGREES + (FAN_DEGREES * 0.9f - FOLDED_DEGREES) * progress
    val lengthFactor = FOLDED_LENGTH + (1f - FOLDED_LENGTH) * progress.coerceIn(0f, 1f)
    val order = (0 until COVERT_COUNT).sortedByDescending { abs(it - (COVERT_COUNT - 1) / 2f) }
    order.forEach { index ->
        val angle = -spread / 2f + index * spread / (COVERT_COUNT - 1)
        withTransform({
            translate(geometry.pivot.x, geometry.pivot.y + geometry.unit * 0.04f)
            rotate(angle, pivot = Offset.Zero)
            scale(lengthFactor, lengthFactor, pivot = Offset.Zero)
        }) {
            drawSprite(sprite)
        }
    }
}

internal fun DrawScope.drawWisp(wisp: Wisp) {
    val alpha =
        (1f - wisp.age / DOWN_SECONDS).coerceIn(0f, 1f) * (wisp.age / 0.08f).coerceIn(0f, 1f)
    if (alpha <= 0f) return
    val r = wisp.size
    val centre = Offset(wisp.x, wisp.y)
    rotate(wisp.angle, pivot = centre) {
        drawCircle(wisp.color.copy(alpha = 0.25f * alpha), radius = r * 0.55f, center = centre)
        for (k in -3..3) {
            val a = k * 0.32f
            val tip = centre + Offset(sin(a) * r, -cos(a) * r)
            drawLine(
                wisp.color.copy(alpha = 0.8f * alpha),
                centre + Offset(0f, r * 0.2f),
                tip,
                r * 0.07f,
                StrokeCap.Round
            )
        }
    }
}
