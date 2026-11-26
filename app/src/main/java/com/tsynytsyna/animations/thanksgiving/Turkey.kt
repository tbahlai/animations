package com.tsynytsyna.animations.thanksgiving

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

internal val Thanks = listOf(
    "Family", "Friends", "Health", "Home", "Love", "Laughter", "Good food", "Warmth", "You",
)
internal val FEATHER_COUNT = Thanks.size
internal val CENTRE = (FEATHER_COUNT - 1) / 2f

// Fan
internal const val FAN_DEGREES = 160f
internal const val FOLDED_DEGREES = 14f
internal const val FOLDED_LENGTH = 0.62f
private const val SPRING_STIFFNESS = 70f
private const val SPRING_DAMPING = 13.4f // damping ratio ~0.8
private const val SPRING_SUBSTEPS = 4
private const val BREATH = 0.012f
private const val BREATH_SPEED = 1.4f
private const val OPEN_STAGGER = 0.05f
private const val CLOSE_STAGGER = 0.04f

// Pulling a feather; lengths are fractions of a feather
private const val MAX_STRETCH = 0.16f
private const val PLUCK_PULL = 0.3f
private const val BEND_FOLLOW = 0.55f
private const val MAX_BEND = 14f
private val NEIGHBOUR_PULL = listOf(1f, 0.35f, 0.12f)
private const val HELD_STIFFNESS = 500f
private const val HELD_DAMPING = 40f
private const val SNAP_STIFFNESS = 260f
private const val SNAP_DAMPING = 11f
private const val FLICK = 160f

// Pop and puff
private const val POP_SPEED = 1500f
private const val POP_DRAG = 5f
internal const val POP_SECONDS = 0.32f
private const val DOWN_WISPS = 26
private const val DOWN_SPEED = 520f
internal const val DOWN_SECONDS = 1.1f

// The turkey itself
internal const val GOBBLE_SECONDS = 0.7f
private const val OUCH_SECONDS = 0.5f
private const val LABEL_SECONDS = 1.2f
private const val WORD_SECONDS = 1.6f
private const val DOUBLE_TAP_MILLIS = 350L
private const val LOOK_RATE = 9f
private const val STRUT_SECONDS = 2.4f
private const val STRUT_PUFF = 0.08f
private const val STRUT_STEP_SECONDS = 1.2f
private const val RIPPLE_DEGREES = 5f
private const val RIPPLE_SPEED = 14f
private const val RIPPLE_SPREAD = 0.9f

internal class Feather(val index: Int) {
    val openAngle = -FAN_DEGREES / 2f + index * FAN_DEGREES / (FEATHER_COUNT - 1)
    val foldedAngle = -FOLDED_DEGREES / 2f + index * FOLDED_DEGREES / (FEATHER_COUNT - 1)

    var progress = 0f
    var velocity = 0f
    var target = 0f
    var delay = 0f
    var plucked = false

    var bend = 0f
    var bendVelocity = 0f
    var bendTarget = 0f

    var stretch = 0f
    var stretchVelocity = 0f
    var stretchTarget = 0f

    var ripple = 0f

    fun drawAngle(time: Float) = angle(time) + bend + ripple

    fun angle(time: Float): Float {
        val breath = 1f + BREATH * sin(time * BREATH_SPEED) * progress.coerceIn(0f, 1f)
        return (foldedAngle + (openAngle - foldedAngle) * progress) * breath
    }

    // Length doesn't follow the spring's overshoot, only the angle does.
    fun lengthFactor() = FOLDED_LENGTH + (1f - FOLDED_LENGTH) * progress.coerceIn(0f, 1f)
}

internal class PoppedFeather(
    val style: FeatherStyle,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    val spin: Float,
    val lengthFactor: Float,
) {
    var age = 0f
    val progress get() = (age / POP_SECONDS).coerceIn(0f, 1f)
}

internal class Wisp(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var angle: Float,
    val spin: Float,
    val size: Float,
    val color: Color,
    val phase: Float,
) {
    var age = 0f
}

internal class FloatingLabel(
    val label: TextLayoutResult,
    var x: Float,
    var y: Float,
    val life: Float = LABEL_SECONDS,
    var age: Float = 0f,
)

