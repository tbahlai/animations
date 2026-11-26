package com.tsynytsyna.animations.thanksgiving

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.drawText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val BLINK_PERIOD = 3.4f
private const val BLINK_SECONDS = 0.14f

internal fun DrawScope.drawHill(geometry: TurkeyGeometry) {
    val edge = geometry.horizonY + geometry.unit * 0.08f
    val hill = Path().apply {
        moveTo(0f, edge)
        quadraticTo(geometry.centreX, geometry.horizonY - geometry.unit * 0.12f, size.width, edge)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(hill, Hill)
}

internal fun DrawScope.drawTurkey(geometry: TurkeyGeometry, turkey: Turkey) {
    drawLegs(geometry, turkey)
    drawBody(geometry)
    drawHead(geometry, turkey)
}

private fun DrawScope.drawLegs(geometry: TurkeyGeometry, turkey: Turkey) {
    val u = geometry.unit
    val cx = geometry.centreX
    val pivot = geometry.pivot

    drawOval(
        color = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(cx - 0.4f * u, pivot.y + 0.62f * u),
        size = Size(0.8f * u, 0.12f * u),
    )
    listOf(-1f, 1f).forEach { side ->
        val hip = Offset(cx + side * 0.12f * u, pivot.y + 0.45f * u)
        val foot = Offset(cx + side * 0.14f * u, pivot.y + 0.67f * u - turkey.footLift(side))
        drawLine(Legs, hip, foot, strokeWidth = 0.035f * u, cap = StrokeCap.Round)
        listOf(-35f, 0f, 35f).forEach { toe ->
            val radians = (toe + side * 8f) / 180f * PI.toFloat()
            val end = foot + Offset(sin(radians) * 0.07f * u, cos(radians) * 0.03f * u)
            drawLine(Legs, foot, end, strokeWidth = 0.025f * u, cap = StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawBody(geometry: TurkeyGeometry) {
    val u = geometry.unit
    val cx = geometry.centreX
    val pivot = geometry.pivot
    val body = geometry.bodyCentre
    val radii = geometry.bodyRadii

    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(BodyLight, BodyDark),
            center = body - Offset(0f, radii.height * 0.3f),
            radius = radii.height * 1.2f,
        ),
        topLeft = body - Offset(radii.width, radii.height),
        size = Size(radii.width * 2f, radii.height * 2f),
    )
    drawOval(Breast, topLeft = Offset(cx - 0.22f * u, pivot.y - 0.06f * u), size = Size(0.44f * u, 0.52f * u))
    listOf(-1f, 1f).forEach { side ->
        val wing = Offset(cx + side * 0.27f * u, pivot.y + 0.16f * u)
        rotate(degrees = side * -18f, pivot = wing) {
            drawOval(Wing, topLeft = wing - Offset(0.12f * u, 0.23f * u), size = Size(0.24f * u, 0.46f * u))
            listOf(0.05f, 0.12f, 0.19f).forEach { dy ->
                drawLine(
                    color = BodyDark,
                    start = wing + Offset(-0.07f * u, dy * u - 0.02f * u),
                    end = wing + Offset(0.07f * u, dy * u),
                    strokeWidth = 0.012f * u,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawHead(geometry: TurkeyGeometry, turkey: Turkey) {
    val u = geometry.unit
    val gobble = turkey.gobbleLeft / GOBBLE_SECONDS
    val gobblePhase = (1f - gobble) * 4f * PI.toFloat()
    val head = geometry.headCentre + Offset(0f, sin(gobblePhase) * 0.025f * u * gobble)

    drawOval(HeadColor, topLeft = Offset(geometry.centreX - 0.09f * u, head.y), size = Size(0.18f * u, 0.28f * u))
    drawCircle(HeadColor, radius = geometry.headRadius, center = head)

    val wattleTop = head + Offset(0.03f * u, 0.06f * u)
    val wattleSwing = sin(gobblePhase * 1.5f) * 18f * gobble + sin(turkey.time * 1.7f) * 4f
    rotate(wattleSwing, pivot = wattleTop) {
        val wattle = Path().apply {
            moveTo(wattleTop.x, wattleTop.y)
            cubicTo(
                wattleTop.x + 0.06f * u, wattleTop.y + 0.05f * u,
                wattleTop.x + 0.05f * u, wattleTop.y + 0.16f * u,
                wattleTop.x + 0.015f * u, wattleTop.y + 0.17f * u,
            )
            cubicTo(
                wattleTop.x - 0.02f * u, wattleTop.y + 0.16f * u,
                wattleTop.x - 0.02f * u, wattleTop.y + 0.05f * u,
                wattleTop.x, wattleTop.y,
            )
            close()
        }
        drawPath(wattle, Wattle)
    }

    val beak = Path().apply {
        moveTo(head.x - 0.045f * u, head.y + 0.03f * u)
        lineTo(head.x + 0.045f * u, head.y + 0.03f * u)
        lineTo(head.x, head.y + 0.12f * u)
        close()
    }
    drawPath(beak, Beak)

    listOf(-1f, 1f).forEach { side ->
        drawEye(head + Offset(side * 0.06f * u, -0.03f * u), side, u, turkey)
    }
}

private fun DrawScope.drawEye(eye: Offset, side: Float, u: Float, turkey: Turkey) {
    val blinking = turkey.time % BLINK_PERIOD < BLINK_SECONDS
    when {
        turkey.ouchLeft > 0f -> {
            val back = eye.x + side * 0.026f * u
            val tip = eye.x - side * 0.022f * u
            val rise = 0.03f * u
            val squeeze = Path().apply {
                moveTo(back, eye.y - rise)
                lineTo(tip, eye.y)
                lineTo(back, eye.y + rise)
            }
            drawPath(squeeze, DarkInk, style = Stroke(0.014f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        blinking -> drawLine(DarkInk, eye - Offset(0.035f * u, 0f), eye + Offset(0.035f * u, 0f), strokeWidth = 0.012f * u)
        else -> {
            val openness = 1f - 0.6f * turkey.tension
            val white = 0.042f * u
            drawOval(Color.White, topLeft = eye - Offset(white, white * openness), size = Size(white * 2f, white * 2f * openness))
            val pupil = eye + turkey.look * (0.016f * u * openness)
            val pupilRadius = 0.022f * u
            drawOval(
                DarkInk,
                topLeft = pupil - Offset(pupilRadius, pupilRadius * openness),
                size = Size(pupilRadius * 2f, pupilRadius * 2f * openness),
            )
            drawCircle(Color.White, radius = 0.007f * u, center = pupil + Offset(-0.006f * u, -0.006f * u * openness))
        }
    }
}

internal fun DrawScope.drawFloatingLabel(floating: FloatingLabel) {
    val label = floating.label
    drawText(
        textLayoutResult = label,
        topLeft = Offset(floating.x - label.size.width / 2f, floating.y - label.size.height / 2f),
        alpha = (1f - floating.age / floating.life).coerceIn(0f, 1f),
    )
}
