package com.tsynytsyna.animations.kinetic_typography

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class TypographyTheme(
    val title: String,
    val backgroundColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val ribbonColor: Color,
    val glowColor: Color
) {
    SWISS_KINETIC(
        title = "Swiss Modern",
        backgroundColor = Color(0xFF09090B),
        textColor = Color(0xFFFAFAFA),
        accentColor = Color(0xFFFF2A55),
        ribbonColor = Color(0xFF27272A),
        glowColor = Color(0xFFFF2A55).copy(alpha = 0.4f)
    ),
    CYBER_HOLO(
        title = "Cyber Hologram",
        backgroundColor = Color(0xFF030712),
        textColor = Color(0xFFE0F2FE),
        accentColor = Color(0xFF00F0FF),
        ribbonColor = Color(0xFF1E293B),
        glowColor = Color(0xFF00F0FF).copy(alpha = 0.5f)
    ),
    MOLTEN_GOLD(
        title = "Molten Chrome",
        backgroundColor = Color(0xFF0C0A09),
        textColor = Color(0xFFFDE047),
        accentColor = Color(0xFFF97316),
        ribbonColor = Color(0xFF292524),
        glowColor = Color(0xFFF97316).copy(alpha = 0.45f)
    ),
    NEO_TOKYO(
        title = "Neo Tokyo",
        backgroundColor = Color(0xFF050510),
        textColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFFD946EF),
        ribbonColor = Color(0xFF3B0764),
        glowColor = Color(0xFFD946EF).copy(alpha = 0.5f)
    )
}

enum class MotionMode(val label: String) {
    SPRING_PULL("SPRING PULL"),
    SINE_RIPPLE("SINE RIPPLE"),
    SHOCKWAVE("SHOCKWAVE")
}

data class LetterNode(
    val index: Int,
    val char: Char,
    var eqX: Float = 0f,
    var eqY: Float = 0f,
    var currentX: Float = 0f,
    var currentY: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var rotationDeg: Float = 0f,
    var isPinned: Boolean = false
)

class TypographyPhysicsEngine {
    var width: Float = 1080f
        private set
    var height: Float = 2200f
        private set

    var currentWord by mutableStateOf("KINETIC")
    var activeTheme by mutableStateOf(TypographyTheme.SWISS_KINETIC)
    var motionMode by mutableStateOf(MotionMode.SPRING_PULL)

    var stiffness by mutableFloatStateOf(180f)   // Spring constant k
    var damping by mutableFloatStateOf(12f)       // Friction damping c
    var waveSpeed by mutableFloatStateOf(4.5f)

    val letters = mutableListOf<LetterNode>()

    private var pinnedIndex: Int? = null
    private var dragTouchX: Float = 0f
    private var dragTouchY: Float = 0f
    private var simTime: Float = 0f

    fun initialize(screenWidth: Float, screenHeight: Float) {
        width = screenWidth
        height = screenHeight
        rebuildWord(currentWord)
    }

    fun rebuildWord(newWord: String) {
        currentWord = newWord
        letters.clear()

        val letterCount = newWord.length
        val letterSpacing = (width * 0.82f / letterCount.coerceAtLeast(1)).coerceIn(42f, 130f)
        val startX = (width - (letterCount - 1) * letterSpacing) * 0.5f
        val centerY = height * 0.45f

        newWord.forEachIndexed { i, c ->
            val eqX = startX + i * letterSpacing
            val eqY = centerY
            letters.add(
                LetterNode(
                    index = i,
                    char = c,
                    eqX = eqX,
                    eqY = eqY,
                    currentX = eqX,
                    currentY = eqY
                )
            )
        }
    }

    fun step(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.04f)
        simTime += dt

        val n = letters.size
        if (n == 0) return

