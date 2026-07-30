package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NextDoseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, NextDoseWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (appWidgetId in appWidgetIds) {
                        updateWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val views = RemoteViews(context.packageName, R.layout.widget_next_dose)

                // Intent to open main app when tapping widget
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                try {
                    val db = AppDatabase.getDatabase(context)
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val logs = db.intakeLogDao().getLogsForDateDirect(todayStr)
                    val nextDose = logs.firstOrNull { it.status == "PENDING" || it.status == "SNOOZED" }

                    if (nextDose != null) {
                        val isSnoozed = nextDose.status == "SNOOZED"
                        val medName = nextDose.medicationName
                        val dosage = nextDose.dosage
                        val time = nextDose.scheduledTime + if (isSnoozed) " (مؤجل)" else ""

                        views.setTextViewText(R.id.widget_med_name, medName)
                        views.setTextViewText(R.id.widget_med_dosage, "الجرعة: $dosage")
                        views.setTextViewText(R.id.widget_time_val, time)
                        views.setTextViewText(
                            R.id.widget_title,
                            if (isSnoozed) "جرعة مؤجلة ⏰" else "الجرعة القادمة 💊"
                        )
                    } else {
                        views.setTextViewText(R.id.widget_med_name, "جميع الجرعات مكتملة! 🎉")
                        views.setTextViewText(R.id.widget_med_dosage, "لا توجد جرعات متبقية اليوم")
                        views.setTextViewText(R.id.widget_time_val, "مكتمل")
                        views.setTextViewText(R.id.widget_title, "الجدول اليومي 💊")
                    }
                } catch (e: Exception) {
                    views.setTextViewText(R.id.widget_med_name, "منبه الدواء")
                    views.setTextViewText(R.id.widget_med_dosage, "انقر لفتح التطبيق")
                    views.setTextViewText(R.id.widget_time_val, "--:--")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
