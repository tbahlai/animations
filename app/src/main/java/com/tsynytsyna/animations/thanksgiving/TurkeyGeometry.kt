package com.tsynytsyna.animations.thanksgiving

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal val SkyTop = Color(0xFF2A1A3E)
internal val SkyMiddle = Color(0xFF8C3B3B)
internal val SkyHorizon = Color(0xFFE0874A)
internal val Hill = Color(0xFF2E1C12)

// Centre of the fan outwards.
internal val FeatherColors = listOf(
    Color(0xFFF2B33D), Color(0xFFE8943B), Color(0xFFE0662F), Color(0xFFC2452D), Color(0xFF8E3A22),
)
internal val FeatherBand = Color(0xFF3A1C0E)
internal val FeatherGlint = Color(0xFFE9B060)
internal val FeatherTip = Color(0xFFF6DDB0)
internal val CovertDark = Color(0xFF3E2213)
internal val CovertLight = Color(0xFF7A4526)
internal val CovertEdge = Color(0xFFB07A48)

internal val DarkInk = Color(0xFF2B140A)
internal val LightInk = Color(0xFFF6E7C8)
internal val BodyLight = Color(0xFF7A4A2A)
internal val BodyDark = Color(0xFF4A2A16)
internal val Breast = Color(0xFF9A6438)
internal val Wing = Color(0xFF5A3420)
internal val HeadColor = Color(0xFFA87048)
internal val Beak = Color(0xFFF2B33D)
internal val Wattle = Color(0xFFD6352B)
internal val Legs = Color(0xFFE39A3B)

internal class TurkeyGeometry(val width: Float, val height: Float) {
    // Length of an open tail feather. Everything else is sized from it.
    val unit = minOf(width * 0.46f, height * 0.27f)
    val scale = width / 1080f
    val centreX = width / 2f
    val pivot = Offset(centreX, height * 0.58f)
    val feet = Offset(centreX, pivot.y + 0.67f * unit)
    val horizonY = height * 0.74f

    val bodyCentre = Offset(centreX, pivot.y + 0.12f * unit)
    val bodyRadii = Size(0.36f * unit, 0.40f * unit)
    val headCentre = Offset(centreX, pivot.y - 0.25f * unit)
    val headRadius = 0.15f * unit

    val featherHalfWidth = unit * 0.12f
    val featherOutline = featherPath(unit, featherHalfWidth)
    val featherBarbs = barbsPath(unit, featherHalfWidth)
    val featherSheen = Brush.horizontalGradient(
        0f to Color.White.copy(alpha = 0.22f),
        0.45f to Color.Transparent,
        0.55f to Color.Transparent,
        1f to Color.Black.copy(alpha = 0.16f),
        startX = -featherHalfWidth,
        endX = featherHalfWidth,
    )

    // Dark bar, glint, pale tip: one radial gradient around the quill keeps every edge soft.
    val featherTip = Brush.radialGradient(
        0f to Color.Transparent,
        0.8f to Color.Transparent,
        0.815f to FeatherBand,
        0.835f to FeatherBand,
        0.848f to FeatherGlint.copy(alpha = 0.8f),
        0.865f to Color.Transparent,
        0.9f to FeatherTip.copy(alpha = 0f),
        0.935f to FeatherTip,
        1f to FeatherTip,
        center = Offset.Zero,
        radius = unit,
    )

    val covertLength = unit * 0.5f
    val covertHalfWidth = unit * 0.075f
    val covertOutline = featherPath(covertLength, covertHalfWidth)
    val covertBarbs = barbsPath(covertLength, covertHalfWidth)
    val covertSheen = Brush.horizontalGradient(
        0f to Color.White.copy(alpha = 0.16f),
        0.5f to Color.Transparent,
        1f to Color.Black.copy(alpha = 0.14f),
        startX = -covertHalfWidth,
        endX = covertHalfWidth,
    )
    val covertFill = Brush.linearGradient(
        0f to CovertDark,
        0.65f to CovertLight,
        0.85f to CovertLight,
        1f to CovertEdge,
        start = Offset.Zero,
        end = Offset(0f, -covertLength),
    )

    // Where the greeting's dots take off from: points along the open tail.
    fun featherSpots(): List<Offset> = buildList {
        for (index in 0 until FEATHER_COUNT) {
            val degrees = -FAN_DEGREES / 2f + index * FAN_DEGREES / (FEATHER_COUNT - 1)
            val radians = degrees / 180f * PI.toFloat()
            for (step in 0..6) {
                val reach = unit * (0.35f + step * 0.1f)
                add(Offset(pivot.x + sin(radians) * reach, pivot.y - cos(radians) * reach))
            }
        }
    }

    fun hitsTurkey(point: Offset): Boolean {
        val dx = (point.x - bodyCentre.x) / bodyRadii.width
        val dy = (point.y - bodyCentre.y) / bodyRadii.height
        val inBody = dx * dx + dy * dy <= 1f
        val inHead = hypot(point.x - headCentre.x, point.y - headCentre.y) <= headRadius * 1.3f
        return inBody || inHead
    }
}

// Pointing straight up with the quill at the origin.
private fun featherPath(length: Float, halfWidth: Float) = Path().apply {
    val l = length
    val w = halfWidth
    moveTo(0f, 0f)
    cubicTo(w * 0.12f, -l * 0.12f, w * 0.85f, -l * 0.38f, w, -l * 0.66f)
    cubicTo(w * 1.05f, -l * 0.86f, w * 0.5f, -l * 0.99f, 0f, -l)
    cubicTo(-w * 0.5f, -l * 0.99f, -w * 1.05f, -l * 0.86f, -w, -l * 0.66f)
    cubicTo(-w * 0.85f, -l * 0.38f, -w * 0.12f, -l * 0.12f, 0f, 0f)
    close()
}

private fun barbsPath(length: Float, halfWidth: Float) = Path().apply {
    var t = 0.16f
    while (t < 0.95f) {
        listOf(-1f, 1f).forEach { side ->
            moveTo(0f, -length * t)
            quadraticTo(
                side * halfWidth * 0.6f, -length * (t + 0.035f),
                side * halfWidth * 1.1f, -length * (t + 0.1f),
            )
        }
        t += 0.032f
    }
}
