package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

/**
 * Enhanced Audio & TTS Helper for fluent Arabic speech synthesis and ringtone management.
 */
class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var currentRingtone: Ringtone? = null

    var speechRate: Float = 0.88f // Optimal natural speed for clear Arabic pronunciation
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
            // Prefer Saudi/Arabic locale for best TTS clarity
            val arabicLocale = Locale("ar", "SA")
            val result = tts?.setLanguage(arabicLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to generic Arabic or default locale
                val genericArabic = Locale("ar")
                if (tts?.setLanguage(genericArabic) == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
            }

            // Try selecting explicit Arabic voice if available
            try {
                val arabicVoices = tts?.voices?.filter { voice ->
                    voice.locale.language == "ar" && !voice.isNetworkConnectionRequired
                }
                val bestVoice = arabicVoices?.firstOrNull() ?: tts?.voices?.firstOrNull { it.locale.language == "ar" }
                if (bestVoice != null) {
                    tts?.voice = bestVoice
                }
            } catch (e: Exception) {
                Log.e("TTS", "Error setting voice: ${e.message}")
            }

            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)
            isInitialized = true
        } else {
            Log.e("TTS", "TextToSpeech initialization failed with status $status")
        }
    }

    /**
     * Fluent Arabic Text Sanitizer and Normalizer.
     * Converts time codes, numbers, and symbols into elegant, spoken Arabic.
     */
    fun sanitizeAndFormatArabic(rawText: String): String {
        var text = rawText.trim()

        // Replace common time patterns e.g. "08:00 AM" or "14:30" or "08:00"
        text = text.replace(Regex("(\\d{1,2}):(\\d{2})\\s*(AM|PM|صباحاً|مساءً)?", RegexOption.IGNORE_CASE)) { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: 0
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val period = match.groupValues[3].uppercase()

            val hourText = when (if (hour > 12) hour - 12 else if (hour == 0) 12 else hour) {
                1 -> "الواحدة"
                2 -> "الثانية"
                3 -> "الثالثة"
                4 -> "الرابعة"
                5 -> "الخامسة"
                6 -> "السادسة"
                7 -> "السابعة"
                8 -> "الثامنة"
                9 -> "التاسعة"
                10 -> "العاشرة"
                11 -> "الحادية عشرة"
                12 -> "الثانية عشرة"
                else -> "$hour"
            }

            val minuteText = when (minute) {
                0 -> ""
                15 -> "والربع"
                30 -> "والنصف"
                45 -> "إلا الربع"
                else -> "و $minute دقيقة"
            }

            val periodText = if (period.contains("PM") || hour >= 12) "مساءً" else "صباحاً"

            "الساعة $hourText $minuteText $periodText".replace("\\s+".toRegex(), " ")
        }

        // Clean punctuation for clear Arabic speech cadence
        text = text.replace(":", "، ")
            .replace("-", " ")
            .replace("_", " ")
            .replace("/", " ")
            .replace("\\s+".toRegex(), " ")

        // Ensure natural pauses with commas and full stops
        if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("؟")) {
            text += "."
        }

        return text
    }

    /**
     * Plays alert according to user mode choice:
     * - "VOICE_ONLY": Speaks Arabic text or recorded voice note
     * - "RINGTONE_ONLY": Plays ringtone chime
     * - "BOTH": Plays ringtone chime then speaks Arabic text or plays recorded voice note
     * - "VIBRATE": Triggers vibration
     */
    fun playCustomVoiceNoteOrTTS(
        voiceNotePath: String?,
        text: String,
        alertMode: String = "BOTH",
        voiceStyle: String = "FRIENDLY"
    ) {
        stop()

        val voiceFile = voiceNotePath?.let { java.io.File(it) }
        val hasCustomVoiceNote = voiceFile != null && voiceFile.exists()

        when (alertMode) {
            "RINGTONE_ONLY" -> {
                playRingtoneSound()
            }
            "VIBRATE" -> {
                triggerVibration()
            }
            "VOICE_ONLY" -> {
                if (hasCustomVoiceNote) {
                    playAudioFile(voiceFile!!.absolutePath)
                } else {
                    speakArabicFluent(text, voiceStyle)
                }
            }
            else -> { // "BOTH"
                playRingtoneSound()
                Handler(Looper.getMainLooper()).postDelayed({
                    stopRingtone()
                    if (hasCustomVoiceNote) {
                        playAudioFile(voiceFile!!.absolutePath)
                    } else {
                        speakArabicFluent(text, voiceStyle)
                    }
                }, 2500)
            }
        }
    }

    private var customMediaPlayer: android.media.MediaPlayer? = null

    fun playAudioFile(filePath: String, onComplete: () -> Unit = {}) {
        stopAudioFile()
        try {
            val player = android.media.MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
            customMediaPlayer = player
        } catch (e: Exception) {
            Log.e("TTS", "Error playing audio file: ${e.message}")
        }
    }

    fun stopAudioFile() {
        try {
            if (customMediaPlayer?.isPlaying == true) {
                customMediaPlayer?.stop()
            }
            customMediaPlayer?.release()
            customMediaPlayer = null
        } catch (e: Exception) {
            Log.e("TTS", "Error stopping custom audio file: ${e.message}")
        }
    }

    fun playAlert(
        text: String,
        alertMode: String = "BOTH", // "VOICE_ONLY", "RINGTONE_ONLY", "BOTH", "VIBRATE"
        voiceStyle: String = "FRIENDLY"
    ) {
        playCustomVoiceNoteOrTTS(null, text, alertMode, voiceStyle)
    }

    fun speakArabicFluent(text: String, voiceStyle: String = "FRIENDLY") {
        if (!isInitialized || text.isBlank()) return

        val formattedText = sanitizeAndFormatArabic(text)

        // Adjust rate and pitch per style for natural tone
        val adjustedPitch = when (voiceStyle) {
            "FRIENDLY" -> 1.02f
            "DIRECT" -> 0.98f
            "EMERGENCY" -> 1.10f
            else -> 1.0f
        }
        val adjustedRate = when (voiceStyle) {
            "EMERGENCY" -> 0.95f
            "FRIENDLY" -> 0.88f
            "DIRECT" -> 0.92f
            else -> 0.88f
        }

        tts?.setPitch(adjustedPitch * pitch)
        tts?.setSpeechRate(adjustedRate * speechRate)

        val greetingPrefix = when (voiceStyle) {
            "FRIENDLY" -> "مرحباً بك، "
            "EMERGENCY" -> "تنبيه هَام وَعَاجِل! "
            "DIRECT" -> "تَذْكِير: "
            else -> ""
        }

        val fullText = greetingPrefix + formattedText
        tts?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, "MedReminderArabicTTS")
    }

    fun playRingtoneSound() {
        try {
            stopRingtone()
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            currentRingtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                currentRingtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            currentRingtone?.play()
        } catch (e: Exception) {
            Log.e("Ringtone", "Error playing ringtone: ${e.message}")
        }
    }

    fun stopRingtone() {
        try {
            if (currentRingtone?.isPlaying == true) {
                currentRingtone?.stop()
            }
            currentRingtone = null
        } catch (e: Exception) {
            Log.e("Ringtone", "Error stopping ringtone: ${e.message}")
        }
    }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            Log.e("Vibration", "Error triggering vibration: ${e.message}")
        }
    }

    fun stop() {
        stopRingtone()
        stopAudioFile()
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        stopRingtone()
        stopAudioFile()
        if (isInitialized) {
            tts?.stop()
            tts?.shutdown()
            isInitialized = false
        }
    }
}
