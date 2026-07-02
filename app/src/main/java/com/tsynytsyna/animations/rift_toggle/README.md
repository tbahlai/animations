# Rift Toggle (Jetpack Compose)

A reusable dark/light theme switch built with Jetpack Compose.  
This project demonstrates revealing the opposite theme through an expanding, wobbling
rift — with screen shake, swirling particles and crackling lightning — perfect for
turning a mundane settings toggle into a "reality is tearing open" moment.

---

## Preview

![Rift Toggle Preview](RiftToggle.gif)

---

## Overview

This effect wraps your two themed screens in a `RiftToggle` container. Tap
anywhere and a rift opens at the touch point: the new theme is revealed through an
expanding circular clip while the whole screen rattles, energy particles spiral inward
and lightning arcs along the rift's edge.

The main idea is a growing reveal mask centered on the tap. The current theme stays
underneath, the target theme is drawn on top and clipped to a wobbly circle that grows
to cover the screen, and a Canvas overlay adds all the rift drama on top.

---

## Key Features

- Works with any two composables (your light and dark screens)
- Expanding circular reveal clipped to an animated, wobbling rift edge
- Full-screen shake with a fade-in / hold / fade-out intensity envelope
- Contrast-aware glowing frame (white when revealing dark, black when revealing light)
  with its own extra rattle
- Swirling particles with spiral trails, plus procedural branching lightning
- Radial glow and flickering edge rings driven by a continuous time clock
- Frame-driven animation loop (`withFrameNanos`) that runs **only during the transition**
- Zero recomposition while idle — the clocks are read only inside draw/layout phases
- Fully configurable: `riftColor`, `durationMs`, `shakeStrength`, `lightningEnabled`

---

## Animation Concept

The effect is driven by two values, both read only inside draw/layout lambdas:

- `progress` (0.0 → 1.0, `FastOutSlowInEasing`) — grows the reveal radius, and via a
  fade-in / hold / fade-out envelope controls the intensity of the shake, frame and
  overlay so they ramp up and settle instead of snapping on and off
- `time` — a continuous clock feeding all the `sin`/`cos` motion: edge wobble, glow
  flicker, particle spirals and the erratic lightning path

The current theme sits underneath; the target theme is drawn on top and clipped to a
wobbly circle of radius `maxReveal * progress`. Because every layer reads from the same
`progress`/`time`, the whole transition feels like one continuous rift opening rather
than an abrupt theme swap.

---

## Data Model

Each rift is built from:

- The tap point that seeds the reveal center
- A reveal `progress` and a continuous `time` clock (both `mutableFloatState`)
- A pre-built list of rift particles (angle, distance, speed, size, color, trail)
- Reusable `Path` objects for the rift edges and lightning bolts
- Configurable duration, color, shake strength and a lightning toggle

This makes the effect fully self-contained and easy to drop over any pair of screens.

---

## How to Run

Wrap your two themed screens and drive `isDark` from the `onToggle` callback:

    var isDark by remember { mutableStateOf(false) }

    RiftToggle(
        isDark = isDark,
        onToggle = { isDark = !isDark },
        modifier = Modifier.fillMaxSize(),
        lightContent = { ThemedScreen(dark = false) },
        darkContent = { ThemedScreen(dark = true) },
    )

You supply the same screen twice — once in each theme:

- `lightContent` — your full UI styled for the light theme
- `darkContent` — the exact same UI styled for the dark theme

Both are composed during the transition: `isDark` picks which one is shown underneath,
while the other is drawn on top and revealed through the growing rift. So they should
render identical layouts and differ only in colors, otherwise the reveal will look like
the content jumps rather than just re-themes. In practice, back both with the same
composable and pass your theme in (as `ThemedScreen(dark = ...)` does above), so the two
lambdas can never drift out of sync.

The container toggles on tap; `onToggle` fires once the rift reaches full coverage, so
your state flips exactly when the new theme has taken over the screen. Keep `isDark` as
the single source of truth — read it elsewhere in your app to stay in sync with the
switch.

See `RiftDemo.kt` for a complete profile-screen example.

---

## Notes

This implementation focuses on:

- a reusable effect contained in a single file (`RiftToggle.kt`)
- a frame loop that runs only while animating, so there is no idle cost
- reading animation clocks only in draw/layout phases to avoid recomposition
- lightweight Canvas rendering with cached `Path` objects for performance

---