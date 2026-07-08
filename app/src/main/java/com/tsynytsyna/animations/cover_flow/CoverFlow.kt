package com.tsynytsyna.animations.cover_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private const val MAX_ROTATION = 55f
private const val SCALE_DROP = 0.20f
private const val ALPHA_DROP = 0.40f
private const val OVERLAP = 0.52f
private const val MIN_SCALE = 0.72f
private const val MIN_ALPHA = 0.45f

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CoverFlow(
    photos: List<String>,
    modifier: Modifier = Modifier,
    cardSize: Dp = 240.dp,
    cardHeight: Dp = cardSize * 1.4f,
    reflection: Boolean = true,
    background: Color = Color.Transparent,
) {
    val count = photos.size
    val loopCount = Int.MAX_VALUE
    val startPage = remember(count) { (loopCount / 2).let { it - it % count } }
    val state = rememberPagerState(initialPage = startPage, pageCount = { loopCount })
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier.fillMaxSize()) {
        val side = ((maxWidth - cardSize) / 2).coerceAtLeast(0.dp)
        val halfCardPx = with(LocalDensity.current) { cardSize.toPx() / 2f }

        HorizontalPager(
            state = state,
            pageSize = PageSize.Fixed(cardSize),
            contentPadding = PaddingValues(horizontal = side),
            pageSpacing = 0.dp,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(halfCardPx) {
                    detectTapGestures { pos ->
                        val center = size.width / 2f
                        when {
                            pos.x < center - halfCardPx -> {
                                scope.launch { state.animateScrollToPage(state.currentPage - 1) }
                            }

                            pos.x > center + halfCardPx -> {
                                scope.launch { state.animateScrollToPage(state.currentPage + 1) }
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            val index = page % count

            Column(
                modifier = Modifier
                    .zIndex(-abs(state.currentPage - page).toFloat())
                    .graphicsLayer {
                        val offset = (state.currentPage - page) + state.currentPageOffsetFraction
                        val ax = abs(offset)
                        cameraDistance = 14f * density
                        rotationY = offset.coerceIn(-1f, 1f) * MAX_ROTATION
                        val scale = (1f - ax * SCALE_DROP).coerceAtLeast(MIN_SCALE)
                        scaleX = scale
                        scaleY = scale
                        alpha = (1f - ax * ALPHA_DROP).coerceAtLeast(MIN_ALPHA)
                        val shift = if (ax <= 1f) offset * OVERLAP
                        else offset.sign * (OVERLAP + (ax - 1f) * 0.16f)
                        translationX = shift * size.width
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .width(cardSize)
                        .height(cardHeight)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    GlideImage(
                        model = photos[index],
                        contentDescription = "Cover ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val ax = abs(
                                    (state.currentPage - page) + state.currentPageOffsetFraction
                                )
                                drawRect(
                                    Color.Black,
                                    alpha = (ax * 0.5f).coerceAtMost(0.55f),
                                )
                            },
                    )
                }

                if (reflection) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .width(cardSize)
                            .height(cardHeight * 0.5f)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        GlideImage(
                            model = photos[index],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleY = -1f
                                    alpha = 0.35f
                                },
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        1f to background,
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}