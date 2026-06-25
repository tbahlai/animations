package com.tsynytsyna.animations.swipe_card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class SwipeCardState(
    private val itemCount: Int,
    private val scope: CoroutineScope,
) {
    var currentIndex by mutableIntStateOf(0)
        private set
    var busy by mutableStateOf(false)
        private set

    internal var cardSize by mutableStateOf(IntSize.Zero)

    internal val offsetX = Animatable(0f)
    internal val rotation = Animatable(0f)
    internal val scale = Animatable(1f)
    internal val alpha = Animatable(1f)

    val impactCenter: Offset
        get() = Offset(cardSize.width / 2f, cardSize.height * 0.45f)

    fun swipe(toRight: Boolean = true) {
        if (busy) return
        busy = true
        scope.launch {
            val sign = if (toRight) 1f else -1f
            launch { rotation.animateTo(22f * sign, tween(420)) }
            launch { alpha.animateTo(0f, tween(420)) }
            offsetX.animateTo(
                cardSize.width * 1.5f * sign,
                tween(420, easing = FastOutSlowInEasing)
            )
            revealNext()
            busy = false
        }
    }

    fun dismiss(effect: (onComplete: () -> Unit) -> Unit) {
        if (busy) return
        busy = true
        effect {
            scope.launch {
                revealNext()
                busy = false
            }
        }
    }

    private suspend fun revealNext() {
        currentIndex = (currentIndex + 1) % itemCount
        offsetX.snapTo(0f)
        rotation.snapTo(0f)
        alpha.snapTo(0f)
        scale.snapTo(0.85f)
        scope.launch { scale.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
        alpha.animateTo(1f, tween(420))
    }
}

@Composable
fun rememberSwipeCardState(itemCount: Int): SwipeCardState {
    val scope = rememberCoroutineScope()
    return remember(itemCount) { SwipeCardState(itemCount, scope) }
}

@Composable
fun SwipeCard(
    state: SwipeCardState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .onSizeChanged { state.cardSize = it }
            .graphicsLayer {
                translationX = state.offsetX.value
                rotationZ = state.rotation.value
                scaleX = state.scale.value
                scaleY = state.scale.value
                alpha = state.alpha.value
            }
    ) {
        content()
    }
}