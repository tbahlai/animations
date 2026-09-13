package com.tsynytsyna.animations.gravitational_lensing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LensingRenderer {

    private class ThemeColorCache(theme: LensingTheme) {
        val haloGradient = listOf(
            theme.primaryColor.copy(alpha = 0.85f),
            theme.secondaryColor.copy(alpha = 0.55f),
            theme.primaryColor.copy(alpha = 0.15f),
            Color.Transparent
        )
        val einsteinRingGlow = listOf(
            theme.primaryColor.copy(alpha = 0.28f),
            theme.secondaryColor.copy(alpha = 0.08f),
            Color.Transparent
        )
        val photonSphereGlow = listOf(
            Color.Transparent,
            theme.coreGlowColor.copy(alpha = 0.75f),
            Color.White.copy(alpha = 0.95f),
            theme.primaryColor.copy(alpha = 0.45f),
            Color.Transparent
        )
        val dopplerColorStops = arrayOf(
            0.0f to Color.White.copy(alpha = 0.95f),
            0.25f to theme.primaryColor.copy(alpha = 0.9f),
            0.65f to theme.secondaryColor.copy(alpha = 0.65f),
            1.0f to theme.secondaryColor.copy(alpha = 0.25f)
        )
    }

    private val themeCaches = LensingTheme.entries.associateWith { ThemeColorCache(it) }
    private val scratchGridPath = Path()

    fun render(
        drawScope: DrawScope,
        engine: LensingEngine
    ) {
        val theme = engine.activeTheme
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius
        val photonR = engine.currentPhotonSphereRadius
        val re = engine.currentEinsteinRadius

        with(drawScope) {
            // 1. Spacetime Coordinate Curvature Grid (Optional Mode)
            if (engine.showSpacetimeGrid) {
                renderSpacetimeGrid(engine, theme)
            }

            // 2. Gravitational Deflection of Background Starfield
            renderStarfield(engine)

            // 3. Gravitational Lensed Back-Halo of Accretion Disk (Gargantua geometry)
            if (engine.showAccretionDisk) {
                renderLensedUpperHalo(engine, theme)
            }

            // 4. Front Equatorial Accretion Disk
            if (engine.showAccretionDisk) {
                renderEquatorialDisk(engine, theme)
            }

            // 5. Relativistic Accretion Particles
            if (engine.showAccretionDisk) {
                renderAccretionParticles(engine, theme)
            }

            // 6. Black Hole Singularity Void & Photon Ring
            renderSingularityCore(sx, sy, rs, photonR, re, theme)

            // 7. Spacetime Gravitational Wave Ripples
            renderGravitationalWaves(engine, theme)
        }
    }

    private fun DrawScope.renderSpacetimeGrid(
        engine: LensingEngine,
        theme: LensingTheme
    ) {
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius
        val gridStep = 75f
        val w = engine.width
        val h = engine.height

        val gridColor = theme.secondaryColor.copy(alpha = 0.16f)
        val funnelColor = theme.primaryColor.copy(alpha = 0.35f)

        // Horizontal Spacetime Lines
        var y = 60f
        while (y < h - 40f) {
            scratchGridPath.reset()
            var first = true
            var x = 0f
            while (x <= w) {
                val dx = x - sx
                val dy = y - sy
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                
                // Funnel gravitational pull inward towards singularity
                val pull = if (dist > rs * 0.9f) {
                    (rs * rs * 1.8f / dist).coerceAtMost(dist * 0.7f)
                } else 0f

                val warpedX = x - (dx / dist) * pull
                val warpedY = y - (dy / dist) * pull

                if (dist > rs * 0.8f) {
                    if (first) {
                        scratchGridPath.moveTo(warpedX, warpedY)
                        first = false
                    } else {
                        scratchGridPath.lineTo(warpedX, warpedY)
                    }
                }
                x += 35f
            }
            drawPath(
                path = scratchGridPath,
                color = if (kotlin.math.abs(y - sy) < rs * 2.5f) funnelColor else gridColor,
                style = Stroke(width = 1.2f)
            )
            y += gridStep
        }

        // Vertical Spacetime Lines
        var x = 40f
        while (x < w) {
            scratchGridPath.reset()
            var first = true
            var currentY = 80f
            while (currentY <= h - 40f) {
                val dx = x - sx
                val dy = currentY - sy
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                
                val pull = if (dist > rs * 0.9f) {
                    (rs * rs * 1.8f / dist).coerceAtMost(dist * 0.7f)
                } else 0f

                val warpedX = x - (dx / dist) * pull
                val warpedY = currentY - (dy / dist) * pull

                if (dist > rs * 0.8f) {
                    if (first) {
                        scratchGridPath.moveTo(warpedX, warpedY)
                        first = false
                    } else {
                        scratchGridPath.lineTo(warpedX, warpedY)
                    }
                }
                currentY += 35f
            }
            drawPath(
                path = scratchGridPath,
                color = if (kotlin.math.abs(x - sx) < rs * 2.5f) funnelColor else gridColor,
                style = Stroke(width = 1.2f)
            )
            x += gridStep
        }
    }

    private fun DrawScope.renderStarfield(engine: LensingEngine) {
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius

        for (star in engine.stars) {
            if (star.isInsideHorizon) continue

            val x = star.apparentX
            val y = star.apparentY
            val alpha = star.apparentBrightness.coerceIn(0f, 1f)
            val starColor = star.spectralColor.copy(alpha = alpha)

            // If star is tangentially sheared near the Einstein ring, render as curved/stretched arc
            if (star.tangentialStretch > 1.35f) {
                val stretchLength = (star.apparentRadius * star.tangentialStretch * 3.2f).coerceAtLeast(4f)
                val halfLen = stretchLength * 0.5f

                rotate(
                    degrees = (star.tangentAngleRad * 180f / PI.toFloat()),
                    pivot = Offset(x, y)
                ) {
                    // Soft tangential outer glow
                    drawLine(
                        color = starColor.copy(alpha = alpha * 0.35f),
                        start = Offset(x - halfLen * 1.3f, y),
                        end = Offset(x + halfLen * 1.3f, y),
                        strokeWidth = star.apparentRadius * 2.6f,
                        cap = StrokeCap.Round
                    )
                    // Brilliant sheared core
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = Offset(x - halfLen, y),
                        end = Offset(x + halfLen, y),
                        strokeWidth = star.apparentRadius * 1.2f,
                        cap = StrokeCap.Round
                    )
                }
            } else {
                // Point star with celestial soft diffraction aura
                if (star.apparentRadius > 2.2f) {
                    drawCircle(
                        color = starColor.copy(alpha = alpha * 0.25f),
                        radius = star.apparentRadius * 2.5f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = starColor,
                    radius = star.apparentRadius,
                    center = Offset(x, y)
                )
            }
        }
    }

    private fun DrawScope.renderLensedUpperHalo(
        engine: LensingEngine,
        theme: LensingTheme
    ) {
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius
        val haloRadius = rs * 1.72f
        val haloThickness = rs * 0.55f

        val cache = themeCaches[theme]
        withTransform({
            rotate(degrees = theme.diskAngleDeg * 0.5f, pivot = Offset(sx, sy))
        }) {
            val haloBrush = Brush.radialGradient(
                colors = cache?.haloGradient ?: listOf(theme.primaryColor, Color.Transparent),
                center = Offset(sx, sy - haloRadius * 0.35f),
                radius = haloRadius + haloThickness
            )

            drawArc(
                brush = haloBrush,
                startAngle = 175f,
                sweepAngle = 190f,
                useCenter = false,
                topLeft = Offset(sx - (haloRadius + haloThickness), sy - (haloRadius + haloThickness) * 0.85f),
                size = Size((haloRadius + haloThickness) * 2f, (haloRadius + haloThickness) * 1.7f),
                style = Stroke(width = haloThickness, cap = StrokeCap.Round)
            )

            // Inner razor halo edge
            drawArc(
                color = theme.coreGlowColor.copy(alpha = 0.9f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(sx - haloRadius, sy - haloRadius * 0.8f),
                size = Size(haloRadius * 2f, haloRadius * 1.6f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
    }

    private fun DrawScope.renderEquatorialDisk(
        engine: LensingEngine,
        theme: LensingTheme
    ) {
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius
        val diskOuterR = rs * 3.6f
        val diskInnerR = rs * 1.45f
        val diskThickness = diskOuterR - diskInnerR

        withTransform({
            rotate(degrees = theme.diskAngleDeg, pivot = Offset(sx, sy))
            scale(scaleX = 1.0f, scaleY = 0.28f, pivot = Offset(sx, sy)) // Isometric perspective squash
        }) {
            // Asymmetric Doppler beaming: Approaching limb (left side) is boosted and blue-shifted;
            // receding limb (right side) is red-shifted and dimmer.
            val cache = themeCaches[theme]
            val dopplerBrush = if (cache != null) {
                Brush.horizontalGradient(
                    colorStops = cache.dopplerColorStops,
                    startX = sx - diskOuterR,
                    endX = sx + diskOuterR
                )
            } else {
                Brush.horizontalGradient(
                    0.0f to Color.White.copy(alpha = 0.95f),
                    1.0f to theme.secondaryColor.copy(alpha = 0.25f),
                    startX = sx - diskOuterR,
                    endX = sx + diskOuterR
                )
            }

            // Primary glowing accretion torus
            drawCircle(
                brush = dopplerBrush,
                radius = (diskInnerR + diskOuterR) * 0.5f,
                center = Offset(sx, sy),
                style = Stroke(width = diskThickness)
            )

            // Hot inner accretion rim
            drawCircle(
                color = theme.coreGlowColor.copy(alpha = 0.95f),
                radius = diskInnerR,
                center = Offset(sx, sy),
                style = Stroke(width = 4.0f)
            )
        }
    }

    private fun DrawScope.renderAccretionParticles(
        engine: LensingEngine,
        theme: LensingTheme
    ) {
        val sx = engine.singularityX
        val sy = engine.singularityY
        val rs = engine.currentSchwarzschildRadius

        val diskTiltRad = theme.diskAngleDeg * PI.toFloat() / 180f
        val cosTilt = cos(diskTiltRad)
        val sinTilt = sin(diskTiltRad)

        for (p in engine.accretionParticles) {
            val r = p.baseRadiusFraction * rs
            // Local 2D disk coordinates (squashed in Y for 3D perspective)
            val localX = r * cos(p.angleRad)
            val localY = r * sin(p.angleRad) * 0.28f + p.zOffset

            // Rotate by disk angle
            val rotX = localX * cosTilt - localY * sinTilt
            val rotY = localX * sinTilt + localY * cosTilt

            val px = sx + rotX
            val py = sy + rotY

            // Doppler boost factor based on X velocity: cos(angleRad + pi/2) = -sin(angleRad)
            val dopplerFactor = (-sin(p.angleRad) * 0.5f + 0.5f).coerceIn(0.15f, 1.0f)
            val particleAlpha = (0.35f + dopplerFactor * 0.65f).coerceIn(0f, 1f)

            val particleColor = if (dopplerFactor > 0.7f) {
                Color.White.copy(alpha = particleAlpha)
            } else {
                theme.primaryColor.copy(alpha = particleAlpha)
            }

            drawCircle(
                color = particleColor,
                radius = p.size * (0.8f + dopplerFactor * 0.5f),
                center = Offset(px, py)
            )
        }
    }

    private fun DrawScope.renderSingularityCore(
        sx: Float,
        sy: Float,
        rs: Float,
        photonR: Float,
        re: Float,
        theme: LensingTheme
    ) {
        val cache = themeCaches[theme]

        // 1. Outer Gravitational Lensing Halo / Einstein Ring ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = cache?.einsteinRingGlow ?: listOf(theme.primaryColor, Color.Transparent),
                center = Offset(sx, sy),
                radius = re * 1.35f
            ),
            radius = re * 1.35f,
            center = Offset(sx, sy)
        )

        // 2. Photon Sphere radiant glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = cache?.photonSphereGlow ?: listOf(Color.White, Color.Transparent),
                center = Offset(sx, sy),
                radius = photonR * 1.25f
            ),
            radius = photonR * 1.25f,
            center = Offset(sx, sy)
        )

        // 3. Razor-sharp Photon Ring line (r = 1.5 Rs)
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = photonR,
            center = Offset(sx, sy),
            style = Stroke(width = 2.5f)
        )

        // 4. Pure Pitch-Black Event Horizon Void (Rs)
        // Nothing can escape this boundary — not even light.
        drawCircle(
            color = Color(0xFF030308),
            radius = rs,
            center = Offset(sx, sy)
        )

        // Soft internal shadow rim at event horizon boundary
        drawCircle(
            color = Color.Black,
            radius = rs * 0.94f,
            center = Offset(sx, sy)
        )
    }

    private fun DrawScope.renderGravitationalWaves(
        engine: LensingEngine,
        theme: LensingTheme
    ) {
        val sx = engine.singularityX
        val sy = engine.singularityY

        for (wave in engine.gravitationalWaves) {
            val alpha = (wave.amplitude * 0.75f).coerceIn(0f, 1f)
            if (alpha <= 0.01f) continue

            // Spacetime metric ripple ring
            drawCircle(
                color = theme.primaryColor.copy(alpha = alpha),
                radius = wave.currentRadius,
                center = Offset(sx, sy),
                style = Stroke(width = 3.0f * (1f + wave.amplitude))
            )

            // High-frequency interference echo
            if (wave.currentRadius > 35f) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.45f),
                    radius = wave.currentRadius - 18f,
                    center = Offset(sx, sy),
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }
}
