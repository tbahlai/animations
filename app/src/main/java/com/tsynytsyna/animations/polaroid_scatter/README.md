# Polaroid Scatter (Jetpack Compose)

A reusable animated sort/filter grid built with Jetpack Compose.  
Photos scatter like polaroids thrown on a table, then reassemble into
a neat grid when sorting changes — all with spring physics.

---

## Preview

![Polaroid Scatter Preview](Polaroid_Scatter.gif)

---

## Overview

This component displays a list of items as polaroid-style cards. When
the user changes the sort/filter, cards scatter to random positions with
random rotations, then spring back into a grid in the new order.

The effect is generic — it works with any data type and any content
inside the cards.

---

## Key Features

- Generic `<T>` API — works with any data type
- Multiple sort/filter options via `SortOption` list
- Spring physics with staggered stiffness (earlier items arrive first)
- Randomized scatter positions and rotations on every sort change
- Dynamic card sizing based on screen dimensions (works on any device)
- Proper item tracking via `itemKey` for correct animations on reorder
- Custom shadow via Canvas `setShadowLayer` (no extra RenderNode)
- Offset animation via `Animatable(Offset)` — x/y as single vector

---

## Animation Concept

Each sort change triggers a two-phase animation:

1. **Scatter** — all cards jump to random positions with random rotations
2. **Assemble** — cards spring into their new grid positions (rotation → 0°)

The phases are separated by just 100ms, so visually it looks like cards
fly from chaos directly into order. Each card has slightly different
spring stiffness creating a satisfying cascade effect.

---

## Data Model

The component uses:

- `SortOption<T>` — label + sort function for each filter button
- `GridLayout` — calculated card sizes, grid offsets, spacing
- `Animatable(Offset)` — position animation per card (x + y as vector)
- `Animatable(Float)` — rotation animation per card
- `key(itemKey(item))` — stable identity for correct recomposition

---

## How to Use

    PolaroidScatter(
        items = myPhotos,
        sortOptions = listOf(
            SortOption("All") { it },
            SortOption("Recent") { it.sortedByDescending { p -> p.date } },
            SortOption("Shuffle") { it.shuffled() },
        ),
        itemKey = { it.id },
    ) { photo ->
        GlideImage(
            model = photo.url,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop
        )
    }

See `PolaroidScatterDemo.kt` for a complete example.

---

## Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `items` | required | List of data items |
| `sortOptions` | required | List of sort/filter buttons |
| `columns` | 3 | Number of grid columns |
| `backgroundColor` | #F2F0E8 | Container background |
| `cardColor` | White | Polaroid card color |
| `itemKey` | `{ it }` | Unique key for item tracking |
| `content` | required | Composable content inside each card |

---

## File Structure

    polaroid_scatter/
    ├── PolaroidScatter.kt      ← Reusable component (copy this one file)
    │   ├── PolaroidScatter()   ← Main composable
    │   ├── SortChips()         ← Filter chip row
    │   ├── PolaroidCard()      ← Single animated card
    │   ├── calculateGridLayout()  ← Dynamic size calculation
    │   ├── generateScatterPositions() ← Random positions
    │   └── polaroidShadow()    ← Custom Canvas shadow
    │
    └── PolaroidScatterDemo.kt  ← Usage example

---

## Limitations

- **No scroll support** — cards are positioned absolutely (offset) inside a
  fixed container, which is not compatible with scrolling. If you need more
  items than fit on one screen, consider reducing the column count or card
  size. The scatter animation relies on a fixed viewport to randomize
  positions, so scrollable containers would break the effect.

---

## Notes

- Best for 9–15 items that fit on one screen (no scroll)
- Card sizes adapt automatically to any screen size
- Spring stiffness is staggered per item index for cascade effect
- Works with Glide, Coil, or any image loading library
- No shaders, no bitmap manipulation — pure layout animation

---
