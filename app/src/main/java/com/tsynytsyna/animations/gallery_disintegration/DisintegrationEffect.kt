package com.tsynytsyna.animations.gallery_disintegration

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Stable
class DisintegrationState(private val durationMs: Int = 2500) {
    internal var triggered by mutableStateOf(false)
        private set
    internal var progress by mutableFloatStateOf(0f)
    internal var particles by mutableStateOf<List<Particle>>(emptyList())
    internal var strips by mutableStateOf<List<Strip>>(emptyList())
    internal var onDone: (() -> Unit)? = null
    internal val duration get() = durationMs

    fun disintegrate(onComplete: () -> Unit = {}) {
        onDone = onComplete
        triggered = true
    }

    internal fun finish() {
        onDone?.invoke()
        triggered = false
        progress = 0f
        particles = emptyList()
        strips = emptyList()
    }
}

@Composable
fun rememberDisintegrationState(durationMs: Int = 2500) =
    remember { DisintegrationState(durationMs) }

@Composable
fun Disintegration(
    state: DisintegrationState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(state.triggered) {
        if (state.triggered.not()) return@LaunchedEffect
        val bitmap = graphicsLayer.toImageBitmap()

        val result = withContext(Dispatchers.Default) {
            buildEffect(bitmap)
        }
        state.particles = result.first
        state.strips = result.second
        val startTime = withFrameMillis { it }
        while (true) {
            val elapsed = withFrameMillis { it } - startTime
            val p = (elapsed.toFloat() / state.duration).coerceAtMost(1f)
            state.progress = p
            if (p >= 1f) break
        }
        state.finish()
    }

    Box(modifier = modifier.drawWithContent {
        graphicsLayer.record { this@drawWithContent.drawContent() }
        if (!state.triggered) {
            drawLayer(graphicsLayer)
        } else if (state.strips.isEmpty()) {
            drawLayer(graphicsLayer)
        } else {
            drawStrips(state.strips, state.progress)
            drawParticles(state.particles, state.progress)
        }
    }) {
        content()
    }
}

internal class Particle(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float,
    val delay: Float,
    val cosAngle: Float,
    val sinAngle: Float,
    val speed: Float,
    val wobble: Float,
)

internal class Strip(
    val bitmap: ImageBitmap,
    val x: Int,
    val width: Int,
    val height: Int,
    val delay: Float,
)

private fun DrawScope.drawStrips(strips: List<Strip>, p: Float) {
    val paint = Paint()
    drawIntoCanvas { canvas ->
        for (strip in strips) {
            val localP = ((p - strip.delay) / 0.3f).coerceIn(0f, 1f)
            val alpha = 1f - localP
            if (alpha <= 0f) continue
            paint.alpha = alpha
            canvas.drawImageRect(
                image = strip.bitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(strip.bitmap.width, strip.bitmap.height),
                dstOffset = IntOffset(strip.x, 0),
                dstSize = IntSize(strip.width, strip.height),
                paint = paint
            )
        }
    }
}

private fun DrawScope.drawParticles(particles: List<Particle>, p: Float) {
    val size = particles.size
    for (i in 0 until size) {
        val pt = particles[i]
        if (p <= pt.delay) continue
        val localP = ((p - pt.delay) / (1f - pt.delay)).coerceAtMost(1f)

        val drift = pt.speed * localP
        val wx = sin(localP * 12f + pt.wobble) * 20f * localP
        val dx = pt.cosAngle * drift + wx
        val dy = pt.sinAngle * drift
        val alpha = 1f - localP * localP
        if (alpha <= 0f) continue

        drawCircle(
            color = pt.color,
            radius = pt.radius,
            center = Offset(pt.x + dx, pt.y + dy),
            alpha = alpha
        )
    }
}

private fun buildEffect(source: ImageBitmap): Pair<List<Particle>, List<Strip>> {
    val hardwareBmp = source.asAndroidBitmap()
    val bmp = hardwareBmp.copy(Bitmap.Config.ARGB_8888, false)
    val w = bmp.width
    val h = bmp.height

    val numStrips = 20
    val stripWidth = w / numStrips
    val strips = buildList {
        for (i in 0 until numStrips) {
            val x = i * stripWidth
            val sw = if (i == numStrips - 1) w - x else stripWidth
            val stripBmp = Bitmap.createBitmap(bmp, x, 0, sw, h)
            val normalizedX = x.toFloat() / w
            add(
                Strip(
                    bitmap = stripBmp.asImageBitmap(),
                    x = x,
                    width = sw,
                    height = h,
                    delay = normalizedX * 0.6f,
                )
            )
        }
    }

    val step = 10
    val particles = buildList {
        for (y in 0 until h step step) {
            for (x in 0 until w step step) {
                val pixel = bmp[x, y]
                if (android.graphics.Color.alpha(pixel) < 50) continue
                val normalizedX = x.toFloat() / w
                val angle = -0.8f + Random.nextFloat() * 0.5f
                add(
                    Particle(
                        x = x.toFloat(),
                        y = y.toFloat(),
                        color = Color(pixel),
                        radius = step / 2f + Random.nextFloat() * 2f,
                        delay = normalizedX * 0.6f + Random.nextFloat() * 0.1f,
                        cosAngle = cos(angle),
                        sinAngle = sin(angle),
                        speed = 120f + Random.nextFloat() * 200f,
                        wobble = Random.nextFloat() * 6.28f,
                    )
                )
            }
        }
    }

    return particles to strips
}
