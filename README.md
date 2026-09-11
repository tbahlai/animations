# Animations

Animation and motion experiments in Jetpack Compose — dissolves, tears, liquid, galleries
and one real cloth simulation. Each one lives in its own package with a demo screen and a
README explaining how it works.

> **This is a learning project.** It exists to explore what Compose's `Canvas`,
> `GraphicsLayer` and the Android `Canvas` underneath can be pushed to do. Nothing here is a
> published library — copy what is useful, and expect to adapt it.

Point `MainActivity` at any demo to run it.

## Gallery

|                                                                                                                                                                                                                                                                                 |                                                                                                                                                                                                                                                                           |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="app/src/main/java/com/tsynytsyna/animations/off_the_wall/OffTheWall.gif" width="260"/><br/>**[Off The Wall](app/src/main/java/com/tsynytsyna/animations/off_the_wall)** — grab your screen, it's fabric. Pull hard and it tears off the wall.                         | <img src="app/src/main/java/com/tsynytsyna/animations/wave_reassembly/WaveReassembly.gif" width="260"/><br/>**[Wave Reassembly](app/src/main/java/com/tsynytsyna/animations/wave_reassembly)** — tiles fly in, triggered by a wave expanding from the centre.             |
| <img src="app/src/main/java/com/tsynytsyna/animations/glass_shatter/GlassShatter.gif" width="260"/><br/>**[Glass Shatter](app/src/main/java/com/tsynytsyna/animations/glass_shatter)** — the image breaks into glass shards and falls.                                          | <img src="app/src/main/java/com/tsynytsyna/animations/gallery_disintegration/Gallery_Disintegration.gif" width="260"/><br/>**[Gallery Disintegration](app/src/main/java/com/tsynytsyna/animations/gallery_disintegration)** — the photo blows away as particles.          |
| <img src="app/src/main/java/com/tsynytsyna/animations/git_diff/GitDiffAnimation.gif" width="260"/><br/>**[Git Diff Swap](app/src/main/java/com/tsynytsyna/animations/git_diff)** — a change of job, told as a diff in a source file.                                            | <img src="app/src/main/java/com/tsynytsyna/animations/happy_birthday/HappyBirthdayAnimation.gif" width="260"/><br/>**[Happy Birthday](app/src/main/java/com/tsynytsyna/animations/happy_birthday)** — a cake drawn entirely on `Canvas`. Swipe to blow out the candles.   |
| <img src="app/src/main/java/com/tsynytsyna/animations/rift_toggle/RiftToggle.gif" width="260"/><br/>**[Rift Toggle](app/src/main/java/com/tsynytsyna/animations/rift_toggle)** — a dark/light switch that tears the screen open.                                                | <img src="app/src/main/java/com/tsynytsyna/animations/polaroid_scatter/Polaroid_Scatter.gif" width="260"/><br/>**[Polaroid Scatter](app/src/main/java/com/tsynytsyna/animations/polaroid_scatter)** — a sort/filter grid that scatters like photos on a table.            |
| <img src="app/src/main/java/com/tsynytsyna/animations/cover_flow/CoverFlow.gif" width="260"/><br/>**[Cover Flow](app/src/main/java/com/tsynytsyna/animations/cover_flow)** — the classic carousel.                                                                              | <img src="app/src/main/java/com/tsynytsyna/animations/fan_cards/FanCards.gif" width="260"/><br/>**[Fan Cards](app/src/main/java/com/tsynytsyna/animations/fan_cards)** — a three-state card stack.                                                                        |
| <img src="app/src/main/java/com/tsynytsyna/animations/paralax_onboarding/Onboarding_Parallax_Preview.gif" width="260"/><br/>**[Parallax Onboarding](app/src/main/java/com/tsynytsyna/animations/paralax_onboarding)** — layered onboarding pages that move at different depths. |                                                                                                                                                                                                                                                                           |

## Stack

Kotlin · Jetpack Compose (BOM 2024.09) · Material 3 · minSdk 26 · Glide for image loading.
No animation libraries — everything is drawn by hand.