        // 1. Continuous Sine Ripple Motion (if mode active)
        if (motionMode == MotionMode.SINE_RIPPLE && pinnedIndex == null) {
            for (i in 0 until n) {
                val letter = letters[i]
                val phase = simTime * waveSpeed + (i * 0.65f)
                val targetOffsetY = sin(phase) * 65f
                val targetOffsetX = cos(phase * 0.5f) * 12f

                // Gently pull toward modulated sine path
                val fx = (letter.eqX + targetOffsetX - letter.currentX) * stiffness * 0.8f
                val fy = (letter.eqY + targetOffsetY - letter.currentY) * stiffness * 0.8f

                letter.vx = (letter.vx + fx * dt) * (1f - damping * 0.5f * dt)
                letter.vy = (letter.vy + fy * dt) * (1f - damping * 0.5f * dt)

                letter.currentX += letter.vx * dt
                letter.currentY += letter.vy * dt

                // Rotational wave tilt
                letter.rotationDeg = cos(phase) * 14f
                // Dynamic squash & stretch
                val stretch = 1f + (letter.vy / 400f).coerceIn(-0.35f, 0.35f)
                letter.scaleY = stretch
                letter.scaleX = 1f / sqrt(stretch.coerceAtLeast(0.5f))
            }
            return
        }

        // 2. Spring-Mass Dynamics with Neighbor Coupling
        for (i in 0 until n) {
            val letter = letters[i]

            if (letter.isPinned) {
                // Directly pinned to finger position
                letter.currentX = dragTouchX
                letter.currentY = dragTouchY
                letter.vx = 0f
                letter.vy = 0f
                continue
            }

            // A. Restoring force towards equilibrium position
            var fx = (letter.eqX - letter.currentX) * stiffness
            var fy = (letter.eqY - letter.currentY) * stiffness

            // B. Neighbor coupling springs (elastic ribbon effect)
            val idealSpacing = if (n > 1) (letters.last().eqX - letters.first().eqX) / (n - 1) else 60f
            val couplingK = stiffness * 0.65f

            if (i > 0) {
                val prev = letters[i - 1]
                val dx = letter.currentX - prev.currentX
                val dy = letter.currentY - prev.currentY
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val strain = dist - idealSpacing
                fx -= (dx / dist) * strain * couplingK
                fy -= (dy / dist) * strain * couplingK
            }

            if (i < n - 1) {
                val next = letters[i + 1]
                val dx = next.currentX - letter.currentX
                val dy = next.currentY - letter.currentY
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                val strain = dist - idealSpacing
                fx += (dx / dist) * strain * couplingK
                fy += (dy / dist) * strain * couplingK
            }

            // C. Damping resistance
            fx -= letter.vx * damping
            fy -= letter.vy * damping

            // Integrate acceleration & velocity
            letter.vx += fx * dt
            letter.vy += fy * dt

            letter.currentX += letter.vx * dt
            letter.currentY += letter.vy * dt

            // Dynamic velocity-induced squash & stretch
            val speed = sqrt(letter.vx * letter.vx + letter.vy * letter.vy)
            val stretchFactor = (1f + speed / 650f).coerceIn(1.0f, 2.2f)
            letter.scaleY = stretchFactor
            letter.scaleX = 1f / sqrt(stretchFactor)

            // Tilt in direction of velocity
            val angle = if (speed > 10f) (letter.vx / 30f).coerceIn(-28f, 28f) else 0f
            letter.rotationDeg += (angle - letter.rotationDeg) * 0.15f
        }
    }

    fun onTouchStart(x: Float, y: Float) {
        // Find closest letter node to touch point
        var closestIdx: Int? = null
        var minDist = Float.MAX_VALUE

        letters.forEachIndexed { i, letter ->
            val dx = letter.currentX - x
            val dy = letter.currentY - y
            val d = sqrt(dx * dx + dy * dy)
            if (d < minDist && d < 180f) {
                minDist = d
                closestIdx = i
            }
        }

        if (closestIdx != null) {
            pinnedIndex = closestIdx
            letters[closestIdx!!].isPinned = true
            dragTouchX = x
            dragTouchY = y
        }
    }

    fun onTouchMove(x: Float, y: Float) {
        dragTouchX = x
        dragTouchY = y
    }

    fun onTouchEnd() {
        pinnedIndex?.let {
            letters[it].isPinned = false
        }
        pinnedIndex = null
    }

    fun triggerShockwave(originX: Float = width * 0.5f, originY: Float = height * 0.45f) {
        for (letter in letters) {
            val dx = letter.currentX - originX
            val dy = letter.currentY - originY
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(10f)
            val impulse = 1200f / (dist * 0.008f + 1f)
            letter.vx += (dx / dist) * impulse
            letter.vy += (dy / dist) * impulse
        }
    }
}
