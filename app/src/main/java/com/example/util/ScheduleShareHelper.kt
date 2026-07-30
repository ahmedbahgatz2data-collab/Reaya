package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.IntakeLog
import com.example.data.model.Medication

object ScheduleShareHelper {

    fun shareSchedule(
        context: Context,
        medications: List<Medication>,
        logs: List<IntakeLog>,
        date: String
    ) {
        val builder = StringBuilder()
        builder.appendLine("📋 *الجدول الدوائي اليومي والجرعات*")
        builder.appendLine("🗓️ التاريخ: $date")
        builder.appendLine("-----------------------------------")
        builder.appendLine()

        if (medications.isEmpty()) {
            builder.appendLine("لا توجد أدوية مسجلة حالياً.")
        } else {
            builder.appendLine("💊 *قائمة الأدوية المعتمدة:*")
            medications.forEachIndexed { index, med ->
                val times = med.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ، ")
                val formArabic = when (med.form) {
                    "PILL" -> "قرص"
                    "SYRUP" -> "شراب"
                    "INJECTION" -> "حقنة"
                    "DROPS" -> "قطرة"
                    "INHALER" -> "بخاخ"
                    else -> "جرعة"
                }
                val foodArabic = when (med.foodInstruction) {
                    "BEFORE_MEAL" -> "قبل الطعام"
                    "AFTER_MEAL" -> "بعد الطعام"
                    "WITH_MEAL" -> "مع وجبة الطعام"
                    "EMPTY_STOMACH" -> "على معدة فارغة"
                    else -> "حسب التعليمات"
                }

                val timesCount = med.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
                val daysLeft = if (timesCount > 0) med.stockCount / timesCount else 0

                builder.appendLine("${index + 1}. *${med.name}*")
                builder.appendLine("   • الجرعة: ${med.dosage} ($formArabic)")
                builder.appendLine("   • المواعيد اليومية: $times")
                builder.appendLine("   • التعليمات: $foodArabic")
                builder.appendLine("   • المخزون المتبقي: ${med.stockCount} (تكفي لـ $daysLeft أيام)")
                if (med.notes.isNotBlank()) {
                    builder.appendLine("   • ملاحظات: ${med.notes}")
                }
                builder.appendLine()
            }

            if (logs.isNotEmpty()) {
                builder.appendLine("-----------------------------------")
                builder.appendLine("📊 *حالة الجرعات لليوم ($date):*")
                logs.forEach { log ->
                    val statusText = when (log.status) {
                        "TAKEN" -> "✅ تم أخذها"
                        "SKIPPED" -> "❌ تم تخطيها"
                        "SNOOZED" -> "⏳ مؤجلة"
                        else -> "⏰ قيد الانتظار"
                    }
                    builder.appendLine("• ${log.scheduledTime} - ${log.medicationName} (${log.dosage}) 👈 $statusText")
                }
            }
        }

        builder.appendLine()
        builder.appendLine("-----------------------------------")
        builder.appendLine("تمت المشاركة من تطبيق 'منبه الأدوية والجرعات الذكي' 🏥")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الجدول الدوائي")
        context.startActivity(shareIntent)
    }
}
