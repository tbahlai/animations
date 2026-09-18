package com.tsynytsyna.animations.fluid_metaballs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Represents a single viscous fluid droplet in 2D space.
 */
data class FluidDroplet(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float,
    var targetRadius: Float = radius,
    var isDragging: Boolean = false,
    var wobbleAngle: Float = 0f,
    var wobbleAmplitude: Float = 0f,
    var wobbleDecay: Float = 0f,
    var wobbleDirX: Float = 1f,
    var wobbleDirY: Float = 0f,
    var hueShift: Float = 0f,
) {
    val mass: Float
        get() = (radius * radius) / 1600f

    fun distanceTo(other: FluidDroplet): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

    fun triggerSnapWobble(dirX: Float, dirY: Float, intensity: Float = 1.0f) {
        val len = sqrt(dirX * dirX + dirY * dirY).coerceAtLeast(0.001f)
        wobbleDirX = dirX / len
        wobbleDirY = dirY / len
        wobbleAmplitude = (radius * 0.38f * intensity).coerceIn(4f, 35f)
        wobbleAngle = 0f
        wobbleDecay = 1.0f
    }
}

/**
 * Visual Theme definitions for different fluid materials.
 */
enum class FluidTheme(
    val title: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val glowColorHex: Long,
    val specularColorHex: Long,
    val surfaceReflectivity: Float,
) {
    MERCURY(
        title = "Chrome Mercury",
        description = "Liquid metal with high reflectivity & viscous surface tension",
        primaryColorHex = 0xFFECEFF1,
        secondaryColorHex = 0xFF78909C,
        glowColorHex = 0xFF90CAF9,
        specularColorHex = 0xFFFFFFFF,
        surfaceReflectivity = 0.95f
    ),
    MAGMA(
        title = "Molten Magma",
        description = "Radiant volcanic core with incandescent heat aura",
        primaryColorHex = 0xFFFF3D00,
        secondaryColorHex = 0xFFFF9100,
        glowColorHex = 0xFFFFAB00,
        specularColorHex = 0xFFFFF9C4,
        surfaceReflectivity = 0.70f
    ),
    CYBERPUNK_BIOLUM(
        title = "Bioluminescence",
        description = "Deep abyssal neon cyan & magenta glowing fluid",
        primaryColorHex = 0xFF00E5FF,
        secondaryColorHex = 0xFFD500F9,
        glowColorHex = 0xFF00B0FF,
        specularColorHex = 0xFFE0F7FA,
        surfaceReflectivity = 0.85f
    ),
    EMERALD_ACID(
        title = "Venomous Acid",
        description = "Radioactive reactive slime with pulsating vitality",
        primaryColorHex = 0xFF00E676,
        secondaryColorHex = 0xFFAEEA00,
        glowColorHex = 0xFF76FF03,
        specularColorHex = 0xFFF1F8E9,
        surfaceReflectivity = 0.75f
    ),
}

/**
 * High-performance fluid simulation engine tracking droplet positions,
 * inter-droplet cohesion, boundary damping, surface tension, and snap rebounds.
 */
class MetaballEngine {

    val droplets = mutableStateListOf<FluidDroplet>()

    var theme by mutableStateOf(FluidTheme.MERCURY)
    var gravityEnabled by mutableStateOf(false)
    var viscosity by mutableFloatStateOf(1.0f) // 0.5f (thin liquid) to 2.0f (heavy gel/honey)
    var surfaceTension by mutableFloatStateOf(1.2f) // max bridge stretch multiplier

    var activeConnectionsCount by mutableStateOf(0)
        private set

    private var nextId = 1L
    private var boundsWidth = 1000f
    private var boundsHeight = 1800f

    /**
     * Initializes default droplet configuration tailored for touch screens.
     */
    fun initialize(width: Float, height: Float) {
        boundsWidth = width
        boundsHeight = height
        gravityEnabled = false
        droplets.clear()

        val cx = width * 0.5f
        val cy = height * 0.46f

        // Central mother droplet
        droplets.add(
            FluidDroplet(
                id = nextId++,
                x = cx,
                y = cy,
                radius = 82f,
                targetRadius = 82f,
                hueShift = 0f
            )
        )

        // Orbiting satellite droplets
        droplets.add(
            FluidDroplet(
                id = nextId++,
                x = cx - 110f,
                y = cy - 90f,
                vx = -12f,
                vy = 18f,
                radius = 58f,
                targetRadius = 58f,
                hueShift = 0.15f
            )
        )
        droplets.add(
            FluidDroplet(
                id = nextId++,
                x = cx + 120f,
                y = cy - 70f,
                vx = 20f,
                vy = -14f,
                radius = 52f,
                targetRadius = 52f,
                hueShift = -0.1f
            )
        )
        droplets.add(
            FluidDroplet(
                id = nextId++,
                x = cx - 75f,
                y = cy + 130f,
                vx = 14f,
                vy = 16f,
                radius = 46f,
                targetRadius = 46f,
                hueShift = 0.25f
            )
        )
        droplets.add(
            FluidDroplet(
                id = nextId++,
                x = cx + 90f,
                y = cy + 120f,
                vx = -16f,
                vy = -18f,
                radius = 42f,
                targetRadius = 42f,
                hueShift = -0.2f
            )
        )
    }

