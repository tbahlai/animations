package com.tsynytsyna.animations.gravitational_lensing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class LensingTheme(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val coreGlowColor: Color,
    val diskAngleDeg: Float
) {
    GARGANTUA(
        title = "Gargantua",
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFFFF6D00),
        coreGlowColor = Color(0xFFFFD54F),
        diskAngleDeg = -18f
    ),
    CYGNUS_X1(
        title = "Cygnus X-1",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF2979FF),
        coreGlowColor = Color(0xFF80D8FF),
        diskAngleDeg = -30f
    ),
    SAGITTARIUS_A(
        title = "Sagittarius A*",
        primaryColor = Color(0xFFE040FB),
        secondaryColor = Color(0xFFFF4081),
        coreGlowColor = Color(0xFFEA80FC),
        diskAngleDeg = 15f
    ),
    SPACETIME_METRIC(
        title = "Curvature Grid",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF00B0FF),
        coreGlowColor = Color(0xFFB9F6CA),
        diskAngleDeg = 0f
    )
}

data class BackgroundStar(
    val originX: Float,
    val originY: Float,
    var apparentX: Float = 0f,
    var apparentY: Float = 0f,
    val baseRadius: Float,
    var apparentRadius: Float = 0f,
    val baseBrightness: Float,
    var apparentBrightness: Float = 0f,
    var tangentialStretch: Float = 1f,
    var tangentAngleRad: Float = 0f,
    var isInsideHorizon: Boolean = false,
    val spectralColor: Color
)

data class AccretionParticle(
    val baseRadiusFraction: Float, // 1.8 to 4.8 of Rs
    var angleRad: Float,
    val speedMultiplier: Float,
    val size: Float,
    val zOffset: Float, // vertical thickness in disk
    val baseColorFactor: Float // variation in hue
)

data class GravitationalWave(
    var currentRadius: Float,
    val maxRadius: Float,
    var amplitude: Float,
    val speed: Float
)

class LensingEngine {
    var width: Float = 1080f
        private set
    var height: Float = 2200f
        private set

    // Singularity State
    var singularityX: Float = 540f
    var singularityY: Float = 900f
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var isDragging: Boolean = false
    var isOrbiting: Boolean = true

    // Mass & Relativistic Radii (in pixels)
    var massMultiplier by mutableFloatStateOf(1.0f)
    val baseSchwarzschildRadius: Float = 85f // Event horizon radius Rs
    val baseEinsteinRadius: Float = 220f     // Einstein ring radius Re

    val currentSchwarzschildRadius: Float
        get() = baseSchwarzschildRadius * massMultiplier

    val currentPhotonSphereRadius: Float
        get() = currentSchwarzschildRadius * 1.5f

    val currentEinsteinRadius: Float
        get() = baseEinsteinRadius * sqrt(massMultiplier)

    // Cosmic Objects
    val stars = mutableListOf<BackgroundStar>()
    val accretionParticles = mutableListOf<AccretionParticle>()
    val gravitationalWaves = mutableListOf<GravitationalWave>()

    // View Options
    var showSpacetimeGrid by mutableStateOf(true)
    var showAccretionDisk by mutableStateOf(true)
    var activeTheme by mutableStateOf(LensingTheme.GARGANTUA)

    // Internal simulation time
    private var simTime: Float = 0f
    private val random = Random(42)

    fun initialize(screenWidth: Float, screenHeight: Float) {
        width = screenWidth
        height = screenHeight
        singularityX = width * 0.5f
        singularityY = height * 0.42f
        velocityX = 25f
        velocityY = 15f

        initStarfield()
        initAccretionDisk()
    }

    private fun initStarfield() {
        stars.clear()
        val spectralPalette = listOf(
            Color(0xFFE1F5FE), // O-type bluish white
            Color(0xFFFFFFFF), // A-type pure white
            Color(0xFFFFF9C4), // G-type solar yellow
            Color(0xFFFFCCBC), // K-type orange
            Color(0xFFFF8A80), // M-type red giant
            Color(0xFF80D8FF)  // Pulsar electric cyan
        )

        val starCount = 220
        for (i in 0 until starCount) {
            val ox = random.nextFloat() * width
            val oy = random.nextFloat() * height
            val r = 1.0f + random.nextFloat() * 2.8f
            val bright = 0.35f + random.nextFloat() * 0.65f
            val color = spectralPalette[random.nextInt(spectralPalette.size)]

            stars.add(
                BackgroundStar(
                    originX = ox,
                    originY = oy,
                    baseRadius = r,
                    baseBrightness = bright,
                    spectralColor = color
                )
            )
        }
    }

    private fun initAccretionDisk() {
        accretionParticles.clear()
        val particleCount = 180
        for (i in 0 until particleCount) {
            val rFraction = 1.7f + (random.nextFloat() * 2.8f) // 1.7 to 4.5 * Rs
            val angle = random.nextFloat() * 2f * PI.toFloat()
            // Keplerian velocity profile: v proportional to r^(-0.5)
            val speed = (1.5f / sqrt(rFraction)) * (0.85f + random.nextFloat() * 0.3f)
            val size = 2.0f + random.nextFloat() * 4.0f
            val zOff = (random.nextFloat() - 0.5f) * 12f
            val factor = random.nextFloat()

            accretionParticles.add(
                AccretionParticle(
                    baseRadiusFraction = rFraction,
                    angleRad = angle,
                    speedMultiplier = speed,
                    size = size,
                    zOffset = zOff,
                    baseColorFactor = factor
                )
            )
        }
    }

