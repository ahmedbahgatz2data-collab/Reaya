package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var pitch: Float = 1.0f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if Arabic missing
                tts?.setLanguage(Locale.getDefault())
                Log.e("TTS", "Arabic language data missing or not supported, using default locale")
            }
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            isInitialized = true
        } else {
            Log.e("TTS", "TextToSpeech initialization failed")
        }
    }

    fun speak(text: String, voiceStyle: String = "FRIENDLY") {
        if (!isInitialized || text.isBlank()) return
        
        // Dynamic pitch adjustment according to voice style
        val adjustedPitch = when (voiceStyle) {
            "FRIENDLY" -> 1.05f
            "DIRECT" -> 0.95f
            "EMERGENCY" -> 1.15f
            else -> 1.0f
        }
        val adjustedRate = when (voiceStyle) {
            "EMERGENCY" -> 1.1f
            "FRIENDLY" -> 0.9f
            else -> 1.0f
        }
        
        tts?.setPitch(adjustedPitch * pitch)
        tts?.setSpeechRate(adjustedRate * speechRate)

        val prefix = when (voiceStyle) {
            "FRIENDLY" -> "مرحباً يا بطل! "
            "EMERGENCY" -> "تنبيه هامة وعاجل! "
            else -> ""
        }

        val fullText = prefix + text
        tts?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, "MedReminderTTS")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            isInitialized = false
        }
    }
}
