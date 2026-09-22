package com.kitchino.app.employee.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "audit_log")
class AuditLogEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = 0,
    val actorId: Long = 0,
    val action: String,
    val payload: String,
    val prevHash: String,
    val hash: String

)