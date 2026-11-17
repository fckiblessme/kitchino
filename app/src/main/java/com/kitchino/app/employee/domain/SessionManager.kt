package com.kitchino.app.employee.domain
import com.kitchino.app.employee.data.EmployeeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Хранит текущего вошедшего сотрудника (FR-12).
 * Другие модули получают его id только через EmployeeApi.
 */
class SessionManager {
    private val _current = MutableStateFlow<EmployeeEntity?>(null)
    val current: StateFlow<EmployeeEntity?> = _current

    fun set(employee: EmployeeEntity) {
        _current.value = employee
    }

    fun clear() {
        _current.value = null
    }

    /** id текущего сотрудника или -1, если никто не вошёл */
    fun currentId(): Long = _current.value?.id ?: -1L
}