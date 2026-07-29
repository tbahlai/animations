# Wave Reassembly (Jetpack Compose)

A reusable reveal effect built with Jetpack Compose.  
Any composable is captured and then rebuilt out of thousands of tiles that fly in from
every side — driven by a ring that expands from the centre outward, so the picture forms
from the middle out.

---

## Preview

![Wave Reassembly Preview](WaveReassembly.gif)

---

## Overview

This is the inverse of [`gallery_disintegration`](../gallery_disintegration): instead of
scattering content away, it assembles content into existence.

The effect wraps any composable. When triggered, it snapshots the current pixels into a
`GraphicsLayer`, samples that snapshot into a grid of coloured tiles, hides the content and
flies the tiles inward along curved paths until each snaps onto its own pixels.

What makes it different from the other particle effects in this project is **when** a tile
is alive. A ring expands from the centre

- **Inside** the ring the photo is already the real, sharp image — one clipped `drawImage`.
- **In** the ring a band of tiles is in flight.
- **Outside** it, nothing yet.

---

## Usage

```kotlin
val wave = rememberWaveReassemblyState()

WaveReassembly(wave, Modifier.fillMaxSize()) {
    GlideImage(model = photo, contentDescription = null)
}

Button(onClick = { wave.assemble() }, enabled = !wave.isRunning) {
    Text("Reveal")
}
```

The effect snapshots **whatever its content currently is**, so to reveal something new you
swap the content *first* and call `assemble()` straight after. The container is hidden from
the same frame, so the new content never flashes ahead of its own reveal:

```kotlin
currentIndex = nextIndex      // swap first
wave.assemble()               // then assemble — this is what gets captured
```

`assemble(onComplete)` fires its callback once the frame is whole again, which is the right
place to advance state or re-enable a button.

Anything that should *not* be reconstructed — scrims, badges, loading overlays — must sit
**outside** the `WaveReassembly` content, or it gets baked into the tiles and assembled along
with the photo.

---

## Parameters

| Parameter           | Default | Effect                                                                         |
|---------------------|---------|--------------------------------------------------------------------------------|
| `durationMs`        | `1400`  | Length of the whole sweep                                                      |
| `captureFrames`     | `2`     | Frames to wait before snapshotting, so new content has been drawn              |
| `tileSize`          | `10`    | Side of a tile in px. **Quadratic** in tile count — the main cost dial         |
| `BAND`              | `0.3f`  | Width of the live ring, as a fraction of centre-to-corner. The other cost dial |
| `SPAWN_RING`        | `1.15f` | How far off-frame tiles spawn                                                  |
| `OVERSIZE`          | `1.6f`  | How much larger a tile is mid-flight before contracting to one cell            |
| `LANDING_OVERSHOOT` | `0.4f`  | Overshoot of the landing snap, as a fraction of distance travelled             |
| `FADE_IN_RATE`      | `6f`    | How fast a tile reaches full opacity after launch                              |

---

## How it works

1. **One snapshot.** `GraphicsLayer.record { drawContent() }` captures the container;
   `toImageBitmap()` is called once, when the effect is triggered. Nothing is drawn during
   the `Capturing` phase, so everything after this has to be quick.
2. **Sampling.** The snapshot is read one **band of rows at a time** with a single
   `getPixels` per band into a reused `IntArray`, and each cell takes the **average** of its
   own area rather than one corner pixel — so the tiles read as the photo, not as noise.
3. **Spawn on the tile's own radial line.** `atan2` to the centre gives each tile its own
   direction, plus a little angular jitter. Neighbours therefore travel together as coherent
   sheets instead of criss-crossing the frame.
4. **One shared swirl.** The perpendicular bow of every path is signed by a single `swirl`
   picked once per build, so the whole swarm spirals the same way. Mixed directions read as
   noise rather than as a field.