    fun step(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.05f)
        simTime += dt

        // 1. Singularity motion (when not dragged by user)
        if (!isDragging && isOrbiting) {
            singularityX += velocityX * dt
            singularityY += velocityY * dt

            // Soft cosmic orbital bounce boundaries
            val marginX = width * 0.22f
            val marginYTop = height * 0.22f
            val marginYBottom = height * 0.68f

            if (singularityX < marginX) {
                singularityX = marginX
                velocityX = kotlin.math.abs(velocityX)
            } else if (singularityX > width - marginX) {
                singularityX = width - marginX
                velocityX = -kotlin.math.abs(velocityX)
            }

            if (singularityY < marginYTop) {
                singularityY = marginYTop
                velocityY = kotlin.math.abs(velocityY)
            } else if (singularityY > marginYBottom) {
                singularityY = marginYBottom
                velocityY = -kotlin.math.abs(velocityY)
            }
        }

        // 2. Gravitational light ray deflection for starfield
        val rs = currentSchwarzschildRadius
        val re = currentEinsteinRadius
        val reSq = re * re

        for (star in stars) {
            val dx = star.originX - singularityX
            val dy = star.originY - singularityY
            val trueDist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
            val angle = atan2(dy, dx)

            // Event horizon shadow check: light originating directly behind horizon is absorbed
            if (trueDist < rs * 0.85f) {
                star.isInsideHorizon = true
                star.apparentBrightness = 0f
                continue
            } else {
                star.isInsideHorizon = false
            }

            // General relativistic lens equation:
            // Apparent position shifts outward by (Re^2 / d)
            // Near the optical axis (small trueDist), this forms an Einstein ring arc!
            val apparentDist = (trueDist + sqrt(trueDist * trueDist + 4f * reSq)) * 0.5f

            // Gravitational wave metric perturbation
            var waveOffset = 0f
            for (wave in gravitationalWaves) {
                val waveDist = kotlin.math.abs(apparentDist - wave.currentRadius)
                if (waveDist < 60f) {
                    val phase = (waveDist / 60f) * PI.toFloat()
                    waveOffset += sin(phase) * wave.amplitude * 8f
                }
            }

            val finalDist = apparentDist + waveOffset
            star.apparentX = singularityX + cos(angle) * finalDist
            star.apparentY = singularityY + sin(angle) * finalDist

            // Tangential shearing & flux magnification:
            // Stars close to Re stretch tangentially into glowing arcs
            val u = trueDist / re.coerceAtLeast(1f)
            val magnification = ((u * u + 2f) / (2f * u * sqrt(u * u + 4f))).coerceIn(1.0f, 6.0f)
            star.apparentBrightness = (star.baseBrightness * magnification).coerceIn(0f, 1.5f)

            // Tangential stretch factor for Einstein arcs
            star.tangentialStretch = (1f + (reSq / (trueDist * trueDist * 1.2f))).coerceIn(1f, 4.5f)
            star.tangentAngleRad = angle + (PI.toFloat() * 0.5f) // perpendicular to radial vector
            star.apparentRadius = star.baseRadius * sqrt(magnification)
        }

        // 3. Update matter accretion disk particles
        for (p in accretionParticles) {
            // Keplerian orbital progression
            p.angleRad += p.speedMultiplier * dt * 2.2f
            if (p.angleRad > 2f * PI.toFloat()) {
                p.angleRad -= 2f * PI.toFloat()
            }
        }

        // 4. Update expanding gravitational waves
        val waveIterator = gravitationalWaves.iterator()
        while (waveIterator.hasNext()) {
            val wave = waveIterator.next()
            wave.currentRadius += wave.speed * dt
            wave.amplitude *= (1f - dt * 0.85f) // attenuation
            if (wave.currentRadius > wave.maxRadius || wave.amplitude < 0.02f) {
                waveIterator.remove()
            }
        }
    }

    fun triggerGravitationalWave() {
        gravitationalWaves.add(
            GravitationalWave(
                currentRadius = currentPhotonSphereRadius,
                maxRadius = width * 1.1f,
                amplitude = 1.0f,
                speed = 650f // Spacetime ripple speed (pixels/sec)
            )
        )
    }

    fun onDragStart(x: Float, y: Float) {
        val dx = x - singularityX
        val dy = y - singularityY
        val d = sqrt(dx * dx + dy * dy)
        if (d < currentEinsteinRadius * 1.25f) {
            isDragging = true
            isOrbiting = false
        }
    }

    fun onDrag(newX: Float, newY: Float) {
        if (isDragging) {
            val clampedX = newX.coerceIn(80f, width - 80f)
            val clampedY = newY.coerceIn(180f, height - 260f)
            velocityX = (clampedX - singularityX) * 15f
            velocityY = (clampedY - singularityY) * 15f
            singularityX = clampedX
            singularityY = clampedY
        }
    }

    fun onDragEnd() {
        if (isDragging) {
            isDragging = false
            velocityX = velocityX.coerceIn(-280f, 280f)
            velocityY = velocityY.coerceIn(-280f, 280f)
        }
    }

    fun resetSingularity() {
        singularityX = width * 0.5f
        singularityY = height * 0.42f
        velocityX = 25f
        velocityY = 15f
        massMultiplier = 1.0f
        isDragging = false
        isOrbiting = true
        triggerGravitationalWave()
    }
}
