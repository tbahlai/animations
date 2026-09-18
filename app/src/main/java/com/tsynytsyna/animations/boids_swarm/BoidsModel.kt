package com.tsynytsyna.animations.boids_swarm

import androidx.compose.ui.graphics.Color

/**
 * Visual themes for the Boids swarm simulation.
 */
enum class BoidTheme(
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val primaryParticleColor: Color,
    val secondaryParticleColor: Color,
    val accentGlowColor: Color,
    val trailColor: Color
) {
    STARLING_DUSK(
        title = "Starling Dusk",
        description = "Natural European starling murmuration over a golden twilight horizon",
        backgroundColor = Color(0xFF0D0F18),
        primaryParticleColor = Color(0xFF222638),
        secondaryParticleColor = Color(0xFF3B4260),
        accentGlowColor = Color(0xFFE5A65E),
        trailColor = Color(0x33E5A65E)
    ),
    BIOLUMINESCENT_OCEAN(
        title = "Abyssal Swarm",
        description = "Deep-sea bioluminescent siphonophore & jellyfish school in nocturnal trenches",
        backgroundColor = Color(0xFF030A14),
        primaryParticleColor = Color(0xFF00E5FF),
        secondaryParticleColor = Color(0xFF00FFB2),
        accentGlowColor = Color(0xFF00E5FF),
        trailColor = Color(0x3300E5FF)
    ),
    CYBER_NANITES(
        title = "Cyber Nanites",
        description = "Autonomous molecular micro-drones guided by algorithmic magnetic flux",
        backgroundColor = Color(0xFF08090C),
        primaryParticleColor = Color(0xFFFF9100),
        secondaryParticleColor = Color(0xFFFF1744),
        accentGlowColor = Color(0xFFFFD700),
        trailColor = Color(0x33FF9100)
    ),
    SOLAR_PLASMA(
        title = "Solar Plasma",
        description = "High-energy magnetohydrodynamic coronal ions orbiting sunspot fields",
        backgroundColor = Color(0xFF100604),
        primaryParticleColor = Color(0xFFFF5722),
        secondaryParticleColor = Color(0xFFFFC107),
        accentGlowColor = Color(0xFFFFEB3B),
        trailColor = Color(0x33FF5722)
    )
}

/**
 * Touch interaction modes that dynamically alter steering behavior.
 */
enum class BoidInteractionMode(val label: String, val description: String) {
    ATTRACT("Attract", "Magnetic gravity well pulling boids inward"),
    VORTEX("Vortex", "Tangential angular spin swirling boids around pointer"),
    REPULSE("Repulse", "High-velocity shockwave dispersing boids outward"),
    FREE_FLOCK("Free Flock", "Autonomous murmuration driven purely by Reynolds rules & wind fields")
}

/**
 * Algorithmic parameters for Craig Reynolds flocking & environmental forces.
 */
data class BoidsConfig(
    val maxBoids: Int = 2000,
    val activeBoidsCount: Int = 1200,
    val visionRadius: Float = 60f,       // Perception radius for alignment & cohesion
    val separationRadius: Float = 26f,   // Personal space threshold to avoid collisions
    val maxSpeed: Float = 14f,           // Terminal velocity
    val maxForce: Float = 0.45f,         // Maximum steering acceleration
    val separationWeight: Float = 1.6f,  // Avoid overcrowding
    val alignmentWeight: Float = 1.1f,   // Match average flock heading
    val cohesionWeight: Float = 1.0f,    // Steer towards flock center of mass
    val attractorWeight: Float = 2.8f,   // Touch magnetic pull strength
    val flowFieldWeight: Float = 0.4f    // Ambient wind turbulence influence
)
