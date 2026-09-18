package com.tsynytsyna.animations.origami_fold

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun OrigamiFoldDemo(modifier: Modifier = Modifier) {
    var theme by remember { mutableStateOf(OrigamiTheme.WASHI) }
    var isBackSide by remember { mutableStateOf(false) }

    val foldAngleAnim = remember { Animatable(38f) }
    val flipAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val panels = if (isBackSide) OrigamiData.backPanels else OrigamiData.frontPanels

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.deskColor)
    ) {
        // Subtle ambient lighting & desk surface texture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        ),
                        radius = 1800f
                    )
                )
        )

        // Top Diagnostics Glass Pill
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val angleDeg = foldAngleAnim.value.roundToInt()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF14141E).copy(alpha = 0.88f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(theme.accentColor)
                )
                Text(
                    text = "${theme.title.uppercase()} • ${angleDeg}° FOLD • 3 PANELS • 120 FPS",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Drag horizontally to fold paper • Tap presets to animate hinges",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 3D Origami Folding Viewport (Center of Screen)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val newAngle = (foldAngleAnim.value - delta * 0.35f).coerceIn(0f, 85f)
                            foldAngleAnim.snapTo(newAngle)
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val currentAngle = foldAngleAnim.value
            val angleRad = (currentAngle * Math.PI / 180.0).toFloat()
            val panelW = (maxWidth * 0.30f).coerceIn(95.dp, 125.dp)
            val panelH = 340.dp

            // Dynamic desk drop shadow width: contracts horizontally as paper folds upright
            val shadowExpansion = cos(angleRad).coerceIn(0.12f, 1.0f)
            val totalShadowW = panelW * (1.0f + 2.0f * shadowExpansion)
            val shadowAlpha = (0.25f + sin(angleRad) * 0.45f).coerceIn(0.2f, 0.7f)

            // 1. Desk Drop Shadow Layer
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(totalShadowW)
                    .height(panelH * 0.88f)
                    .graphicsLayer {
                        rotationX = 65f
                        translationY = with(density) { 150.dp.toPx() }
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = shadowAlpha),
                                Color.Black.copy(alpha = shadowAlpha * 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 2. Main 3-Panel Origami Assembly Layer
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        // Camera distance for natural isometric depth
                        cameraDistance = 12f * density.density
                        // 3D paper rotation for overall perspective tilt
                        rotationX = 14f
                        rotationY = flipAnim.value
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PANEL 1: Left Wing (Folds forward around its RIGHT hinge)
                Box(
                    modifier = Modifier
                        .width(panelW)
                        .height(panelH)
                        .graphicsLayer {
                            cameraDistance = 12f * density.density
                            transformOrigin = TransformOrigin(1.0f, 0.5f) // Right edge hinge
                            rotationY = -currentAngle * 0.95f
                        }
                ) {
                    OrigamiPanelView(
                        panelIndex = 0,
                        content = panels[0],
                        theme = theme,
                        foldAngle = currentAngle,
                        isLeftHinge = false,
                        isRightHinge = true
                    )
                }

                // PANEL 2: Center Spine Panel (Stationary)
                Box(
                    modifier = Modifier
                        .width(panelW)
                        .height(panelH)
                ) {
                    OrigamiPanelView(
                        panelIndex = 1,
                        content = panels[1],
                        theme = theme,
                        foldAngle = currentAngle,
                        isLeftHinge = true,
                        isRightHinge = true
                    )
                }

                // PANEL 3: Right Wing (Folds forward around its LEFT hinge)
                Box(
                    modifier = Modifier
                        .width(panelW)
                        .height(panelH)
                        .graphicsLayer {
                            cameraDistance = 12f * density.density
                            transformOrigin = TransformOrigin(0.0f, 0.5f) // Left edge hinge
                            rotationY = currentAngle * 0.95f
                        }
                ) {
                    OrigamiPanelView(
                        panelIndex = 2,
                        content = panels[2],
                        theme = theme,
                        foldAngle = currentAngle,
                        isLeftHinge = true,
                        isRightHinge = false
                    )
                }
            }
        }

        // Bottom Glassmorphic Controls Bar
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F0E17).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.12f)
            ),
            shadowElevation = 20.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // 1. Theme Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrigamiTheme.values().forEach { t ->
                        val isSelected = theme == t
                        val chipBg = if (isSelected) t.accentColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f)
                        val chipBorder = if (isSelected) t.accentColor else Color.White.copy(alpha = 0.10f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                .clickable { theme = t }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(t.accentColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = t.title,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Fold Preset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FoldPreset.values().forEach { preset ->
                        val isCurrent = (foldAngleAnim.value - preset.targetAngle).let { kotlin.math.abs(it) < 4f }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCurrent) theme.accentColor.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isCurrent) theme.accentColor else Color.White.copy(alpha = 0.10f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    scope.launch {
                                        foldAngleAnim.animateTo(
                                            targetValue = preset.targetAngle,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.65f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Continuous Angle Slider & Flip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flip Paper Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.accentColor)
                            .clickable {
                                scope.launch {
                                    val target = if (isBackSide) 0f else 180f
                                    flipAnim.animateTo(
                                        targetValue = target,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    isBackSide = !isBackSide
                                    if (target == 180f) flipAnim.snapTo(0f)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBackSide) "↻ REVERSE" else "↻ FLIP MAP",
                            color = Color.Black,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }

                    // Continuous Slider
                    Slider(
                        value = foldAngleAnim.value,
                        onValueChange = { angle ->
                            scope.launch { foldAngleAnim.snapTo(angle) }
                        },
                        valueRange = 0f..85f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentColor,
                            activeTrackColor = theme.accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    // Reset Flatten Button
                    IconButton(
                        onClick = {
                            scope.launch {
                                foldAngleAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Flatten Paper",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
