package com.tsynytsyna.animations.paralax_onboarding

import androidx.compose.ui.graphics.Color

data class BlobConfig(val xFrac: Float, val yFrac: Float, val radiusFrac: Float)
data class ShapeConfig(val xFrac: Float, val yFrac: Float)

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val bgColor: Color,
    val accentColor: Color,
    val shapeColor: Color,
    val blobs: List<BlobConfig>,
    val shapes: List<ShapeConfig>
)

val pages = listOf(
    OnboardingPage(
        title = "Design with\nintention",
        subtitle = "Every pixel has a purpose.\nEvery motion tells a story.",
        bgColor = Color(0xFF0F0A1E),
        accentColor = Color(0xFF7C3AED),
        shapeColor = Color(0xFF4F46E5),
        blobs = listOf(
            BlobConfig(0.2f, 0.25f, 0.45f),
            BlobConfig(0.8f, 0.6f, 0.35f),
            BlobConfig(0.5f, 0.85f, 0.28f)
        ),
        shapes = listOf(ShapeConfig(0.75f, 0.22f), ShapeConfig(0.15f, 0.65f), ShapeConfig(0.6f, 0.55f))
    ),
    OnboardingPage(
        title = "Move with\npurpose",
        subtitle = "Animation is not decoration.\nIt's communication.",
        bgColor = Color(0xFF0A1628),
        accentColor = Color(0xFF0EA5E9),
        shapeColor = Color(0xFF0284C7),
        blobs = listOf(
            BlobConfig(0.7f, 0.2f, 0.4f),
            BlobConfig(0.15f, 0.7f, 0.38f),
            BlobConfig(0.55f, 0.45f, 0.25f)
        ),
        shapes = listOf(ShapeConfig(0.2f, 0.18f), ShapeConfig(0.8f, 0.7f), ShapeConfig(0.45f, 0.6f))
    ),
    OnboardingPage(
        title = "Feel the\ndifference",
        subtitle = "Details create the experience.\nExperience creates emotion.",
        bgColor = Color(0xFF0A1A12),
        accentColor = Color(0xFF10B981),
        shapeColor = Color(0xFF059669),
        blobs = listOf(
            BlobConfig(0.3f, 0.3f, 0.42f),
            BlobConfig(0.75f, 0.65f, 0.36f),
            BlobConfig(0.1f, 0.8f, 0.3f)
        ),
        shapes = listOf(ShapeConfig(0.8f, 0.25f), ShapeConfig(0.25f, 0.72f), ShapeConfig(0.55f, 0.42f))
    )
)