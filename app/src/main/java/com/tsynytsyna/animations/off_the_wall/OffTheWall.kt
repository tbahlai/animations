package com.tsynytsyna.animations.off_the_wall

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.createBitmap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Stable
class OffTheWallState internal constructor() {

    internal var sheet by mutableStateOf<Sheet?>(null)

    internal var tick by mutableIntStateOf(0)

    var awake by mutableStateOf(false)
        internal set

    var pinned by mutableStateOf(true)
        internal set

    var dismissed by mutableStateOf(false)
        internal set

    internal fun wake() {
        if (sheet != null) awake = true
    }

    fun drop() {
        sheet?.unpin()
        pinned = false
        wake()
    }

    fun reset() {
        sheet?.reset()
        pinned = true
        dismissed = false
        awake = false
    }
}

@Composable
fun rememberOffTheWallState() = remember { OffTheWallState() }

@Composable
fun OffTheWall(
    state: OffTheWallState,
    modifier: Modifier = Modifier,
    columns: Int = 22,
    wind: Float = 1f,
    tearAt: Float = 1.45f,
    onDismissed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val texturePaint = remember { Paint().apply { isFilterBitmap = true; isDither = true } }
    val shadePaint = remember { Paint().apply { isFilterBitmap = true } }
    val dismissHandler by rememberUpdatedState(onDismissed)

    LaunchedEffect(columns) {
        repeat(2) { withFrameNanos { } }
        val texture = layer.toImageBitmap().asAndroidBitmap()
        state.sheet = Sheet(columns, texture.width.toFloat(), texture.height.toFloat(), texture)
    }

    LaunchedEffect(state.awake, state.sheet) {
        val sheet = state.sheet
        if (!state.awake || sheet == null) return@LaunchedEffect
        var previousFrame = withFrameNanos { it }
        while (true) {
            val frame = withFrameNanos { it }
            val dt = ((frame - previousFrame) / 1e9f).coerceIn(MIN_STEP, MAX_STEP)
            previousFrame = frame
            sheet.step(dt, wind, tearAt)
            state.pinned = sheet.anyPinned
            state.tick++
            if (sheet.gone) {
                state.dismissed = true
                state.awake = false
                dismissHandler()
                break
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                val slop = viewConfiguration.touchSlop
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val sheet = state.sheet
                        var holding = false
                        var travelled = 0f
                        var previousPosition = down.position

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break

                            travelled += (change.position - previousPosition).getDistance()
                            previousPosition = change.position

                            if (!holding && travelled > slop && sheet != null && !state.dismissed) {
                                holding = true
                                state.wake()
                                sheet.grab(down.position.x, down.position.y)
                            }
                            if (holding) {
                                sheet?.dragTo(change.position.x, change.position.y)
                                change.consume()
                            }
                        }
                        if (holding) sheet?.release()
                    }
                }
            }
            .drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                @Suppress("UNUSED_EXPRESSION") state.tick
                if (state.dismissed) return@drawWithContent
                val sheet = state.sheet
                if (sheet == null || !state.awake) {
                    drawLayer(layer)
                    return@drawWithContent
                }
                sheet.project()
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    native.drawBitmapMesh(
                        sheet.texture, sheet.cols, sheet.rows,
                        sheet.vertices, 0, null, 0, texturePaint,
                    )
                    native.drawBitmapMesh(
                        sheet.shade, sheet.cols, sheet.rows,
                        sheet.vertices, 0, null, 0, shadePaint,
                    )
                }
            }
    ) { content() }
}

