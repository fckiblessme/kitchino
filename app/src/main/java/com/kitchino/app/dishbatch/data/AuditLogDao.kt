package com.kitchino.app.dishbatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kitchino.app.employee.data.AuditLogEntity

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log : AuditLogEntity) : Long

    @Query("Select * from audit_log order by id desc limit 1")
    suspend fun getLastLog() : AuditLogEntity?

    @Query("Select * from audit_log order by id asc")
    suspend fun getAllLogs() :List<AuditLogEntity>
}