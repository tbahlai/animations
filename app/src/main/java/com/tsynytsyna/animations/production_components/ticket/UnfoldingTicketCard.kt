package com.tsynytsyna.animations.production_components.ticket

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun UnfoldingTicketCard(
    ticket: FlightTicket,
    unfoldProgress: Float, // 0f (compact folded) to 1f (fully expanded)
    onToggleUnfold: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D hinge fold angles
    // When progress = 0 (folded), middle panel is folded backwards (-75 deg)
    // and bottom barcode panel is folded forwards (80 deg)
    val middleFoldAngle = (1f - unfoldProgress) * -82f
    val bottomFoldAngle = (1f - unfoldProgress) * 86f

    // Ambient occlusion crease shadow intensity proportional to fold angle
    val creaseOcclusionAlpha = sin((1f - unfoldProgress) * kotlin.math.PI.toFloat() * 0.5f) * 0.72f

    // Laser scanner line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laserScan")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = (12f + (1f - unfoldProgress) * 16f).dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(ticket.paperColor)
    ) {
        // =========================================================================
        // 1. TOP SUMMARY PANEL (Always Flat, Anchor Base)
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ticket.paperColor,
                            ticket.paperColor.copy(alpha = 0.96f)
                        )
                    )
                )
                .clickable(onClick = onToggleUnfold)
                .padding(20.dp)
        ) {
            // Airline Branding & Flight Code
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ticket.accentColor)
                    )
                    Text(
                        text = ticket.airline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ticket.accentColor.copy(alpha = 0.12f))
                        .border(1.dp, ticket.accentColor.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = ticket.flightNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ticket.accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Flight Route & Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin
                Column {
                    Text(
                        text = ticket.originCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = ticket.originCity,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = ticket.departureTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ticket.accentColor
                    )
                }

                // Flight Vector Plane Icon & Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ticket.duration,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.width(36.dp).height(2.dp)) {
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset.Zero,
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Flight Direction",
                            tint = ticket.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Canvas(modifier = Modifier.width(36.dp).height(2.dp)) {
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset.Zero,
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                    Text(
                        text = "NON-STOP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                // Destination
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ticket.destinationCode,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = ticket.destinationCity,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = ticket.arrivalTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unfold Prompt Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (unfoldProgress > 0.6f) "COLLAPSE PASS" else "TAP TO UNFOLD BOARDING PASS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF475569)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = ticket.accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            rotationZ = unfoldProgress * 180f
                        }
                )
            }
        }

        // =========================================================================
        // 2. MIDDLE ITINERARY PANEL (3D Hinge Folded from top seam)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = middleFoldAngle
                    cameraDistance = 14f * density
                    alpha = (unfoldProgress * 1.5f).coerceIn(0.15f, 1f)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ticket.paperColor)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Perforated Seam with Edge Notches
                TicketPerforation(accentColor = ticket.accentColor)

                Spacer(modifier = Modifier.height(16.dp))

                // Passenger & Gate Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PASSENGER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ticket.passengerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CABIN CLASS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ticket.cabinClass,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ticket.accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gate, Seat, Group Highlights
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ticket.gate,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SEAT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ticket.seat,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ticket.accentColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GROUP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = ticket.boardingGroup,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Crease Ambient Occlusion Overlay
            if (creaseOcclusionAlpha > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = creaseOcclusionAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // =========================================================================
        // 3. BOTTOM BARCODE PANEL (3D Hinge Folded from middle seam)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    rotationX = bottomFoldAngle
                    cameraDistance = 14f * density
                    alpha = (unfoldProgress * 1.5f).coerceIn(0.1f, 1f)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ticket.paperColor)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary Perforation
                TicketPerforation(accentColor = ticket.accentColor)

                Spacer(modifier = Modifier.height(14.dp))

                // High-Contrast Barcode with Laser Scan Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Procedural Barcode Canvas
                    Canvas(modifier = Modifier.fillMaxWidth().height(42.dp)) {
                        val barWidths = floatArrayOf(
                            2f, 4f, 1.5f, 5f, 2f, 3f, 6f, 1.5f, 4f, 2f, 7f, 1.5f,
                            3f, 5f, 2f, 4f, 2f, 6f, 3f, 1.5f, 5f, 2f, 4f, 3f,
                            6f, 2f, 1.5f, 5f, 3f, 4f, 2f, 7f, 1.5f, 4f, 2f, 3f,
                            5f, 2f, 6f, 1.5f, 3f, 5f, 2f, 4f, 2f, 6f, 3f, 2f
                        )
                        var currentX = 10f
                        val totalW = size.width - 20f
                        val stepW = totalW / barWidths.sum()

                        for (w in barWidths) {
                            val barW = w * stepW * 0.7f
                            drawRect(
                                color = Color(0xFF0F172A),
                                topLeft = Offset(currentX, 0f),
                                size = Size(barW, size.height)
                            )
                            currentX += w * stepW
                        }

                        // Animated Laser Scan Line
                        if (unfoldProgress > 0.8f) {
                            val laserX = size.width * laserPosition
                            drawLine(
                                color = ticket.accentColor,
                                start = Offset(laserX, 0f),
                                end = Offset(laserX, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawCircle(
                                color = ticket.accentColor.copy(alpha = 0.45f),
                                radius = 6.dp.toPx(),
                                center = Offset(laserX, size.height * 0.5f)
                            )
                        }
                    }

                    // Barcode Payload Number
                    Text(
                        text = ticket.barcodeNumber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet CTA Button
                Button(
                    onClick = { /* Add to wallet action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Wallet",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add to Google Wallet",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Crease Ambient Occlusion Overlay
            if (creaseOcclusionAlpha > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = creaseOcclusionAlpha * 0.85f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * Perforated dashed divider line with left and right semicircular cutter notches.
 */
@Composable
private fun TicketPerforation(accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val notchRadius = 8.dp.toPx()
            val cy = size.height * 0.5f

            // Left circular notch
            drawCircle(
                color = Color(0xFF0A0E17), // Match screen backdrop
                radius = notchRadius,
                center = Offset(-12.dp.toPx(), cy)
            )

            // Right circular notch
            drawCircle(
                color = Color(0xFF0A0E17),
                radius = notchRadius,
                center = Offset(size.width + 12.dp.toPx(), cy)
            )

            // Dashed tear line across middle
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(notchRadius, cy),
                end = Offset(size.width - notchRadius, cy),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }
    }
}
