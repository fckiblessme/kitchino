package com.kitchino.app.employee.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitchino.app.employee.data.Role
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.PermissionChecker

enum class EmployeeScreenState {LIST, MENU, ACTIONS, TRAINING}

@Composable
fun EmployeeScreen() {
    var currentScreen by remember {mutableStateOf(EmployeeScreenState.LIST)}
    var currentRole by remember { mutableStateOf(Role.COOK) }

    when (currentScreen) {
        EmployeeScreenState.LIST -> EmployeeScreen()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.padding(vertical = 8.dp)) {}
        Button(onClick = {currentRole = Role.COOK}) {Text("Повар")}
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {currentRole = Role.CASHIER}) {Text("Кассир")}
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {currentRole = Role.MANAGER}) {Text("Менеджер")}
        Spacer(modifier = Modifier.width(16.dp))
    }
    Text("Текущая роль: $currentRole", style = MaterialTheme.typography.titleLarge)

    ActionButton("Проиготовить блюдо", Action.MAKE_DISH, currentRole, checker)
    ActionButton("Списать продукты", Action.WRITE_OFF_PRODUCTS, currentRole, checker)
    ActionButton("Применить скидку", Action.APPLICATION_DISCOUNT, currentRole, checker)
}

@Composable
fun ActionButton(title: String, action: Action, role: Role, checker: PermissionChecker) {
    val access = checker.can(role, action)

    Button(onClick = {}, enabled = access, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(if (access) "$title" else "$title (Нет прав)")
    }

}

