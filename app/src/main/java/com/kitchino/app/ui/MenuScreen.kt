package com.kitchino.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.ui.theme.StartScreenColor
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card

@Composable
fun MenuScreen(onActionClick: (Action) -> Unit, viewModel: AuthViewModel) {
    val authState by viewModel.state.collectAsState()
    val role = (authState as? AuthViewModel.State.LoggedIn)?.user?.role
    val checker = PermissionChecker()

    val allowedActions = if (role != null){
        Action.entries.filter { checker.can(role, it) }
    } else {
        emptyList()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StartScreenColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Меню",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 96.dp, bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2)
            ) {
                items(allowedActions) { action ->
                    Card(
                        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onActionClick(action)}
                    ) {
                        Text(
                            text =  when (action) {
                                Action.MAKE_DISH -> "Приготовление блюда"
                                Action.WRITE_OFF_PRODUCTS -> "Списание продуктов"
                                Action.APPLICATION_DISCOUNT -> "Применение скидки"
                                Action.EDIT_RECIPE -> "Редактирование рецепта"
                                Action.VIEW_AUDIT -> "Просмотр аудита"
                                Action.EMPLOYEE_MANAGEMENT -> "Управление сотрудниками"
                            },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}