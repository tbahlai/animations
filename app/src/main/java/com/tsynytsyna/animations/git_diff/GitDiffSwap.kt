package com.tsynytsyna.animations.git_diff

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val CYCLE_MILLIS = 7000
private const val PAUSE_MILLIS = 1400L

// Beats of the cycle, as fractions of it.
private const val MARK_FROM = 0.10f
private const val MARK_TO = 0.20f
private const val OPEN_FROM = 0.26f
private const val OPEN_TO = 0.36f
private const val TYPE_FROM = 0.36f
private const val TYPE_TO = 0.62f
private const val LIFT_FROM = 0.68f
private const val LIFT_TO = 0.78f
private const val COMMIT_FROM = 0.82f
private const val COMMIT_TO = 0.98f
private const val LIFT_FADE = 0.65f

private const val COMMIT_HASH = "7f3a2b1"

private val Editor = Color(0xFF0D1117)
private val Chrome = Color(0xFF161B22)
private val Gutter = Color(0xFF484F58)
private val Punctuation = Color(0xFFC9D1D9)
private val Keyword = Color(0xFFFF7B72)
private val Property = Color(0xFF79C0FF)
private val TypeName = Color(0xFFFFA657)
private val Comment = Color(0xFF8B949E)

private val LiftedTint = Color(0x14FFFFFF)
private val AddedTint = Color(0x2E3FB950)
private val AddedMark = Color(0xFF3FB950)

private val WindowDots = listOf(Color(0xFFFF5F57), Color(0xFFFEBC2E), Color(0xFF28C840))

private val Mono = FontFamily.Monospace

private const val MIN_CODE_SIZE = 10f
private const val MAX_CODE_SIZE = 22f

private val ProbeSize = 20.sp

private const val FIT_SAFETY = 0.97f
private const val INDENT = 4

private val EditorPadding = 14.dp
private val GutterWidth = 24.dp

private class CodeStyle(val fontSize: TextUnit, val lineHeight: Dp) {
    val statSize = fontSize * 0.82f
    val caretWidth = lineHeight * 0.2f
    val caretHeight = lineHeight * 0.55f
    val blockGap = lineHeight * 0.6f
}

/**
 * Swaps [from] for [to] as a diff in a source file. Every string is a parameter, so the same
 * component tells any "this replaced that" story.
 *
 * ```
 * GitDiffSwap(
 *     from = "United Tech",
 *     to = "Zello",
 *     fromColor = Color(0xFF2FBF71),
 *     toColor = Color(0xFFFF7A18),
 * )
 * ```
 */
@Composable
fun GitDiffSwap(
    from: String,
    to: String,
    fromColor: Color,
    toColor: Color,
    modifier: Modifier = Modifier,
    fileName: String = "career.kt",
    className: String = "Career",
    propertyName: String = "company",
    role: String = "Android Engineer",
    commitMessage: String = "feat: new chapter",
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val progress = remember { Animatable(0f) }
    val blink by rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1060, easing = LinearEasing), RepeatMode.Restart),
        label = "blink",
    )

    LaunchedEffect(Unit) {
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(CYCLE_MILLIS, easing = LinearEasing))
            delay(PAUSE_MILLIS.milliseconds)
        }
    }

    val time = progress.value
    val marked = span(MARK_FROM, MARK_TO, time)
    val opened = span(OPEN_FROM, OPEN_TO, time)
    val typed = span(TYPE_FROM, TYPE_TO, time)
    val lifted = span(LIFT_FROM, LIFT_TO, time)
    val committed = span(COMMIT_FROM, COMMIT_TO, time)

    val indent = " ".repeat(INDENT)
    val lines = remember(className, role, propertyName, from, to, commitMessage) {
        listOf(
            "class $className {",
            "${indent}val role = \"$role\"",
            "${indent}val $propertyName = \"$from\"",
            "${indent}val $propertyName = \"$to\"",
            "$ git commit -m \"$commitMessage\"",
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Editor)
            .padding(horizontal = EditorPadding),
    ) {
        val style = remember(maxWidth, lines, density) {
            fitCode(maxWidth - GutterWidth, lines, density, measurer)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            FileTab(fileName, style)
            Spacer(Modifier.height(style.blockGap))

            CodeLine("1", classHeader(className), style)
            CodeLine("2", property("role", role, Comment), style)

            CodeLine(
                number = "3",
                text = property(propertyName, from, fromColor),
                style = style,
                mark = if (marked > 0f) "–" else null,
                markColor = Comment,
                tint = LiftedTint.copy(alpha = LiftedTint.alpha * marked * (1f - lifted)),
                fade = 1f - lifted * LIFT_FADE,
                scale = 1f - lifted,
            )

            CodeLine(
                number = "4",
                text = property(propertyName, to.take((to.length * typed).toInt()), toColor),
                style = style,
                mark = if (opened > 0f) "+" else null,
                markColor = AddedMark,
                tint = AddedTint.copy(alpha = AddedTint.alpha * opened * (1f - committed)),
                caret = typed > 0f && typed < 1f && blink < 0.5f,
                scale = opened,
            )

            CodeLine("5", AnnotatedString("}", SpanStyle(color = Punctuation)), style)

            Spacer(Modifier.height(style.blockGap * 1.6f))
            Terminal(committed = committed, message = commitMessage, style = style)
        }
    }
}