    /**
     * Advances the physical simulation by dt seconds.
     */
    fun step(dt: Float, width: Float, height: Float) {
        boundsWidth = width
        boundsHeight = height
        val clampedDt = dt.coerceIn(0.001f, 0.033f)

        val gravityAcc = if (gravityEnabled) 980f else 0f
        val fluidDrag = (0.75f / viscosity).coerceIn(0.2f, 1.8f)

        var connections = 0

        // 1. Inter-droplet cohesion & repulsion forces
        val count = droplets.size
        for (i in 0 until count) {
            val a = droplets[i]
            for (j in i + 1 until count) {
                val b = droplets[j]
                val dx = b.x - a.x
                val dy = b.y - a.y
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                val sumR = a.radius + b.radius
                val maxBridge = sumR * surfaceTension * 1.55f

                if (dist < maxBridge) {
                    connections++
                    val nx = dx / dist
                    val ny = dy / dist

                    if (dist < sumR) {
                        // Strong elastic repulsion preventing total overlap
                        val overlap = sumR - dist
                        val force = overlap * 140f
                        if (!a.isDragging) {
                            a.vx -= nx * force * clampedDt / a.mass
                            a.vy -= ny * force * clampedDt / a.mass
                        }
                        if (!b.isDragging) {
                            b.vx += nx * force * clampedDt / b.mass
                            b.vy += ny * force * clampedDt / b.mass
                        }
                    } else {
                        // Cohesive surface tension attraction (pulling towards each other)
                        val stretch = (dist - sumR) / (maxBridge - sumR)
                        val pull = (1f - stretch) * 65f * surfaceTension
                        if (!a.isDragging) {
                            a.vx += nx * pull * clampedDt / a.mass
                            a.vy += ny * pull * clampedDt / a.mass
                        }
                        if (!b.isDragging) {
                            b.vx -= nx * pull * clampedDt / b.mass
                            b.vy -= ny * pull * clampedDt / b.mass
                        }
                    }
                }
            }
        }
        activeConnectionsCount = connections

        // 2. Integration and boundary handling for each droplet
        for (droplet in droplets) {
            // Smooth target radius interpolation (for merges or spawns)
            if (droplet.radius != droplet.targetRadius) {
                droplet.radius += (droplet.targetRadius - droplet.radius) * 0.15f
            }

            // Wobble physics decay post-snap
            if (droplet.wobbleDecay > 0.01f) {
                droplet.wobbleAngle += 32f * clampedDt
                droplet.wobbleDecay *= (1f - 3.5f * clampedDt)
            } else {
                droplet.wobbleDecay = 0f
                droplet.wobbleAmplitude = 0f
            }

            if (droplet.isDragging) continue

            // Gravity
            droplet.vy += gravityAcc * clampedDt

            // Zero-G ambient Brownian wandering
            if (!gravityEnabled) {
                droplet.vx += (sin(droplet.y * 0.015f + droplet.id.toFloat()) * 18f) * clampedDt
                droplet.vy += (cos(droplet.x * 0.015f + droplet.id.toFloat()) * 18f) * clampedDt
            }

            // Fluid drag
            droplet.vx *= (1f - fluidDrag * clampedDt)
            droplet.vy *= (1f - fluidDrag * clampedDt)

            // Velocity update
            droplet.x += droplet.vx * clampedDt
            droplet.y += droplet.vy * clampedDt

            // Boundary bounce damping
            val r = droplet.radius
            val restitution = 0.65f

            if (droplet.x - r < 0f) {
                droplet.x = r
                droplet.vx = -droplet.vx * restitution
                droplet.triggerSnapWobble(1f, 0f, 0.4f)
            } else if (droplet.x + r > width) {
                droplet.x = width - r
                droplet.vx = -droplet.vx * restitution
                droplet.triggerSnapWobble(-1f, 0f, 0.4f)
            }

            // Top boundary (clears top status bar & pill)
            val topPadding = 220f
            if (droplet.y - r < topPadding) {
                droplet.y = topPadding + r
                droplet.vy = -droplet.vy * restitution
                droplet.triggerSnapWobble(0f, 1f, 0.4f)
            }
            // Bottom boundary (clears bottom floating control bar)
            val bottomPadding = height - 360f
            if (droplet.y + r > bottomPadding) {
                droplet.y = bottomPadding - r
                droplet.vy = -droplet.vy * restitution
                droplet.triggerSnapWobble(0f, -1f, 0.4f)
            }
        }
    }

