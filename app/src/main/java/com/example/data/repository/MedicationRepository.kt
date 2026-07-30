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

    suspend fun snoozeDose(log: IntakeLog, minutes: Int = 60) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            val dateObj = sdf.parse(log.scheduledTime)
            if (dateObj != null) {
                val newTime = sdf.format(Date(dateObj.time + minutes * 60 * 1000))
                val updatedLog = log.copy(
                    scheduledTime = newTime,
                    status = "SNOOZED"
                )
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

    suspend fun exportBackupJson(): String {
        val activeMeds = medicationDao.getAllActiveMedications().first()
        val allLogs = intakeLogDao.getAllLogs().first()

        val rootJson = org.json.JSONObject()
        rootJson.put("version", 1)
        rootJson.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        val medsArray = org.json.JSONArray()
        for (med in activeMeds) {
            val medObj = org.json.JSONObject()
            medObj.put("id", med.id)
            medObj.put("name", med.name)
            medObj.put("dosage", med.dosage)
            medObj.put("form", med.form)
            medObj.put("foodInstruction", med.foodInstruction)
            medObj.put("timesOfDay", med.timesOfDay)
            medObj.put("stockCount", med.stockCount)
            medObj.put("lowStockThreshold", med.lowStockThreshold)
            medObj.put("colorHex", med.colorHex)
            medObj.put("notes", med.notes)
            medObj.put("voiceNotePath", med.voiceNotePath ?: org.json.JSONObject.NULL)
            medsArray.put(medObj)
        }
        rootJson.put("medications", medsArray)

        val logsArray = org.json.JSONArray()
        for (log in allLogs) {
            val logObj = org.json.JSONObject()
            logObj.put("id", log.id)
            logObj.put("medicationId", log.medicationId)
            logObj.put("medicationName", log.medicationName)
            logObj.put("dosage", log.dosage)
            logObj.put("scheduledDate", log.scheduledDate)
            logObj.put("scheduledTime", log.scheduledTime)
            logObj.put("status", log.status)
            logObj.put("takenTimestamp", log.takenTimestamp ?: org.json.JSONObject.NULL)
            logObj.put("voicePromptText", log.voicePromptText)
            logsArray.put(logObj)
        }
        rootJson.put("intakeLogs", logsArray)

        return rootJson.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<Int> {
        return try {
            val rootJson = org.json.JSONObject(jsonString)
            val medsArray = rootJson.optJSONArray("medications") ?: org.json.JSONArray()
            val logsArray = rootJson.optJSONArray("intakeLogs") ?: org.json.JSONArray()

            var importedCount = 0
            for (i in 0 until medsArray.length()) {
                val medObj = medsArray.getJSONObject(i)
                val med = Medication(
                    id = 0, // auto generate new id to prevent primary key conflicts
                    name = medObj.getString("name"),
                    dosage = medObj.optString("dosage", "قرص"),
                    form = medObj.optString("form", "PILL"),
                    foodInstruction = medObj.optString("foodInstruction", "NO_RESTRICTION"),
                    timesOfDay = medObj.optString("timesOfDay", "08:00"),
                    stockCount = medObj.optInt("stockCount", 20),
                    lowStockThreshold = medObj.optInt("lowStockThreshold", 5),
                    colorHex = medObj.optString("colorHex", "#00897B"),
                    notes = medObj.optString("notes", ""),
                    voiceNotePath = if (medObj.isNull("voiceNotePath")) null else medObj.optString("voiceNotePath", null)
                )
                medicationDao.insertMedication(med)
                importedCount++
            }

            // Regenerate schedule logs for today
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            generateLogsForDate(today)

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
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
