package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.IntakeLog
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {
    @Query("SELECT * FROM intake_logs WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getLogsForDate(date: String): Flow<List<IntakeLog>>

    @Query("SELECT * FROM intake_logs WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    suspend fun getLogsForDateDirect(date: String): List<IntakeLog>

    @Query("SELECT * FROM intake_logs ORDER BY scheduledDate DESC, scheduledTime DESC")
    fun getAllLogs(): Flow<List<IntakeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IntakeLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<IntakeLog>)

    @Update
    suspend fun updateLog(log: IntakeLog)

    @Query("UPDATE intake_logs SET status = :status, takenTimestamp = :timestamp WHERE id = :logId")
    suspend fun updateLogStatus(logId: Long, status: String, timestamp: Long?)

    @Query("DELETE FROM intake_logs WHERE medicationId = :medicationId")
    suspend fun deleteLogsForMedication(medicationId: Long)

    @Query("DELETE FROM intake_logs")
    suspend fun deleteAllLogs()
}
