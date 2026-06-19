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
    Photo("https://picsum.photos/id/29/400/400", "Mountain view", "Nature"),
    Photo("https://picsum.photos/id/8/400/400", "Autumn walk", "Nature"),
    Photo("https://picsum.photos/id/169/400/400", "Ocean breeze", "Nature"),
    Photo("https://picsum.photos/id/40/400/400", "Green hills", "Nature"),
    Photo("https://picsum.photos/id/15/400/400", "Golden hour", "Nature"),
    Photo("https://picsum.photos/id/119/400/400", "Night sky", "City"),
    Photo("https://picsum.photos/id/180/400/400", "Forest path", "Nature"),
    Photo("https://picsum.photos/id/209/400/400", "City lights", "City"),
    Photo("https://picsum.photos/id/237/400/400", "Good boy", "Animals"),
    Photo("https://picsum.photos/id/100/400/400", "Adventure", "City"),
    Photo("https://picsum.photos/id/160/400/400", "Calm waters", "Nature"),
    Photo("https://picsum.photos/id/56/400/400", "Warm sand", "Nature"),
    Photo("https://picsum.photos/id/65/400/400", "Misty lake", "Nature"),
    Photo("https://picsum.photos/id/76/400/400", "Wild flower", "Nature"),
    Photo("https://picsum.photos/id/110/400/400", "Road trip", "City"),
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
