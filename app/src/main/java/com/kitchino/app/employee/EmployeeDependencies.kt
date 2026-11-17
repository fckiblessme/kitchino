package com.kitchino.app.employee

import com.kitchino.app.core.contracts.AuditApi
import com.kitchino.app.core.contracts.EmployeeApi
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.employee.data.repository.EmployeeRepository
import com.kitchino.app.employee.domain.AuditApiImpl
import com.kitchino.app.employee.domain.EmployeeApiImpl
//import com.kitchino.app.employee.domain.EmployeeApiImpl
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.employee.domain.SessionManager

/**
 * Обвязка модуля сотрудников. SessionManager один на приложение,
 * поэтому создайте EmployeeDependencies один раз (в MainActivity) и передавайте вниз.
 */
class EmployeeDependencies(database: AppDatabase) {
    val repository: EmployeeRepository = EmployeeRepository(
        employeeDao = database.returnEmployeeDao(),
        auditLogDao = database.returnAuditLogDao(),
        trainingDao = database.returnTrainingDao()
    )

    val permissionChecker: PermissionChecker = PermissionChecker()

    val sessionManager: SessionManager = SessionManager()

    val employeeApi: EmployeeApi = EmployeeApiImpl(
        sessionManager = sessionManager,
        permissionChecker = permissionChecker
    )

    val auditApi: AuditApi = AuditApiImpl(database.returnAuditLogDao())
}