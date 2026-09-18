package com.tsynytsyna.animations.polaroid_scatter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

private data class Photo(
    val url: String,
    val caption: String,
    val category: String,
)

private val photos = listOf(
    Photo("https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=85", "Yosemite Valley", "Nature"),
    Photo("https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=85", "Emerald Lake", "Nature"),
    Photo("https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=800&q=85", "Aurora Borealis", "Cosmos"),
    Photo("https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=800&q=85", "Misty Forest", "Nature"),
    Photo("https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=800&q=85", "Tokyo Neon Rain", "City"),
    Photo("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=85", "Alpine Alpenglow", "Nature"),
    Photo("https://images.unsplash.com/photo-1534430480872-3498386e7856?auto=format&fit=crop&w=800&q=85", "Manhattan Skyline", "City"),
    Photo("https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=85", "Milky Way Galaxy", "Cosmos"),
    Photo("https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=800&q=85", "Sahara Sunset", "Nature"),
    Photo("https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=800&q=85", "Cyber Alleyway", "City"),
    Photo("https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=800&q=85", "Stellar Nebula", "Cosmos"),
    Photo("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=85", "Pacific Breakers", "Nature"),
    Photo("https://images.unsplash.com/photo-1501594907352-04cda38ebc29?auto=format&fit=crop&w=800&q=85", "Golden Gate Mist", "City"),
    Photo("https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=800&q=85", "Deep Andromeda", "Cosmos"),
    Photo("https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=800&q=85", "Antelope Sunbeam", "Nature"),
    Photo("https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=85", "Shibuya Crossing", "City"),
    Photo("https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=800&q=85", "Twilight Ocean", "Nature"),
    Photo("https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=85", "Prismatic Light", "Cosmos"),
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PolaroidScatterDemo(modifier: Modifier = Modifier) {
    PolaroidScatter(
        items = photos,
        sortOptions = listOf(
            SortOption("All") { it },
            SortOption("🌿 Nature") { list -> list.sortedByDescending { it.category == "Nature" } },
            SortOption("🏙️ City") { list -> list.sortedByDescending { it.category == "City" } },
            SortOption("✨ Cosmos") { list -> list.sortedByDescending { it.category == "Cosmos" } },
            SortOption("🎲 Shuffle") { list -> list.shuffled() },
        ),
        modifier = modifier,
        itemKey = { it.url }
    ) { photo ->
        GlideImage(
            model = photo.url,
            contentDescription = photo.caption,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
