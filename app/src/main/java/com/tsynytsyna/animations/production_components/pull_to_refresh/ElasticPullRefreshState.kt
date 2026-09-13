package com.tsynytsyna.animations.production_components.pull_to_refresh

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.tanh

class ElasticPullRefreshState(
    val triggerThresholdPx: Float = 220f,
    val maxPullPx: Float = 420f,
    val refreshingRestingPx: Float = 160f,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit
) {
    var isRefreshing by mutableStateOf(false)
        private set

    var rawPullPx by mutableFloatStateOf(0f)
        private set

    val effectivePullPx: Float
        get() = if (isRefreshing) {
            refreshingRestingPx
        } else {
            val ratio = (rawPullPx / maxPullPx).coerceIn(0f, 2f)
            tanh(ratio) * maxPullPx
        }

    val progress: Float
        get() = (effectivePullPx / triggerThresholdPx).coerceIn(0f, 1.8f)

    private val animatableOffset = Animatable(0f)

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // When already pulled down, scrolling up collapses pull first
            if (rawPullPx > 0f && available.y < 0f) {
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
            // Overscroll pulling down when at top
            if (available.y > 0f && !isRefreshing) {
                val resistance = 0.55f
                rawPullPx += available.y * resistance
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (rawPullPx >= triggerThresholdPx && !isRefreshing) {
                triggerRefresh()
            } else {
                animateBackToZero()
            }
            return Velocity.Zero
        }
    }

    fun triggerRefresh() {
        if (isRefreshing) return
        isRefreshing = true
        coroutineScope.launch {
            onRefresh()
        }
    }

    fun finishRefresh() {
        isRefreshing = false
        animateBackToZero()
    }

    private fun animateBackToZero() {
        coroutineScope.launch {
            animatableOffset.snapTo(rawPullPx)
            animatableOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) {
                rawPullPx = value
            }
            rawPullPx = 0f
        }
    }
}

@Composable
fun rememberElasticPullRefreshState(
    onRefresh: () -> Unit,
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
