package com.tsynytsyna.animations.git_diff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val FromColor = Color(0xFF2FBF71)
private val ToColor = Color(0xFFFF7A18)

private const val FROM = "United Tech"
private const val TO = "Zello"

@Composable
fun CareerSwapDemo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        GitDiffSwap(from = FROM, to = TO, fromColor = FromColor, toColor = ToColor)
    }
}
