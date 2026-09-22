package com.kitchino.app.employee.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: Role,
    val pinHash: String


)