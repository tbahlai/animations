package com.tsynytsyna.animations.fan_cards

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private const val SPREAD = 32f
private const val PIVOT = 360f
private const val SELECTED_SCALE = 1.35f
private const val UNSELECTED_SCALE = 0.62f

private val cardSpring = spring<Float>(
    dampingRatio = 0.72f,
    stiffness = Spring.StiffnessMediumLow,
)

private class CardAnim(target: Target) {
    val rotation = Animatable(target.rotation)
    val translationX = Animatable(target.translationX)
    val translationY = Animatable(target.translationY)
    val scale = Animatable(target.scale)
    val alpha = Animatable(target.alpha)
}

private data class Target(
    val rotation: Float,
    val translationX: Float,
    val translationY: Float,
    val scale: Float,
    val alpha: Float,
)

private fun targetFor(pos: Int, size: Int, expanded: Boolean, selected: Int?): Target {
    val center = (size - 1) / 2f
    val off = pos - center

    return when {
        selected != null && pos == selected -> {
            Target(
                rotation = 0f,
                translationX = 0f,
                translationY = -24f,
                scale = SELECTED_SCALE,
                alpha = 1f
            )
        }

        selected != null -> {
            Target(
                rotation = off * 7f,
                translationX = off * 34f,
                translationY = -196f,
                scale = UNSELECTED_SCALE,
                alpha = 0.5f
            )
        }

        expanded -> {
            val t = if (size == 1) 0.5f else pos / (size - 1f)
            val angle = lerp(-SPREAD, SPREAD, t)
            val rad = angle * PI.toFloat() / 180f
            Target(
                rotation = angle,
                translationX = sin(rad) * PIVOT,
                translationY = (1f - cos(rad)) * PIVOT,
                scale = 1f,
                alpha = 1f,
            )
        }

        else -> {
            Target(
                rotation = off * 1.5f,
                translationX = off * 3f,
                translationY = off * -3f,
                scale = 1f,
                alpha = 1f
            )
        }
    }
}

private fun zFor(pos: Int, selected: Int?): Float = if (selected == pos) 1000f else pos.toFloat()

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FanCards(
    photos: List<String>,
    modifier: Modifier = Modifier,
) {
    val photosSize = photos.size
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Int?>(null) }

    val cards = remember(photosSize) {
        List(photosSize) { pos ->
            CardAnim(
                targetFor(pos = pos, size = photosSize, expanded = false, selected = null)
            )
        }
    }

    LaunchedEffect(expanded, selected, photosSize) {
        cards.forEachIndexed { index, card ->
            val target = targetFor(index, photosSize, expanded, selected)
            launch {
                if (selected == null) delay((index * 22L).milliseconds)
                card.rotation.animateTo(target.rotation, cardSpring)
            }
            launch {
                if (selected == null) delay((index * 22L).milliseconds)
                card.translationX.animateTo(target.translationX, cardSpring)
            }
            launch {
                if (selected == null) delay((index * 22L).milliseconds)
                card.translationY.animateTo(target.translationY, cardSpring)
            }
            launch { card.scale.animateTo(target.scale, cardSpring) }
            launch { card.alpha.animateTo(target.alpha, cardSpring) }
        }
    }

    BackHandler(enabled = expanded || selected != null) {
        if (selected != null) selected = null else expanded = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    when {
                        selected != null -> selected = null
                        expanded -> expanded = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        cards.forEachIndexed { index, card ->
            Box(
                Modifier
                    .zIndex(zFor(index, selected))
                    .size(230.dp, 305.dp)
                    .graphicsLayer {
                        rotationZ = card.rotation.value
                        translationX = card.translationX.value
                        translationY = card.translationY.value
                        scaleX = card.scale.value
                        scaleY = card.scale.value
                        alpha = card.alpha.value
                    }
                    .shadow(14.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        when {
                            !expanded -> expanded = true
                            selected == index -> selected = null
                            else -> selected = index
                        }
                    }
                    .padding(7.dp)
            ) {
                GlideImage(
                    model = photos[index],
                    contentDescription = "Card ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}