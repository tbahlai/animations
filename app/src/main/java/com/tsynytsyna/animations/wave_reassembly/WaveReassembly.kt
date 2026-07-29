package com.tsynytsyna.animations.wave_reassembly

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Wave Reassembly — the image is reconstructed from tiles that fly in from all directions.
 *
 * Each tile follows the same curved trajectory and magnetic landing as in
 * [com.tsynytsyna.animations.wave_reassembly]. The difference is not *how* tiles move,
 * but *when* they appear: their animation is triggered by a radial wave that expands
 * from the centre toward the edges.
 *
 * At any moment:
 * - Inside the wave, the image is already fully assembled and rendered as one clipped
 *   `drawImage`.
 * - Along the wave front, only a narrow band of tiles is actively flying into place.
 * - Outside the wave, tiles have not started yet.
 *
 * Limiting animation to this moving band dramatically reduces rendering work. Effects
 * that keep every tile alive for the entire animation—such as `magnetic_reassemble`,
 * `marvel_effects/ReverseSnap`, or `gallery_disintegration`—draw every tile on every
 * frame (~17,000 draw calls for a full-screen image, measured at 28–42 ms/frame on a
 * Galaxy S21). Here, only tiles inside the [BAND]-wide wave are animated, while the
 * assembled area is rendered with a single `drawImage`.
 */
@Stable
class WaveReassemblyState(private val durationMs: Int = DEFAULT_DURATION_MS) {

    internal enum class Phase { Idle, Capturing, Animating }

    internal var phase by mutableStateOf(Phase.Idle)
        private set

    /** Bumped per run: the runner moves the phase itself, so it cannot key off the phase. */
    internal var runId by mutableIntStateOf(0)
        private set

    internal var progress by mutableFloatStateOf(0f)

    /** Kept across runs — the buffers are sized by the container, which does not change. */
    internal var field: CellField? = null
        private set

    internal var captured by mutableStateOf<ImageBitmap?>(null)
    internal var onDone: (() -> Unit)? = null
    internal val duration get() = durationMs

    val isRunning: Boolean get() = phase != Phase.Idle

    /**
     * Hides the current content and re-forms it from flying tiles.
     *
     * Call this *after* the content to reveal is composed — the effect snapshots whatever
     * the container holds and rebuilds exactly that.
     */
    fun assemble(onComplete: () -> Unit = {}) {
        if (phase != Phase.Idle) return
        onDone = onComplete
        progress = 0f
        phase = Phase.Capturing
        runId++
    }

    internal fun prewarm(built: CellField) {
        if (phase == Phase.Idle && field == null) {
            built.count = 0
            field = built
        }
    }

    internal fun startAnimating(built: CellField) {
        field = built
        phase = Phase.Animating
    }

    internal fun finish() {
        phase = Phase.Idle
        progress = 0f
        field?.count = 0
        captured = null
        onDone?.invoke()
        onDone = null
    }
}

@Composable
fun rememberWaveReassemblyState(durationMs: Int = DEFAULT_DURATION_MS) =
    remember { WaveReassemblyState(durationMs) }

/**
 * @param captureFrames frames to wait before snapshotting, so freshly composed content has
 * been drawn into the layer at least once.
 * @param tileSize side of one sampled cell in pixels. The cost dial, quadratic in tile
 * count: 10 is fine-grained, 14 halves the work.
 */
