package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingFile: File? = null

    private var _isRecording = false
    val isRecording: Boolean get() = _isRecording

    private var _isPlaying = false
    val isPlaying: Boolean get() = _isPlaying

    fun startRecording(outputFile: File): Boolean {
        stopPlayback()
        stopRecording()

        return try {
            recordingFile = outputFile

            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording: ${e.message}")
            _isRecording = false
            mediaRecorder = null
            false
        }
    }

    fun stopRecording(): String? {
        if (!_isRecording) return recordingFile?.absolutePath

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording = false
            recordingFile?.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recording: ${e.message}")
            mediaRecorder = null
            _isRecording = false
            recordingFile?.absolutePath
        }
    }

    fun playAudio(filePath: String, onComplete: () -> Unit = {}): Boolean {
        stopRecording()
        stopPlayback()

        val file = File(filePath)
        if (!file.exists()) return false

        return try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying = false
                    onComplete()
                }
                start()
            }
            mediaPlayer = player
            _isPlaying = true
            true
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error playing audio: ${e.message}")
            _isPlaying = false
            mediaPlayer = null
            false
        }
    }

    fun stopPlayback() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying = false
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping playback: ${e.message}")
            mediaPlayer = null
            _isPlaying = false
        }
    }
}
