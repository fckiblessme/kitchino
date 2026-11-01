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

    @Query("SELECT * FROM employees WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): EmployeeEntity?

    @Query("UPDATE employees SET pinHash = :hash WHERE id = :id")
    suspend fun updatePin(id: Long, hash: String)
}