private fun fitCode(
    available: Dp,
    lines: List<String>,
    density: Density,
    measurer: TextMeasurer,
): CodeStyle {
    val probeStyle = TextStyle(fontFamily = Mono, fontSize = ProbeSize)
    val widest = with(density) {
        lines.maxOf { measurer.measure(it, probeStyle).size.width }.toDp()
    }
    val fitted = ProbeSize.value * (available.value / widest.value) * FIT_SAFETY
    val fontSize = fitted.coerceIn(MIN_CODE_SIZE, MAX_CODE_SIZE)
    return CodeStyle(fontSize = fontSize.sp, lineHeight = (fontSize * 2.1f).dp)
}

@Composable
private fun FileTab(fileName: String, style: CodeStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        WindowDots.forEach { dot ->
            Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(7.dp))
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Chrome)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(fileName, color = Punctuation, fontFamily = Mono, fontSize = style.statSize)
        }
    }
}

@Composable
private fun CodeLine(
    number: String,
    text: AnnotatedString,
    style: CodeStyle,
    mark: String? = null,
    markColor: Color = Gutter,
    tint: Color = Color.Transparent,
    caret: Boolean = false,
    fade: Float = 1f,
    scale: Float = 1f,
) {
    if (scale <= 0f) return

    Box(
        Modifier
            .fillMaxWidth()
            .height(style.lineHeight * scale)
            .background(tint)
            .alpha(fade),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = mark ?: number,
                color = if (mark != null) markColor else Gutter,
                fontFamily = Mono,
                fontSize = style.fontSize,
                fontWeight = if (mark != null) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.width(GutterWidth),
            )
            Text(
                text = text,
                fontFamily = Mono,
                fontSize = style.fontSize,
                maxLines = 1,
                softWrap = false,
            )
            if (caret) {
                Box(
                    Modifier
                        .padding(start = 1.dp)
                        .size(width = style.caretWidth, height = style.caretHeight)
                        .background(Punctuation),
                )
            }
        }
    }
}

@Composable
private fun Terminal(committed: Float, message: String, style: CodeStyle) {
    val command = "git commit -m \"$message\""
    Column {
        Row {
            Text("$ ", color = AddedMark, fontFamily = Mono, fontSize = style.fontSize)
            Text(
                text = command.take((command.length * committed).toInt()),
                color = Punctuation,
                fontFamily = Mono,
                fontSize = style.fontSize,
                maxLines = 1,
                softWrap = false,
            )
        }
        if (committed >= 1f) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "[main $COMMIT_HASH] 1 file changed",
                color = Comment,
                fontFamily = Mono,
                fontSize = style.statSize,
            )
        }
    }
}

private fun classHeader(className: String) = buildAnnotatedString {
    withStyle(SpanStyle(color = Keyword)) { append("class ") }
    withStyle(SpanStyle(color = TypeName)) { append(className) }
    withStyle(SpanStyle(color = Punctuation)) { append(" {") }
}

private fun property(name: String, value: String, valueColor: Color) = buildAnnotatedString {
    withStyle(SpanStyle(color = Punctuation)) { append(" ".repeat(INDENT)) }
    withStyle(SpanStyle(color = Keyword)) { append("val ") }
    withStyle(SpanStyle(color = Property)) { append(name) }
    withStyle(SpanStyle(color = Punctuation)) { append(" = ") }
    withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.Bold)) { append("\"$value\"") }
}

private fun span(from: Float, to: Float, value: Float) =
    ((value - from) / (to - from)).coerceIn(0f, 1f)