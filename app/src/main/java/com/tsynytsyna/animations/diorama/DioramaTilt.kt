package com.tsynytsyna.animations.diorama

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.exp

/** Where the viewer's eye is relative to the screen, and how far lifts are raised (0–1). */
internal interface TiltSource {
    val eyeX: Float
    val eyeY: Float
    val rise: Float
}

internal class FrameTilt(private val frame: DioramaFrame) : TiltSource {
    override val eyeX get() = frame.state.eyeX
    override val eyeY get() = frame.state.eyeY
    override val rise get() = frame.state.rise.value
}

/**
 * One gyroscope for every lifted element on screen. It starts when the first element appears and
 * stops when the last one leaves, and smooths inside the sensor callback so no frame loop is needed.
 */
internal class SharedTilt private constructor(private val manager: SensorManager?) :
    TiltSource, SensorEventListener {

    private var smoothedX by mutableFloatStateOf(0f)
    private var smoothedY by mutableFloatStateOf(0f)
    override val eyeX get() = smoothedX
    override val eyeY get() = smoothedY
    override val rise = 1f

    private var gyroX = 0f
    private var gyroY = 0f
    private var lastTimestamp = 0L
    private var users = 0

    fun acquire() {
        if (users++ > 0 || manager == null) return
        val sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ?: return
        lastTimestamp = 0L
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun release() {
        if (--users > 0) return
        users = 0
        manager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val previous = lastTimestamp
        lastTimestamp = event.timestamp
        if (previous == 0L) return
        val dt = ((event.timestamp - previous) / 1e9f).coerceIn(0f, 0.05f)

        val recenter = exp(-dt / GYRO_RECENTER_SECONDS)
        gyroX = ((gyroX + deadZone(event.values[1]) * dt * GYRO_GAIN) * recenter).coerceIn(-EYE_LIMIT, EYE_LIMIT)
        gyroY = ((gyroY - deadZone(event.values[0]) * dt * GYRO_GAIN) * recenter).coerceIn(-EYE_LIMIT, EYE_LIMIT)

        val follow = 1f - exp(-dt * EYE_SMOOTHING)
        val nextX = smoothedX + (gyroX - smoothedX) * follow
        val nextY = smoothedY + (gyroY - smoothedY) * follow
        if (abs(nextX - smoothedX) > EYE_EPSILON) smoothedX = nextX
        if (abs(nextY - smoothedY) > EYE_EPSILON) smoothedY = nextY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        @Volatile
        private var instance: SharedTilt? = null

        fun get(context: Context): SharedTilt = instance ?: synchronized(this) {
            instance ?: SharedTilt(
                context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ).also { instance = it }
        }
    }
}

@Composable
internal fun rememberSharedTilt(): SharedTilt {
    val context = LocalContext.current
    val tilt = remember { SharedTilt.get(context) }
    DisposableEffect(tilt) {
        tilt.acquire()
        onDispose { tilt.release() }
    }
    return tilt
}
