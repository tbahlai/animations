package com.tsynytsyna.animations.off_the_wall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF08080A)
private val Panel = Color(0xFF141418)
private val Bone = Color(0xFFF0EDE7)
private val Muted = Color(0xFF75726D)
private val Accent = Color(0xFFD9704A)

private data class Track(val title: String, val artist: String, val length: String)

private val Tracks = listOf(
    Track("Slow Tide", "Vera Lune", "4:12"),
    Track("Paper Weather", "Oyster Club", "3:38"),
    Track("Thread Count", "Marlow", "5:01"),
    Track("Undercurrent", "Vera Lune", "3:55"),
    Track("Loom", "Kestrel Bay", "4:44"),
)

@Composable
fun OffTheWallDemo(modifier: Modifier = Modifier) {
    val state = rememberOffTheWallState()

    Box(modifier.fillMaxSize().background(Ink)) {
        BehindTheScreen()

        OffTheWall(
            state = state,
            modifier = Modifier.fillMaxSize(),
            columns = 22,
        ) {
            NowPlayingScreen()
        }

        Controls(
            state = state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 18.dp),
        )
    }
}

@Composable
private fun BehindTheScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101014))
            .systemBarsPadding()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "You pulled the screen\noff the wall.",
            color = Bone,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 38.sp,
        )
    }
}

@Composable
private fun NowPlayingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .systemBarsPadding()
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("NOW PLAYING", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
            Spacer(Modifier.weight(1f))
            Text("···", color = Muted, fontSize = 16.sp)
        }

        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF3B4A5A), Color(0xFF8A5238), Color(0xFF1B1F26))
                    )
                )
        )

        Spacer(Modifier.height(22.dp))
        Text("Slow Tide", color = Bone, fontSize = 26.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text("Vera Lune — Ganymede EP", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(Color(0xFF25252B))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.42f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Accent)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("1:44", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text("4:12", color = Muted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(26.dp))
        Text("UP NEXT", color = Muted, fontSize = 10.sp, letterSpacing = 2.4.sp)
        Spacer(Modifier.height(12.dp))
        Tracks.drop(1).forEach { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Panel)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(track.title, color = Bone, fontSize = 14.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(track.artist, color = Muted, fontSize = 11.sp)
                }
                Text(track.length, color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Controls(state: OffTheWallState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Pill(label = "PULL THE PINS", enabled = state.pinned) { state.drop() }
            Pill(label = "RESET", enabled = state.awake || state.dismissed) { state.reset() }
        }
    }
}

@Composable
private fun Pill(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (enabled) Color(0xFF1D1D22) else Color(0xFF131317))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) Bone else Muted,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}