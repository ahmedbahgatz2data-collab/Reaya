package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.database.AppDatabase
import com.example.data.model.IntakeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MedicationWorkScheduler {

    private const val PERIODIC_CHECK_WORK_TAG = "periodic_med_check_work"

    fun scheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val logs = db.intakeLogDao().getLogsForDateDirect(todayStr)

                logs.filter { it.status == "PENDING" || it.status == "SNOOZED" }.forEach { log ->
                    scheduleSingleLogReminder(context, log)
                }

                // Schedule 15-minute safety periodic worker
                val periodicWork = PeriodicWorkRequestBuilder<MedicationReminderWorker>(
                    15, TimeUnit.MINUTES
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    PERIODIC_CHECK_WORK_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )

                Log.d("MedWorkScheduler", "Scheduled WorkManager background reminders successfully")
            } catch (e: Exception) {
                Log.e("MedWorkScheduler", "Error scheduling reminders: ${e.message}")
            }
        }
    }

    fun scheduleSingleLogReminder(context: Context, log: IntakeLog) {
        try {
            val delayMs = calculateDelayMs(log.scheduledTime)
            if (delayMs <= 0) return

            val data = Data.Builder()
                .putString(MedicationReminderWorker.KEY_MED_NAME, log.medicationName)
                .putString(MedicationReminderWorker.KEY_DOSAGE, log.dosage)
                .putString(MedicationReminderWorker.KEY_SCHEDULED_TIME, log.scheduledTime)
                .putLong(MedicationReminderWorker.KEY_LOG_ID, log.id)
                .build()

            val oneTimeWork = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            val workName = "med_reminder_${log.id}"
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                oneTimeWork
            )
        } catch (e: Exception) {
            Log.e("MedWorkScheduler", "Error scheduling single log reminder: ${e.message}")
        }
    }

    private fun calculateDelayMs(scheduledTimeStr: String): Long {
        return try {
            val parts = scheduledTimeStr.split(":")
            if (parts.size != 2) return 0L

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val targetCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val now = System.currentTimeMillis()
            val diff = targetCal.timeInMillis - now
            if (diff > 0) diff else 0L
        } catch (e: Exception) {
            0L
        }
    }
}
