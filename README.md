# Animations

Animation and motion experiments in Jetpack Compose — dissolves, tears, liquid, galleries
and one real cloth simulation. Each one lives in its own package with a demo screen and a
README explaining how it works.

> **This is a learning project.** It exists to explore what Compose's `Canvas`,
> `GraphicsLayer` and the Android `Canvas` underneath can be pushed to do. Nothing here is a
> published library — copy what is useful, and expect to adapt it.

Point `MainActivity` at any demo to run it.

## Off The Wall

Grab your screen — it's fabric. Pull hard enough and it tears off the wall.
A mass-spring cloth solver: ~1,000 nodes, ~5,900 springs, every frame.

<img alt="Off The Wall preview" src="app/src/main/java/com/tsynytsyna/animations/off_the_wall/OffTheWall.gif" width="800"/>

[→ How it works](app/src/main/java/com/tsynytsyna/animations/off_the_wall)

## Gallery

|                                                                                                                                                 &nbsp;                                                                                                                                                  |                                                                                                                                                       &nbsp;                                                                                                                                                        |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|      <img alt="Wave Reassembly preview" src="app/src/main/java/com/tsynytsyna/animations/wave_reassembly/WaveReassembly.gif" width="400"/><br/>**[Wave Reassembly](app/src/main/java/com/tsynytsyna/animations/wave_reassembly)**<br/>Tiles fly in, triggered by a wave expanding from the centre.      |                        <img alt="Glass Shatter preview" src="app/src/main/java/com/tsynytsyna/animations/glass_shatter/GlassShatter.gif" width="400"/><br/>**[Glass Shatter](app/src/main/java/com/tsynytsyna/animations/glass_shatter)**<br/>The image breaks into glass shards and falls.                         |
| <img alt="Gallery Disintegration preview" src="app/src/main/java/com/tsynytsyna/animations/gallery_disintegration/Gallery_Disintegration.gif" width="400"/><br/>**[Gallery Disintegration](app/src/main/java/com/tsynytsyna/animations/gallery_disintegration)**<br/>The photo blows away as particles. |                            <img alt="Rift Toggle preview" src="app/src/main/java/com/tsynytsyna/animations/rift_toggle/RiftToggle.gif" width="400"/><br/>**[Rift Toggle](app/src/main/java/com/tsynytsyna/animations/rift_toggle)**<br/>A dark/light switch that tears the screen open.                             |
|                   <img alt="Git Diff Swap preview" src="app/src/main/java/com/tsynytsyna/animations/git_diff/GitDiffAnimation.gif" width="400"/><br/>**[Git Diff Swap](app/src/main/java/com/tsynytsyna/animations/git_diff)**<br/>A change of job, told as a diff in a source file.                    |       <img alt="Happy Birthday preview" src="app/src/main/java/com/tsynytsyna/animations/happy_birthday/HappyBirthdayAnimation.gif" width="400"/><br/>**[Happy Birthday](app/src/main/java/com/tsynytsyna/animations/happy_birthday)**<br/>A cake drawn entirely on `Canvas`. Swipe to blow out the candles.        |
|     <img alt="Polaroid Scatter preview" src="app/src/main/java/com/tsynytsyna/animations/polaroid_scatter/Polaroid_Scatter.gif" width="400"/><br/>**[Polaroid Scatter](app/src/main/java/com/tsynytsyna/animations/polaroid_scatter)**<br/>A sort/filter grid that scatters like photos on a table.     |                                            <img alt="Cover Flow preview" src="app/src/main/java/com/tsynytsyna/animations/cover_flow/CoverFlow.gif" width="400"/><br/>**[Cover Flow](app/src/main/java/com/tsynytsyna/animations/cover_flow)**<br/>The classic carousel.                                            |
|                                      <img alt="Fan Cards preview" src="app/src/main/java/com/tsynytsyna/animations/fan_cards/FanCards.gif" width="400"/><br/>**[Fan Cards](app/src/main/java/com/tsynytsyna/animations/fan_cards)**<br/>A three-state card stack.                                       | <img alt="Parallax Onboarding preview" src="app/src/main/java/com/tsynytsyna/animations/paralax_onboarding/Onboarding_Parallax_Preview.gif" width="400"/><br/>**[Parallax Onboarding](app/src/main/java/com/tsynytsyna/animations/paralax_onboarding)**<br/>Layered onboarding pages that move at different depths. |

## Stack

Kotlin · Jetpack Compose (BOM 2024.09) · Material 3 · minSdk 26 · Glide for image loading.
No animation libraries — everything is drawn by hand.