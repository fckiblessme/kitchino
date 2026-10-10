package com.kitchino.app.employee.data.repository

import com.kitchino.app.employee.data.AuditLogDao
import com.kitchino.app.employee.data.AuditLogEntity
import com.kitchino.app.employee.data.EmployeeDao
import com.kitchino.app.employee.data.EmployeeEntity
import com.kitchino.app.employee.data.TrainingCardEntity
import com.kitchino.app.employee.data.TrainingDao
import com.kitchino.app.employee.data.TrainingProgressEntity

class EmployeeRepository (
    private val employeeDao: EmployeeDao,
    private val auditLogDao: AuditLogDao,
    private val trainingDao: TrainingDao
) {
    suspend fun addEmployee(employee : EmployeeEntity) : Long {
        return employeeDao.insertEmployee(employee)
    }

    suspend fun getAllEmployees() : List<EmployeeEntity> {
        return employeeDao.getAllEmployees()
    }

    suspend fun findEmployee(id: Long) : EmployeeEntity? {
        return employeeDao.findById(id)
    }

    suspend fun addTrainingCard(card: TrainingCardEntity) : Long {
        return trainingDao.insertCard(card)
    }

    suspend fun getCardsForRecipe(recipeId: Long) : List<TrainingCardEntity> {
        return trainingDao.getRecipeCard(recipeId)
    }

    suspend fun addProgress(progress: TrainingProgressEntity): Long {
        return trainingDao.insertProgress(progress)
    }

    suspend fun updateProgress(progress: TrainingProgressEntity) {
        trainingDao.updateProgress(progress)
    }

    suspend fun findProgress(employeeId: Long, cardId: Long) : TrainingProgressEntity? {
        return trainingDao.findProgress(employeeId, cardId)
    }

    suspend fun getEmployeeProgress(employeeId: Long) : List<TrainingProgressEntity> {
        return trainingDao.getProgress(employeeId)
    }

    suspend fun logAction(log: AuditLogEntity) : Long {
        return auditLogDao.insertLog(log)
    }

    suspend fun getLastLog() : AuditLogEntity? {
        return auditLogDao.getLastLog()
    }

    suspend fun getAllLogs() : List<AuditLogEntity> {
        return auditLogDao.getAllLogs()
    }
}