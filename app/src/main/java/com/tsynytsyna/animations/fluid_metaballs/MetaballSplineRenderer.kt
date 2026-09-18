package com.tsynytsyna.animations.fluid_metaballs

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Handles mathematical tangent-bridge spline synthesis and 3D specular volume rendering
 * for viscous fluid metaballs.
 */
object MetaballSplineRenderer {

    private val bridgePath = Path()
    private val neckHighlightPath = Path()

    // Pre-allocated static gradient color lists to prevent per-frame GC thrashing
    private val shadowGradientColors = listOf(
        Color.Black.copy(alpha = 0.42f),
        Color.Transparent
    )
    private val glintGradientColors = listOf(
        Color.White.copy(alpha = 0.88f),
        Color.White.copy(alpha = 0.40f),
        Color.Transparent
    )

    // Cached theme palette color lists
    private class ThemeColorCache(theme: FluidTheme) {
        val primary = Color(theme.primaryColorHex)
        val secondary = Color(theme.secondaryColorHex)
        val glow = Color(theme.glowColorHex)
        val specular = Color(theme.specularColorHex)

        val bridgeColors = listOf(
            secondary.copy(alpha = 0.95f),
            primary,
            secondary.copy(alpha = 0.95f)
        )
        val haloColors = listOf(
            glow.copy(alpha = 0.38f),
            glow.copy(alpha = 0.12f),
            Color.Transparent
        )
        val volumeColors = listOf(
            specular.copy(alpha = 0.90f),
            primary,
            secondary,
            Color(0xFF10141D)
        )
        val bounceColors = listOf(
            glow.copy(alpha = 0.45f),
            Color.Transparent
        )
    }

    private val themeCaches = FluidTheme.entries.associateWith { ThemeColorCache(it) }

    /**
     * Renders all fluid droplets, connecting liquid bridges, specular reflections, and ambient depth.
     */
    fun drawFluidSystem(
        scope: DrawScope,
        engine: MetaballEngine,
        theme: FluidTheme,
        surfaceTension: Float,
    ) {
        val droplets = engine.droplets
        val count = droplets.size
        if (count == 0) return

        val cache = themeCaches[theme] ?: return
        val primaryColor = cache.primary
        val secondaryColor = cache.secondary
        val glowColor = cache.glow
        val specularColor = cache.specular

        // 1. Draw ambient fluid shadow underneath
        for (i in 0 until count) {
            val d = droplets[i]
            val shadowY = d.y + d.radius * 0.45f
            val shadowRx = d.radius * 0.95f
            val shadowRy = d.radius * 0.42f

            scope.drawOval(
                brush = Brush.radialGradient(
                    colors = shadowGradientColors,
                    center = Offset(d.x, shadowY),
                    radius = shadowRx * 1.3f
                ),
                topLeft = Offset(d.x - shadowRx, shadowY - shadowRy),
                size = androidx.compose.ui.geometry.Size(shadowRx * 2f, shadowRy * 2f)
            )
        }

        // 2. Draw viscous tangent-bridge liquid necks between connected droplets
        for (i in 0 until count) {
            val a = droplets[i]
            for (j in i + 1 until count) {
                val b = droplets[j]
                val dx = b.x - a.x
                val dy = b.y - a.y
                val dist = sqrt(dx * dx + dy * dy)
                val sumR = a.radius + b.radius
                val maxDist = sumR * surfaceTension * 1.55f

                if (dist < maxDist && dist > 1f) {
                    drawMetaballBridge(
                        scope = scope,
                        a = a,
                        b = b,
                        dist = dist,
                        maxDist = maxDist,
                        cache = cache
                    )
                }
            }
        }

        // 3. Draw individual fluid droplets with 3D volume gradient & wobble deformation
        for (i in 0 until count) {
            val d = droplets[i]
            drawDroplet(
                scope = scope,
                droplet = d,
                cache = cache
            )
        }
    }

