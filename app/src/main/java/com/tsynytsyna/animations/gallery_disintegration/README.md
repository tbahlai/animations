# Gallery Disintegration (Jetpack Compose)

A reusable disintegration effect built with Jetpack Compose.  
This project demonstrates dissolving any composable into thousands of flying
particles, perfect for photo transitions and dramatic content swaps.

---

## Preview

![Disintegration Preview](Gallery_Disintegration.gif)

---

## Overview

This effect wraps any composable in a `Disintegration` container. When triggered,
it captures the current pixels into a `GraphicsLayer`, splits them into vertical
strips and colored particles, then animates them away frame by frame.

The main idea is to create a sweeping dissolve from left to right, where the image
fades in columns while particles fly off in the same direction.

---

## Key Features

- Works with any composable content (images, text, layouts)
- GPU snapshot via `GraphicsLayer` for an accurate capture
- Vertical strips that fade out in a staggered left-to-right sweep
- Thousands of particles sampled from the original pixels
- Particle physics with drift, wobble, and fade-out
- Frame-driven animation loop (`withFrameMillis`) running at 60–120 FPS
- Simple state holder with an `onComplete` callback to swap content

---

## Animation Concept

The effect is composed of two animated layers:

- Strips — the original image split into 20 vertical columns, each fading out
  with a delay based on its X position
- Particles — colored circles sampled from every 10th pixel, flying right-upward
  with wavy motion and fading as they go

Both layers are driven by a single `progress` value (0.0 → 1.0), creating a
continuous, sweeping dissolve instead of an abrupt cut.

---

## Data Model

Each disintegration is built from:

- A captured bitmap of the current content
- A list of strips (bitmap slice, position, fade delay)
- A list of particles (position, color, angle, speed, wobble, delay)
- A configurable duration driving the whole animation

This makes the effect fully self-contained and easy to drop onto any composable.

---

## How to Run

Wrap your content and trigger the effect from a button or gesture:

    val state = rememberDisintegrationState(durationMs = 1000)

    Disintegration(state, Modifier.fillMaxSize()) {
        // your content here
    }

    // later
    state.disintegrate {
        // swap photo / content when the dissolve finishes
    }

See `GalleryDemo.kt` for a complete photo-gallery example.

---

## Notes

This implementation focuses on:

- a reusable effect contained in a single file (`DisintegrationEffect.kt`)
- offloading the heavy bitmap work to a background thread
- smooth, staggered motion instead of an abrupt swap
- lightweight Canvas-based rendering for performance

---