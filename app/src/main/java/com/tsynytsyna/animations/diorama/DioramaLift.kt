package com.tsynytsyna.animations.diorama

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Lifts this element [depth] above the screen and moves it with the phone's tilt.
 *
 * With a [shape] the element becomes a solid block: it shows its [edge] on the side facing you
 * and casts a soft shadow onto the surface below, which it slides over as the phone tilts.
 * Without a shape it simply floats — the right choice for text.
 *
 * Put it where the element's visible bounds are: a Material `Button` reserves an invisible 48 dp
 * touch area, so lift it with `LocalMinimumInteractiveComponentSize` set to `Dp.Unspecified`.
 *
 * The element itself is moved, not a copy of it, so it stays live and clickable. Lifts nest:
 * a lifted title inside a lifted card rises above the card. Works on its own; inside a [Diorama]
 * it also follows finger drags and that host's raise / flatten animation.
 */
fun Modifier.diorama(
    depth: Dp,
    shape: Shape? = null,
    edge: Color = Color(0xFF1E1E26),
): Modifier = composed {
    val frame = LocalDioramaFrame.current
    val tilt: TiltSource = if (frame != null) remember(frame) { FrameTilt(frame) } else rememberSharedTilt()
    val density = LocalDensity.current
    val height = with(density) { depth.toPx() }
    val stroke = with(density) { EDGE_STEP.toPx() }
    val edgeStyle = remember(stroke) { Stroke(width = stroke * 2f) }
    val blur = height * SHADOW_SOFTNESS + with(density) { SHADOW_MIN_BLUR.toPx() }
    val shadowLayer = rememberGraphicsLayer()
    val edgeLayers = List(EDGE_TONES) { rememberGraphicsLayer() }
    val edgeCache = remember { EdgeCache() }

    fun shiftX() = -tilt.eyeX * height * PARALLAX * tilt.rise
    fun shiftY() = -tilt.eyeY * height * PARALLAX * tilt.rise

    val grounded = if (shape == null) {
        Modifier
    } else {
        Modifier.drawBehind {
            val rise = tilt.rise
            if (rise == 0f) return@drawBehind

            // Rendered at a quarter of the size and scaled up: it is blurred anyway, and a small
            // texture keeps big shapes cheap to blur and to composite.
            val bleed = blur * 2.5f
            val padded = IntSize(
                ((size.width + bleed * 2f) / SHADOW_DOWNSAMPLE).roundToInt().coerceAtLeast(1),
                ((size.height + bleed * 2f) / SHADOW_DOWNSAMPLE).roundToInt().coerceAtLeast(1),
            )
            if (shadowLayer.size != padded) {
                val outline = shape.createOutline(size, layoutDirection, this)
                shadowLayer.record(padded) {
                    scale(1f / SHADOW_DOWNSAMPLE, pivot = Offset.Zero) {
                        translate(bleed, bleed) { drawOutline(outline, Color.Black) }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val small = blur / SHADOW_DOWNSAMPLE
                    shadowLayer.renderEffect = BlurEffect(small, small, TileMode.Decal)
                }
            }
            shadowLayer.alpha = rise * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) SHADOW_ALPHA else 0.25f
            translate(height * LIGHT_X * rise - bleed, height * LIGHT_Y * rise - bleed) {
                scale(SHADOW_DOWNSAMPLE, pivot = Offset.Zero) { drawLayer(shadowLayer) }
            }

            val dx = shiftX()
            val dy = shiftY()
            val reach = max(abs(dx), abs(dy))
            if (reach < 0.5f) return@drawBehind
            // The edge is a stack of outline rings. Each tone of ring is rasterised once into its
            // own texture, so a frame only places textures instead of stroking the path again.
            val bounds = IntSize(size.width.roundToInt(), size.height.roundToInt())
            if (edgeCache.size != bounds || edgeCache.edge != edge || edgeCache.shape != shape) {
                edgeCache.size = bounds
                edgeCache.edge = edge
                edgeCache.shape = shape
                val outline = shape.createOutline(
                    Size(size.width - stroke * 2f, size.height - stroke * 2f), layoutDirection, this,
                )
                edgeLayers.forEachIndexed { tone, layer ->
                    val t = tone / (EDGE_TONES - 1f)
                    val shade = lerp(lerp(edge, Color.Black, EDGE_BASE_SHADE), lerp(edge, Color.White, EDGE_TOP_LIGHT), t)
                    layer.compositingStrategy = CompositingStrategy.Offscreen
                    layer.record(bounds) {
                        translate(stroke, stroke) { drawOutline(outline, shade, style = edgeStyle) }
                    }
                }
            }
            val slices = ceil(reach / stroke).toInt().coerceIn(1, MAX_SLICES)
            for (slice in 0..slices) {
                val t = slice.toFloat() / slices
                translate(dx * t, dy * t) { drawLayer(edgeLayers[(t * (EDGE_TONES - 1)).roundToInt()]) }
            }
        }
    }

    this
        .then(grounded)
        .graphicsLayer {
            val rise = tilt.rise
            translationX = shiftX()
            translationY = shiftY()
            if (shape != null) {
                this.shape = shape
                // The platform shadow of an arbitrary path is very costly to render, so free-form
                // shapes rely on the blurred cast shadow alone.
                val simple = shape.createOutline(size, LayoutDirection.Ltr, this) !is Outline.Generic
                shadowElevation = if (simple) height * rise * CONTACT_SHADOW else 0f
            }
        }
}

private val EDGE_STEP = 1.5.dp
private val SHADOW_MIN_BLUR = 4.dp
private const val PARALLAX = 1.2f
private const val CONTACT_SHADOW = 0.25f
private const val SHADOW_SOFTNESS = 0.5f
private const val SHADOW_ALPHA = 0.75f
private const val LIGHT_X = 0.3f
private const val LIGHT_Y = 0.85f
private const val MAX_SLICES = 32
private const val SHADOW_DOWNSAMPLE = 4f
private const val EDGE_TONES = 4

private class EdgeCache {
    var size = IntSize.Zero
    var edge = Color.Unspecified
    var shape: Shape? = null
}
private const val EDGE_BASE_SHADE = 0.45f
private const val EDGE_TOP_LIGHT = 0.18f

/** Wraps any content and lifts it — the same as [Modifier.diorama], as a container. */
@Composable
fun Lifted(
    depth: Dp,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    edge: Color = Color(0xFF1E1E26),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.diorama(depth, shape, edge), content = content)
}