internal class Sheet(
    val cols: Int,
    private val width: Float,
    private val height: Float,
    val texture: Bitmap,
) {
    val rows = (cols * height / width).roundToInt().coerceIn(4, 80)
    private val stride = cols + 1
    private val nodeCount = stride * (rows + 1)
    private val cellWidth = width / cols
    private val cellHeight = height / rows

    private val positionX = FloatArray(nodeCount)
    private val positionY = FloatArray(nodeCount)
    private val positionZ = FloatArray(nodeCount)
    private val previousX = FloatArray(nodeCount)
    private val previousY = FloatArray(nodeCount)
    private val previousZ = FloatArray(nodeCount)
    private val inverseMass = FloatArray(nodeCount)

    private val springA: IntArray
    private val springB: IntArray
    private val springRest: FloatArray
    private val springStiffness: FloatArray

    private val breezeAcross = FloatArray(stride)
    private val breezeDown = FloatArray(rows + 1)

    val vertices = FloatArray(nodeCount * 2)
    val shade: Bitmap = createBitmap(stride, rows + 1)
    private val shadePixels = IntArray(nodeCount)

    private var heldNode = -1
    private var heldX = 0f
    private var heldY = 0f
    private var heldFor = 0f
    private var time = 0f
    private var topPinned = true

    var anyPinned = true
        private set

    var gone = false
        private set

    init {
        val endsA = ArrayList<Int>()
        val endsB = ArrayList<Int>()
        val restLengths = ArrayList<Float>()
        val stiffnesses = ArrayList<Float>()

        fun link(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, stiffness: Float) {
            if (toRow > rows || toCol > cols) return
            val spanX = (toCol - fromCol) * cellWidth
            val spanY = (toRow - fromRow) * cellHeight
            endsA += fromRow * stride + fromCol
            endsB += toRow * stride + toCol
            restLengths += sqrt(spanX * spanX + spanY * spanY)
            stiffnesses += stiffness
        }

        for (row in 0..rows) for (col in 0..cols) {
            link(row, col, row, col + 1, STRUCTURAL)
            link(row, col, row + 1, col, STRUCTURAL)
            link(row, col, row + 1, col + 1, SHEAR)
            if (col > 0) link(row, col, row + 1, col - 1, SHEAR)
            link(row, col, row, col + 2, BEND)
            link(row, col, row + 2, col, BEND)
        }

        springA = endsA.toIntArray()
        springB = endsB.toIntArray()
        springRest = restLengths.toFloatArray()
        springStiffness = stiffnesses.toFloatArray()
        reset()
    }

    fun reset() {
        for (row in 0..rows) for (col in 0..cols) {
            val node = row * stride + col
            positionX[node] = col * cellWidth
            positionY[node] = row * cellHeight
            positionZ[node] = 0f
            previousX[node] = positionX[node]
            previousY[node] = positionY[node]
            previousZ[node] = 0f
            inverseMass[node] = if (row == 0) 0f else 1f
        }
        heldNode = -1
        heldFor = 0f
        time = 0f
        topPinned = true
        anyPinned = true
        gone = false
    }

    fun unpin() {
        for (node in 0 until nodeCount) inverseMass[node] = 1f
        topPinned = false
        anyPinned = false
    }

    fun step(dt: Float, wind: Float, tearAt: Float) {
        time += dt
        followFinger(dt)
        integrate(dt, wind)
        repeat(ITERATIONS) { relax() }
        if (tearAt > 0f) tear(tearAt)
        if (!anyPinned && !gone) gone = hasFallenOffScreen()
    }

    private fun followFinger(dt: Float) {
        val node = heldNode
        if (node < 0) return
        heldFor = (heldFor + dt / PINCH_RISE_SECONDS).coerceAtMost(1f)
        previousX[node] = positionX[node]
        previousY[node] = positionY[node]
        previousZ[node] = positionZ[node]
        positionX[node] = heldX
        positionY[node] = heldY
        positionZ[node] = PINCH_DEPTH * width * heldFor
    }

    private fun integrate(dt: Float, wind: Float) {
        val dtSquared = dt * dt
        val windy = wind != 0f

        if (windy) {
            for (col in 0..cols) {
                breezeAcross[col] = sin(col * cellWidth * BREEZE_SCALE_X + time * BREEZE_SPEED_X)
            }
            for (row in 0..rows) {
                breezeDown[row] = cos(row * cellHeight * BREEZE_SCALE_Y - time * BREEZE_SPEED_Y)
            }
        }

        for (row in 0..rows) {
            val downward = if (windy) breezeDown[row] else 0f
            for (col in 0..cols) {
                val node = row * stride + col
                if (inverseMass[node] == 0f) continue

                val velocityX = (positionX[node] - previousX[node]) * DAMPING
                val velocityY = (positionY[node] - previousY[node]) * DAMPING
                val velocityZ = (positionZ[node] - previousZ[node]) * DAMPING * AIR_DRAG

                previousX[node] = positionX[node]
                previousY[node] = positionY[node]
                previousZ[node] = positionZ[node]

                val breeze = if (windy) {
                    wind * WIND * (breezeAcross[col] * downward + BREEZE_BIAS)
                } else {
                    0f
                }

                positionX[node] += velocityX
                positionY[node] += velocityY + GRAVITY * dtSquared
                positionZ[node] += velocityZ + breeze * dtSquared
            }
        }
    }

    private fun relax() {
        for (spring in springA.indices) {
            val nodeA = springA[spring]
            val nodeB = springB[spring]
            val massA = inverseMass[nodeA]
            val massB = inverseMass[nodeB]
            val totalMass = massA + massB
            if (totalMass == 0f) continue

            val deltaX = positionX[nodeB] - positionX[nodeA]
            val deltaY = positionY[nodeB] - positionY[nodeA]
            val deltaZ = positionZ[nodeB] - positionZ[nodeA]
            val distance = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
            if (distance < 1e-4f) continue

            val correction = (distance - springRest[spring]) / distance *
                springStiffness[spring] / totalMass
            val shareA = correction * massA
            val shareB = correction * massB

            positionX[nodeA] += deltaX * shareA
            positionY[nodeA] += deltaY * shareA
            positionZ[nodeA] += deltaZ * shareA
            positionX[nodeB] -= deltaX * shareB
            positionY[nodeB] -= deltaY * shareB
            positionZ[nodeB] -= deltaZ * shareB
        }
    }

    private fun tear(threshold: Float) {
        if (!anyPinned) return
        val limit = cellHeight * threshold
        var remaining = 0
        for (col in 0..cols) {
            if (inverseMass[col] != 0f) continue
            if (col == heldNode) {
                remaining++
                continue
            }
            val below = col + stride
            val deltaX = positionX[below] - positionX[col]
            val deltaY = positionY[below] - positionY[col]
            val deltaZ = positionZ[below] - positionZ[col]
            val tension = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
            if (tension > limit) inverseMass[col] = 1f else remaining++
        }
        anyPinned = remaining > 0
        if (!anyPinned) topPinned = false
    }

    private fun hasFallenOffScreen(): Boolean {
        var highest = Float.MAX_VALUE
        for (node in 0 until nodeCount) {
            if (positionY[node] < highest) highest = positionY[node]
        }
        return highest > height
    }

    fun grab(touchX: Float, touchY: Float) {
        var nearest = -1
        var nearestDistance = Float.MAX_VALUE
        for (node in 0 until nodeCount) {
            val deltaX = positionX[node] - touchX
            val deltaY = positionY[node] - touchY
            val distance = deltaX * deltaX + deltaY * deltaY
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearest = node
            }
        }
        if (nearest < 0) return
        heldNode = nearest
        heldFor = 0f
        heldX = positionX[nearest]
        heldY = positionY[nearest]
        inverseMass[nearest] = 0f
    }

    fun dragTo(touchX: Float, touchY: Float) {
        if (heldNode < 0) return
        heldX = touchX
        heldY = touchY
    }

    fun release() {
        val node = heldNode
        heldNode = -1
        heldFor = 0f
        if (node < 0) return
        inverseMass[node] = 1f

        if (node < stride && topPinned) {
            var others = 0
            for (col in 0..cols) if (col != node && inverseMass[col] == 0f) others++
            if (others > 0) inverseMass[node] = 0f
        }

        var nails = 0
        for (col in 0..cols) if (inverseMass[col] == 0f) nails++
        anyPinned = nails > 0
        if (!anyPinned) topPinned = false
    }

    fun project() {
        val camera = maxOf(width, height) * CAMERA
        val centerX = width * 0.5f
        val centerY = height * 0.5f

        for (node in 0 until nodeCount) {
            val depth = (camera - positionZ[node]).coerceAtLeast(camera * 0.25f)
            val scale = camera / depth
            vertices[node * 2] = centerX + (positionX[node] - centerX) * scale
            vertices[node * 2 + 1] = centerY + (positionY[node] - centerY) * scale
        }

        for (row in 0..rows) {
            for (col in 0..cols) {
                val node = row * stride + col
                val left = if (col > 0) node - 1 else node
                val right = if (col < cols) node + 1 else node
                val above = if (row > 0) node - stride else node
                val below = if (row < rows) node + stride else node

                val acrossX = positionX[right] - positionX[left]
                val acrossY = positionY[right] - positionY[left]
                val acrossZ = positionZ[right] - positionZ[left]
                val downX = positionX[below] - positionX[above]
                val downY = positionY[below] - positionY[above]
                val downZ = positionZ[below] - positionZ[above]

                var normalX = acrossY * downZ - acrossZ * downY
                var normalY = acrossZ * downX - acrossX * downZ
                var normalZ = acrossX * downY - acrossY * downX
                val length = sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ)
                if (length < 1e-5f) {
                    shadePixels[node] = 0
                    continue
                }
                normalX /= length
                normalY /= length
                normalZ /= length

                val lambert =
                    normalX * LIGHT_X + normalY * LIGHT_Y + normalZ * LIGHT_Z - LIGHT_Z
                val sheen = if (lambert > 0f) lambert * lambert * SHEEN else 0f
                val level = ((lambert + sheen) * GAIN).coerceIn(-1f, 1f)
                val alpha = (abs(level) * 255f).toInt().coerceIn(0, 255)
                shadePixels[node] =
                    if (level > 0f) (alpha shl 24) or 0x00FFFFFF else (alpha shl 24)
            }
        }
        shade.setPixels(shadePixels, 0, stride, 0, 0, stride, rows + 1)
    }
}

private const val STRUCTURAL = 0.92f
private const val SHEAR = 0.38f
private const val BEND = 0.14f
private const val ITERATIONS = 5

private const val GRAVITY = 2600f
private const val DAMPING = 0.988f
private const val AIR_DRAG = 0.97f

private const val WIND = 520f
private const val BREEZE_SCALE_X = 0.0042f
private const val BREEZE_SCALE_Y = 0.0031f
private const val BREEZE_SPEED_X = 1.7f
private const val BREEZE_SPEED_Y = 1.1f
private const val BREEZE_BIAS = 0.35f

private const val PINCH_DEPTH = 0.20f
private const val PINCH_RISE_SECONDS = 0.28f

private const val CAMERA = 2.2f

private const val LIGHT_X = -0.35f
private const val LIGHT_Y = -0.54f
private const val LIGHT_Z = 0.76f

private const val GAIN = 1.35f
private const val SHEEN = 1.6f

private const val MIN_STEP = 1f / 240f
private const val MAX_STEP = 1f / 24f