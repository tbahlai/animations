# Glass Shatter (Jetpack Compose)

A reusable glass-shatter effect built with Jetpack Compose.  
This project demonstrates smashing any composable into dozens of glass shards that
fly out from an impact point, perfect for dramatic dismissals and "smash" interactions.

---

## Preview

![Glass Shatter Preview](GlassShatter.gif)

---

## Overview

This effect wraps any composable in a `GlassShatter` container. When triggered at a
given impact point, it captures the current pixels into a `GraphicsLayer`, splits the
surface into irregular glass shards via a Voronoi tessellation, then animates each
shard flying away frame by frame.

The main idea is a crack that propagates outward from the point of impact: shards near
the hit launch first and fastest, while outer shards follow with a short delay.

---

## Key Features

- Works with any composable content (images, text, layouts)
- GPU snapshot via `GraphicsLayer` for an accurate capture
- Voronoi shard tessellation seeded around the impact point
- Radial physics — each shard flies outward with gravity, spin, and a 3D-like flip
- Crack propagation via per-shard delays based on distance from impact
- Frame-driven animation loop (`withFrameMillis`) running at 60–120 FPS
- Heavy shard generation offloaded to a background thread (`Dispatchers.Default`)
- Optional tap-to-shatter, or trigger programmatically from any gesture
- Simple state holder with an `onComplete` callback to swap content

---

## Animation Concept

The effect is driven by a single `progress` value (0.0 → 1.0). For every shard a local
progress is derived from its `delay`, then used to animate:

- Translation — outward along the shard's radial velocity, plus an accelerating
  downward pull (gravity)
- Rotation — each shard spins at its own random speed
- Flip — a `cos`-based horizontal scale fakes a shard tumbling in 3D
- Fade — shards become transparent as they travel, with a thin white edge highlight

Because every shard reads from the same `progress`, the whole pane shatters as one
continuous, physical motion instead of an abrupt cut.

---

## Data Model

Each shatter is built from:

- A captured bitmap of the current content
- An impact point that seeds the crack pattern
- A list of glass shards (clip path, centroid, radial velocity, spin speed, flip
  speed, propagation delay)
- A configurable duration driving the whole animation

This makes the effect fully self-contained and easy to drop onto any composable.

---

## How to Run

Wrap your content and trigger the effect from a button or gesture:

    val state = rememberGlassShatterState(durationMs = 1500)

    GlassShatter(state, Modifier.fillMaxSize()) {
        // your content here
    }

    // later — shatter from a specific impact point
    state.shatter(impactPoint) {
        // swap photo / content when the shatter finishes
    }

By default the container also shatters on tap (`tapEnabled = true`); set it to `false`
to drive the effect entirely from your own controls.

See `GlassShatterEffectDemo.kt` for a complete "Pick or Smash" example, where it is
paired with the reusable `SwipeCard` component (`../swipe_card/`): liking flings the
card away, while smashing shatters it in place.

---

## Notes

This implementation focuses on:

- a reusable effect contained in a single file (`GlassShatterState.kt`)
- offloading the Voronoi shard generation to a background thread
- physically-driven, staggered shard motion instead of an abrupt swap
- lightweight Canvas-based rendering with cached shard paths for performance

---