@Composable
fun WaveReassembly(
    state: WaveReassemblyState,
    modifier: Modifier = Modifier,
    captureFrames: Int = DEFAULT_CAPTURE_FRAMES,
    tileSize: Int = DEFAULT_TILE_SIZE,
    content: @Composable () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    // One silent dry run at startup, so the first real run does not pay for JIT, heap growth
    // and the readback all at once while nothing is on screen. Retried because the content
    // is often still blank this early — a transparent layer yields no cells, which warms the
    // sampling loop but never the half that fills the buffers.
    LaunchedEffect(Unit) {
        repeat(captureFrames + WARM_UP_EXTRA_FRAMES) { withFrameMillis { it } }
        repeat(WARM_UP_ATTEMPTS) {
            if (state.phase != WaveReassemblyState.Phase.Idle) return@LaunchedEffect
            val layerSize = graphicsLayer.size
            if (layerSize.width != 0 && layerSize.height != 0) {
                val bitmap = graphicsLayer.toImageBitmap()
                val warmed =
                    withContext(Dispatchers.Default) { buildCells(bitmap, null, tileSize) }
                if (warmed != null && warmed.count > 0) {
                    state.prewarm(warmed)
                    return@LaunchedEffect
                }
            }
            repeat(WARM_UP_RETRY_FRAMES) { withFrameMillis { it } }
        }
    }

    LaunchedEffect(state.runId) {
        if (state.phase != WaveReassemblyState.Phase.Capturing) return@LaunchedEffect

        repeat(captureFrames) { withFrameMillis { it } }
        val bitmap = graphicsLayer.toImageBitmap()
        val built =
            withContext(Dispatchers.Default) { buildCells(bitmap, state.field, tileSize) }
        if (built == null || built.count == 0) {
            state.finish()
            return@LaunchedEffect
        }
        state.captured = bitmap
        state.startAnimating(built)

        val startTime = withFrameMillis { it }
        while (true) {
            val elapsed = withFrameMillis { it } - startTime
            val progress = (elapsed.toFloat() / state.duration).coerceAtMost(1f)
            state.progress = progress
            if (progress >= 1f) break
        }
        state.finish()
    }

    Box(modifier = modifier.drawWithContent {
        // Once captured, the content is frozen and hidden — re-recording it is pure waste.
        if (state.phase != WaveReassemblyState.Phase.Animating) {
            graphicsLayer.record { this@drawWithContent.drawContent() }
        }
        when (state.phase) {
            WaveReassemblyState.Phase.Idle -> drawLayer(graphicsLayer)

            // Nothing on screen yet — the cells have not been built.
            WaveReassemblyState.Phase.Capturing -> Unit

            WaveReassemblyState.Phase.Animating -> {
                val field = state.field
                val image = state.captured
                if (field != null && image != null) {
                    drawWave(image, field, state.progress)
                }
            }
        }
    }) {
        content()
    }
}


/** Length of the whole sweep. */
private const val DEFAULT_DURATION_MS = 1400

/** Frames to let the content lay out and reach the layer before snapshotting it. */
private const val DEFAULT_CAPTURE_FRAMES = 2

/** Extra frames the startup dry run waits on top of [DEFAULT_CAPTURE_FRAMES]. */
private const val WARM_UP_EXTRA_FRAMES = 2

/** How many times the startup dry run retries while the content is still blank. */
private const val WARM_UP_ATTEMPTS = 5

/** Frames to wait between dry-run attempts. */
private const val WARM_UP_RETRY_FRAMES = 12


/** Width of the live annulus, as a fraction of the centre-to-corner distance. Cost dial. */
private const val BAND = 0.3f


/** Side of one sampled cell, in pixels. Quadratic in tile count, so the other cost dial. */
private const val DEFAULT_TILE_SIZE = 10

/** Stride inside one cell when averaging its colour. */
private const val SUB_SAMPLE = 3

/** Cells fainter than this are dropped as transparent, on a 0..255 scale. */
private const val MIN_CELL_ALPHA = 50

/** Full opacity on the same 0..255 scale, for packing alpha into the colour. */
private const val ALPHA_MAX = 255


/** Where tiles spawn, as a multiple of the centre-to-corner distance: just off-frame. */
private const val SPAWN_RING = 1.15f

/** Random extra distance beyond [SPAWN_RING], as a fraction of it. */
private const val SPAWN_DEPTH_JITTER = 0.35f

/** Random spread of the spawn angle around the tile's own radial line, in radians. */
private const val SPAWN_ANGLE_JITTER = 0.55f

/** Sideways bow of the flight path, in pixels: the swirl on the way in. */
private const val BOW_MIN = 50f
private const val BOW_RANGE = 90f

/** How fast a tile reaches full opacity, as a multiple of its own flight progress. */
private const val FADE_IN_RATE = 6f

/** How much larger a tile is at the start of its flight, before it contracts to one cell. */
private const val OVERSIZE = 1.6f

/**
 * Overshoot of the landing snap, as a fraction of the distance travelled.
 *
 * Deliberately tiny — the 1.70158 that `easeOutBack` conventionally uses overshoots by ~10%,
 * which on a flight across the whole frame is hundreds of pixels of visible bounce.
 */
private const val LANDING_OVERSHOOT = 0.4f

/** Cells as parallel primitive arrays: no per-cell objects, contiguous in the draw loop. */
internal class CellField(val capacity: Int) {
    var count = 0

    /** Where the tile flies in from — outside the frame, on the cell's own radial line. */
    val startX = FloatArray(capacity)
    val startY = FloatArray(capacity)

    /** Centre of the source cell, so landed tiles abut exactly. */
    val targetX = FloatArray(capacity)
    val targetY = FloatArray(capacity)

