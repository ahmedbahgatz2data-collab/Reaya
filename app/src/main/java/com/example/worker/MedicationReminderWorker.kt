package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.audio.TextToSpeechHelper
import com.example.data.database.AppDatabase
import com.example.widget.NextDoseWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedicationReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val medName = inputData.getString(KEY_MED_NAME)
            val dosage = inputData.getString(KEY_DOSAGE)
            val scheduledTime = inputData.getString(KEY_SCHEDULED_TIME)
            val logId = inputData.getLong(KEY_LOG_ID, -1L)

            if (medName != null && dosage != null) {
                showNotification(medName, dosage, scheduledTime ?: "", logId)
            } else {
                // Check database for any due pending logs right now
                checkAndTriggerDueLogs()
            }

            // Update home screen widget
            NextDoseWidgetProvider.updateAllWidgets(appContext)

            Result.success()
        } catch (e: Exception) {
            Log.e("MedReminderWorker", "Error executing reminder work: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun checkAndTriggerDueLogs() {
        val db = AppDatabase.getDatabase(appContext)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val logs = db.intakeLogDao().getLogsForDateDirect(todayStr)
        val nowTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val dueLogs = logs.filter { log ->
            (log.status == "PENDING" || log.status == "SNOOZED") && log.scheduledTime <= nowTimeStr
        }

        dueLogs.forEach { dueLog ->
            showNotification(
                medName = dueLog.medicationName,
                dosage = dueLog.dosage,
                scheduledTime = dueLog.scheduledTime,
                logId = dueLog.id
            )
        }
    }

    private fun showNotification(
        medName: String,
        dosage: String,
        scheduledTime: String,
        logId: Long
    ) {
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "medication_background_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكيرات الأدوية الصوتية في الخلفية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة تنبيهات موعد تناول الدواء بدقة والتذكيرات الصوتية"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            logId.toInt().coerceAtLeast(1),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⏰ موعد تناول الدواء: $medName"
        val message = "حان موعد تناول جرعة ($dosage) المحددة الساعة $scheduledTime. يرجى التناول وتأكيد الجرعة."

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = if (logId > 0) logId.toInt() else (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, notification)

        // Trigger TTS Voice Announcement in background if enabled
        try {
            val tts = TextToSpeechHelper(appContext)
            tts.speakArabicFluent("تذكير منبه الدواء. حان موعد تناول دواء $medName جرعة $dosage")
        } catch (e: Exception) {
            Log.e("MedReminderWorker", "TTS error: ${e.message}")
        }
    }

    companion object {
        const val KEY_MED_NAME = "key_med_name"
        const val KEY_DOSAGE = "key_dosage"
        const val KEY_SCHEDULED_TIME = "key_scheduled_time"
        const val KEY_LOG_ID = "key_log_id"
    }
}
