package com.tsynytsyna.animations.diorama

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val Night = Color(0xFF0E0E1C)
private val Panel = Color(0xFF1C1B30)
private val Bone = Color(0xFFF6EFE6)
private val Muted = Color(0xFF8E8AA8)
private val Ember = Brush.linearGradient(listOf(Color(0xFFF7B072), Color(0xFFE2694A)))
private val EmberEdge = Color(0xFF8A3A22)

@Composable
fun DioramaDemo(modifier: Modifier = Modifier) {
    val state = rememberDioramaState()

    LaunchedEffect(Unit) {
        delay(900.milliseconds)
        state.raise()
    }

    Box(modifier.fillMaxSize().background(Night)) {
        Diorama(state = state, modifier = Modifier.fillMaxSize()) {
            WeatherScreen()
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(top = 12.dp, end = 16.dp)
                .clip(CircleShape)
                .background(Bone.copy(alpha = 0.14f))
                .clickable { state.toggle() }
                .padding(horizontal = 22.dp, vertical = 11.dp)
        ) {
            Text(
                if (state.raised) "FLATTEN" else "RAISE",
                color = Bone,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun WeatherScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("YAREMCHE", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
                Spacer(Modifier.height(2.dp))
                Text("Friday, 13 November", color = Bone, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Landscape()

        Spacer(Modifier.height(26.dp))
        Text("HOURLY", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Hours.forEachIndexed { index, hour ->
                HourChip(hour, now = index == 0, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(26.dp))
        Lifted(depth = 24.dp, shape = RoundedCornerShape(50), edge = EmberEdge) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Ember)
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Text("Plan a sunset hike", color = Night, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("THIS WEEK", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
        Spacer(Modifier.height(6.dp))
        Week.forEach { day -> DayRow(day) }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun Landscape() {
    val card = RoundedCornerShape(30.dp)
    val drift by rememberInfiniteTransition(label = "clouds").animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.86f)
            .diorama(depth = 14.dp, shape = card, edge = Color(0xFF2A1E3C))
            .clip(card)
            .background(Brush.verticalGradient(SkyColors))
            .drawBehind {
                Stars.forEach { (x, y, r) ->
                    drawCircle(Bone.copy(alpha = 0.7f), radius = r * density, center = Offset(x * size.width, y * size.height))
                }
            },
    ) {
        val w = maxWidth
        val h = maxHeight

        // Sun, sinking behind the far ridge, with a glow painted on the sky itself.
        Box(
            Modifier
                .offset(w * 0.5f, h * 0.3f)
                .size(w * 0.44f)
                .background(Brush.radialGradient(listOf(Color(0x66FFD59E), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier
                .offset(w * 0.61f, h * 0.41f)
                .size(w * 0.22f)
                .diorama(depth = 4.dp, shape = CircleShape, edge = Color(0xFFC0613C))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFEBB0), Color(0xFFFFA866))), CircleShape)
        )


        Ridge(0.46f, 10.dp, RidgeFar, listOf(Color(0xFF9A73A3), Color(0xFF6F5890)), Color(0xFF4A3A68))

        Cloud(x = w * 0.56f, y = h * 0.5f, width = w * 0.34f, depth = 22.dp, shift = 20.dp * -drift)

        Ridge(0.3f, 26.dp, RidgeNear, listOf(Color(0xFF392B5A), Color(0xFF2A2046)), Color(0xFF1B1530))
        Ridge(0.32f, 34.dp, RidgeFront, listOf(Color(0xFF1F1A38), Color(0xFF120F22)), Color(0xFF07060F))

        Column(
            Modifier
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .diorama(depth = 42.dp)
        ) {
            Text(
                "18°",
                style = TextStyle(
                    color = Bone,
                    fontSize = 86.sp,
                    fontWeight = FontWeight.Light,
                    shadow = Shadow(Color(0x66000000), Offset(0f, 14f), 24f),
                ),
            )
            Text(
                "Clear · sunset 16:24",
                style = TextStyle(
                    color = Bone.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    shadow = Shadow(Color(0x66000000), Offset(0f, 6f), 12f),
                ),
            )
        }
    }
}

/** A sheet of mountains that runs past the card's edges, so tilting never reveals its sides. */
@Composable
private fun BoxWithConstraintsScope.Ridge(
    height: Float,
    depth: Dp,
    shape: Shape,
    colors: List<Color>,
    edge: Color,
) {
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .offset(y = OVERSCAN)
            .requiredWidth(maxWidth + OVERSCAN * 2)
            .height(maxHeight * height + OVERSCAN)
            .diorama(depth, shape, edge)
            .background(Brush.verticalGradient(colors), shape)
    )
}

@Composable
private fun Cloud(x: Dp, y: Dp, width: Dp, depth: Dp, shift: Dp) {
    Box(
        Modifier
            .offset { IntOffset((x + shift).roundToPx(), y.roundToPx()) }
            .size(width, width * 0.42f)
            .diorama(depth, CloudShape, Color(0xFFB0828E))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE6D8), Color(0xFFF2B9A8))), CloudShape)
    )
}

private val OVERSCAN = 32.dp

private val SkyColors = listOf(
    Color(0xFF15183F),
    Color(0xFF3D3473),
    Color(0xFFA35A7E),
    Color(0xFFF29A6E),
    Color(0xFFFFC98A),
)

private val Stars = listOf(
    Triple(0.62f, 0.06f, 1.4f), Triple(0.78f, 0.12f, 1f), Triple(0.9f, 0.05f, 1.6f),
    Triple(0.7f, 0.2f, 0.9f), Triple(0.86f, 0.24f, 1.2f), Triple(0.55f, 0.15f, 0.8f),
    Triple(0.95f, 0.17f, 0.9f), Triple(0.47f, 0.05f, 1.1f),
)

private class Pine(val x: Float, val size: Float)

/**
 * A shape built from overlapping pieces, merged into a single outline so its paper edge has no
 * seams inside. Merging paths is costly, so each size is built once.
 */
private class PaperShape(private val pieces: (Size) -> List<Path>) : Shape {
    private val cache = HashMap<Size, Outline>()

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        cache.getOrPut(size) {
            if (cache.size > 8) cache.clear()
            val merged = pieces(size).reduce { acc, piece -> Path().apply { op(acc, piece, PathOperation.Union) } }
            Outline.Generic(merged)
        }
}

/** A smooth ridge through [peaks] (fractions of the height, from the top), with optional pines. */
private fun ridge(peaks: List<Float>, pines: List<Pine> = emptyList()) = PaperShape { size ->
    val w = size.width
    val h = size.height
    val step = w / (peaks.size - 1)
    fun y(i: Int) = peaks[i] * h

    val land = Path().apply {
        moveTo(0f, h)
        lineTo(0f, y(0))
        for (i in 1 until peaks.size - 1) {
            quadraticTo(i * step, y(i), (i + 0.5f) * step, (y(i) + y(i + 1)) / 2f)
        }
        lineTo(w, y(peaks.lastIndex))
        lineTo(w, h)
        close()
    }

    listOf(land) + pines.map { pine ->
        val cx = pine.x * w
        val f = cx / step
        val i = f.toInt().coerceAtMost(peaks.size - 2)
        val ground = y(i) + (y(i + 1) - y(i)) * (f - i)
        val tall = pine.size * h
        val base = ground + tall * 0.2f
        Path().apply {
            PineOutline.forEachIndexed { index, (x, y) ->
                if (index == 0) moveTo(cx + x * tall, base + y * tall) else lineTo(cx + x * tall, base + y * tall)
            }
            close()
        }
    }
}

/** A three-tier pine as one polygon, in units of its height, from the tip clockwise. */
private val PineOutline = listOf(
    0f to -1f, 0.17f to -0.64f, 0.08f to -0.64f, 0.25f to -0.3f, 0.13f to -0.3f, 0.33f to 0.02f,
    -0.33f to 0.02f, -0.13f to -0.3f, -0.25f to -0.3f, -0.08f to -0.64f, -0.17f to -0.64f,
)

private val RidgeFar = ridge(listOf(0.34f, 0.1f, 0.28f, 0.04f, 0.22f, 0.14f, 0.38f))
private val RidgeNear = ridge(listOf(0.16f, 0.4f, 0.2f, 0.46f, 0.1f, 0.3f))
private val RidgeFront = ridge(
    peaks = listOf(0.62f, 0.5f, 0.66f, 0.56f, 0.7f),
    pines = listOf(
        Pine(0.1f, 0.42f), Pine(0.17f, 0.3f), Pine(0.24f, 0.36f),
        Pine(0.7f, 0.4f), Pine(0.78f, 0.3f), Pine(0.86f, 0.46f), Pine(0.93f, 0.32f),
    ),
)

private val CloudShape = PaperShape { size ->
    val w = size.width
    val h = size.height
    listOf(
        Path().apply { addRoundRect(RoundRect(0f, h * 0.5f, w, h, CornerRadius(h * 0.25f))) },
        Path().apply { addOval(Rect(w * 0.08f, h * 0.28f, w * 0.46f, h * 0.96f)) },
        Path().apply { addOval(Rect(w * 0.28f, 0f, w * 0.72f, h * 0.9f)) },
        Path().apply { addOval(Rect(w * 0.54f, h * 0.22f, w * 0.92f, h * 0.94f)) },
    )
}

// endregion

// region Plain UI below — the same modifier on ordinary cards

private data class Hour(val time: String, val glyph: String, val temp: Int)
private data class Day(val name: String, val glyph: String, val low: Int, val high: Int)

private val Hours = listOf(
    Hour("Now", "☀", 18), Hour("17", "☀", 16), Hour("18", "☾", 13), Hour("19", "☁", 11), Hour("20", "☾", 9),
)

private val Week = listOf(
    Day("Today", "☀", 7, 18), Day("Sat", "☁", 5, 14), Day("Sun", "☂", 4, 11),
    Day("Mon", "☁", 3, 12), Day("Tue", "☀", 6, 16), Day("Wed", "☀", 8, 19),
)

@Composable
private fun HourChip(hour: Hour, now: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .diorama(if (now) 24.dp else 18.dp, shape, if (now) EmberEdge else Color(0xFF2A2840))
            .clip(shape)
            .background(if (now) Ember else SolidColor(Panel))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(hour.time, color = if (now) Night else Muted, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp))
        Text(hour.glyph, color = if (now) Night else Bone, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text("${hour.temp}°", color = if (now) Night else Bone, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DayRow(day: Day) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(day.name, color = Bone, fontSize = 15.sp, modifier = Modifier.width(64.dp))
        Text(day.glyph, color = Bone, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text("${day.low}°", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .width(96.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(Panel)
                .drawBehind {
                    val start = (day.low - 2) / 20f * size.width
                    val end = (day.high - 2) / 20f * size.width
                    drawRoundRect(
                        Brush.horizontalGradient(listOf(Color(0xFF7C6BC4), Color(0xFFF29A6E)), start, end),
                        topLeft = Offset(start, 0f),
                        size = Size(end - start, size.height),
                        cornerRadius = CornerRadius(size.height / 2f),
                    )
                }
        )
        Spacer(Modifier.width(10.dp))
        Text("${day.high}°", color = Bone, fontSize = 14.sp)
    }
}

// endregion