    /** Perpendicular bow of the flight path, already scaled — gives the curved approach. */
    val curveX = FloatArray(capacity)
    val curveY = FloatArray(capacity)

    /** Distance from the centre, as a fraction of centre-to-corner: when the ring arrives. */
    val radius = FloatArray(capacity)

    /** Packed ARGB, so no Color objects are held. */
    val color = IntArray(capacity)
    val size = FloatArray(capacity)

    /** Centre and reach of the ring, from the captured frame. */
    var centerX = 0f
    var centerY = 0f
    var maxRadius = 1f

    /** One Paint, mutated per tile. No antialiasing: the tiles are axis-aligned squares. */
    val paint = android.graphics.Paint().apply { isAntiAlias = false }

    /** Reused clip for the assembled disc, rewound each frame instead of reallocated. */
    val disc = Path()
}

private fun DrawScope.drawWave(
    image: ImageBitmap,
    field: CellField,
    progress: Float,
) {
    val width = size.width
    val height = size.height

    // Ease the ring so it does not start and stop dead. It has to travel the full reach
    // *plus* one band, or the corners never finish assembling.
    val easedProgress = progress * progress * (3f - 2f * progress)
    val front = easedProgress * (1f + BAND)

    // Everything the ring has passed is the real photo — one draw, not a mosaic.
    val landedRadius = (front - BAND) * field.maxRadius
    if (landedRadius > 0f) {
        val disc = field.disc
        disc.rewind()
        disc.addOval(Rect(Offset(field.centerX, field.centerY), landedRadius))
        clipPath(disc) {
            drawImage(image = image)
        }
    }

    val paint = field.paint

    drawIntoCanvas { canvas ->
        val androidCanvas = canvas.nativeCanvas

        for (i in 0 until field.count) {
            // Live only inside the ring. Inside it: already covered by the clipped image
            // above. Outside: not started. This is what keeps the frame cheap.
            val tileProgress = (front - field.radius[i]) / BAND
            if (tileProgress <= 0f || tileProgress >= 1f) continue

            val travelled = magneticEase(tileProgress)
            val arc = sin(tileProgress * PI.toFloat())
            val spawnX = field.startX[i]
            val spawnY = field.startY[i]
            val tileX = spawnX + (field.targetX[i] - spawnX) * travelled + field.curveX[i] * arc
            val tileY = spawnY + (field.targetY[i] - spawnY) * travelled + field.curveY[i] * arc

            // Fade in fast on launch, then hold — arrival should read as landing.
            val alpha = (tileProgress * FADE_IN_RATE).coerceAtMost(1f)

            // Tiles fly in oversized and contract to exactly one cell, so the landed
            // mosaic is gapless and the join with the sharp disc is invisible.
            val remaining = 1f - tileProgress
            val drawnSize = field.size[i] * (1f + OVERSIZE * remaining * remaining)
            val halfSize = drawnSize / 2f
            val left = tileX - halfSize
            val top = tileY - halfSize
            val right = tileX + halfSize
            val bottom = tileY + halfSize
            // Tiles spawn well outside the frame, so early in each flight many contribute
            // nothing. Four comparisons are cheaper than the draw they would cost.
            if (right < 0f || left > width || bottom < 0f || top > height) continue

            // Alpha rides in the color's own channel, so the paint is set once per tile.
            val alphaByte = (alpha * ALPHA_MAX).toInt().coerceIn(0, ALPHA_MAX)
            paint.color = (alphaByte shl 24) or (field.color[i] and 0x00FFFFFF)

            androidCanvas.drawRect(left, top, right, bottom, paint)
        }
    }
}

/**
 * Slow drift that accelerates into a snap, overshooting by [LANDING_OVERSHOOT].
 *
 * This is `easeOutBack` applied to pre-warped time, so a tile hangs back before being pulled
 * in rather than leaving at full speed.
 */
private fun magneticEase(t: Float): Float {
    val warped = t * t * (2f - t * t)
    val fromEnd = warped - 1f
    return 1f + (LANDING_OVERSHOOT + 1f) * fromEnd * fromEnd * fromEnd +
        LANDING_OVERSHOOT * fromEnd * fromEnd
}

