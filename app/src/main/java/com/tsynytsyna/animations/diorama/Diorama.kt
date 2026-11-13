package com.tsynytsyna.animations.diorama

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.exp

@Stable
class DioramaState internal constructor(raised: Boolean) {

    var raised by mutableStateOf(raised)
        private set

    internal val rise = Animatable(if (raised) 1f else 0f)

    internal var eyeX by mutableFloatStateOf(0f)
    internal var eyeY by mutableFloatStateOf(0f)

    fun raise() {
        raised = true
    }

    fun flatten() {
        raised = false
    }

    fun toggle() {
        raised = !raised
    }
}

@Composable
fun rememberDioramaState(raised: Boolean = false) = remember { DioramaState(raised) }

/** Tilt inputs of a [Diorama] host, combined into [DioramaState.eyeX] / [DioramaState.eyeY] every frame. */
internal class DioramaFrame(val state: DioramaState) {
    var gyroX = 0f
    var gyroY = 0f
    var dragX = 0f
    var dragY = 0f
    var dragging = false
}

internal val LocalDioramaFrame = staticCompositionLocalOf<DioramaFrame?> { null }

@Composable
internal fun rememberDioramaFrame(state: DioramaState, tilt: Boolean): DioramaFrame {
    val frame = remember(state) { DioramaFrame(state) }

    LaunchedEffect(state.raised) {
        state.rise.animateTo(
            targetValue = if (state.raised) 1f else 0f,
            animationSpec = spring(dampingRatio = RISE_DAMPING, stiffness = RISE_STIFFNESS),
        )
    }

    LaunchedEffect(frame) {
        var previous = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - previous) / 1e9f).coerceIn(0f, 0.05f)
            previous = now

            val recenter = exp(-dt / GYRO_RECENTER_SECONDS)
            frame.gyroX *= recenter
            frame.gyroY *= recenter
            if (!frame.dragging) {
                val settle = exp(-dt * DRAG_RETURN_RATE)
                frame.dragX *= settle
                frame.dragY *= settle
            }

            val targetX = (frame.gyroX + frame.dragX).coerceIn(-EYE_LIMIT, EYE_LIMIT)
            val targetY = (frame.gyroY + frame.dragY).coerceIn(-EYE_LIMIT, EYE_LIMIT)
            val follow = 1f - exp(-dt * EYE_SMOOTHING)
            val nextX = state.eyeX + (targetX - state.eyeX) * follow
            val nextY = state.eyeY + (targetY - state.eyeY) * follow
            if (abs(nextX - state.eyeX) > EYE_EPSILON) state.eyeX = nextX
            if (abs(nextY - state.eyeY) > EYE_EPSILON) state.eyeY = nextY
        }
    }

    if (tilt) Gyroscope(frame)
    return frame
}

/** Tilts the host by dragging a finger across it, unless a child has already taken the drag. */
internal fun Modifier.dioramaRig(frame: DioramaFrame, dragToTilt: Boolean): Modifier {
    if (!dragToTilt) return this
    return pointerInput(frame) {
        val slop = viewConfiguration.touchSlop
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(requireUnconsumed = false)
                val startX = frame.dragX
                val startY = frame.dragY
                var dragging = false
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    if (!dragging && change.isConsumed) break
                    val delta = change.position - down.position
                    if (!dragging && delta.getDistance() > slop) {
                        dragging = true
                        frame.dragging = true
                    }
                    if (dragging) {
                        val span = size.width.toFloat()
                        frame.dragX = startX + delta.x / span * DRAG_GAIN
                        frame.dragY = startY + delta.y / span * DRAG_GAIN
                        change.consume()
                    }
                }
                frame.dragging = false
            }
        }
    }
}

@Composable
private fun Gyroscope(frame: DioramaFrame) {
    val context = LocalContext.current
    DisposableEffect(frame) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return@DisposableEffect onDispose { }
        val sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?: return@DisposableEffect onDispose { }

        val listener = object : SensorEventListener {
            private var last = 0L

            override fun onSensorChanged(event: SensorEvent) {
                val previous = last
                last = event.timestamp
                if (previous == 0L) return
                val dt = ((event.timestamp - previous) / 1e9f).coerceIn(0f, 0.05f)
                val rateX = deadZone(event.values[1])
                val rateY = deadZone(event.values[0])
                frame.gyroX = (frame.gyroX + rateX * dt * GYRO_GAIN).coerceIn(-EYE_LIMIT, EYE_LIMIT)
                frame.gyroY = (frame.gyroY - rateY * dt * GYRO_GAIN).coerceIn(-EYE_LIMIT, EYE_LIMIT)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { manager.unregisterListener(listener) }
    }
}

internal const val EYE_LIMIT = 0.38f
internal const val EYE_SMOOTHING = 7f
internal const val EYE_EPSILON = 1e-4f

internal const val GYRO_GAIN = 1.6f
internal const val GYRO_DEAD_ZONE = 0.03f
internal const val GYRO_RECENTER_SECONDS = 7f

internal fun deadZone(rate: Float): Float = when {
    rate > GYRO_DEAD_ZONE -> rate - GYRO_DEAD_ZONE
    rate < -GYRO_DEAD_ZONE -> rate + GYRO_DEAD_ZONE
    else -> 0f
}

private const val DRAG_GAIN = 0.9f
private const val DRAG_RETURN_RATE = 3.5f

private const val RISE_DAMPING = 0.78f
private const val RISE_STIFFNESS = 110f
