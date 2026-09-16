package com.tsynytsyna.animations.production_components.pull_to_refresh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class FeedArticle(
    val id: Int,
    val title: String,
    val category: String,
    val timestamp: String,
    val snippet: String,
    val readTime: String,
    val tagColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElasticPullRefreshDemo(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var selectedWord by remember { mutableStateOf("REFRESH") }
    var showInspector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val articles = remember {
        mutableStateListOf(
            FeedArticle(
                id = 1,
                title = "Hooke's Law in Modern Mobile Interfaces",
                category = "PHYSICS & UX",
                timestamp = "JUST NOW",
                snippet = "Why linear easing feels synthetic and how harmonic spring-mass matrices create physical intuition in Android touch interactions.",
                readTime = "4 MIN READ",
                tagColor = Color(0xFFFF2A55)
            ),
            FeedArticle(
                id = 2,
                title = "Volumetric Conservation in Kinetic Type",
                category = "TYPOGRAPHY",
                timestamp = "12M AGO",
                snippet = "Preserving perceptual character weight with squash & stretch (ΔW × ΔH = const) during high-velocity fling gestures.",
                readTime = "6 MIN READ",
                tagColor = Color(0xFF00F0FF)
            ),
            FeedArticle(
                id = 3,
                title = "Zero-Allocation Nested Scroll Solvers",
                category = "COMPOSE ARCH",
                timestamp = "45M AGO",
                snippet = "Handling 120 FPS overscroll dynamics without allocating garbage collection objects inside NestedScrollConnection callbacks.",
                readTime = "5 MIN READ",
                tagColor = Color(0xFF10B981)
            ),
            FeedArticle(
                id = 4,
                title = "From 3D Paper Folds to Digital Boarding Passes",
                category = "SPATIAL DESIGN",
                timestamp = "2H AGO",
                snippet = "Applying ambient occlusion crease shadow gradients and seam transforms to enterprise ticketing systems.",
                readTime = "8 MIN READ",
                tagColor = Color(0xFFF59E0B)
            )
        )
    }

    val state = rememberElasticPullRefreshState(
        onRefresh = {
            // Simulate asynchronous backend fetch
            delay(2000)
            articles.add(
                0,
                FeedArticle(
                    id = articles.size + 1,
                    title = "Live Synced Update #${articles.size + 1}",
                    category = "REALTIME STREAM",
                    timestamp = "JUST NOW",
                    snippet = "Fresh payload streamed through elastic pull-to-refresh architecture.",
                    readTime = "2 MIN READ",
                    tagColor = Color(0xFFD946EF)
                )
            )
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B12))
    ) {
        // Atmospheric Gradient Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF2A55).copy(alpha = 0.08f),
                            Color(0xFF080B12)
                        ),
                        radius = 1200f
                    )
                )
        )

        // Nested Scrollable Container with GPU-accelerated Layer Translation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(state.nestedScrollConnection)
                .pointerInput(state) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        state.onPointerDown()
                        try {
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                            } while (event.changes.any { it.pressed })
                        } finally {
                            state.onPointerRelease()
                        }
                    }
                }
        ) {
            // Elastic Kinetic Typography Header revealed behind/above feed
            ElasticPullRefreshHeader(
                state = state,
                word = selectedWord,
                accentColor = Color(0xFFFF2A55),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        // Smooth parallax descent: Header settles into view as list pulls down
                        translationY = ((state.effectivePullPx - 140f * density) * 0.35f).coerceAtMost(10f * density)
                    }
            )

            // Scrollable Feed List translated via GPU RenderNode (ZERO relayout thrashing)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = state.effectivePullPx
                    }
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Diagnostics & Physics Telemetry Card
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF101626).copy(alpha = 0.92f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (state.motionState) {
                                PullMotionState.DRAGGING -> Color(0xFF10B981)
                                PullMotionState.SETTLING -> Color(0xFFF59E0B)
                                PullMotionState.REFRESHING -> Color(0xFF00E5FF)
                                PullMotionState.IDLE -> Color.White.copy(alpha = 0.12f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInspector = true }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (state.motionState) {
                                                    PullMotionState.DRAGGING -> Color(0xFF10B981)
                                                    PullMotionState.SETTLING -> Color(0xFFF59E0B)
                                                    PullMotionState.REFRESHING -> Color(0xFF00E5FF)
                                                    PullMotionState.IDLE -> Color(0xFF64748B)
                                                }
                                            )
                                    )
                                    Text(
                                        text = "PHYSICS: ${state.motionState.name}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${state.effectivePullPx.roundToInt()}PX",
                                        color = Color(0xFFFF2A55),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val vel = state.lastReleaseVelocity.roundToInt()
                                val velArrow = if (vel > 100) "↓" else if (vel < -100) "↑" else "—"
                                Text(
                                    text = "RELEASE VEL: $vel PX/S $velArrow",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "INTERRUPTED: ${state.interruptionsCount}×",
                                    color = if (state.interruptionsCount > 0) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Fling release to carry momentum into settling • Re-grab mid-motion to test C⁰ continuous position.",
                                color = Color.White.copy(alpha = 0.48f),
                                fontSize = 9.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(articles, key = { it.id }) { article ->
                    FeedCard(article = article)
                }

                item {
                    Spacer(modifier = Modifier.height(160.dp))
                }
            }
        }

        // Bottom Glass HUD Controls
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F1523).copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.14f)
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
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Word Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("REFRESH", "MOTION", "UPDATE", "KINETIC", "STREAM").forEach { word ->
                        val isSelected = selectedWord == word
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFFFF2A55)
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF2A55)
                                    else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedWord = word }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = word,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Simulate Refresh Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFF2A55))
                            .clickable {
                                state.triggerRefresh()
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Simulate Refresh",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.isRefreshing) "REFRESHING FEED..." else "SIMULATE PULL REFRESH",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Technical Inspector Sheet
        if (showInspector) {
            ModalBottomSheet(
                onDismissRequest = { showInspector = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0F1523),
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ELASTIC PULL-TO-REFRESH ARCHITECTURE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFF2A55)
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { state.finishRefresh() }
                        )
                    }

                    Text(
                        text = "1. HYPERBOLIC RESISTANCE: d = d_max * tanh(pull/d_max) with volume-conserving glyph squash & stretch.\n\n2. VELOCITY CONTINUITY: onPreFling captures fling release velocity (px/s) and passes it directly as initialVelocity into spring(DampingRatioMediumBouncy, StiffnessMediumLow).\n\n3. ZERO-POP INTERRUPTIBILITY: Touching down mid-settling cancels the background spring job, freezing the header at its exact instantaneous coordinate (C⁰ positional continuity) and handing drag control back to the finger seamlessly.",
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.72f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedCard(article: FeedArticle) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111728)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.category,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = article.tagColor
                )
                Text(
                    text = "${article.timestamp} • ${article.readTime}",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.snippet,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = Color.White.copy(alpha = 0.62f)
            )
        }
    }
}
