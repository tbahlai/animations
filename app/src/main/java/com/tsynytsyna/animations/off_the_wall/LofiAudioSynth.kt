package com.tsynytsyna.animations.off_the_wall

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Real-time Lofi Music & Vinyl Audio Engine.
 * Generates warm, chill ambient chords and vinyl crackle using Android's native AudioTrack
 * with zero audio assets or network downloads.
 */
class LofiAudioSynth {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private val isMuted = AtomicBoolean(false)

    // Warm Lofi Chord Progression (frequencies in Hz)
    // Fmaj7 -> Dm9 -> Gm7 -> C7sus4
    private val chords = listOf(
        floatArrayOf(174.61f, 220.00f, 261.63f, 329.63f), // F3, A3, C4, E4
        floatArrayOf(146.83f, 174.61f, 220.00f, 261.63f), // D3, F3, A3, C4
        floatArrayOf(196.00f, 233.08f, 293.66f, 349.23f), // G3, Bb3, D4, F4
        floatArrayOf(130.81f, 174.61f, 196.00f, 233.08f), // C3, F3, G3, Bb3
    )

    fun start(scope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Exception) {
            isRunning.set(false)
            return
        }

        synthJob = scope.launch(Dispatchers.Default) {
            val bufferSize = 2048
            val audioBuffer = ShortArray(bufferSize)
            var sampleIndex = 0L
            val chordDurationSamples = (sampleRate * 2.2).toLong() // ~2.2s per chord

            while (isActive && isRunning.get()) {
                if (isMuted.get()) {
                    audioBuffer.fill(0)
                } else {
                    for (i in 0 until bufferSize) {
                        val currentSample = sampleIndex + i
                        val chordIndex = ((currentSample / chordDurationSamples) % chords.size).toInt()
                        val chord = chords[chordIndex]
                        val chordPhase = (currentSample % chordDurationSamples).toFloat() / chordDurationSamples

                        // Soft ADSR envelope: smooth attack & release
                        val envelope = when {
                            chordPhase < 0.08f -> chordPhase / 0.08f
                            chordPhase > 0.85f -> (1f - chordPhase) / 0.15f
                            else -> 1f - 0.15f * (chordPhase - 0.08f)
                        }

                        val timeSec = currentSample.toFloat() / sampleRate
                        var sample = 0f

                        // Sum harmonics for each note in chord
                        for (f in chord) {
                            val twoPiF = 2.0 * PI * f
                            // Fundamental sine + warm octave harmonic + gentle detune
                            val fundamental = sin(twoPiF * timeSec)
                            val octave = 0.28 * sin(twoPiF * 2.0 * timeSec + 0.3)
                            val subFifth = 0.15 * sin(twoPiF * 1.5 * timeSec)
                            sample += (fundamental + octave + subFifth).toFloat()
                        }

                        // Warm tape saturation & vinyl texture
                        val vinylCrackle = if (Random.nextFloat() < 0.003f) {
                            (Random.nextFloat() - 0.5f) * 0.45f
                        } else {
                            (Random.nextFloat() - 0.5f) * 0.02f
                        }

                        // Master mix with soft clipping
                        val mixed = (sample * 0.10f * envelope + vinylCrackle * 0.035f)
                        val clipped = mixed.coerceIn(-0.95f, 0.95f)
                        audioBuffer[i] = (clipped * 32767f).toInt().toShort()
                    }
                }

                audioTrack?.write(audioBuffer, 0, bufferSize)
                sampleIndex += bufferSize
            }
        }
    }

    fun pause() {
        isMuted.set(true)
    }

    fun resume() {
        isMuted.set(false)
    }

    fun isMuted(): Boolean = isMuted.get()

    fun toggleMute(): Boolean {
        val newMuted = !isMuted.get()
        isMuted.set(newMuted)
        return newMuted
    }

    fun stop() {
        isRunning.set(false)
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
