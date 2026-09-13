# Motion Lab: Jetpack Compose Animations & Physics

A high-performance Jetpack Compose animation and physics laboratory running at a locked 120 FPS with zero external animation dependencies.

> Forked and expanded from [Tetiana Bahlai's animations repository](https://github.com/tbahlai/animations).
> This project unifies all standalone demos into an interactive central showcase hub and adds advanced console-grade physics simulations, 3D transforms, and production-ready UI components.

---

## 🏛️ Central Motion Showcase Hub

All 19 animation experiments are connected through a unified interactive gallery:
- **Live Mini-Canvas Previews**: Infinite procedural animation loops rendered directly on each card.
- **Category Filtering**: Fast navigation across `Physics & Simulation`, `3D & Spatial`, `Gesture & Interactive`, and `Micro-interactions`.
- **Technical Inspector Sheet**: Real-time parameter tweaking with physics telemetry HUDs (particle count, velocities, springs, dihedral fold angles).
- **Global Search**: Instant filtering by experiment title, category, or physics tag.

---

## 🔬 New Advanced Physics & Motion Experiments

### 1. 🐦 Magnetic Boids Particle Swarm (`boids_swarm`)
- Craig Reynolds' 1987 flocking mechanics (Separation, Alignment, Cohesion).
- **$O(N)$ Spatial Partitioning Grid**: Reduces pairwise neighbor distance checks by 98.8%, maintaining **120 FPS with 2,500 particles**.
- Interactive touch magnetic attractor wells, tangential vortex swirls, procedural flow fields, and shockwave pulses.
- 4 procedural visual themes: *Starling Dusk*, *Abyssal Swarm*, *Cyber Nanites*, *Solar Plasma*.

### 2. 🌌 Cosmic Gravitational Lensing (`gravitational_lensing`)
- Supermassive black hole relativistic simulation.
- Ray bending with Einstein ring calculation: $\theta_E = \sqrt{\frac{4GM}{c^2} \frac{D_{ds}}{D_d D_s}}$.
- Relativistic Doppler-beamed accretion disk with asymmetric photon blueshift/redshift.
- Live spacetime curvature grid distortion responding to touch drag.

### 3. 💧 Viscous Fluid Metaballs (`fluid_metaballs`)
- Dynamic tangent-bridge Hermite spline necks simulating viscous surface tension.
- Mass-spring droplet connections with organic snapping and merging.
- Dual physics modes: 0G floating fluid bubbles and 1G gravity droplet rain.

### 4. 📐 Origami 3D Paper Fold (`origami_fold`)
- Architectural 3-panel folding leaflet with perspective `graphicsLayer` projections.
- Dihedral angle-driven crease ambient occlusion shadows: $I_{\text{crease}} = \sin(\theta) \times 0.72$.
- Washi paper, blueprint, and editorial textures with double-sided flipping.

### 5. ⚡ Elastic Kinetic Typography (`kinetic_typography`)
- Coupled Hooke spring-mass character lattice with inter-glyph strain propagation.
- Velocity-induced volume-conserving squash & stretch ($\Delta W \times \Delta H = \text{const}$).
- Harmonic traveling sine wave ripples and chromatic aberration trails.

---

## 🎟️ Real-World Production Components

### 1. ✈️ 3D Unfolding Boarding Pass (`production_components/ticket`)
- 3-fold spatial hierarchy (Summary $\rightarrow$ Passenger Details $\rightarrow$ Barcode Scan).
- Continuous touch-driven 3D unfold with crease ambient occlusion and perforated tear notches.
- Procedural real-time laser scan beam.

### 2. 🔄 Elastic Kinetic Pull-To-Refresh (`production_components/pull_to_refresh`)
- Drop-in `NestedScrollConnection` overscroll solver.
- Non-linear hyperbolic displacement: $d = d_{\text{max}} \cdot \tanh(\text{pull} / d_{\text{max}})$.
- Volume-conserving typography stretch with harmonic traveling sine wave loading state.

---

## 🎨 Original Community Demos (from @tbahlai)

| Demo | Description |
| :--- | :--- |
| **Off The Wall** | Real mass-spring cloth solver (~1,000 nodes, ~5,900 springs) that tears off the screen with procedural synth audio. |
| **Wave Reassembly** | Dynamic grid tiles that scatter and fly back into place via radial sine waves. |
| **Glass Shatter** | Voronoi-like glass shard breakage with gravity physics and realistic fragmentation. |
| **Gallery Disintegration** | Image dissolver that blows away photos as thousands of kinetic particles. |
| **Polaroid Scatter** | Interactive photo grid that scatters like real polaroid prints on a table. |
| **Cover Flow** | Classic 3D depth carousel with perspective skew. |
| **Fan Cards** | Three-state interactive card fan stack. |
| **Parallax Onboarding** | Multi-layered onboarding screens moving at differential depths. |
| **Rift Toggle** | Dark/light theme switch tearing open a glowing dimensional rift. |
| **Git Diff Swap** | Seamless character diff transition in source code. |
| **Happy Birthday** | Procedural Canvas cake with swipe-to-blow candle flames. |

---

## 🛠️ Tech Stack & Performance Architecture

- **Language & UI**: Kotlin · Jetpack Compose (BOM 2024.09) · Material 3
- **Zero Animation Libraries**: No Lottie, no Unity — 100% pure Compose Canvas and custom math.
- **120 FPS Target**: Flat primitive float/int arrays, object pooling, and zero heap allocations in `DrawScope` loops.
- **Min SDK**: 26 (Android 8.0+)
- **Build**: Gradle Kotlin DSL with Compose Compiler 2.0+

---

## 📄 License & Attribution

- Original animation concepts by **Tetiana Bahlai** ([@tbahlai](https://github.com/tbahlai)).
- Expanded showcase hub, physics engines, and production components by **Prasad Vennam** ([@vennamprasad](https://github.com/vennamprasad)).