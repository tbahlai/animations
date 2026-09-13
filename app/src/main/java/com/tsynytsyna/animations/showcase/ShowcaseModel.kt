package com.tsynytsyna.animations.showcase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tsynytsyna.animations.boids_swarm.BoidsSwarmDemo
import com.tsynytsyna.animations.cover_flow.CoverFlowDemo
import com.tsynytsyna.animations.fan_cards.FanCardsDemo
import com.tsynytsyna.animations.fluid_metaballs.FluidMetaballsDemo
import com.tsynytsyna.animations.gallery_disintegration.GalleryDemo
import com.tsynytsyna.animations.git_diff.CareerSwapDemo
import com.tsynytsyna.animations.glass_shatter.GlassShatterEffectDemo
import com.tsynytsyna.animations.gravitational_lensing.GravitationalLensingDemo
import com.tsynytsyna.animations.happy_birthday.HappyBirthdayDemo
import com.tsynytsyna.animations.kinetic_typography.KineticTypographyDemo
import com.tsynytsyna.animations.off_the_wall.OffTheWallDemo
import com.tsynytsyna.animations.origami_fold.OrigamiFoldDemo
import com.tsynytsyna.animations.paralax_onboarding.OnboardingScreen
import com.tsynytsyna.animations.production_components.pull_to_refresh.ElasticPullRefreshDemo
import com.tsynytsyna.animations.production_components.ticket.UnfoldingTicketDemo
import com.tsynytsyna.animations.polaroid_scatter.PolaroidScatterDemo
import com.tsynytsyna.animations.rift_toggle.RiftDemo
import com.tsynytsyna.animations.swipe_card.SwipeCardDemo
import com.tsynytsyna.animations.wave_reassembly.WaveReassemblyDemo

enum class AnimationCategory(val label: String) {
    ALL("All"),
    PHYSICS("Physics & Mesh"),
    DESTRUCTION("Fragmentation & FX"),
    TRANSITIONS("Transitions & Reveals"),
    SPATIAL("3D & Spatial"),
    PROCEDURAL("Procedural Canvas"),
}

@Immutable
data class AnimationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: AnimationCategory,
    val tags: List<String>,
    val accentColor: Color,
    val instructionHint: String,
    val technicalBadge: String,
    val algorithmDetails: String,
    val isFeatured: Boolean = false,
    val previewImageUrl: String = "",
    val content: @Composable (Modifier) -> Unit,
)

