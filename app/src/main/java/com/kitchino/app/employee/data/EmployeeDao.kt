package com.kitchino.app.employee.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EmployeeDao {

    @Insert
    suspend fun insertEmployee(employee:EmployeeEntity) : Long

    @Query("Select * from employees ")
    suspend fun getAllEmployees() : List<EmployeeEntity>

    @Query("Select * from employees where id = :id")
    suspend fun findById(id: Long) :EmployeeEntity?
}
