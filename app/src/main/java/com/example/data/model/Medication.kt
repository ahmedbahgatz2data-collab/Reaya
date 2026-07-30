package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: String, // "PILL", "SYRUP", "INJECTION", "DROPS", "INHALER"
    val foodInstruction: String, // "BEFORE_MEAL", "AFTER_MEAL", "WITH_MEAL", "EMPTY_STOMACH", "ANYTIME"
    val timesOfDay: String, // Comma separated HH:mm e.g., "08:00,20:00"
    val stockCount: Int = 30,
    val lowStockThreshold: Int = 5,
    val colorHex: String = "#00897B",
    val notes: String = "",
    val isActive: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)
