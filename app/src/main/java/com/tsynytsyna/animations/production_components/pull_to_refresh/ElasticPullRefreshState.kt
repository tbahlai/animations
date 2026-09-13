package com.tsynytsyna.animations.production_components.pull_to_refresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.tanh

/**
 * Real-time motion states for gesture telemetry & physics tracking.
 */
enum class PullMotionState {
    IDLE,
    DRAGGING,
    SETTLING,
    REFRESHING
}

class ElasticPullRefreshState(
    val triggerThresholdPx: Float = 240f,
    val maxPullPx: Float = 440f,
    val refreshingRestingPx: Float = 175f,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: suspend () -> Unit
) {
    var isRefreshing by mutableStateOf(false)
        private set

    var rawPullPx by mutableFloatStateOf(0f)
        private set

    var motionState by mutableStateOf(PullMotionState.IDLE)
        private set

    var lastReleaseVelocity by mutableFloatStateOf(0f)
        private set

    var instantaneousVelocity by mutableFloatStateOf(0f)
        private set

    var interruptionsCount by mutableIntStateOf(0)
        private set

    val effectivePullPx: Float
        get() {
            val ratio = (rawPullPx / maxPullPx).coerceIn(0f, 2.5f)
            return tanh(ratio) * maxPullPx
        }

    val progress: Float
        get() = (effectivePullPx / triggerThresholdPx).coerceIn(0f, 2.0f)

    private val animatableOffset = Animatable(0f)
    private var settlingJob: Job? = null

    /**
     * Halts any active settling animation immediately and preserves the exact
     * instantaneous position without any snap jump (C⁰ positional continuity).
     */
    private fun interruptSettling(reason: String = "User touch") {
        if (settlingJob?.isActive == true) {
            settlingJob?.cancel()
            settlingJob = null
            interruptionsCount++
            // Keep current instantaneous value
            rawPullPx = animatableOffset.value
            motionState = PullMotionState.DRAGGING
        }
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // If the user touches down / drags during settling, immediately interrupt!
            if (source == NestedScrollSource.UserInput && settlingJob?.isActive == true) {
                interruptSettling("onPreScroll")
            }

            // When already pulled down, scrolling up collapses pull first
            if (rawPullPx > 0f && available.y < 0f) {
                motionState = PullMotionState.DRAGGING
                val consumed = available.y.coerceAtLeast(-rawPullPx)
                rawPullPx += consumed
                return Offset(0f, consumed)
            }
            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            // Overscroll pulling down when at the top of the scrollable container
            if (available.y > 0f) {
                if (source == NestedScrollSource.UserInput && settlingJob?.isActive == true) {
                    interruptSettling("onPostScroll")
                }

                motionState = PullMotionState.DRAGGING
                val resistance = 0.52f
                rawPullPx += available.y * resistance
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val flingY = available.y
            lastReleaseVelocity = flingY

            // Scale release velocity (px/sec) into spring coordinate space
            val releaseVelocity = (flingY * 0.65f).coerceIn(-4500f, 4500f)

            if (rawPullPx >= triggerThresholdPx && !isRefreshing) {
                triggerRefresh(initialVelocity = releaseVelocity)
            } else {
                animateToTarget(targetValue = 0f, initialVelocity = releaseVelocity)
            }
            return Velocity.Zero
        }
    }

    fun triggerRefresh(initialVelocity: Float = 0f) {
        if (isRefreshing) return
        isRefreshing = true
        motionState = PullMotionState.REFRESHING

        // Smoothly spring towards resting pull height using release momentum
        animateToTarget(
            targetValue = refreshingRestingPx * 1.15f,
            initialVelocity = initialVelocity,
            onComplete = {
                motionState = PullMotionState.REFRESHING
            }
        )

        coroutineScope.launch {
            try {
                onRefresh()
            } finally {
                finishRefresh()
            }
        }
    }

    fun finishRefresh() {
        isRefreshing = false
        animateToTarget(
            targetValue = 0f,
            initialVelocity = 0f,
            onComplete = {
                motionState = PullMotionState.IDLE
            }
        )
    }

    private fun animateToTarget(
        targetValue: Float,
        initialVelocity: Float = 0f,
        onComplete: (() -> Unit)? = null
    ) {
        settlingJob?.cancel()
        motionState = if (isRefreshing) PullMotionState.REFRESHING else PullMotionState.SETTLING

        settlingJob = coroutineScope.launch {
            try {
                animatableOffset.snapTo(rawPullPx)
                animatableOffset.animateTo(
                    targetValue = targetValue,
                    initialVelocity = initialVelocity,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) {
                    rawPullPx = value
                    instantaneousVelocity = velocity
                }
                rawPullPx = targetValue
                instantaneousVelocity = 0f
                if (!isRefreshing) {
                    motionState = PullMotionState.IDLE
                }
                onComplete?.invoke()
            } catch (e: CancellationException) {
                // Interrupted by user touch — freeze at current value
                rawPullPx = animatableOffset.value
            } finally {
                if (settlingJob?.isActive != true && motionState == PullMotionState.SETTLING) {
                    motionState = if (isRefreshing) PullMotionState.REFRESHING else PullMotionState.IDLE
                }
            }
        }
    }
}

@Composable
fun rememberElasticPullRefreshState(
    onRefresh: suspend () -> Unit,
    triggerThresholdPx: Float = 240f,
    maxPullPx: Float = 440f
): ElasticPullRefreshState {
    val coroutineScope = rememberCoroutineScope()
    return remember(coroutineScope) {
        ElasticPullRefreshState(
            triggerThresholdPx = triggerThresholdPx,
            maxPullPx = maxPullPx,
            coroutineScope = coroutineScope,
            onRefresh = onRefresh
        )
    }
}
