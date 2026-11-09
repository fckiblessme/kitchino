package com.kitchino.app.employee.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.employee.data.AuditLogDao
import com.kitchino.app.employee.data.AuditLogEntity
import com.kitchino.app.employee.data.EmployeeDao
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.AuditChain
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.employee.domain.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditUiState(
    val logs: List<AuditLogEntity> = emptyList(),
    val employeeNames: Map<Long, String> = emptyMap(),
    val tamperedId: Long? = null,
    val canView: Boolean = false
)

class AuditViewModel(
    private val auditLogDao: AuditLogDao,
    private val employeeDao: EmployeeDao,
    private val sessionManager: SessionManager,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditUiState())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val logs = auditLogDao.getAllLogs()
        val current = sessionManager.current.value
        val canView = current != null &&
                permissionChecker.can(current.role, Action.VIEW_AUDIT)
        val names = logs.map { it.actorId }.distinct()
            .mapNotNull { id -> employeeDao.findById(id)?.let { id to it.name } }
            .toMap()
        _uiState.update {
            it.copy(
                logs = logs.asReversed(),
                employeeNames = names,
                tamperedId = AuditChain.verifyChain(logs),
                canView = canView
            )
        }
    }
}

class AuditViewModelFactory(
    private val auditLogDao: AuditLogDao,
    private val employeeDao: EmployeeDao,
    private val sessionManager: SessionManager,
    private val permissionChecker: PermissionChecker
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuditViewModel(auditLogDao, employeeDao, sessionManager, permissionChecker) as T
}