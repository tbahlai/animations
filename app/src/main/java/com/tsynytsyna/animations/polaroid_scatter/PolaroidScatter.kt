package com.tsynytsyna.animations.polaroid_scatter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SortOption<T>(
    val label: String,
    val sort: (List<T>) -> List<T>,
)

private data class GridLayout(
    val cardWidthPx: Float,
    val cardHeightPx: Float,
    val cardWidthDp: Dp,
    val cardHeightDp: Dp,
    val gridStartX: Float,
    val gridStartY: Float,
    val gridSpacing: Float,
)

@Composable
fun <T : Any> PolaroidScatter(
    items: List<T>,
    sortOptions: List<SortOption<T>>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    backgroundColor: Color = Color(0xFFF2F0E8),
    cardColor: Color = Color.White,
    itemKey: (T) -> Any = { it },
    content: @Composable (T) -> Unit,
) {
    var selectedSort by remember { mutableIntStateOf(0) }
    var isScattered by remember { mutableStateOf(true) }
    var scatterKey by remember { mutableIntStateOf(0) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val sortedItems = remember(selectedSort, scatterKey) {
        sortOptions[selectedSort].sort(items)
    }

    LaunchedEffect(selectedSort, scatterKey) {
        isScattered = true
        delay(100)
        isScattered = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onSizeChanged { containerSize = it }
    ) {
        if (containerSize == IntSize.Zero) return@Box

        val density = LocalDensity.current
        val layout = remember(containerSize, sortedItems.size, columns) {
            calculateGridLayout(containerSize, sortedItems.size, columns, density)
        }

        val scatterPositions = remember(selectedSort, scatterKey) {
            generateScatterPositions(
                count = sortedItems.size,
                screenW = containerSize.width.toFloat(),
                screenH = containerSize.height.toFloat(),
                cardWidthPx = layout.cardWidthPx,
                cardHeightPx = layout.cardHeightPx
            )
        }

        SortChips(
            options = sortOptions,
            selectedIndex = selectedSort,
            onSelect = { index ->
                if (selectedSort != index) {
                    selectedSort = index
                    scatterKey++
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        sortedItems.forEachIndexed { index, item ->
            key(itemKey(item)) {
                PolaroidCard(
                    index = index,
                    item = item,
                    isScattered = isScattered,
                    layout = layout,
                    scatterPosition = scatterPositions[index],
                    totalItems = sortedItems.size,
                    columns = columns,
                    cardColor = cardColor,
                    content = content
                )
            }
        }
    }
}

@Composable
private fun <T> SortChips(
    options: List<SortOption<T>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        options.forEachIndexed { index, option ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                label = { Text(option.label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF333333),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun <T> PolaroidCard(
    index: Int,
    item: T,
    isScattered: Boolean,
    layout: GridLayout,
    scatterPosition: Triple<Float, Float, Float>,
    totalItems: Int,
    columns: Int,
    cardColor: Color,
    content: @Composable (T) -> Unit,
) {
    val col = index % columns
    val row = index / columns

    val gridX = layout.gridStartX + col * (layout.cardWidthPx + layout.gridSpacing)
    val gridY = layout.gridStartY + row * (layout.cardHeightPx + layout.gridSpacing)

    val (scatterX, scatterY, scatterRot) = scatterPosition

    val targetX = if (isScattered) scatterX else gridX
    val targetY = if (isScattered) scatterY else gridY
    val targetRotation = if (isScattered) scatterRot else 0f

    val stiffness = Spring.StiffnessLow + index * 4f

    val positionAnim = remember { Animatable(Offset(targetX, targetY), Offset.VectorConverter) }
    val rotationAnim = remember { Animatable(targetRotation) }

    LaunchedEffect(targetX, targetY, targetRotation) {
        launch {
            positionAnim.animateTo(
                Offset(targetX, targetY),
                spring(dampingRatio = 0.7f, stiffness = stiffness)
            )
        }
        launch {
            rotationAnim.animateTo(
                targetRotation,
                spring(dampingRatio = 0.6f, stiffness = stiffness)
            )
        }
    }

    val pos = positionAnim.value
    val rot = rotationAnim.value

    Box(
        modifier = Modifier
            .offset { IntOffset(pos.x.toInt(), pos.y.toInt()) }
            .zIndex(if (isScattered) (totalItems - index).toFloat() else 0f)
            .graphicsLayer { rotationZ = rot }
            .size(width = layout.cardWidthDp, height = layout.cardHeightDp)
            .polaroidShadow()
            .background(cardColor, RoundedCornerShape(4.dp))
            .padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 18.dp)
    ) {
        content(item)
    }
}

private fun calculateGridLayout(
    containerSize: IntSize,
    itemCount: Int,
    columns: Int,
    density: Density,
): GridLayout {
    val screenW = containerSize.width.toFloat()
    val screenH = containerSize.height.toFloat()

    val chipBarHeight = with(density) { 56.dp.toPx() }
    val gridSpacing = with(density) { 8.dp.toPx() }
    val rows = (itemCount + columns - 1) / columns

    val availableW = screenW - gridSpacing * (columns + 1)
    val availableH = screenH - chipBarHeight - gridSpacing * (rows + 1)

    val cardWidthPx = availableW / columns
    val cardHeightPx = (cardWidthPx * 1.25f).coerceAtMost(availableH / rows)

    val gridTotalW = columns * cardWidthPx + (columns - 1) * gridSpacing
    val gridStartX = (screenW - gridTotalW) / 2f
    val gridTotalH = rows * cardHeightPx + (rows - 1) * gridSpacing
    val gridStartY = chipBarHeight + (screenH - chipBarHeight - gridTotalH) / 2f

    return GridLayout(
        cardWidthPx = cardWidthPx,
        cardHeightPx = cardHeightPx,
        cardWidthDp = with(density) { cardWidthPx.toDp() },
        cardHeightDp = with(density) { cardHeightPx.toDp() },
        gridStartX = gridStartX,
        gridStartY = gridStartY,
        gridSpacing = gridSpacing,
    )
}

private fun generateScatterPositions(
    count: Int,
    screenW: Float,
    screenH: Float,
    cardWidthPx: Float,
    cardHeightPx: Float,
): List<Triple<Float, Float, Float>> {
    return List(count) {
        Triple(
            Random.nextFloat() * (screenW - cardWidthPx),
            Random.nextFloat() * (screenH - cardHeightPx * 1.2f) + screenH * 0.05f,
            Random.nextFloat() * 50f - 25f
        )
    }
}

private fun Modifier.polaroidShadow(): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = Color(0x44000000).toArgb()
                setShadowLayer(32f, 0f, 8f, Color(0x44000000).toArgb())
            }
        }
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f, top = 0f,
                    right = size.width, bottom = size.height,
                    cornerRadius = CornerRadius(8f, 8f)
                )
            )
        }
        canvas.drawPath(path, paint)
    }
}
