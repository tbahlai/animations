package com.tsynytsyna.animations.production_components.ticket

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnfoldingTicketDemo(modifier: Modifier = Modifier) {
    var selectedFlight by remember { mutableStateOf(SampleFlightTickets.tokyoFlight) }
    var targetUnfoldProgress by remember { mutableFloatStateOf(1f) }
    var showInspector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val animatedProgress by animateFloatAsState(
        targetValue = targetUnfoldProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "unfoldSpring"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        // Atmospheric Airport Radar Radial Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            selectedFlight.accentColor.copy(alpha = 0.12f),
                            selectedFlight.accentColor.copy(alpha = 0.03f),
                            Color(0xFF0A0E17)
                        ),
                        radius = 1400f
                    )
                )
        )

        // Main Scrollable Area for Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF131C2E).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    .clickable { showInspector = true }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(selectedFlight.accentColor)
                )
                Text(
                    text = "${selectedFlight.flightNumber} • GATE ${selectedFlight.gate} • ${(animatedProgress * 100).roundToInt()}% UNFOLDED",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = selectedFlight.accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Production Unfolding Ticket Card
            UnfoldingTicketCard(
                ticket = selectedFlight,
                unfoldProgress = animatedProgress,
                onToggleUnfold = {
                    targetUnfoldProgress = if (targetUnfoldProgress > 0.5f) 0f else 1f
                }
            )
        }

        // Bottom Glass Control HUD
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
                // Flight Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SampleFlightTickets.all.forEach { flight ->
                        val isSelected = selectedFlight.id == flight.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) flight.accentColor
                                    else Color.White.copy(alpha = 0.06f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) flight.accentColor
                                    else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFlight = flight }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${flight.flightNumber} (${flight.originCode}➔${flight.destinationCode})",
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Presets Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "COMPACT (0%)" to 0f,
                        "HALF FOLD (50%)" to 0.5f,
                        "EXPANDED (100%)" to 1f
                    ).forEach { (label, value) ->
                        val isSelected = (targetUnfoldProgress - value).let { kotlin.math.abs(it) < 0.1f }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) selectedFlight.accentColor.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) selectedFlight.accentColor
                                    else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { targetUnfoldProgress = value }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) selectedFlight.accentColor else Color.White.copy(alpha = 0.6f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Continuous Fold Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3D FOLD",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Slider(
                        value = targetUnfoldProgress,
                        onValueChange = { targetUnfoldProgress = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = selectedFlight.accentColor,
                            activeTrackColor = selectedFlight.accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Inspector Sheet
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
                            text = "3D TICKET FOLD ARCHITECTURE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = selectedFlight.accentColor
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { targetUnfoldProgress = 1f }
                        )
                    }

                    Text(
                        text = "Translates 3D origami seam hinges into a production airline boarding pass. Uses graphicsLayer transformOrigin seam anchors with trigonometric ambient crease occlusion shadows and vector laser scanning.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
