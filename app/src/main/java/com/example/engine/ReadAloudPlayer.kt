package com.example.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Reads page text aloud using the platform TextToSpeech engine.
 *
 * Text is chunked so long articles do not exceed the TTS engine's per-call
 * limits. The UI observes [isSpeaking] to show play / stop controls.
 */
class ReadAloudPlayer(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                try {
                    tts?.setLanguage(Locale.getDefault())
                } catch (e: Exception) {
                    // Language not available; leave the engine in its default state.
                }
            }
        }
    }

    fun speak(text: String) {
        stop()
        if (text.isBlank()) return
        val engine = tts ?: return
        if (!ready) return // not initialised yet; user can press again
        val chunks = text.chunked(450)
        chunks.forEachIndexed { index, chunk ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            try {
                engine.speak(chunk, TextToSpeech.QUEUE_ADD, null, "nova-$index")
            } catch (e: Exception) {
                _isSpeaking.value = false
            }
        }
        _isSpeaking.value = true
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Engine may not be ready; ignore.
        }
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore teardown failures.
        }
        tts = null
        ready = false
    }
}