internal class TurkeyLabels(
    val gobble: TextLayoutResult,
    val ouch: TextLayoutResult,
    val words: List<TextLayoutResult>,
)

internal class Turkey(
    val geometry: TurkeyGeometry,
    private val styles: List<FeatherStyle>,
    private val labels: TurkeyLabels,
) {
    val feathers = List(FEATHER_COUNT) { Feather(it) }
    val drawOrder = feathers.sortedByDescending { abs(it.index - CENTRE) }
    val popped = ArrayList<PoppedFeather>()
    val wisps = ArrayList<Wisp>()
    val floating = ArrayList<FloatingLabel>()

    var time = 0f
        private set
    var gobbleLeft = 0f
        private set
    var ouchLeft = 0f
        private set
    var tension = 0f
        private set
    var look = Offset(0f, 0.3f)
        private set

    var isOpen by mutableStateOf(false)
        private set
    var remaining by mutableIntStateOf(FEATHER_COUNT)
        private set

    private var held: Feather? = null
    private var grabDistance = 0f
    private var grabAngle = 0f
    private var fingerVelocity = Offset.Zero
    private var lastTurkeyTap = -1_000L
    private var strutTime = -1f
    private val random = Random(2026)

    private val strutBell
        get() = if (strutTime < 0f) 0f else sin(PI.toFloat() * strutTime / STRUT_SECONDS)
    private val stepPhase get() = 2f * PI.toFloat() * strutTime / STRUT_STEP_SECONDS

    val puff get() = 1f + STRUT_PUFF * strutBell
    val sway get() = sin(stepPhase) * 0.035f * geometry.unit * strutBell

    fun footLift(side: Float) =
        (-side * sin(stepPhase)).coerceAtLeast(0f) * 0.04f * geometry.unit * strutBell

    fun open() {
        if (remaining == 0) {
            regrow()
            return
        }
        if (isOpen) return
        isOpen = true
        feathers.forEach {
            it.target = 1f
            it.delay = abs(it.index - CENTRE) * OPEN_STAGGER
        }
    }

    fun close() {
        if (!isOpen) return
        release(wasTap = false)
        isOpen = false
        feathers.forEach {
            it.target = 0f
            it.delay = (CENTRE - abs(it.index - CENTRE)) * CLOSE_STAGGER
        }
    }

    fun regrow() {
        feathers.forEach {
            it.plucked = false
            it.progress = 0f
            it.velocity = 0f
            it.bend = 0f
            it.bendVelocity = 0f
            it.bendTarget = 0f
            it.stretch = 0f
            it.stretchVelocity = 0f
            it.stretchTarget = 0f
        }
        popped.clear()
        wisps.clear()
        floating.clear()
        held = null
        tension = 0f
        remaining = FEATHER_COUNT
        isOpen = false
        open()
    }

    fun step(dt: Float) {
        time += dt
        gobbleLeft = (gobbleLeft - dt).coerceAtLeast(0f)
        ouchLeft = (ouchLeft - dt).coerceAtLeast(0f)
        if (strutTime >= 0f) {
            strutTime += dt
            if (strutTime > STRUT_SECONDS) strutTime = -1f
        }

        updateLook(dt)
        stepFeathers(dt)
        stepPopped(dt)

        floating.forEach {
            it.age += dt
            it.y -= 90f * geometry.scale * dt
        }
        floating.removeAll { it.age > it.life }
    }

    private fun updateLook(dt: Float) {
        val holding = held
        val watched = if (holding != null) {
            val radians = holding.drawAngle(time) / 180f * PI.toFloat()
            val reach = geometry.unit * holding.lengthFactor() * 0.8f + holding.stretch
            geometry.pivot + Offset(sin(radians), -cos(radians)) * reach
        } else {
            popped.lastOrNull()?.let { Offset(it.x, it.y) }
        }
        val target = if (watched == null) {
            Offset(0f, 0.3f)
        } else {
            val away = watched - geometry.headCentre
            away / away.getDistance().coerceAtLeast(1f)
        }
        look += (target - look) * (1f - exp(-LOOK_RATE * dt))
    }

    private fun stepFeathers(dt: Float) {
        val h = dt / SPRING_SUBSTEPS
        feathers.forEach { feather ->
            feather.ripple = if (strutTime < 0f) {
                0f
            } else {
                RIPPLE_DEGREES * strutBell * sin(strutTime * RIPPLE_SPEED - feather.index * RIPPLE_SPREAD)
            }

            val stiffness = if (feather === held) HELD_STIFFNESS else SNAP_STIFFNESS
            val damping = if (feather === held) HELD_DAMPING else SNAP_DAMPING
            repeat(SPRING_SUBSTEPS) {
                feather.bendVelocity += (stiffness * (feather.bendTarget - feather.bend) - damping * feather.bendVelocity) * h
                feather.bend += feather.bendVelocity * h
                feather.stretchVelocity += (stiffness * (feather.stretchTarget - feather.stretch) - damping * feather.stretchVelocity) * h
                feather.stretch += feather.stretchVelocity * h
            }

            if (feather.delay > 0f) {
                feather.delay -= dt
                return@forEach
            }
            repeat(SPRING_SUBSTEPS) {
                feather.velocity += (SPRING_STIFFNESS * (feather.target - feather.progress) - SPRING_DAMPING * feather.velocity) * h
                feather.progress += feather.velocity * h
            }
        }
    }

    private fun stepPopped(dt: Float) {
        val s = geometry.scale
        val drag = exp(-POP_DRAG * dt)
        val iterator = popped.iterator()
        while (iterator.hasNext()) {
            val f = iterator.next()
            f.age += dt
            f.vx *= drag
            f.vy *= drag
            f.x += f.vx * dt
            f.y += f.vy * dt
            f.angle += f.spin * dt
            if (f.age >= POP_SECONDS) {
                burst(f)
                iterator.remove()
            }
        }

        val airDrag = exp(-2.2f * dt)
        wisps.forEach { w ->
            w.age += dt
            w.vx *= airDrag
            w.vy = w.vy * airDrag + 60f * s * dt
            w.x += (w.vx + sin(w.age * 5f + w.phase) * 40f * s) * dt
            w.y += w.vy * dt
            w.angle += w.spin * dt
        }
        wisps.removeAll { it.age > DOWN_SECONDS }
    }

    private fun burst(f: PoppedFeather) {
        val s = geometry.scale
        repeat(DOWN_WISPS) {
            val a = random.range(0f, 2f * PI.toFloat())
            val speed = random.range(0.3f, 1f) * DOWN_SPEED * s
            wisps += Wisp(
                x = f.x + random.range(-30f, 30f) * s,
                y = f.y + random.range(-30f, 30f) * s,
                vx = cos(a) * speed + f.vx * 0.3f,
                vy = sin(a) * speed - 120f * s,
                angle = random.range(0f, 360f),
                spin = random.range(-200f, 200f),
                size = random.range(26f, 46f) * s,
                color = if (it % 3 == 0) FeatherTip else lerp(
                    f.style.color,
                    Color.White,
                    random.range(0.1f, 0.4f)
                ),
                phase = random.range(0f, 6.28f),
            )
        }
    }

    fun grab(point: Offset): Boolean {
        if (!isOpen) return false
        val feather = featherAt(point) ?: return false
        held = feather
        grabDistance = (point - geometry.pivot).getDistance()
        grabAngle = polarAngle(point)
        fingerVelocity = Offset.Zero
        return true
    }

    fun dragTo(point: Offset, velocity: Offset) {
        val feather = held ?: return
        fingerVelocity = velocity

        val u = geometry.unit
        val pull = ((point - geometry.pivot).getDistance() - grabDistance).coerceAtLeast(0f)
        val maxStretch = MAX_STRETCH * u
        val stretch = maxStretch * (1f - exp(-pull / maxStretch))
        val bend =
            (angleBetween(polarAngle(point), grabAngle) * BEND_FOLLOW).coerceIn(-MAX_BEND, MAX_BEND)
        tension = (pull / (PLUCK_PULL * u)).coerceIn(0f, 1f)

        feathers.forEach {
            val apart = abs(it.index - feather.index)
            val share = NEIGHBOUR_PULL.getOrElse(apart) { 0f }
            it.bendTarget = bend * share
            it.stretchTarget = stretch * share * if (apart == 0) 1f else 0.6f
        }

        if (pull >= PLUCK_PULL * u) pop(feather)
    }

    fun release(wasTap: Boolean) {
        val feather = held ?: return
        held = null
        tension = 0f
        feathers.forEach {
            it.bendTarget = 0f
            it.stretchTarget = 0f
        }
        if (wasTap) feather.bendVelocity += FLICK * if (random.nextBoolean()) 1f else -1f
    }

    fun tap(point: Offset, uptimeMillis: Long) {
        if (!geometry.hitsTurkey(point)) return
        if (uptimeMillis - lastTurkeyTap < DOUBLE_TAP_MILLIS) strutTime = 0f else gobble()
        lastTurkeyTap = uptimeMillis
    }

    private fun gobble() {
        gobbleLeft = GOBBLE_SECONDS
        say(labels.gobble)
    }

    private fun say(label: TextLayoutResult) {
        floating += FloatingLabel(
            label = label,
            x = geometry.headCentre.x + geometry.headRadius * 2.2f,
            y = geometry.headCentre.y - geometry.headRadius,
        )
    }

    private fun pop(feather: Feather) {
        feather.plucked = true
        remaining -= 1

        val angle = feather.drawAngle(time)
        val radians = angle / 180f * PI.toFloat()
        val direction = Offset(sin(radians), -cos(radians))
        val lengthFactor = feather.lengthFactor()
        val centre =
            geometry.pivot + direction * (feather.stretch + geometry.unit * lengthFactor / 2f)
        val launch = direction * (POP_SPEED * geometry.scale) + fingerVelocity * 0.3f

        popped += PoppedFeather(
            style = styles[feather.index],
            x = centre.x,
            y = centre.y,
            vx = launch.x,
            vy = launch.y,
            angle = angle,
            spin = (420f + random.range(0f, 240f)) * if (direction.x >= 0f) 1f else -1f,
            lengthFactor = lengthFactor,
        )
        floating += FloatingLabel(
            label = labels.words[feather.index],
            x = centre.x + direction.x * geometry.unit * 0.25f,
            y = centre.y + direction.y * geometry.unit * 0.25f,
            life = WORD_SECONDS,
        )

        release(wasTap = false)
        ouchLeft = OUCH_SECONDS
        say(labels.ouch)
    }

    private fun featherAt(point: Offset): Feather? {
        val distance = (point - geometry.pivot).getDistance()
        val pointAngle = polarAngle(point)
        val hit = feathers
            .filter { !it.plucked && it.progress > 0.5f }
            .filter { distance in geometry.unit * 0.3f..geometry.unit * it.lengthFactor() + it.stretch }
            .minByOrNull { abs(angleBetween(pointAngle, it.drawAngle(time))) }
            ?: return null
        val halfWidthDegrees =
            atan(geometry.featherHalfWidth * hit.lengthFactor() / distance) * 180f / PI.toFloat()
        return hit.takeIf {
            abs(
                angleBetween(
                    pointAngle,
                    it.drawAngle(time)
                )
            ) <= halfWidthDegrees * 1.4f
        }
    }

    // 0 is straight up from the pivot, positive to the right.
    private fun polarAngle(point: Offset): Float {
        val d = point - geometry.pivot
        return atan2(d.x, -d.y) * 180f / PI.toFloat()
    }
}

internal fun buildLabels(
    geometry: TurkeyGeometry,
    density: Density,
    measurer: TextMeasurer
): TurkeyLabels {
    fun exclamation(text: String) = measurer.measure(
        text = text,
        style = TextStyle(
            color = LightInk,
            fontSize = with(density) { (geometry.unit * 0.09f).toSp() },
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
        ),
    )
    return TurkeyLabels(
        gobble = exclamation("gobble!"),
        ouch = exclamation("ouch!"),
        words = Thanks.map { word ->
            measurer.measure(
                text = word,
                style = TextStyle(
                    color = LightInk,
                    fontSize = with(density) { (geometry.unit * 0.11f).toSp() },
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                ),
            )
        },
    )
}

private fun angleBetween(a: Float, b: Float): Float = ((a - b) % 360f + 540f) % 360f - 180f

private fun Random.range(from: Float, until: Float) = from + nextFloat() * (until - from)
