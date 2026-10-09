package com.kitchino.app.core.contracts

interface EmployeeApi {

    suspend fun getCurrentEmployeeId() : Long

    suspend fun hasPermission(employeeId: Long, action: String): Boolean
}