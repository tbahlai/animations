package com.tsynytsyna.animations.production_components.pull_to_refresh

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ElasticPullRefreshHeader(
    state: ElasticPullRefreshState,
    word: String = "REFRESH",
    accentColor: Color = Color(0xFFFF2A55),
    modifier: Modifier = Modifier
) {
    val pullPx = state.effectivePullPx
    val isRefreshing = state.isRefreshing
    val progress = state.progress

    val infiniteTransition = rememberInfiniteTransition(label = "refreshWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val n = word.length
    val spline = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .graphicsLayer {
                alpha = if (isRefreshing) 1f else (pullPx / 40f).coerceIn(0f, 1f)
            },
        contentAlignment = Alignment.Center
    ) {
        // Underlying Spring Tension Chords and Spline Ribbon
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.42f
            val spacing = (size.width * 0.72f / (n.coerceAtLeast(1) - 1).coerceAtLeast(1)).coerceIn(32f, 85f)
            val startX = cx - (n - 1) * spacing * 0.5f

            val points = mutableListOf<Offset>()

            for (i in 0 until n) {
                val px = startX + i * spacing
                val archFactor = sin((i.toFloat() / (n - 1).coerceAtLeast(1)) * PI.toFloat())

                val py = if (isRefreshing) {
                    cy + sin(wavePhase + i * 0.85f) * 22f
                } else {
                    cy + (pullPx * 0.35f * archFactor)
                }

                points.add(Offset(px, py))

                // Tension chord from top baseline
                drawLine(
                    color = accentColor.copy(alpha = (progress * 0.5f).coerceIn(0.15f, 0.75f)),
                    start = Offset(px, cy - 20f),
                    end = Offset(px, py),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Spring anchor dot
                drawCircle(
                    color = accentColor.copy(alpha = 0.5f),
                    radius = 2.5.dp.toPx(),
                    center = Offset(px, cy - 20f)
                )
            }

            // Continuous elastic baseline spline
            if (points.size >= 2) {
                spline.reset()
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
                    color = accentColor.copy(alpha = 0.75f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Typographic Characters
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val spacingDp = (280f / n).coerceIn(24f, 48f).dp
            val halfCount = (n - 1) * 0.5f

            word.forEachIndexed { i, char ->
                val archFactor = sin((i.toFloat() / (n - 1).coerceAtLeast(1)) * PI.toFloat())
                val displacement = if (isRefreshing) {
                    sin(wavePhase + i * 0.85f) * 18f
                } else {
                    pullPx * 0.35f * archFactor
                }

                // Squash & Stretch
                val stretch = 1f + (displacement / 180f).coerceIn(-0.25f, 0.95f)
                val scaleY = stretch
                val scaleX = 1f / sqrt(stretch.coerceAtLeast(0.5f))
                val rotation = if (isRefreshing) {
                    kotlin.math.cos(wavePhase + i * 0.85f) * 12f
                } else {
                    ((i - halfCount) / halfCount.coerceAtLeast(1f)) * (pullPx * 0.06f)
                }

                val xOffsetDp = (i - halfCount) * spacingDp.value

                Text(
                    text = char.toString(),
                    color = if (progress >= 1f || isRefreshing) accentColor else Color.White.copy(alpha = 0.88f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = xOffsetDp * density
                            translationY = displacement
                            this.scaleX = scaleX
                            this.scaleY = scaleY
                            rotationZ = rotation
                        }
                )
            }
        }

        // Subtitle Status Prompt
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val statusText = when {
                isRefreshing -> "FETCHING LIVE UPDATES..."
                progress >= 1f -> "RELEASE TO REFRESH"
                else -> "PULL DOWN TO REFRESH"
            }
            Text(
                text = statusText,
                color = if (progress >= 1f || isRefreshing) accentColor else Color.White.copy(alpha = 0.45f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
