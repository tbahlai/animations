package com.tsynytsyna.animations.boids_swarm

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * High-performance 120 FPS Craig Reynolds Flocking Engine with O(N) Spatial Grid Hashing.
 * Uses flat primitive arrays for zero GC allocation during frame updates.
 */
class BoidsEngine(
    val maxBoids: Int = 2500,
    var config: BoidsConfig = BoidsConfig()
) {
    // Parallel flat arrays for cache-friendly zero-allocation physics
    val posX = FloatArray(maxBoids)
    val posY = FloatArray(maxBoids)
    val velX = FloatArray(maxBoids)
    val velY = FloatArray(maxBoids)
    val accX = FloatArray(maxBoids)
    val accY = FloatArray(maxBoids)
    val tailX = FloatArray(maxBoids)
    val tailY = FloatArray(maxBoids)
    val colorVariation = FloatArray(maxBoids)
    val sizeVariation = FloatArray(maxBoids)

    // Viewport dimensions
    var width: Float = 1080f
        private set
    var height: Float = 2400f
        private set

    // Spatial partitioning grid to accelerate neighbor queries from O(N^2) to O(N)
    private val cellSize = 65f
    private var gridCols = 1
    private var gridRows = 1
    private var totalGridCells = 1
    private var cellHead = IntArray(1) { -1 }
    private var nextInCell = IntArray(maxBoids) { -1 }

    // Touch interaction state
    var isTouchActive: Boolean = false
    var touchX: Float = 0f
    var touchY: Float = 0f
    var interactionMode: BoidInteractionMode = BoidInteractionMode.ATTRACT
    var repulseRadius: Float = 0f

    // Ambient time tracker for procedural vector flow fields
    private var flowTime: Float = 0f

    init {
        initializeBoids()
    }

    private fun initializeBoids() {
        val rnd = Random(42)
        for (i in 0 until maxBoids) {
            posX[i] = rnd.nextFloat() * 1080f
            posY[i] = rnd.nextFloat() * 2400f
            val angle = rnd.nextFloat() * 2f * PI.toFloat()
            val speed = rnd.nextFloat() * 4f + 4f
            velX[i] = cos(angle) * speed
            velY[i] = sin(angle) * speed
            accX[i] = 0f
            accY[i] = 0f
            tailX[i] = posX[i] - velX[i] * 2.5f
            tailY[i] = posY[i] - velY[i] * 2.5f
            colorVariation[i] = rnd.nextFloat()
            sizeVariation[i] = rnd.nextFloat() * 0.6f + 0.7f
        }
    }

    fun resize(newWidth: Float, newHeight: Float) {
        if (newWidth <= 0f || newHeight <= 0f) return
        width = newWidth
        height = newHeight

        gridCols = (width / cellSize).toInt().coerceAtLeast(1) + 1
        gridRows = (height / cellSize).toInt().coerceAtLeast(1) + 1
        totalGridCells = gridCols * gridRows

        if (cellHead.size < totalGridCells) {
            cellHead = IntArray(totalGridCells) { -1 }
        }
    }

    fun triggerRepulseShockwave(originX: Float, originY: Float) {
        touchX = originX
        touchY = originY
        repulseRadius = 400f

        val count = config.activeBoidsCount.coerceAtMost(maxBoids)
        for (i in 0 until count) {
            val dx = posX[i] - originX
            val dy = posY[i] - originY
            val distSq = dx * dx + dy * dy
            if (distSq < 400f * 400f && distSq > 0.001f) {
                val dist = sqrt(distSq)
                val blastStrength = (1f - dist / 400f) * 22f
                velX[i] += (dx / dist) * blastStrength
                velY[i] += (dy / dist) * blastStrength
            }
        }
    }

    /**
     * Primary physics simulation step executed every frame (60-120 Hz).
     */
    fun update(dtSeconds: Float) {
        val boidCount = config.activeBoidsCount.coerceAtMost(maxBoids)
        if (width <= 0f || height <= 0f || boidCount <= 0) return

        flowTime += dtSeconds

        // Step 1: Rebuild O(N) Spatial Partitioning Hash Grid
        java.util.Arrays.fill(cellHead, 0, totalGridCells, -1)
        for (i in 0 until boidCount) {
            val cx = (posX[i] / cellSize).toInt().coerceIn(0, gridCols - 1)
            val cy = (posY[i] / cellSize).toInt().coerceIn(0, gridRows - 1)
            val cellIdx = cy * gridCols + cx
            nextInCell[i] = cellHead[cellIdx]
            cellHead[cellIdx] = i
        }

        val visionRadiusSq = config.visionRadius * config.visionRadius
        val sepRadiusSq = config.separationRadius * config.separationRadius
        val maxSpeed = config.maxSpeed
        val maxForce = config.maxForce

        // Step 2: Solve Reynolds Flocking Steering & Environment Forces
        for (i in 0 until boidCount) {
            val px = posX[i]
            val py = posY[i]
            val vx = velX[i]
            val vy = velY[i]

            // Record tail position for motion trail rendering
            tailX[i] = px - vx * 2.2f
            tailY[i] = py - vy * 2.2f

            var sepX = 0f
            var sepY = 0f
            var sepCount = 0

            var alignX = 0f
            var alignY = 0f
            var alignCount = 0

            var cohX = 0f
            var cohY = 0f
            var cohCount = 0

            val cellX = (px / cellSize).toInt().coerceIn(0, gridCols - 1)
            val cellY = (py / cellSize).toInt().coerceIn(0, gridRows - 1)

            // Query only the 9 adjacent spatial grid cells
            val minX = (cellX - 1).coerceAtLeast(0)
            val maxX = (cellX + 1).coerceAtMost(gridCols - 1)
            val minY = (cellY - 1).coerceAtLeast(0)
            val maxY = (cellY + 1).coerceAtMost(gridRows - 1)

            for (gy in minY..maxY) {
                val rowOffset = gy * gridCols
                for (gx in minX..maxX) {
                    var neighbor = cellHead[rowOffset + gx]
                    while (neighbor != -1) {
                        if (neighbor != i) {
                            val dx = px - posX[neighbor]
                            val dy = py - posY[neighbor]
                            val distSq = dx * dx + dy * dy

                            if (distSq < visionRadiusSq && distSq > 0.0001f) {
                                // Rule 1: Separation (inversely proportional to distance)
                                if (distSq < sepRadiusSq) {
                                    val dist = sqrt(distSq)
                                    sepX += (dx / dist) / dist
                                    sepY += (dy / dist) / dist
                                    sepCount++
                                }

                                // Rule 2: Alignment (velocity averaging)
                                alignX += velX[neighbor]
                                alignY += velY[neighbor]
                                alignCount++

                                // Rule 3: Cohesion (center of mass averaging)
                                cohX += posX[neighbor]
                                cohY += posY[neighbor]
                                cohCount++
                            }
                        }
                        neighbor = nextInCell[neighbor]
                    }
                }
            }

            var totalAccX = 0f
            var totalAccY = 0f

            // Apply Rule 1: Separation
            if (sepCount > 0) {
                sepX /= sepCount
                sepY /= sepCount
                val sepLen = sqrt(sepX * sepX + sepY * sepY)
                if (sepLen > 0.0001f) {
                    val steerX = (sepX / sepLen) * maxSpeed - vx
                    val steerY = (sepY / sepLen) * maxSpeed - vy
                    totalAccX += clampForce(steerX, maxForce) * config.separationWeight
                    totalAccY += clampForce(steerY, maxForce) * config.separationWeight
                }
            }

            // Apply Rule 2: Alignment
            if (alignCount > 0) {
                alignX /= alignCount
                alignY /= alignCount
                val alignLen = sqrt(alignX * alignX + alignY * alignY)
                if (alignLen > 0.0001f) {
                    val steerX = (alignX / alignLen) * maxSpeed - vx
                    val steerY = (alignY / alignLen) * maxSpeed - vy
                    totalAccX += clampForce(steerX, maxForce) * config.alignmentWeight
                    totalAccY += clampForce(steerY, maxForce) * config.alignmentWeight
                }
            }

            // Apply Rule 3: Cohesion
            if (cohCount > 0) {
                cohX /= cohCount
                cohY /= cohCount
                val targetDx = cohX - px
                val targetDy = cohY - py
                val targetDist = sqrt(targetDx * targetDx + targetDy * targetDy)
                if (targetDist > 0.0001f) {
                    val steerX = (targetDx / targetDist) * maxSpeed - vx
                    val steerY = (targetDy / targetDist) * maxSpeed - vy
                    totalAccX += clampForce(steerX, maxForce) * config.cohesionWeight
                    totalAccY += clampForce(steerY, maxForce) * config.cohesionWeight
                }
            }

            // Environmental Force: Procedural Trigonometric Vector Flow Field (Wind Turbulence)
            if (config.flowFieldWeight > 0.01f) {
                val flowAngle = (sin(px * 0.0025f + flowTime * 0.35f) +
                        cos(py * 0.0025f + flowTime * 0.25f)) * PI.toFloat()
                val flowVx = cos(flowAngle) * maxSpeed
                val flowVy = sin(flowAngle) * maxSpeed
                totalAccX += clampForce(flowVx - vx, maxForce) * config.flowFieldWeight
                totalAccY += clampForce(flowVy - vy, maxForce) * config.flowFieldWeight
            }

            // Touch Attractor / Vortex / Repulsor Interactions
            if (isTouchActive) {
                val tdx = touchX - px
                val tdy = touchY - py
                val tDist = sqrt(tdx * tdx + tdy * tdy)

                when (interactionMode) {
                    BoidInteractionMode.ATTRACT -> {
                        if (tDist > 20f) {
                            val steerX = (tdx / tDist) * maxSpeed - vx
                            val steerY = (tdy / tDist) * maxSpeed - vy
                            val pull = (1f / (tDist * 0.005f + 1f)) * config.attractorWeight
                            totalAccX += clampForce(steerX, maxForce * 1.5f) * pull
                            totalAccY += clampForce(steerY, maxForce * 1.5f) * pull
                        }
                    }
                    BoidInteractionMode.VORTEX -> {
                        if (tDist > 10f) {
                            // Tangential perpendicular vector + slight inward pull
                            val tangX = -tdy / tDist
                            val tangY = tdx / tDist
                            val pullInX = tdx / tDist * 0.35f
                            val pullInY = tdy / tDist * 0.35f
                            val steerX = (tangX + pullInX) * maxSpeed - vx
                            val steerY = (tangY + pullInY) * maxSpeed - vy
                            totalAccX += clampForce(steerX, maxForce * 1.8f) * config.attractorWeight
                            totalAccY += clampForce(steerY, maxForce * 1.8f) * config.attractorWeight
                        }
                    }
                    BoidInteractionMode.REPULSE -> {
                        if (tDist < 350f && tDist > 0.001f) {
                            val blastPower = (1f - tDist / 350f) * 4f
                            val steerX = -(tdx / tDist) * maxSpeed * blastPower - vx
                            val steerY = -(tdy / tDist) * maxSpeed * blastPower - vy
                            totalAccX += clampForce(steerX, maxForce * 2.5f)
                            totalAccY += clampForce(steerY, maxForce * 2.5f)
                        }
                    }
                    BoidInteractionMode.FREE_FLOCK -> {
                        // Natural unconstrained motion
                    }
                }
            }

            accX[i] = totalAccX
            accY[i] = totalAccY
        }

        // Step 3: Integrate Euler velocities, positions, and viewport wrap
        for (i in 0 until boidCount) {
            velX[i] += accX[i]
            velY[i] += accY[i]

            // Clamp velocity to maxSpeed
            val speedSq = velX[i] * velX[i] + velY[i] * velY[i]
            if (speedSq > maxSpeed * maxSpeed) {
                val speed = sqrt(speedSq)
                velX[i] = (velX[i] / speed) * maxSpeed
                velY[i] = (velY[i] / speed) * maxSpeed
            }

            posX[i] += velX[i]
            posY[i] += velY[i]

            // Seamless Toroidal Viewport Wrap with margin
            val margin = 24f
            if (posX[i] < -margin) posX[i] = width + margin
            else if (posX[i] > width + margin) posX[i] = -margin

            if (posY[i] < -margin) posY[i] = height + margin
            else if (posY[i] > height + margin) posY[i] = -margin
        }
    }

    private fun clampForce(value: Float, max: Float): Float {
        return value.coerceIn(-max, max)
    }
}
