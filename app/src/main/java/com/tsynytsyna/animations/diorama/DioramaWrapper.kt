package com.tsynytsyna.animations.diorama

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * Optional host for lifted elements. Nothing is drawn by it and the screen is not moved — it only
 * adds two things over the shared gyroscope every [Lifted] element already uses on its own:
 * tilting by dragging a finger, and [DioramaState.raise] / [DioramaState.flatten] to animate
 * every lift inside it up or down together.
 */
@Composable
fun Diorama(
    state: DioramaState,
    modifier: Modifier = Modifier,
    tilt: Boolean = true,
    dragToTilt: Boolean = true,
    content: @Composable () -> Unit,
) {
    val frame = rememberDioramaFrame(state, tilt = tilt)
    Box(modifier = modifier.dioramaRig(frame, dragToTilt), propagateMinConstraints = true) {
        CompositionLocalProvider(LocalDioramaFrame provides frame) { content() }
    }
}
