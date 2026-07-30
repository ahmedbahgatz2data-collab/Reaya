package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intake_logs")
data class IntakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val dosage: String,
    val scheduledDate: String, // format YYYY-MM-DD
    val scheduledTime: String, // format HH:mm
    val takenTimestamp: Long? = null,
    val status: String = "PENDING", // PENDING, TAKEN, SKIPPED, MISSED
    val voicePromptText: String = ""
)