object ShowcaseRegistry {
    val items: List<AnimationItem> = listOf(
        AnimationItem(
            id = "unfolding_ticket",
            title = "Unfolding Boarding Pass",
            subtitle = "3D Origami Hinge Ticket",
            description = "Production-ready airline boarding pass with 3D seam hinges, ambient crease occlusion, perforated tear notches, vector laser barcode scanning, and wallet actions.",
            category = AnimationCategory.SPATIAL,
            tags = listOf("Production", "Boarding Pass", "3D Hinge", "Ticket"),
            accentColor = Color(0xFFE11D48),
            instructionHint = "Tap ticket to unfold 3D pass • Switch flight routes • Adjust fold slider",
            technicalBadge = "Production • 120 FPS",
            algorithmDetails = "Spatial 3D seam hinges with graphicsLayer transformOrigin (0.5f, 0f), dynamic ambient occlusion shading I = sin(theta) * 0.72, and procedural barcode vector rendering.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> UnfoldingTicketDemo(modifier) }
        ),
        AnimationItem(
            id = "elastic_pull_refresh",
            title = "Kinetic Pull-to-Refresh",
            subtitle = "Elastic Typographic Scroll Header",
            description = "Production NestedScrollConnection pull-to-refresh container with Hooke spring-mass character lattice, volume conservation, and harmonic wave loading.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Production", "Pull-to-Refresh", "Hooke Springs", "Typography"),
            accentColor = Color(0xFFFF2A55),
            instructionHint = "Pull down feed to stretch kinetic typography • Release to refresh",
            technicalBadge = "NestedScroll • 120 FPS",
            algorithmDetails = "Non-linear hyperbolic displacement d = d_max * tanh(pull / d_max), coupled spring strain matrix, volumetric conservation (scaleY * scaleX^2 = 1), and harmonic sine wave loading.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1507842229451-79b1be88688e?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> ElasticPullRefreshDemo(modifier) }
        ),
        AnimationItem(
            id = "boids_swarm",
            title = "Magnetic Boids Swarm",
            subtitle = "Craig Reynolds Flocking & Spatial Hash",
            description = "High-performance simulation of 2,000+ autonomous flocking agents with separation, alignment, and cohesion, accelerated by O(N) spatial grid hashing and touch magnetic wells.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Boids", "Reynolds Flocking", "Magnetic Swarm", "Spatial Hash", "120 FPS"),
            accentColor = Color(0xFF00E5FF),
            instructionHint = "Touch canvas to attract flock • Drag to swirl vortex • Pulse to repulse",
            technicalBadge = "Spatial Grid • 120 FPS",
            algorithmDetails = "Craig Reynolds 1987 steering equations (Separation Fs, Alignment Fa, Cohesion Fc) accelerated from O(N²) to O(N) using uniform spatial grid hashing with zero heap allocations.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> BoidsSwarmDemo(modifier) }
        ),
        AnimationItem(
            id = "origami_fold",
            title = "Origami 3D Fold",
            subtitle = "Accordion & Crease Occlusion",
            description = "Multi-panel architectural origami leaflet with dynamic crease ambient occlusion, grazing light sheen, and continuous touch-driven folding physics.",
            category = AnimationCategory.SPATIAL,
            tags = listOf("3D Origami", "Accordion Fold", "Ambient Occlusion", "Hinges"),
            accentColor = Color(0xFFD32F2F),
            instructionHint = "Drag horizontally to fold/unfold leaflet • Tap presets or flip map",
            technicalBadge = "Spatial Hinge • 120 FPS",
            algorithmDetails = "Dynamic graphicsLayer transformOrigin seam hinges combined with trigonometric ambient occlusion shading I = sin(theta) * 0.72 and isometric camera projection.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> OrigamiFoldDemo(modifier) }
        ),
        AnimationItem(
            id = "kinetic_typography",
            title = "Kinetic Typography",
            subtitle = "Elastic Spring & Harmonic Waves",
            description = "Spring-mass typography lattice with neighbor coupling, velocity squash & stretch, harmonic sine ripples, and high-energy shockwave dispersion.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Spring Physics", "Kinetic Type", "Hooke Coupling", "Squash & Stretch"),
            accentColor = Color(0xFFFF2A55),
            instructionHint = "Drag any letter to stretch spring lattice • Double-tap shockwave • Switch modes",
            technicalBadge = "Hooke Lattice • 120 FPS",
            algorithmDetails = "Euler-integrated spring-mass lattice with Hooke restoring forces, inter-glyph strain coupling, dynamic volume-conserving squash & stretch (ΔW × ΔH = const), and chromatic trail aberration.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> KineticTypographyDemo(modifier) }
        ),
        AnimationItem(
            id = "gravitational_lensing",
            title = "Gravitational Lensing",
            subtitle = "Einstein Ring & Relativistic Warping",
            description = "General Relativistic black hole simulation: background starfield ray bending, Einstein ring shearing, Doppler-beamed accretion disk, and spacetime curvature grid.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("General Relativity", "Black Hole", "Einstein Ring", "Spacetime"),
            accentColor = Color(0xFFFFB300),
            instructionHint = "Drag black hole across cosmos • Double-tap for gravitational waves • Toggle grid/disk",
            technicalBadge = "Relativistic Deflection • 120 FPS",
            algorithmDetails = "Analytical Schwarzschild deflection theta' = (theta + sqrt(theta^2 + 4 theta_E^2)) / 2, tangential arc stretching, and Keplerian accretion matter simulation with relativistic Doppler boosting.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> GravitationalLensingDemo(modifier) }
        ),
        AnimationItem(
            id = "fluid_metaballs",
            title = "Fluid Metaballs",
            subtitle = "Surface Tension & Viscous Droplets",
            description = "Real-time viscous liquid droplets that stretch with organic surface tension, merge seamlessly, and snap apart with elastic rebound.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Fluid Dynamics", "Metaball", "Surface Tension", "Spline Bridge"),
            accentColor = Color(0xFF00E5FF),
            instructionHint = "Drag droplets to stretch liquid neck • Pull past tension to snap • Tap to spawn",
            technicalBadge = "Tangent-Bridge Bezier • 120 FPS",
            algorithmDetails = "Cubic Hermite/Bezier tangent bridges computed analytically from circle pairs, coupled with Euler momentum integration, boundary restitution, and sinusoidal wobble decay.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> FluidMetaballsDemo(modifier) }
        ),
        AnimationItem(
            id = "off_the_wall",
            title = "Off The Wall",
            subtitle = "Cloth Simulation & Dynamic Tear",
            description = "A real-time mass-spring cloth solver running ~1,000 nodes and ~5,900 springs every frame with Verlet integration and drawBitmapMesh.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Mass-Spring", "Verlet", "Mesh", "Tearable"),
            accentColor = Color(0xFFFF5722),
            instructionHint = "Drag to stretch cloth • Pull hard to tear it off the wall",
            technicalBadge = "1,000 Nodes • 5,900 Springs",
            algorithmDetails = "Discrete Verlet integration with constraint relaxation solving Hooke's Law across structural, shear, and flexion spring pairs, projected onto native Canvas drawBitmapMesh.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> OffTheWallDemo(modifier) }
        ),
        AnimationItem(
            id = "glass_shatter",
            title = "Glass Shatter",
            subtitle = "Polygon Voronoi Fragmentation",
            description = "Procedural Sutherland-Hodgman polygon clipping breaks the screen into glass shards that fall with gravity and 3D flip.",
            category = AnimationCategory.DESTRUCTION,
            tags = listOf("Sutherland-Hodgman", "3D Flip", "Physics"),
            accentColor = Color(0xFF00E5FF),
            instructionHint = "Tap anywhere on the image to shatter it into glass shards",
            technicalBadge = "Polygon Slicing • 3D Rotation",
            algorithmDetails = "Ray-cast Voronoi centroid generation clipped against viewport convex hulls using Sutherland-Hodgman line-intersection clipping with gravity parabolic falloff.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> GlassShatterEffectDemo(modifier) }
        ),
        AnimationItem(
            id = "rift_toggle",
            title = "Rift Toggle",
            subtitle = "Cosmic Spacetime Screen Tear",
            description = "A dramatic dark/light theme switch tearing the screen open with multi-stop lightning bolts, glow rings, and camera shake.",
            category = AnimationCategory.TRANSITIONS,
            tags = listOf("Lightning", "Shake Matrix", "Edge Glow"),
            accentColor = Color(0xFFFF1744),
            instructionHint = "Tap anywhere to tear open a cosmic rift between themes",
            technicalBadge = "Multi-Stop Lightning • Shake Matrix",
            algorithmDetails = "Dynamic bezier path subdivision with Perlin-style displacement noise, dual circular clipPath masks, and randomized camera matrix translation offsets.",
            isFeatured = true,
            previewImageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> RiftDemo(modifier) }
        ),
        AnimationItem(
            id = "wave_reassembly",
            title = "Wave Reassembly",
            subtitle = "Radial Wavefront Reconstruction",
            description = "Tiles fly into place along a radial expanding wave front, dramatically minimizing active draw calls per frame.",
            category = AnimationCategory.TRANSITIONS,
            tags = listOf("Radial Wave", "Curved Path", "Optimized"),
            accentColor = Color(0xFF7C4DFF),
            instructionHint = "Tap 'Assemble' to watch tiles fly in along the expanding wave front",
            technicalBadge = "O(Band) Draw Call Culling",
            algorithmDetails = "Spatiotemporal wave gating: tiles outside the radial front are idle, inside are rendered as 1 single drawImage, and only active tiles within the wavefront animate.",
            previewImageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> WaveReassemblyDemo(modifier) }
        ),
        AnimationItem(
            id = "gallery_disintegration",
            title = "Gallery Disintegration",
            subtitle = "Particle Drift & Strip Dissolve",
            description = "Transforms bitmap snapshots into swirling wind-blown particles and staggered disintegrating vertical strips.",
            category = AnimationCategory.DESTRUCTION,
            tags = listOf("Particles", "Strip Slice", "Swirl"),
            accentColor = Color(0xFFFFAB00),
            instructionHint = "Tap 'Next' to disintegrate photo into swirling particles",
            technicalBadge = "Dual-Phase Particle System",
            algorithmDetails = "Hybrid macro-strip slicing combined with sub-sampled micro-particles subjected to sinusoidal wind turbulence and quadratic drag decay.",
            previewImageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> GalleryDemo(modifier) }
        ),
        AnimationItem(
            id = "happy_birthday",
            title = "Happy Birthday",
            subtitle = "Procedural Cake & Candle Flame",
            description = "100% Canvas-rendered cake with dynamic bezier-curved candle flames that react to swipe velocity.",
            category = AnimationCategory.PROCEDURAL,
            tags = listOf("Procedural Canvas", "Flame Physics", "Swipe"),
            accentColor = Color(0xFFFF4081),
            instructionHint = "Swipe across candle flames to blow them out!",
            technicalBadge = "Bezier Geometry • Dynamic Sparks",
            algorithmDetails = "Zero image assets. Fully procedural vector rendering with quadratic flame teardrops, velocity threshold detection, and rising spark particle fountains.",
            previewImageUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> HappyBirthdayDemo(modifier) }
        ),
        AnimationItem(
            id = "cover_flow",
            title = "Cover Flow",
            subtitle = "3D Perspective Album Carousel",
            description = "Classic Cover Flow carousel featuring real-time 3D rotation, depth scaling, overlap offsets, and floor reflections.",
            category = AnimationCategory.SPATIAL,
            tags = listOf("3D Rotation", "Reflection", "Infinite Pager"),
            accentColor = Color(0xFF2979FF),
            instructionHint = "Swipe horizontally through the album covers or tap to focus",
            technicalBadge = "Perspective Camera • Shader Floor",
            algorithmDetails = "Custom graphicsLayer camera distance with Z-axis rotation falloff, logarithmic page scale dampening, and inverted gradient alpha reflection masks.",
            previewImageUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> CoverFlowDemo(modifier) }
        ),
        AnimationItem(
            id = "git_diff",
            title = "Git Diff Swap",
            subtitle = "Terminal Code Diff Transition",
            description = "A narrative told as interactive syntax-highlighted git diff edits inside a retro terminal IDE.",
            category = AnimationCategory.PROCEDURAL,
            tags = listOf("Code Diff", "Syntax", "Terminal"),
            accentColor = Color(0xFF2FBF71),
            instructionHint = "Tap to step through git commits and watch lines diff in real time",
            technicalBadge = "Character-Level Morph",
            algorithmDetails = "Levenshtein distance character alignment with staggered opacity interpolation and terminal monospace syntax highlighter.",
            previewImageUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> CareerSwapDemo(modifier) }
        ),
        AnimationItem(
            id = "polaroid_scatter",
            title = "Polaroid Scatter",
            subtitle = "Physics Photo Table Scatter",
            description = "Sortable polaroid photo cards that scatter, spin, and settle naturally across a virtual table surface.",
            category = AnimationCategory.SPATIAL,
            tags = listOf("Scatter", "Rotation", "Table"),
            accentColor = Color(0xFF64DD17),
            instructionHint = "Tap filters to watch polaroids scatter and reorganize",
            technicalBadge = "Spring Dynamics • Angular Jitter",
            algorithmDetails = "Underdamped harmonic oscillators with randomized angular momentum, simulating physical photo prints dropped onto a mahogany desk.",
            previewImageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> PolaroidScatterDemo(modifier) }
        ),
        AnimationItem(
            id = "fan_cards",
            title = "Fan Cards",
            subtitle = "Three-State Card Deck Physics",
            description = "Gestural multi-card deck cycling seamlessly between stacked, fanned, and individual card focus modes.",
            category = AnimationCategory.SPATIAL,
            tags = listOf("Fan Spread", "Deck Gestures", "State Machine"),
            accentColor = Color(0xFFFF6D00),
            instructionHint = "Tap cards or deck to toggle between stacked, fanned, and single views",
            technicalBadge = "Arc Interpolation • Radial Pivot",
            algorithmDetails = "Radial arc trigonometry with dynamic card zIndex sorting, spring elevation shadows, and touch-target hit testing.",
            previewImageUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> FanCardsDemo(modifier) }
        ),
        AnimationItem(
            id = "paralax_onboarding",
            title = "Parallax Onboarding",
            subtitle = "Multi-Depth Layered Pager",
            description = "Multi-layered onboarding screens where background, midground, and foreground assets shift at differential speeds.",
            category = AnimationCategory.TRANSITIONS,
            tags = listOf("Multi-layer", "Depth Parallax", "Pager"),
            accentColor = Color(0xFF00B0FF),
            instructionHint = "Swipe pages to observe parallax speed differentials",
            technicalBadge = "3-Plane Depth Offset",
            algorithmDetails = "PagerState fraction normalization decomposed across distinct spatial planes with linear interpolation (lerp) and spring snap points.",
            previewImageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> OnboardingScreen(modifier) }
        ),
        AnimationItem(
            id = "swipe_card",
            title = "Swipe Card",
            subtitle = "Flingable Deck with Torque",
            description = "Tinder-inspired gesture cards with physics drag resistance, rotational torque, and velocity dismissals.",
            category = AnimationCategory.PHYSICS,
            tags = listOf("Swipe Gesture", "Torque", "Scale Reveal"),
            accentColor = Color(0xFFD500F9),
            instructionHint = "Fling card left/right or tap action buttons below",
            technicalBadge = "Spring Torque • Velocity Fling",
            algorithmDetails = "PointerInput velocity tracking coupled with rotational torque proportional to X-displacement and spring scale back-card revelation.",
            previewImageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80",
            content = { modifier -> SwipeCardDemo(modifier) }
        ),
    )
}
