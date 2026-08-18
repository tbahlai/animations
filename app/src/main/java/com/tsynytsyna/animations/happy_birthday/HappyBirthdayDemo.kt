package com.tsynytsyna.animations.happy_birthday

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Swipe across the candles to blow them out; the smoking wicks then give up the sparks that rise
 * and settle into "Happy Birthday".
 *
 * The cake and its candles live in `CakeAnimation.kt`, the greeting in `GreetingText.kt`.
 */

/** Horizontal reach of the swipe, in design px — a flame within this of the finger gets hit. */
private const val BLOW_RADIUS = 130f

private const val REVEAL_MILLIS = 2400

private const val NANOS_PER_SECOND = 1_000_000_000f

/**
 * Longest step the simulation will take. A frame the system skipped — or a return from the
 * background — would otherwise arrive as a single huge delta and teleport every particle.
 */
private const val MAX_FRAME_SECONDS = 0.05f

private val NightSky = listOf(Color(0xFF1A1140), Color(0xFF07040F))

private class Scene(val geometry: CakeGeometry, val candles: List<Candle>, val greeting: Greeting)

@Composable
fun HappyBirthdayDemo(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.radialGradient(NightSky)),
    ) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }

        val scene = remember(width, height) {
            val geometry = CakeGeometry(width, height)
            Scene(
                geometry = geometry,
                candles = geometry.buildCandles(),
                greeting = buildGreeting(
                    width = width,
                    height = height,
                    sourceXs = geometry.candleXs,
                    sourceY = geometry.wickY,
                    scale = geometry.scale,
                    density = density,
                    measurer = measurer,
                ),
            )
        }

        val scope = rememberCoroutineScope()
        val reveal = remember(scene) { Animatable(0f) }
        val wind = remember(scene) { Wind() }
        val smoke = remember(scene) { mutableListOf<SmokePuff>() }
        var litCount by remember(scene) { mutableIntStateOf(CANDLE_COUNT) }
        var time by remember { mutableFloatStateOf(0f) }

        // Once the last spark has landed nothing moves anymore, so the frame loop shuts down
        // instead of redrawing an unchanging picture forever.
        var settled by remember(scene) { mutableStateOf(false) }

        LaunchedEffect(scene, settled) {
            if (settled) return@LaunchedEffect
            animationFrames { deltaSeconds ->
                time += deltaSeconds
                wind.settle(deltaSeconds)
                advanceCandles(scene.candles, deltaSeconds)
                advanceSmoke(smoke, deltaSeconds)
                litCount = scene.candles.count { it.lit }

                settled = litCount == 0 && smoke.isEmpty() && reveal.targetValue == reveal.value
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(scene) {
                    detectDragGestures { change, drag ->
                        wind.gust(drag.x)
                        if (!wind.blowsOutFlames) return@detectDragGestures

                        val reach = BLOW_RADIUS * scene.geometry.scale
                        val overTheCandles = change.position.y < scene.geometry.upperTierTop

                        scene.candles.forEach { candle ->
                            val underFinger = abs(change.position.x - candle.x) < reach
                            if (!candle.lit || !underFinger || !overTheCandles) return@forEach

                            scene.geometry.blowOut(candle, smoke)
                            if (scene.candles.none { it.lit }) {
                                scope.launch {
                                    reveal.animateTo(1f, tween(REVEAL_MILLIS, easing = EaseOutCubic))
                                }
                            }
                        }
                    }
                },
        ) {
            time // read every frame so the canvas keeps redrawing

            drawCake(scene.geometry, scene.candles, glow = litCount.toFloat() / CANDLE_COUNT)
            drawFlames(scene.geometry, scene.candles, time, wind)
            drawSmoke(smoke)
            drawGreeting(scene.greeting, reveal.value)
        }

        // The canvas runs edge to edge, under the system bars; only the button keeps clear of them.
        if (litCount == 0) {
            TextButton(
                onClick = {
                    scene.candles.forEach { it.lit = true; it.dying = 0f }
                    smoke.clear()
                    settled = false
                    scope.launch { reveal.snapTo(0f) }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 24.dp),
            ) {
                Text("Light them again", color = Color(0xFFFF5C8A))
            }
        }
    }
}

/** Runs [onFrame] once per displayed frame with the time since the previous one, in seconds. */
private suspend inline fun animationFrames(crossinline onFrame: (deltaSeconds: Float) -> Unit) {
    var previousFrame = withFrameNanos { it }
    while (true) {
        withFrameNanos { now ->
            val deltaSeconds = (now - previousFrame) / NANOS_PER_SECOND
            previousFrame = now
            onFrame(deltaSeconds.coerceAtMost(MAX_FRAME_SECONDS))
        }
    }
}