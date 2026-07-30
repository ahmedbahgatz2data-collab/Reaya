package com.example.data.repository

import com.example.data.dao.IntakeLogDao
import com.example.data.dao.MedicationDao
import com.example.data.model.IntakeLog
import com.example.data.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val intakeLogDao: IntakeLogDao
) {
    val allActiveMedications: Flow<List<Medication>> = medicationDao.getAllActiveMedications()
    val allIntakeLogs: Flow<List<IntakeLog>> = intakeLogDao.getAllLogs()

    fun getLogsForDate(date: String): Flow<List<IntakeLog>> {
        return intakeLogDao.getLogsForDate(date)
    }

    suspend fun markDoseTaken(log: IntakeLog) {
        intakeLogDao.updateLogStatus(log.id, "TAKEN", System.currentTimeMillis())
        medicationDao.decrementStock(log.medicationId)
    }

    suspend fun markDoseSkipped(log: IntakeLog) {
        intakeLogDao.updateLogStatus(log.id, "SKIPPED", null)
    }

    suspend fun snoozeDose(log: IntakeLog, minutes: Int = 15) {
        // Adjust scheduled time by 15 mins for UI view
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            val dateObj = sdf.parse(log.scheduledTime)
            if (dateObj != null) {
                val newTime = sdf.format(Date(dateObj.time + minutes * 60 * 1000))
                val updatedLog = log.copy(scheduledTime = newTime)
                intakeLogDao.updateLog(updatedLog)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addMedication(medication: Medication): Long {
        val id = medicationDao.insertMedication(medication)
        // Generate intake logs for today
        generateLogsForDate(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        return id
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
        intakeLogDao.deleteLogsForMedication(medication.id)
    }

    suspend fun refillStock(medicationId: Long, amount: Int) {
        medicationDao.refillStock(medicationId, amount)
    }

    suspend fun generateLogsForDate(targetDate: String) {
        val activeMeds = medicationDao.getAllActiveMedications().first()
        val existingLogs = intakeLogDao.getLogsForDate(targetDate).first()
        val existingMap = existingLogs.associateBy { "${it.medicationId}_${it.scheduledTime}" }

        val newLogs = mutableListOf<IntakeLog>()
        for (med in activeMeds) {
            val times = med.timesOfDay.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            for (time in times) {
                val key = "${med.id}_$time"
                if (!existingMap.containsKey(key)) {
                    val voicePrompt = generateArabicVoicePrompt(med, time)
                    newLogs.add(
                        IntakeLog(
                            medicationId = med.id,
                            medicationName = med.name,
                            dosage = med.dosage,
                            scheduledDate = targetDate,
                            scheduledTime = time,
                            status = "PENDING",
                            voicePromptText = voicePrompt
                        )
                    )
                }
            }
        }
        if (newLogs.isNotEmpty()) {
            intakeLogDao.insertLogs(newLogs)
        }
    }

    suspend fun prepopulateDefaultsIfEmpty() {
        val existing = medicationDao.getAllActiveMedications().first()
        if (existing.isEmpty()) {
            val sample1 = Medication(
                name = "بندول فورت 500 ملغ",
                dosage = "قرص واحد",
                form = "PILL",
                foodInstruction = "بعد الأكل",
                timesOfDay = "08:00,14:00,20:00",
                stockCount = 24,
                lowStockThreshold = 6,
                colorHex = "#00897B",
                notes = "لتسكين الآلام وتخفيض الحرارة عند اللزوم"
            )
            val sample2 = Medication(
                name = "أنسولين لانتوس",
                dosage = "10 وحدات",
                form = "INJECTION",
                foodInstruction = "قبل الأكل",
                timesOfDay = "07:30,21:00",
                stockCount = 15,
                lowStockThreshold = 4,
                colorHex = "#1E88E5",
                notes = "حقنة تحت الجلد قبل وجبة الفطور والعشاء"
            )
            val sample3 = Medication(
                name = "شراب أوميبرازول للمعدة",
                dosage = "10 مل",
                form = "SYRUP",
                foodInstruction = "على الريق",
                timesOfDay = "07:00",
                stockCount = 8,
                lowStockThreshold = 10,
                colorHex = "#FF7043",
                notes = "تؤخذ قبل الفطور بـ 30 دقيقة لحماية المعدة"
            )
            medicationDao.insertMedication(sample1)
            medicationDao.insertMedication(sample2)
            medicationDao.insertMedication(sample3)

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            generateLogsForDate(today)
        }
    }

    private fun generateArabicVoicePrompt(med: Medication, time: String): String {
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
            else -> ""
        }
        val foodPhrase = if (foodArabic.isNotEmpty()) "، $foodArabic" else ""
        return "تذكير صحي: حان الآن موعد تناول دواء ${med.name}، الجرعة المطلوب تناولها هي ${med.dosage} $formArabic$foodPhrase. نتمنى لك دوام الصحة والعافية."
    }
}
