package com.tsynytsyna.animations.showcase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun ShowcaseHomeScreen(
    onSelectAnimation: (AnimationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf(AnimationCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(selectedCategory, searchQuery) {
        ShowcaseRegistry.items.filter { item ->
            val matchesCategory = (selectedCategory == AnimationCategory.ALL || item.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    val featuredItem = remember { ShowcaseRegistry.items.first { it.id == "off_the_wall" } }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val cutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val topInset = maxOf(statusBarPadding.calculateTopPadding(), cutoutPadding.calculateTopPadding())
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val bottomInset = navBarPadding.calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04060A),
                        Color(0xFF0A0F1D),
                        Color(0xFF04060A),
                    )
                )
            )
    ) {
        // Ambient chromatic background backlights
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.14f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.CenterStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFF5722).copy(alpha = 0.09f), Color.Transparent),
                    ),
                    shape = CircleShape,
                )
        )

        // Top status bar scrim with subtle blur gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topInset + 18.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04060A),
                            Color(0xFF04060A).copy(alpha = 0.95f),
                            Color.Transparent,
                        )
                    )
                )
                .zIndex(10f)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topInset + 16.dp,
                bottom = bottomInset + 36.dp,
                start = 18.dp,
                end = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header Section
            item(key = "header") {
                HeaderSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }

            // Spotlight Hero Section (only when no search is active)
            if (searchQuery.isBlank() && selectedCategory == AnimationCategory.ALL) {
                item(key = "spotlight_hero") {
                    SpotlightHeroCard(
                        item = featuredItem,
                        onLaunch = { onSelectAnimation(featuredItem) }
                    )
                }
            }

            // Specs Ticker Bar
            item(key = "specs_ticker") {
                SpecsTickerBar()
            }

            // Category Filter Row
            item(key = "categories") {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it }
                )
            }

            // Experiment Count & Filter Status
            item(key = "summary_counter") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF38BDF8), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${filteredItems.size} MOTION EXPERIMENTS",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp,
                        )
                    }

                    if (searchQuery.isNotBlank() || selectedCategory != AnimationCategory.ALL) {
                        Text(
                            text = "Reset Filter",
                            color = Color(0xFF818CF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                selectedCategory = AnimationCategory.ALL
                            }
                        )
                    }
                }
            }

            // Animation Items Cards with Live Previews
            items(
                items = filteredItems,
                key = { it.id }
            ) { item ->
                PremiumAnimationCard(
                    item = item,
                    onClick = { onSelectAnimation(item) }
                )
            }

            if (filteredItems.isEmpty()) {
                item(key = "empty_state") {
                    EmptyResultsView(
                        query = searchQuery,
                        onClear = {
                            searchQuery = ""
                            selectedCategory = AnimationCategory.ALL
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Tag Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF131B2E).copy(alpha = 0.85f), RoundedCornerShape(100.dp))
                .border(1.dp, Color(0xFF24324F), RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF10B981), CircleShape)
                    .shadow(4.dp, CircleShape, spotColor = Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "JETPACK COMPOSE • EXPERIMENTAL",
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Motion Lab",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Mass-spring cloth solvers, Voronoi fragmentation, and procedural Canvas shaders — built 100% by hand.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            lineHeight = 21.sp,
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search experiments, algorithms, tags...",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0B1120).copy(alpha = 0.85f),
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF818CF8),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun SpotlightHeroCard(
    item: AnimationItem,
    onLaunch: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "heroCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = item.accentColor.copy(alpha = 0.35f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onLaunch,
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1424)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                colors = listOf(
                    item.accentColor.copy(alpha = 0.7f),
                    Color(0xFF1E293B).copy(alpha = 0.5f),
                )
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hero Live Preview Canvas Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            ) {
                if (item.previewImageUrl.isNotBlank()) {
                    GlideImage(
                        model = item.previewImageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                ExperimentMiniPreview(
                    id = item.id,
                    accentColor = item.accentColor,
                    hasImageBackdrop = item.previewImageUrl.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                )

                // Top Badge: "SPOTLIGHT SIMULATION"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(100.dp))
                        .border(1.dp, item.accentColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(item.accentColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FLAGSHIP SOLVER",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                }

                // Gradient falloff into card body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0E1424))
                            )
                        )
                )
            }

            // Hero Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.subtitle,
                    color = item.accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Launch CTA Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    item.accentColor,
                                    item.accentColor.copy(alpha = 0.85f),
                                )
                            )
                        )
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Launch Live Simulation",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecsTickerBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF090E1A), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1A2640), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TickerItem("${ShowcaseRegistry.items.size}", "EXPERIMENTS")
        TickerDivider()
        TickerItem("120 Hz", "PROMOTION")
        TickerDivider()
        TickerItem("0 Libs", "PURE COMPOSE")
        TickerDivider()
        TickerItem("100%", "CANVAS MESH")
    }
}

@Composable
private fun TickerItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
private fun TickerDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Color(0xFF1E293B))
    )
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: AnimationCategory,
    onSelectCategory: (AnimationCategory) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(AnimationCategory.entries.toTypedArray()) { category ->
            val isSelected = category == selectedCategory
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF4F46E5) else Color(0xFF0F172A),
                label = "chipContainer"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF818CF8) else Color(0xFF1E293B),
                label = "chipBorder"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.label,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalGlideComposeApi::class)
@Composable
private fun PremiumAnimationCard(
    item: AnimationItem,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.982f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = item.accentColor.copy(alpha = 0.25f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1322)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.verticalGradient(
                colors = listOf(
                    item.accentColor.copy(alpha = 0.45f),
                    Color(0xFF1E293B).copy(alpha = 0.35f),
                )
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Live Mini-Canvas Preview Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            ) {
                if (item.previewImageUrl.isNotBlank()) {
                    GlideImage(
                        model = item.previewImageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                ExperimentMiniPreview(
                    id = item.id,
                    accentColor = item.accentColor,
                    hasImageBackdrop = item.previewImageUrl.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                )

                // Category pill on top of preview
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .border(1.dp, item.accentColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category.label,
                        color = item.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Interactive Launch Button on preview top right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(100.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Open",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "→",
                        color = item.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Bottom gradient blending into card content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0D1322))
                            )
                        )
                )
            }

            // Card Body Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.subtitle,
                    color = item.accentColor.copy(alpha = 0.95f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Algorithm Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF070B14), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ ${item.technicalBadge}",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tags FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF090E1A), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF1A2640), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = Color(0xFF64748B),
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

@Composable
private fun EmptyResultsView(
    query: String,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No experiments found",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "No matches for \"$query\"",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF334155))
                .clickable(onClick = onClear)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Clear Filter",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
