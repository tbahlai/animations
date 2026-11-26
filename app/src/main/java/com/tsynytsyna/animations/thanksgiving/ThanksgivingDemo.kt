package com.tsynytsyna.animations.thanksgiving

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * A turkey whose tail feathers are things to be thankful for. Swipe up to fan the tail, pull
 * the feathers out one by one, and the last one leaves "Happy Thanksgiving" behind.
 */

private const val SWIPE_DP = 60f
private const val GREETING_MILLIS = 2600
private const val MAX_FRAME_SECONDS = 0.05f

@Composable
fun ThanksgivingDemo(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyMiddle, SkyHorizon))),
    ) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }

        val geometry = remember(width, height) { TurkeyGeometry(width, height) }
        val styles = remember(geometry, density) { buildStyles(geometry, density, measurer) }
        val labels = remember(geometry, density) { buildLabels(geometry, density, measurer) }
        val turkey = remember(geometry, styles, labels) { Turkey(geometry, styles, labels) }
        val covertSprite = remember(geometry) { buildCovertSprite(geometry) }
        val greeting = remember(geometry, density) {
            buildGreeting(width, height, geometry.featherSpots(), geometry.scale, density, measurer)
        }
        val greetingReveal = remember(turkey) { Animatable(0f) }
        var frame by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(turkey) {
            animationFrames { dt ->
                turkey.step(dt)
                frame += dt
            }
        }

        val allPlucked = turkey.remaining == 0
        LaunchedEffect(allPlucked) {
            if (allPlucked) {
                greetingReveal.animateTo(1f, tween(GREETING_MILLIS, delayMillis = 400, easing = EaseOutCubic))
            } else {
                greetingReveal.snapTo(0f)
            }
        }

        val swipeThreshold = with(density) { SWIPE_DP.dp.toPx() }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(turkey) {
                    // A touch on a feather holds it; anywhere else it is a swipe or a tap.
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val holding = turkey.grab(down.position)
                        var travelled = Offset.Zero
                        var last = down.position
                        var lastTime = down.uptimeMillis
                        var velocity = Offset.Zero
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val moved = change.position - last
                            val seconds = (change.uptimeMillis - lastTime).coerceAtLeast(1L) / 1000f
                            velocity = velocity * 0.5f + moved / seconds * 0.5f
                            travelled += moved
                            last = change.position
                            lastTime = change.uptimeMillis
                            change.consume()
                            if (holding) {
                                turkey.dragTo(change.position, velocity)
                            } else {
                                if (travelled.y < -swipeThreshold) turkey.open()
                                if (travelled.y > swipeThreshold) turkey.close()
                            }
                        }
                        val wasTap = travelled.getDistance() < viewConfiguration.touchSlop
                        if (holding) {
                            turkey.release(wasTap)
                        } else if (wasTap) {
                            turkey.tap(down.position, down.uptimeMillis)
                        }
                    }
                },
        ) {
            frame // read every frame so the canvas keeps redrawing

            drawHill(geometry)
            withTransform({
                translate(turkey.sway, 0f)
                scale(turkey.puff, turkey.puff, pivot = geometry.feet)
            }) {
                turkey.drawOrder.forEach { feather ->
                    if (!feather.plucked) drawAttachedFeather(geometry, feather, styles[feather.index], turkey.time)
                }
                drawCoverts(geometry, turkey, covertSprite)
                drawTurkey(geometry, turkey)
            }
            turkey.popped.forEach { drawPoppedFeather(geometry, it) }
            turkey.wisps.forEach { drawWisp(it) }
            turkey.floating.forEach { drawFloatingLabel(it) }
            drawGreeting(greeting, greetingReveal.value)
        }
    }
}

private suspend inline fun animationFrames(crossinline onFrame: (deltaSeconds: Float) -> Unit) {
    var previousFrame = withFrameNanos { it }
    while (true) {
        withFrameNanos { now ->
            val deltaSeconds = (now - previousFrame) / 1_000_000_000f
            previousFrame = now
            // A skipped frame must not arrive as one huge step.
            onFrame(deltaSeconds.coerceAtMost(MAX_FRAME_SECONDS))
        }
    }
}
