package com.tsynytsyna.animations.glass_shatter

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

@Stable
class GlassShatterState(
    private val durationMs: Int = 1800
) {
    internal var triggered by mutableStateOf(false)
    internal var progress by mutableFloatStateOf(0f)
    internal var shards by mutableStateOf<List<GlassShard>>(emptyList())
    internal var impactPoint by mutableStateOf(Offset.Zero)
    internal var captured by mutableStateOf<ImageBitmap?>(null)

    internal var onDone: (() -> Unit)? = null

    fun shatter(point: Offset, onComplete: () -> Unit = {}) {
        if (triggered) return
        onDone = onComplete
        impactPoint = point
        triggered = true
    }

    internal fun finish() {
        onDone?.invoke()
        onDone = null
        triggered = false
        progress = 0f
        shards = emptyList()
        captured = null
    }

    internal val duration get() = durationMs
}

@Composable
fun rememberGlassShatterState(
    durationMs: Int = 1800
): GlassShatterState = remember {
    GlassShatterState(durationMs)
}

internal data class GlassShard(
    val path: Path,
    val centroid: Offset,
    val velocity: Offset,
    val rotationSpeed: Float,
    val flipSpeed: Float,
    val delay: Float,
)

@Composable
fun GlassShatter(
    state: GlassShatterState,
    modifier: Modifier = Modifier,
    tapEnabled: Boolean = true,
    onShatterComplete: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(state.triggered) {
        if (state.triggered.not()) return@LaunchedEffect

        val bitmap = graphicsLayer.toImageBitmap()
        state.captured = bitmap

        state.shards = withContext(Dispatchers.Default) {
            generateShards(
                center = state.impactPoint,
                width = bitmap.width.toFloat(),
                height = bitmap.height.toFloat(),
            )
        }

        val start = withFrameMillis { it }
        while (true) {
            val elapsed = withFrameMillis { it } - start
            val p = (elapsed.toFloat() / state.duration).coerceAtMost(1f)
            state.progress = p
            if (p >= 1f) break
        }

        state.finish()
    }

    Box(
        modifier = modifier
            .then(
                if (tapEnabled) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            state.shatter(it, onComplete = onShatterComplete)
                        }
                    }
                } else Modifier
            )
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                val image = state.captured
                if (state.triggered.not() || state.shards.isEmpty() || image == null) {
                    drawLayer(graphicsLayer)
                } else {
                    drawShards(
                        image = image,
                        shards = state.shards,
                        progress = state.progress,
                    )
                }
            }
    ) {
        content()
    }
}

