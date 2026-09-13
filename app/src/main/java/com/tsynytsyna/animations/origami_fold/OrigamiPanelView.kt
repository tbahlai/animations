package com.tsynytsyna.animations.origami_fold

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun OrigamiPanelView(
    panelIndex: Int,
    content: PanelContent,
    theme: OrigamiTheme,
    foldAngle: Float,
    isLeftHinge: Boolean,
    isRightHinge: Boolean,
    modifier: Modifier = Modifier
) {
    // Crease occlusion shadow intensity proportional to sin(foldAngle)
    val foldRad = (foldAngle * Math.PI / 180.0).toFloat()
    val shadowIntensity = (sin(foldRad) * 0.72f).coerceIn(0f, 0.85f)
    val lightAngleSheen = (sin(foldRad * 0.5f) * 0.25f).coerceIn(0f, 0.35f)

    Surface(
        color = theme.paperColor,
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
            .fillMaxHeight()
            .border(0.75.dp, theme.secondaryInk.copy(alpha = 0.25f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Subtle Paper Fiber Grain & Micro-Grid Texture
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 20.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = theme.secondaryInk.copy(alpha = 0.04f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5f
                    )
                    y += step
                }
            }

            // 2. Rich Editorial Panel Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Metadata & Section Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = content.sectionNumber,
                        color = theme.secondaryInk,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(theme.accentColor.copy(alpha = 0.12f))
                            .border(0.8.dp, theme.accentColor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = content.badgeLabel,
                            color = theme.accentColor,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Panel Vector Graphic Illustration (Isometric, Topo, or Compass)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.secondaryInk.copy(alpha = 0.05f))
                        .border(0.5.dp, theme.secondaryInk.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                ) {
                    when (panelIndex % 3) {
                        0 -> IsometricCubeIllustration(theme)
                        1 -> TopoContourIllustration(theme)
                        2 -> TransitMapIllustration(theme)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Typography Section
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = content.headerTitle,
                        color = theme.inkColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = content.subHeader,
                        color = theme.accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = content.bodyParagraph,
                        color = theme.inkColor.copy(alpha = 0.82f),
                        fontSize = 9.5.sp,
                        lineHeight = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Bottom Meta Tags & Barcode Seal
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        content.metaTags.forEach { tag ->
                            Text(
                                text = "[$tag]",
                                color = theme.secondaryInk,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = content.coordinates,
                            color = theme.secondaryInk.copy(alpha = 0.7f),
                            fontSize = 7.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Stylized vector stamp seal
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.dp, theme.accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "印",
                                color = theme.accentColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // 3. Dynamic Crease Ambient Occlusion Shadows
            // Shadow on Left Hinge (casts into panel from the left crease)
            if (isLeftHinge && shadowIntensity > 0.02f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(28.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    theme.creaseColor.copy(alpha = shadowIntensity),
                                    theme.creaseColor.copy(alpha = shadowIntensity * 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Shadow on Right Hinge (casts into panel from the right crease)
            if (isRightHinge && shadowIntensity > 0.02f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(28.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    theme.creaseColor.copy(alpha = shadowIntensity * 0.4f),
                                    theme.creaseColor.copy(alpha = shadowIntensity)
                                )
                            )
                        )
                )
            }

            // 4. Directional Grazing Light Sheen across Paper Surface
            if (lightAngleSheen > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = lightAngleSheen),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = lightAngleSheen * 0.35f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun IsometricCubeIllustration(theme: OrigamiTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = size.height * 0.35f

        val topFace = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.866f, cy - r * 0.5f)
            lineTo(cx, cy)
            lineTo(cx - r * 0.866f, cy - r * 0.5f)
            close()
        }
        val leftFace = Path().apply {
            moveTo(cx - r * 0.866f, cy - r * 0.5f)
            lineTo(cx, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.866f, cy + r * 0.5f)
            close()
        }
        val rightFace = Path().apply {
            moveTo(cx, cy)
            lineTo(cx + r * 0.866f, cy - r * 0.5f)
            lineTo(cx + r * 0.866f, cy + r * 0.5f)
            lineTo(cx, cy + r)
            close()
        }

        drawPath(topFace, theme.accentColor.copy(alpha = 0.25f))
        drawPath(leftFace, theme.secondaryInk.copy(alpha = 0.20f))
        drawPath(rightFace, theme.secondaryInk.copy(alpha = 0.35f))

        drawPath(topFace, theme.accentColor, style = Stroke(width = 1.2f))
        drawPath(leftFace, theme.inkColor.copy(alpha = 0.6f), style = Stroke(width = 1.2f))
        drawPath(rightFace, theme.inkColor.copy(alpha = 0.6f), style = Stroke(width = 1.2f))
    }
}

@Composable
private fun TopoContourIllustration(theme: OrigamiTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f

        // Concentric topographic elevation rings
        for (i in 1..4) {
            val rx = size.width * (0.12f * i)
            val ry = size.height * (0.10f * i)
            drawOval(
                color = if (i == 3) theme.accentColor else theme.secondaryInk.copy(alpha = 0.45f),
                topLeft = Offset(cx - rx, cy - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2f, ry * 2f),
                style = Stroke(
                    width = if (i == 3) 1.5f else 0.8f,
                    pathEffect = if (i % 2 == 0) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null
                )
            )
        }

        // Crosshairs
        drawLine(
            color = theme.accentColor.copy(alpha = 0.5f),
            start = Offset(cx - 15f, cy),
            end = Offset(cx + 15f, cy),
            strokeWidth = 1f
        )
        drawLine(
            color = theme.accentColor.copy(alpha = 0.5f),
            start = Offset(cx, cy - 15f),
            end = Offset(cx, cy + 15f),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun TransitMapIllustration(theme: OrigamiTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Transit route lines
        val line1 = Path().apply {
            moveTo(w * 0.15f, h * 0.8f)
            lineTo(w * 0.45f, h * 0.4f)
            lineTo(w * 0.85f, h * 0.25f)
        }
        val line2 = Path().apply {
            moveTo(w * 0.15f, h * 0.25f)
            lineTo(w * 0.55f, h * 0.55f)
            lineTo(w * 0.85f, h * 0.75f)
        }

        drawPath(line1, theme.accentColor, style = Stroke(width = 2.5f))
        drawPath(line2, theme.secondaryInk, style = Stroke(width = 2.0f))

        // Station nodes
        val stations = listOf(
            Offset(w * 0.15f, h * 0.8f),
            Offset(w * 0.45f, h * 0.4f),
            Offset(w * 0.85f, h * 0.25f),
            Offset(w * 0.55f, h * 0.55f)
        )
        for (st in stations) {
            drawCircle(Color.White, radius = 4f, center = st)
            drawCircle(theme.inkColor, radius = 4f, center = st, style = Stroke(width = 1.2f))
        }
    }
}
