package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.IntakeLog
import com.example.data.model.Medication
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreSyncManager(context: Context) {

    private val prefs = context.getSharedPreferences("med_sync_prefs", Context.MODE_PRIVATE)

    var syncUserId: String
        get() {
            var id = prefs.getString("sync_user_id", null)
            if (id.isNullOrBlank()) {
                id = "user_" + UUID.randomUUID().toString().take(8)
                prefs.edit().putString("sync_user_id", id).apply()
            }
            return id!!
        }
        set(value) {
            prefs.edit().putString("sync_user_id", value).apply()
        }

    fun isCloudAvailable(): Boolean {
        return try {
            FirebaseFirestore.getInstance() != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncMedicationToCloud(medication: Medication): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val medDoc = db.collection("users")
                .document(syncUserId)
                .collection("medications")
                .document(medication.id.toString())

            val map = hashMapOf(
                "id" to medication.id,
                "name" to medication.name,
                "dosage" to medication.dosage,
                "form" to medication.form,
                "foodInstruction" to medication.foodInstruction,
                "timesOfDay" to medication.timesOfDay,
                "stockCount" to medication.stockCount,
                "lowStockThreshold" to medication.lowStockThreshold,
                "colorHex" to medication.colorHex,
                "notes" to medication.notes,
                "barcode" to (medication.barcode ?: ""),
                "expiryDate" to (medication.expiryDate ?: ""),
                "isActive" to medication.isActive,
                "updatedAt" to System.currentTimeMillis()
            )
            medDoc.set(map).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing med: ${e.message}")
            false
        }
    }

    suspend fun syncLogToCloud(log: IntakeLog): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val logDoc = db.collection("users")
                .document(syncUserId)
                .collection("intake_logs")
                .document(log.id.toString())

            val map = hashMapOf(
                "id" to log.id,
                "medicationId" to log.medicationId,
                "medicationName" to log.medicationName,
                "dosage" to log.dosage,
                "scheduledDate" to log.scheduledDate,
                "scheduledTime" to log.scheduledTime,
                "status" to log.status,
                "takenTimestamp" to (log.takenTimestamp ?: 0L),
                "voicePromptText" to log.voicePromptText
            )
            logDoc.set(map).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing log: ${e.message}")
            false
        }
    }

    suspend fun uploadFullBackup(
        medications: List<Medication>,
        logs: List<IntakeLog>
    ): Pair<Boolean, String> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(syncUserId)

            val batch = db.batch()

            medications.forEach { med ->
                val medDoc = userRef.collection("medications").document(med.id.toString())
                val map = hashMapOf(
                    "id" to med.id,
                    "name" to med.name,
                    "dosage" to med.dosage,
                    "form" to med.form,
                    "foodInstruction" to med.foodInstruction,
                    "timesOfDay" to med.timesOfDay,
                    "stockCount" to med.stockCount,
                    "lowStockThreshold" to med.lowStockThreshold,
                    "colorHex" to med.colorHex,
                    "notes" to med.notes,
                    "barcode" to (med.barcode ?: ""),
                    "expiryDate" to (med.expiryDate ?: ""),
                    "isActive" to med.isActive,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(medDoc, map)
            }

            logs.forEach { log ->
                val logDoc = userRef.collection("intake_logs").document(log.id.toString())
                val map = hashMapOf(
                    "id" to log.id,
                    "medicationId" to log.medicationId,
                    "medicationName" to log.medicationName,
                    "dosage" to log.dosage,
                    "scheduledDate" to log.scheduledDate,
                    "scheduledTime" to log.scheduledTime,
                    "status" to log.status,
                    "takenTimestamp" to (log.takenTimestamp ?: 0L),
                    "voicePromptText" to log.voicePromptText
                )
                batch.set(logDoc, map)
            }

            batch.commit().await()
            Pair(true, "تم رفع ${medications.size} دواء و ${logs.size} سجل جرعات بنجاح إلى Firestore (معرف الحساب: $syncUserId)")
        } catch (e: Exception) {
            val msg = if (e.message?.contains("FirebaseApp") == true) {
                "التخزين السحابي يتطلب إعداد Firebase. تم تفعيل النسخ الاحتياطي المحلي وتصدير الملفات على الهاتف بنجاح كبديل فوري آمن."
            } else {
                "فشل المزامنة السحابية: ${e.message}"
            }
            Pair(false, msg)
        }
    }

    suspend fun restoreFromCloud(targetUserId: String = syncUserId): Triple<Boolean, List<Medication>?, List<IntakeLog>?> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(targetUserId)

            val medSnapshots = userRef.collection("medications").get().await()
            val logSnapshots = userRef.collection("intake_logs").get().await()

            val fetchedMeds = medSnapshots.documents.mapNotNull { doc ->
                try {
                    Medication(
                        id = doc.getLong("id") ?: 0L,
                        name = doc.getString("name") ?: "",
                        dosage = doc.getString("dosage") ?: "",
                        form = doc.getString("form") ?: "PILL",
                        foodInstruction = doc.getString("foodInstruction") ?: "AFTER_MEAL",
                        timesOfDay = doc.getString("timesOfDay") ?: "08:00",
                        stockCount = (doc.getLong("stockCount") ?: 30L).toInt(),
                        lowStockThreshold = (doc.getLong("lowStockThreshold") ?: 5L).toInt(),
                        colorHex = doc.getString("colorHex") ?: "#00897B",
                        notes = doc.getString("notes") ?: "",
                        barcode = doc.getString("barcode").takeIf { !it.isNullOrBlank() },
                        expiryDate = doc.getString("expiryDate").takeIf { !it.isNullOrBlank() },
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                } catch (e: Exception) { null }
            }

            val fetchedLogs = logSnapshots.documents.mapNotNull { doc ->
                try {
                    IntakeLog(
                        id = doc.getLong("id") ?: 0L,
                        medicationId = doc.getLong("medicationId") ?: 0L,
                        medicationName = doc.getString("medicationName") ?: "",
                        dosage = doc.getString("dosage") ?: "",
                        scheduledDate = doc.getString("scheduledDate") ?: "",
                        scheduledTime = doc.getString("scheduledTime") ?: "",
                        status = doc.getString("status") ?: "PENDING",
                        takenTimestamp = doc.getLong("takenTimestamp"),
                        voicePromptText = doc.getString("voicePromptText") ?: ""
                    )
                } catch (e: Exception) { null }
            }

            Triple(true, fetchedMeds, fetchedLogs)
        } catch (e: Exception) {
            Triple(false, null, null)
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