private fun generateShards(
    center: Offset,
    width: Float,
    height: Float,
): List<GlassShard> {

    val sites = ArrayList<Offset>()
    val maxR = hypot(width, height)

    val spokes = 8
    val startR = maxR * 0.07f
    val ratio = 1.8f
    val angJitter = 0.55f
    for (s in 0 until spokes) {
        val baseAng = s / spokes.toFloat() * TWO_PI
        var r = startR
        while (r < maxR * 0.95f) {
            val ang = baseAng + (Random.nextFloat() - 0.5f) * angJitter
            val rr = r * (0.82f + Random.nextFloat() * 0.36f)
            sites += Offset(center.x + cos(ang) * rr, center.y + sin(ang) * rr)
            r *= ratio
        }
    }
    repeat(6) {
        sites += Offset(Random.nextFloat() * width, Random.nextFloat() * height)
    }

    val bounds = listOf(
        Offset(0f, 0f),
        Offset(width, 0f),
        Offset(width, height),
        Offset(0f, height),
    )

    val shards = ArrayList<GlassShard>(sites.size)

    for (i in sites.indices) {
        val si = sites[i]
        var cell: List<Offset> = bounds

        for (j in sites.indices) {
            if (i == j) continue
            val sj = sites[j]
            val mid = Offset((si.x + sj.x) / 2f, (si.y + sj.y) / 2f)
            val normal = Offset(sj.x - si.x, sj.y - si.y) // points toward sj
            cell = clipHalfPlane(cell, mid, normal)
            if (cell.size < 3) break
        }
        if (cell.size < 3) continue

        val path = Path().apply {
            moveTo(cell[0].x, cell[0].y)
            for (k in 1 until cell.size) lineTo(cell[k].x, cell[k].y)
            close()
        }

        var cx = 0f
        var cy = 0f
        for (p in cell) {
            cx += p.x
            cy += p.y
        }
        val centroid = Offset(cx / cell.size, cy / cell.size)

        var dir = centroid - center
        val len = hypot(dir.x, dir.y)
        dir = if (len < 1f) {
            val a = Random.nextFloat() * TWO_PI
            Offset(cos(a), sin(a))
        } else {
            Offset(dir.x / len, dir.y / len)
        }

        val normDist = (len / (maxR * 0.6f)).coerceIn(0f, 1f)

        val speed = 520f * (1f - 0.55f * normDist) + Random.nextFloat() * 180f

        shards += GlassShard(
            path = path,
            centroid = centroid,
            velocity = Offset(dir.x * speed, dir.y * speed),
            rotationSpeed = -240f + Random.nextFloat() * 480f,
            flipSpeed = 1.6f + Random.nextFloat() * 2.2f,
            delay = normDist * 0.3f, // crack propagates outward
        )
    }

    return shards
}

private fun clipHalfPlane(poly: List<Offset>, point: Offset, normal: Offset): List<Offset> {
    if (poly.isEmpty()) return poly
    val out = ArrayList<Offset>(poly.size + 2)

    fun side(p: Offset) = (p.x - point.x) * normal.x + (p.y - point.y) * normal.y

    for (i in poly.indices) {
        val cur = poly[i]
        val prev = poly[(i + poly.size - 1) % poly.size]
        val curIn = side(cur) <= 0f
        val prevIn = side(prev) <= 0f

        if (curIn) {
            if (!prevIn) out += intersect(prev, cur, point, normal)
            out += cur
        } else if (prevIn) {
            out += intersect(prev, cur, point, normal)
        }
    }
    return out
}

private fun intersect(a: Offset, b: Offset, point: Offset, normal: Offset): Offset {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val denom = dx * normal.x + dy * normal.y
    if (denom == 0f) return a
    val t = ((point.x - a.x) * normal.x + (point.y - a.y) * normal.y) / denom
    return Offset(a.x + dx * t, a.y + dy * t)
}

private fun DrawScope.drawShards(
    image: ImageBitmap,
    shards: List<GlassShard>,
    progress: Float,
) {
    shards.forEach { shard ->
        val lp = ((progress - shard.delay) / (1f - shard.delay)).coerceIn(0f, 1f)
        if (lp <= 0f) {
            clipPath(shard.path) { drawImage(image = image) }
            return@forEach
        }

        val eased = 1f - (1f - lp) * (1f - lp)
        val dx = shard.velocity.x * eased
        val dy = shard.velocity.y * eased + 900f * lp * lp // gravity
        val alpha = (1f - lp * lp).coerceIn(0f, 1f)
        val flip = cos(lp * shard.flipSpeed)
        val shrink = 1f - 0.25f * lp

        withTransform({
            translate(dx, dy)
            rotate(degrees = shard.rotationSpeed * eased, pivot = shard.centroid)
            scale(scaleX = flip * shrink, scaleY = shrink, pivot = shard.centroid)
        }) {
            clipPath(shard.path) {
                drawImage(image = image, alpha = alpha)
            }
            drawPath(
                path = shard.path,
                color = Color.White.copy(alpha = alpha * 0.35f),
                style = Stroke(width = 1.5f),
            )
        }
    }
}

private const val TWO_PI = (Math.PI * 2.0).toFloat()