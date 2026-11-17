package com.kitchino.app.employee.domain

import com.kitchino.app.core.contracts.EmployeeApi

class EmployeeApiImpl(
    private val sessionManager: SessionManager,
    private val permissionChecker: PermissionChecker
) : EmployeeApi {

    override suspend fun getCurrentEmployeeId(): Long = sessionManager.currentId()

    override suspend fun hasPermission(employeeId: Long, action: String): Boolean {
        val current = sessionManager.current.value ?: return false
        if (current.id != employeeId) return false
        val act = runCatching { Action.valueOf(action) }.getOrNull() ?: return false
        return permissionChecker.can(current.role, act)
    }
}