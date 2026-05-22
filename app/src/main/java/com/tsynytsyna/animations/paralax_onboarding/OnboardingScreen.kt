package com.tsynytsyna.animations.paralax_onboarding

import androidx.compose.ui.unit.lerp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(page = page, pagerState = pagerState)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val fraction = pagerState.currentPageOffsetFraction
                repeat(pages.size) { i ->
                    val selectedFraction = when (i) {
                        pagerState.currentPage -> 1f - fraction.absoluteValue
                        pagerState.currentPage + 1 -> fraction.coerceAtLeast(0f)
                        pagerState.currentPage - 1 -> (-fraction).coerceAtLeast(0f)
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(lerp(6.dp, 24.dp, selectedFraction))
                            .clip(CircleShape)
                            .background(
                                lerp(Color.White.copy(alpha = 0.3f), pages[i].accentColor, selectedFraction)
                            )
                    )
                }
            }

            val fraction = pagerState.currentPageOffsetFraction
            val buttonColor = when {
                fraction > 0 && pagerState.currentPage < pages.size - 1 ->
                    lerp(pages[pagerState.currentPage].accentColor, pages[pagerState.currentPage + 1].accentColor, fraction)
                fraction < 0 && pagerState.currentPage > 0 ->
                    lerp(pages[pagerState.currentPage].accentColor, pages[pagerState.currentPage - 1].accentColor, -fraction)
                else -> pages[pagerState.currentPage].accentColor
            }

            Button(
                onClick = {
                    val next = (pagerState.currentPage + 1).coerceAtMost(pages.size - 1)
                    scope.launch { pagerState.animateScrollToPage(next) }
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) {
                        "Get started"
                    } else {
                        "Continue"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun PagerState.pageOffset(page: Int): Float {
    return (currentPage - page + currentPageOffsetFraction).coerceIn(-1f, 1f)
}

@Composable
fun OnboardingPage(page: Int, pagerState: PagerState) {
    val data = pages[page]
    val offset = pagerState.pageOffset(page)

    Box(modifier = Modifier
        .fillMaxSize()
        .clip(RectangleShape)
        .background(data.bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blobAlpha = (1f - offset.absoluteValue).coerceIn(0f, 1f)
            data.blobs.forEach { (xFrac, yFrac, rFrac) ->
                val cx = size.width * xFrac + offset * size.width * 0.15f
                val cy = size.height * yFrac
                val r = size.width * rFrac

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            data.accentColor.copy(alpha = 0.25f * blobAlpha),
                            data.accentColor.copy(alpha = 0f)
                        ),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            data.shapes.forEachIndexed { i, (xFrac, yFrac) ->
                val parallaxX = offset * size.width * 0.35f
                val cx = size.width * xFrac + parallaxX
                val cy = size.height * yFrac
                val baseR = (28f + i * 14f).dp.toPx()
                val alpha = (1f - offset.absoluteValue).coerceIn(0f, 1f)

                if (i % 2 == 0) {
                    drawCircle(
                        color = data.shapeColor.copy(alpha = alpha * 0.5f),
                        radius = baseR,
                        center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx()
                        )
                    )
                    drawCircle(
                        color = data.shapeColor.copy(alpha = alpha * 0.15f),
                        radius = baseR * 0.55f,
                        center = Offset(cx, cy)
                    )
                } else {
                    val path = Path().apply {
                        moveTo(cx, cy - baseR)
                        lineTo(cx + baseR * 0.6f, cy)
                        lineTo(cx, cy + baseR)
                        lineTo(cx - baseR * 0.6f, cy)
                        close()
                    }
                    drawPath(
                        path,
                        color = data.shapeColor.copy(alpha = alpha * 0.35f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.5.dp.toPx()
                        )
                    )
                }
            }
        }

        val centerParallax = offset * 0.55f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
                .offset(x = (-centerParallax * 80).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .alpha((1f - offset.absoluteValue * 1.2f).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                data.accentColor.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale((1f - offset.absoluteValue * 0.3f).coerceIn(0.7f, 1f))
                    .alpha((1f - offset.absoluteValue * 1.5f).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                data.accentColor.copy(alpha = 0.55f),
                                data.shapeColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }

        val textParallax = offset * 0.75f
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
                .padding(horizontal = 36.dp)
                .offset(x = (-textParallax * 60).dp)
                .alpha((1f - offset.absoluteValue * 2f).coerceIn(0f, 1f)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = data.title,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 44.sp,
                textAlign = TextAlign.Start
            )
            Text(
                text = data.subtitle,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 24.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}