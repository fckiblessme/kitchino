package com.kitchino.app.auth

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitchino.app.employee.data.EmployeeDao
import com.kitchino.app.employee.data.EmployeeEntity
import com.kitchino.app.employee.data.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val dao: EmployeeDao) : ViewModel() {
    sealed class State {
        object Idle: State()
        object Loading: State()
        data class LoggedIn(val user: EmployeeEntity): State()
        data class Registered(val id: Long): State()
        data class Error(val message: String): State()

    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    // Вход
    fun login(name: String, pin: String) = viewModelScope.launch {
        _state.value = State.Loading
        try{
            val user = dao.findByName(name)
            if (user == null) {
                _state.value = State.Error("Сотрудник не найден")
                return@launch
            }
            if(!PinHasher.verify(pin, user.pinHash)) {
                _state.value = State.Error("Неверный PIN")
                return@launch
            }
            _state.value = State.LoggedIn(user)
        } catch (e: Exception) {
            _state.value = State.Error(e.message ?: "Ошибка")
        }
    }

    // Регистрация
    fun register(name: String, role: Role, pin:String) = viewModelScope.launch {
        _state.value = State.Loading
        try{
            if(name.isBlank()){
                _state.value = State.Error("Введите имя")
                return@launch
            }

            if(pin.length != 4 || !pin.all { it.isDigit() }) {
                _state.value = State.Error("PIN должен быть из 4 цифр")
                return@launch
            }

            if(dao.findByName(name) != null){
                _state.value = State.Error("Сотрудник уже существует")
                return@launch
            }

            val hash = PinHasher.hash(pin)
            val id = dao.insertEmployee(
                EmployeeEntity(name = name, role = role, pinHash = hash)
            )
            _state.value = State.Registered(id)
        } catch (e: Exception){
            _state.value = State.Error(e.message ?: "Ошибка")
        }
    }

    fun reset() {_state.value = State.Idle
    }
}
