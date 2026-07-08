# Cover Flow (Jetpack Compose)

A reusable, classic "cover flow" carousel built with Jetpack Compose.
Covers slide past a centered focal point, tilting away in 3D with a scale,
fade and darkening falloff — plus an optional mirrored reflection underneath.

---

## Preview

![Cover Flow Preview](CoverFlow.gif)

---

## Overview

This component lays out a list of images as a horizontal carousel. The
centered cover faces the viewer flat, while neighbours rotate away on the
Y axis like pages of an open book, shrinking, dimming and darkening the
further they sit from the center.

It is built on top of `HorizontalPager`, so flinging, snapping and offset
tracking come for free. The 3D look is layered on entirely through
`graphicsLayer`, driven by each page's distance from the current page.

---

## Key Features

- Built on `HorizontalPager` — native fling, snap and offset tracking
- Infinite looping via a huge virtual page count centered at start
- 3D `rotationY` tilt with `cameraDistance` for real perspective
- Distance-based scale, alpha and dark-overlay falloff
- Cards overlap toward the center, then fan out past the neighbours
- `zIndex` ordering so the focused cover always sits on top
- Optional mirrored reflection that fades into the background color
- Tap left / right of the focused cover to step focus one at a time
- Fully configurable: `cardSize`, `cardHeight`, `reflection`, `background`

---

## Animation Concept

Every page computes a single signed `offset` = distance from the centered
page (including the in-flight fraction while scrolling). From that one value
the `graphicsLayer` derives the whole look:

- `rotationY` — `offset` (clamped to ±1) × `MAX_ROTATION`, so covers tilt
  away from the viewer as they leave the center and hold a flat max angle
- `scaleX/Y` — shrinks with distance down to `MIN_SCALE`
- `alpha` — fades with distance down to `MIN_ALPHA`
- `translationX` — pulls covers into a tight overlap near the center
  (`OVERLAP`), then spreads them out again once they are more than one slot
  away, so the stack reads as depth rather than a flat row
- A dark overlay rectangle deepens with distance, pushing off-center covers
  visually into the background

Because every transform reads from the same live `offset`, the carousel
animates continuously with the scroll instead of snapping between states.

---

## Data Model

The component is intentionally minimal:

- `photos: List<String>` — image URLs (loaded with Glide)
- A `PagerState` seeded at the middle of `Int.MAX_VALUE` pages, aligned to a
  multiple of `photos.size` so `page % count` maps cleanly onto real images
- Per-page transforms derived from `currentPage`, the page index and
  `currentPageOffsetFraction` — no per-item state to manage

This keeps the effect self-contained: pass a list of URLs and it runs.

---

## How to Use

    CoverFlow(
        photos = listOf(
            "https://picsum.photos/id/1084/600/600",
            "https://picsum.photos/id/1080/600/600",
            // ...
        ),
        cardSize = 250.dp,
        reflection = true,
        background = darkBg,   // reflection fades into this color
    )

Swipe to flow through the covers, or tap to the left / right of the focused
cover to step focus by one. When `reflection` is on, set `background` to the
color behind the carousel so the mirrored image fades out cleanly.

See `CoverFlowDemo.kt` for a complete example.

---

## Parameters

| Parameter    | Default           | Description                               |
|--------------|-------------------|-------------------------------------------|
| `photos`     | required          | List of image URLs                        |
| `modifier`   | `Modifier`        | Applied to the outer container            |
| `cardSize`   | `240.dp`          | Width of each cover                       |
| `cardHeight` | `cardSize * 1.4f` | Height of each cover                      |
| `reflection` | `true`            | Show mirrored reflection under each cover |
| `background` | `Transparent`     | Color the reflection fades into           |

---

## File Structure

    cover_flow/
    ├── CoverFlow.kt        ← Reusable component (copy this one file)
    └── CoverFlowDemo.kt    ← Usage example

---

## Notes

- Uses Glide (`GlideImage`) for image loading — swap for Coil if preferred
- Infinite loop uses a virtual page count, so there is no visible seam
- All 3D is `graphicsLayer` only — no shaders or bitmap work
- Reflection re-decodes the same image flipped and faded; disable it via
  `reflection = false` if you want a lighter carousel

---