5. **The ring.** `front = smoothstep(progress) × (1 + BAND)`, and a tile's own progress is
   `(front − radius) / BAND`. It travels `1 + BAND` rather than `1`, or the corner tiles
   would never finish.
6. **Landing.** `magneticEase` is `easeOutBack` over pre-warped time: a tile hangs back, then
   accelerates into a snap. Its size contracts to exactly one cell on arrival, so the landed
   mosaic is gapless and the join with the sharp disc is invisible.

---

## Performance

Measured on a Galaxy S21 (Android 13), full-screen photo, `tileSize = 10` ≈ 16 700 tiles.

The naive version of this effect — every tile live at once, as in
[`magnetic_reassemble`](../magnetic_reassemble) and `marvel_effects/ReverseSnap` — ran at
**28–42 ms per frame** while the GPU sat at 2–6 ms. The bottleneck was never the GPU. Four
things fixed it:

| Change                             | Why it mattered                                                                                                                                                                                                                                    |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **The ring**                       | Only tiles inside a `BAND`-wide annulus are live; the rest of the frame is one clipped `drawImage`. Everything else below is a constant factor — this one is the order of magnitude                                                                |
| **Direct `nativeCanvas.drawRect`** | `DrawScope.drawRect` reconfigures its paint per call — colour, alpha, blend mode, colour filter — several JNI hops per tile. One shared `Paint`, alpha packed into the colour's own channel, antialiasing off (the tiles are axis-aligned squares) |
| **Primitive arrays**               | `CellField` is nine parallel arrays, not a list of 16 700 objects, and the buffers are **reused across runs** since the container size does not change                                                                                             |
| **Off-frame culling**              | Tiles spawn well outside the frame; four comparisons are cheaper than the draw they would cost                                                                                                                                                     |

Two approaches were tried and rejected:

- **`Canvas.drawVertices`** to batch every tile into a single native call. On this pipeline the
  per-vertex colour array is ignored and every tile comes out the Paint's colour — solid
  black. Do not re-attempt without checking on-device first.
- **Warming up on a small bitmap.** The first run also pays for JIT, heap growth and the
  GPU→CPU readback, which lands as a visible freeze because nothing is drawn while the tiles
  are being built. A 384×384 warm-up bitmap warmed the code but neither the readback nor the
  heap. What works is a **dry run at the container's real size** — see below.

### The startup dry run

`LaunchedEffect(Unit)` performs a full silent capture-and-build at startup, so the first real
run does not pay that bill on screen. It **retries** up to `WARM_UP_ATTEMPTS` times: the
content is often still blank this early, and a dry run over transparent pixels warms the
sampling loop but never the half that fills the buffers.

---

## The demo

`WaveReassemblyDemo.kt` is a product-shaped screen rather than a playground: an **AI Photo
Enhancer** where the effect reads as the enhanced image being reconstructed.

| Stage                      | On screen                             | Button                 |
|----------------------------|---------------------------------------|------------------------|
| `Idle`                     | original                              | "Enhance Photo"        |
| `Processing` (700 ms)      | original under a light scrim          | "Enhancing…", disabled |
| `Reconstructing` (1400 ms) | the wave assembles the enhanced frame | disabled               |
| `Done`                     | enhanced frame + "AI Enhanced" badge  | "Enhance Photo"        |

The "enhanced" version is not a second image — it is the same one under a `ColorMatrix`
(saturation 1.35, contrast 1.18). Since that is a draw-time parameter and not a new `model`,
Glide reloads nothing and the enhanced frame is ready in the same frame it is needed.

Status lines are spread across the processing *and* the reconstruction: three messages inside
700 ms would be unreadable, and stretching them makes the animation part of the work rather
than a separate flourish.

---

## Notes

- Requires API 29+ for `GraphicsLayer.toImageBitmap()`.
- The effect does not clip to its bounds; tiles fly in from off-frame. Wrap it in
  `Modifier.clipToBounds()` if it sits above other content that must not be drawn over.