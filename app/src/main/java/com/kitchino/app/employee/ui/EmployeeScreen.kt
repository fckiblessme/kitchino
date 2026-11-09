package com.kitchino.app.employee.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchino.app.employee.EmployeeDependencies
import com.kitchino.app.employee.data.Role
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.employee.ui.audit.AuditScreen
import com.kitchino.app.employee.ui.audit.AuditViewModel
import com.kitchino.app.employee.ui.audit.AuditViewModelFactory

enum class EmployeeScreenState { ACTIONS, TRAINING, AUDIT }

@Composable
fun EmployeeScreen(dependencies: EmployeeDependencies) {
    // Берем текущего сотрудника из SessionManager (который сделала команда)
    val currentEmployee = remember {
        com.kitchino.app.employee.data.EmployeeEntity(
            id = 1, name = "Тестовый Повар", role = com.kitchino.app.employee.data.Role.COOK, pinHash = "0000"
        )
    }
    val checker = remember { PermissionChecker() }

    // Если никто не вошел, показываем заглушку
//    if (currentEmployee == null) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text("Сначала войдите в систему (вкладка Партии -> Выйти -> Войти)")
//        }
//        return
//    }

    val employee = currentEmployee!!
    var currentScreen by remember { mutableStateOf(EmployeeScreenState.ACTIONS) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Шапка
        Text("Сотрудник: ${employee.name}", style = MaterialTheme.typography.titleLarge)
        Text("Роль: ${employee.role}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки навигации по разделам модуля
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { currentScreen = EmployeeScreenState.ACTIONS }) { Text("Действия") }
            Button(onClick = { currentScreen = EmployeeScreenState.TRAINING }) { Text("Обучение") }
            // Кнопку Аудита видит только менеджер
            if (checker.can(employee.role, Action.VIEW_AUDIT)) {
                Button(onClick = { currentScreen = EmployeeScreenState.AUDIT }) { Text("Журнал") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Контент в зависимости от выбранной кнопки
        when (currentScreen) {
            EmployeeScreenState.ACTIONS -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Доступные действия:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButton("Приготовить блюдо", Action.MAKE_DISH, employee.role, checker) {}
                    ActionButton("Списать продукты", Action.WRITE_OFF_PRODUCTS, employee.role, checker) {}
                    ActionButton("Применить скидку", Action.APPLICATION_DISCOUNT, employee.role, checker) {}
                    ActionButton("Редактировать рецепт", Action.EDIT_RECIPE, employee.role, checker) {}
                }
            }

            EmployeeScreenState.TRAINING -> {
                TrainingScreen()
            }

            EmployeeScreenState.AUDIT -> {
                // Используем ViewModel команды для журнала
                val auditVm: AuditViewModel = viewModel(
                    factory = AuditViewModelFactory(
                        auditLogDao = dependencies.repository.getAuditLogDao(), // заглушка, см. ниже
                        employeeDao = dependencies.repository.getEmployeeDao(),
                        sessionManager = dependencies.sessionManager,
                        permissionChecker = checker
                    )
                )
                // В реальном проекте тут нужно передавать DAO напрямую.
                // Пока для компиляции можно сделать простую заглушку экрана аудита.
                Text("Журнал аудита (в разработке)")
            }
        }
    }
}

@Composable
fun ActionButton(title: String, action: Action, role: Role, checker: PermissionChecker, onClick: () -> Unit) {
    val access = checker.can(role, action)
    Button(
        onClick = onClick,
        enabled = access,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(if (access) title else "$title (Нет прав)")
    }
}