    /**
     * Attempts to grab the nearest droplet near touch (x, y).
     */
    fun grabDroplet(x: Float, y: Float): Long? {
        var closest: FluidDroplet? = null
        var closestDist = Float.MAX_VALUE

        for (d in droplets) {
            val dist = sqrt((d.x - x) * (d.x - x) + (d.y - y) * (d.y - y))
            if (dist < d.radius + 36f && dist < closestDist) {
                closest = d
                closestDist = dist
            }
        }

        closest?.let {
            it.isDragging = true
            it.vx = 0f
            it.vy = 0f
            return it.id
        }
        return null
    }

    /**
     * Updates dragging droplet position and velocity.
     */
    fun dragDroplet(id: Long, x: Float, y: Float, vx: Float, vy: Float) {
        val d = droplets.find { it.id == id } ?: return
        d.x = x.coerceIn(d.radius, boundsWidth - d.radius)
        d.y = y.coerceIn(220f + d.radius, boundsHeight - 360f - d.radius)
        d.vx = vx
        d.vy = vy
    }

    /**
     * Releases dragging droplet, applying fling velocity and snapping impulse.
     */
    fun releaseDroplet(id: Long, flingVx: Float, flingVy: Float) {
        val d = droplets.find { it.id == id } ?: return
        d.isDragging = false
        d.vx = flingVx.coerceIn(-1800f, 1800f)
        d.vy = flingVy.coerceIn(-1800f, 1800f)

        // Check if it was stretched away from a neighbor to trigger snap wobble
        var nearestNeighborDist = Float.MAX_VALUE
        var neighborDirX = 0f
        var neighborDirY = 0f

        for (other in droplets) {
            if (other.id == id) continue
            val dist = d.distanceTo(other)
            if (dist < nearestNeighborDist) {
                nearestNeighborDist = dist
                neighborDirX = other.x - d.x
                neighborDirY = other.y - d.y
            }
        }

        d.triggerSnapWobble(neighborDirX, neighborDirY, 0.8f)
    }

    /**
     * Spawns a new fluid droplet at the specified coordinate.
     */
    fun spawnDroplet(x: Float, y: Float) {
        if (droplets.size >= 16) {
            // Remove oldest small droplet if exceeding budget
            val oldest = droplets.filter { !it.isDragging }.minByOrNull { it.id }
            if (oldest != null) droplets.remove(oldest)
        }

        val baseRadius = Random.nextFloat() * 24f + 36f
        val newDrop = FluidDroplet(
            id = nextId++,
            x = x.coerceIn(baseRadius + 20f, boundsWidth - baseRadius - 20f),
            y = y.coerceIn(160f + baseRadius, boundsHeight - 200f - baseRadius),
            vx = (Random.nextFloat() - 0.5f) * 120f,
            vy = (Random.nextFloat() - 0.5f) * 120f,
            radius = 10f,
            targetRadius = baseRadius,
            hueShift = (Random.nextFloat() - 0.5f) * 0.4f
        )
        newDrop.triggerSnapWobble(0f, 1f, 1.0f)
        droplets.add(newDrop)
    }

    /**
     * Merges two colliding droplets into a unified higher-mass orb.
     */
    fun mergeDroplets(aId: Long, bId: Long) {
        val a = droplets.find { it.id == aId } ?: return
        val b = droplets.find { it.id == bId } ?: return

        // New area = sum of areas -> r_new = sqrt(r1^2 + r2^2)
        val combinedRadius = sqrt(a.radius * a.radius + b.radius * b.radius).coerceAtMost(120f)
        val newX = (a.x * a.mass + b.x * b.mass) / (a.mass + b.mass)
        val newY = (a.y * a.mass + b.y * b.mass) / (a.mass + b.mass)
        val newVx = (a.vx * a.mass + b.vx * b.mass) / (a.mass + b.mass)
        val newVy = (a.vy * a.mass + b.vy * b.mass) / (a.mass + b.mass)

        a.x = newX
        a.y = newY
        a.vx = newVx
        a.vy = newVy
        a.radius = min(a.radius, b.radius)
        a.targetRadius = combinedRadius
        a.triggerSnapWobble(0f, 1f, 1.2f)

        droplets.remove(b)
    }

    /**
     * Clears all secondary droplets and resets to central core.
     */
    fun clearChamber() {
        if (droplets.isEmpty()) return
        val core = droplets.maxByOrNull { it.radius } ?: droplets.first()
        core.targetRadius = 80f
        core.x = boundsWidth * 0.5f
        core.y = boundsHeight * 0.46f
        core.vx = 0f
        core.vy = 0f
        droplets.retainAll(listOf(core))
    }
}
