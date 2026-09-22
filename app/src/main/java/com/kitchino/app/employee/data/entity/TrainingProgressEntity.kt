package com.kitchino.app.employee.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
class TrainingProgressEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long = 0,
    val cardId: Long = 0,
    val interval: Int = 0,
    val factor: Double = 2.5,
    val nextReview: Long,
    val repetition: Int = 0

)