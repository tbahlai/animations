package com.tsynytsyna.animations.showcase

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ShowcaseNavigator(modifier: Modifier = Modifier) {
    var activeItem by remember { mutableStateOf<AnimationItem?>(null) }

    // Intercept hardware/gesture back press
    BackHandler(enabled = activeItem != null) {
        activeItem = null
    }

    AnimatedContent(
        targetState = activeItem,
        transitionSpec = {
            if (targetState != null) {
                // Opening demo: slide in from right
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(380)
                ) + fadeIn(animationSpec = tween(380))).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(380)
                    ) + fadeOut(animationSpec = tween(200))
                )
            } else {
                // Returning to gallery: slide in from left
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(380)
                ) + fadeIn(animationSpec = tween(380))).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(380)
                    ) + fadeOut(animationSpec = tween(200))
                )
            }
        },
        label = "showcaseNavigation"
    ) { currentItem ->
        if (currentItem == null) {
            ShowcaseHomeScreen(
                onSelectAnimation = { item -> activeItem = item },
                modifier = modifier,
            )
        } else {
            ActiveDemoContainer(
                item = currentItem,
                onBack = { activeItem = null },
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveDemoContainer(
    item: AnimationItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHintBanner by remember { mutableStateOf(true) }
    var showInspectorSheet by remember { mutableStateOf(false) }

    // Auto-dismiss hint banner after 3.5 seconds
    LaunchedEffect(item.id, showHintBanner) {
        if (showHintBanner) {
            delay(3500.milliseconds)
            showHintBanner = false
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val cutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val topInset = maxOf(statusBarPadding.calculateTopPadding(), cutoutPadding.calculateTopPadding())
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarPadding.calculateBottomPadding()
    val topBarHeight = topInset + 56.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
    ) {
        // 1. Active Composable Content - Padded safely below the top app bar and above bottom nav bar
        item.content(
            Modifier
                .fillMaxSize()
                .padding(top = topBarHeight, bottom = bottomInset)
        )

        // 2. Unified Ultra-Premium Top App Bar & Status Bar Scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04060A),
                            Color(0xFF060913),
                            Color(0xFF080D1A).copy(alpha = 0.98f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF1E293B).copy(alpha = 0.65f),
                        )
                    ),
                    shape = RectangleShape,
                )
                .zIndex(100f),
        ) {
            // Action Row within the 56.dp bar below status bar inset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Floating Frosted Back Button
                Row(
                    modifier = Modifier
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF131C2E))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable(onClick = onBack)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gallery",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Right Action: Title Pill + Inspector Trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Demo Title Pill
                    Row(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(100.dp))
                            .background(Color(0xFF0E1626), RoundedCornerShape(100.dp))
                            .border(1.dp, item.accentColor.copy(alpha = 0.45f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(item.accentColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Hint Re-trigger Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (showHintBanner) item.accentColor.copy(alpha = 0.25f)
                                else Color(0xFF131C2E)
                            )
                            .border(
                                1.dp,
                                if (showHintBanner) item.accentColor
                                else Color.White.copy(alpha = 0.16f),
                                CircleShape
                            )
                            .clickable { showHintBanner = !showHintBanner },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 13.sp,
                        )
                    }

                    // Inspector Info Pill Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (showInspectorSheet) item.accentColor.copy(alpha = 0.25f)
                                else Color(0xFF131C2E)
                            )
                            .border(
                                1.dp,
                                if (showInspectorSheet) item.accentColor
                                else Color.White.copy(alpha = 0.16f),
                                CircleShape
                            )
                            .clickable { showInspectorSheet = !showInspectorSheet },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡",
                            color = if (showInspectorSheet) item.accentColor else Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Gesture Hint Banner (Top-docked below Top App Bar, never covers bottom CTAs)
        AnimatedVisibility(
            visible = showHintBanner && !showInspectorSheet,
            enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260)) { -it / 2 },
            exit = fadeOut(tween(200)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topBarHeight + 10.dp, start = 16.dp, end = 16.dp)
                .zIndex(95f)
        ) {
            Row(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(100.dp))
                    .background(Color(0xFF070B14).copy(alpha = 0.95f), RoundedCornerShape(100.dp))
                    .border(1.dp, item.accentColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                    .clickable { showHintBanner = false }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(item.accentColor, CircleShape)
                        .shadow(4.dp, CircleShape, spotColor = item.accentColor)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = item.instructionHint,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { showHintBanner = false }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "✕",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Frosted Glass Algorithm Inspector Modal Sheet
        AnimatedVisibility(
            visible = showInspectorSheet,
            enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(250)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(200f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = navBarPadding.calculateBottomPadding() + 16.dp
                    )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(28.dp, RoundedCornerShape(26.dp), spotColor = item.accentColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF070B14).copy(alpha = 0.96f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                item.accentColor.copy(alpha = 0.65f),
                                Color(0xFF1E293B).copy(alpha = 0.4f),
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡",
                                    fontSize = 16.sp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ALGORITHM INSPECTOR",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.3.sp,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .clickable { showInspectorSheet = false }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✕ Close",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.technicalBadge,
                            color = item.accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.algorithmDetails,
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tags
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