    /**
     * Constructs and renders a tangent-bridge spline connecting two droplets.
     */
    private fun drawMetaballBridge(
        scope: DrawScope,
        a: FluidDroplet,
        b: FluidDroplet,
        dist: Float,
        maxDist: Float,
        cache: ThemeColorCache,
    ) {
        val r1 = a.radius
        val r2 = b.radius
        val sumR = r1 + r2

        // Normalized stretch ratio: 0.0 (merged/touching) -> 1.0 (snapping point)
        val stretchRatio = if (dist <= sumR) {
            0f
        } else {
            ((dist - sumR) / (maxDist - sumR)).coerceIn(0f, 1f)
        }

        val gamma = atan2(b.y - a.y, b.x - a.x)

        // As the droplets pull apart, the neck angle pinches inward
        val pinchFactor = 1f - stretchRatio * 0.65f
        val alpha1 = (PI.toFloat() * 0.46f) * pinchFactor
        val alpha2 = (PI.toFloat() * 0.46f) * pinchFactor

        // Coordinates on circle A
        val p1aX = a.x + r1 * cos(gamma + alpha1)
        val p1aY = a.y + r1 * sin(gamma + alpha1)
        val p1bX = a.x + r1 * cos(gamma - alpha1)
        val p1bY = a.y + r1 * sin(gamma - alpha1)

        // Coordinates on circle B
        val p2aX = b.x + r2 * cos(gamma + PI.toFloat() - alpha2)
        val p2aY = b.y + r2 * sin(gamma + PI.toFloat() - alpha2)
        val p2bX = b.x + r2 * cos(gamma - PI.toFloat() + alpha2)
        val p2bY = b.y + r2 * sin(gamma - PI.toFloat() + alpha2)

        // Midpoint of bridge
        val midX = (a.x + b.x) * 0.5f
        val midY = (a.y + b.y) * 0.5f

        // Normal vector perpendicular to gamma
        val normalX = -sin(gamma)
        val normalY = cos(gamma)

        // Neck waist thickness decreases non-linearly with stretch
        val waistThickness = ((r1 + r2) * 0.48f * (1f - stretchRatio * 0.85f)).coerceAtLeast(2f)

        // Control points on side A
        val c1aX = midX + normalX * waistThickness
        val c1aY = midY + normalY * waistThickness

        // Control points on side B
        val c1bX = midX - normalX * waistThickness
        val c1bY = midY - normalY * waistThickness

        bridgePath.reset()
        bridgePath.moveTo(p1aX, p1aY)
        bridgePath.quadraticTo(c1aX, c1aY, p2aX, p2aY)
        bridgePath.lineTo(p2bX, p2bY)
        bridgePath.quadraticTo(c1bX, c1bY, p1bX, p1bY)
        bridgePath.close()

        // Bridge volume fill
        val bridgeBrush = Brush.linearGradient(
            colors = cache.bridgeColors,
            start = Offset(a.x, a.y),
            end = Offset(b.x, b.y)
        )
        scope.drawPath(path = bridgePath, brush = bridgeBrush, style = Fill)

        // Subtle specular highlight line along the stretched liquid neck
        if (stretchRatio < 0.88f) {
            neckHighlightPath.reset()
            val highlightOffset = waistThickness * 0.45f
            neckHighlightPath.moveTo(
                (p1aX * 0.7f + p2aX * 0.3f),
                (p1aY * 0.7f + p2aY * 0.3f)
            )
            neckHighlightPath.quadraticTo(
                c1aX - normalX * highlightOffset,
                c1aY - normalY * highlightOffset,
                (p1aX * 0.3f + p2aX * 0.7f),
                (p1aY * 0.3f + p2aY * 0.7f)
            )

            scope.drawPath(
                path = neckHighlightPath,
                color = Color.White.copy(alpha = (0.55f * (1f - stretchRatio))),
                style = Stroke(width = (waistThickness * 0.22f).coerceIn(1.5f, 4f))
            )
        }
    }

    /**
     * Renders a single fluid droplet with 3D sphere volume shading,
     * specular glint, and dynamic wobble oscillation.
     */
    private fun drawDroplet(
        scope: DrawScope,
        droplet: FluidDroplet,
        cache: ThemeColorCache,
    ) {
        val r = droplet.radius

        // Calculate wobble deformation along snap vector
        val wobble = if (droplet.wobbleDecay > 0.01f) {
            sin(droplet.wobbleAngle) * droplet.wobbleAmplitude * droplet.wobbleDecay
        } else 0f

        val scaleX = 1f + (wobble / r) * droplet.wobbleDirX
        val scaleY = 1f + (wobble / r) * droplet.wobbleDirY

        scope.withTransform({
            translate(droplet.x, droplet.y)
            scale(scaleX.coerceIn(0.6f, 1.4f), scaleY.coerceIn(0.6f, 1.4f), pivot = Offset.Zero)
        }) {
            // Outer glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = cache.haloColors,
                    center = Offset.Zero,
                    radius = r * 1.35f
                ),
                radius = r * 1.35f,
                center = Offset.Zero
            )

            // Main fluid volume 3D gradient
            val lightSourceOffset = Offset(-r * 0.32f, -r * 0.36f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = cache.volumeColors,
                    center = lightSourceOffset,
                    radius = r * 1.15f
                ),
                radius = r,
                center = Offset.Zero
            )

            // Meniscus inner rim contour (dark edge definition for surface tension)
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = r - 1f,
                center = Offset.Zero,
                style = Stroke(width = 2.5f)
            )

            // Primary specular reflection crest (top-left glassmorphic gleam)
            val glintRadiusX = r * 0.38f
            val glintRadiusY = r * 0.22f
            val glintCenterX = -r * 0.32f
            val glintCenterY = -r * 0.36f

            drawOval(
                brush = Brush.radialGradient(
                    colors = glintGradientColors,
                    center = Offset(glintCenterX, glintCenterY),
                    radius = glintRadiusX
                ),
                topLeft = Offset(glintCenterX - glintRadiusX, glintCenterY - glintRadiusY),
                size = androidx.compose.ui.geometry.Size(glintRadiusX * 2f, glintRadiusY * 2f)
            )

            // Secondary subtle bottom reflection (subsurface bounce)
            val bounceRadiusX = r * 0.26f
            val bounceRadiusY = r * 0.12f
            val bounceCenterX = r * 0.22f
            val bounceCenterY = r * 0.40f

            drawOval(
                brush = Brush.radialGradient(
                    colors = cache.bounceColors,
                    center = Offset(bounceCenterX, bounceCenterY),
                    radius = bounceRadiusX
                ),
                topLeft = Offset(bounceCenterX - bounceRadiusX, bounceCenterY - bounceRadiusY),
                size = androidx.compose.ui.geometry.Size(bounceRadiusX * 2f, bounceRadiusY * 2f)
            )
        }
    }
}