private fun buildCells(source: ImageBitmap, reuse: CellField?, cellSize: Int): CellField? {
    val original = source.asAndroidBitmap()
    // toImageBitmap() hands back a HARDWARE bitmap on most devices, which can be neither
    // read nor drawn into a software canvas. The one copy is recycled straight after.
    val copied = original.config == Bitmap.Config.HARDWARE
    val bitmap = if (copied) {
        original.copy(Bitmap.Config.ARGB_8888, false) ?: return null
    } else {
        original
    }
    val sourceWidth = bitmap.width
    val sourceHeight = bitmap.height
    if (sourceWidth == 0 || sourceHeight == 0) {
        if (copied) bitmap.recycle()
        return null
    }

    val capacity = ((sourceWidth + cellSize - 1) / cellSize) *
        ((sourceHeight + cellSize - 1) / cellSize)
    val field = if (reuse != null && reuse.capacity == capacity) reuse else CellField(capacity)
    field.count = 0

    val centerX = sourceWidth / 2f
    val centerY = sourceHeight / 2f
    val maxRadius = hypot(centerX, centerY)
    field.centerX = centerX
    field.centerY = centerY
    field.maxRadius = maxRadius

    // Every tile starts outside the frame, on a ring around the content.
    val spawnRadius = maxRadius * SPAWN_RING
    // All tiles bow the same way; mixing directions reads as noise, not as a field.
    val swirl = if (Random.nextBoolean()) 1f else -1f
    val cellSizeF = cellSize.toFloat()

    // One reusable band of `cellSize` rows: height/cellSize calls to getPixels instead of one
    // getPixel per cell, and each cell takes the average of its own area rather than a single
    // corner pixel, so the mosaic is smooth instead of aliased.
    val band = IntArray(sourceWidth * cellSize)
    var count = 0

    for (cellTop in 0 until sourceHeight step cellSize) {
        val cellRows = minOf(cellSize, sourceHeight - cellTop)
        bitmap.getPixels(band, 0, sourceWidth, 0, cellTop, sourceWidth, cellRows)

        for (cellLeft in 0 until sourceWidth step cellSize) {
            val cellCols = minOf(cellSize, sourceWidth - cellLeft)

            var alphaSum = 0
            var redSum = 0
            var greenSum = 0
            var blueSum = 0
            var sampleCount = 0
            var sampleY = 0
            while (sampleY < cellRows) {
                val rowStart = sampleY * sourceWidth + cellLeft
                var sampleX = 0
                while (sampleX < cellCols) {
                    val pixel = band[rowStart + sampleX]
                    alphaSum += (pixel ushr 24) and 0xFF
                    redSum += (pixel ushr 16) and 0xFF
                    greenSum += (pixel ushr 8) and 0xFF
                    blueSum += pixel and 0xFF
                    sampleCount++
                    sampleX += SUB_SAMPLE
                }
                sampleY += SUB_SAMPLE
            }
            if (sampleCount == 0) continue

            val averageAlpha = alphaSum / sampleCount
            if (averageAlpha < MIN_CELL_ALPHA) continue

            val tileCenterX = cellLeft + cellSizeF / 2f
            val tileCenterY = cellTop + cellSizeF / 2f

            val fromCenterX = tileCenterX - centerX
            val fromCenterY = tileCenterY - centerY

            // Spawn just outside the frame on the tile's *own* radial line, with a little
            // angular jitter. Neighbours then travel together as coherent sheets instead of
            // criss-crossing the whole frame.
            val spawnAngle = atan2(fromCenterY, fromCenterX) +
                (Random.nextFloat() - 0.5f) * SPAWN_ANGLE_JITTER
            val spawnDistance = spawnRadius * (1f + Random.nextFloat() * SPAWN_DEPTH_JITTER)
            val spawnX = centerX + cos(spawnAngle) * spawnDistance
            val spawnY = centerY + sin(spawnAngle) * spawnDistance

            // Bow every path the same way around the centre: the swarm spirals in.
            val toTargetX = tileCenterX - spawnX
            val toTargetY = tileCenterY - spawnY
            val flightLength = sqrt(toTargetX * toTargetX + toTargetY * toTargetY)
                .coerceAtLeast(1f)
            val bow = (BOW_MIN + Random.nextFloat() * BOW_RANGE) * swirl

            with(field) {
                startX[count] = spawnX
                startY[count] = spawnY
                targetX[count] = tileCenterX
                targetY[count] = tileCenterY
                curveX[count] = -toTargetY / flightLength * bow
                curveY[count] = toTargetX / flightLength * bow
                radius[count] = hypot(fromCenterX, fromCenterY) / maxRadius
                color[count] = (averageAlpha shl 24) or
                        ((redSum / sampleCount) shl 16) or
                        ((greenSum / sampleCount) shl 8) or
                        (blueSum / sampleCount)
                size[count] = cellSizeF
            }
            count++
        }
    }

    field.count = count
    if (copied) bitmap.recycle()
    return field
}
