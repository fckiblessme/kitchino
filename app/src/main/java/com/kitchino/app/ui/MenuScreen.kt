package com.kitchino.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.employee.data.EmployeeEntity
import com.kitchino.app.employee.data.Role
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.ui.theme.StartScreenColor

@Composable
fun MenuScreen(
    viewModel: AuthViewModel,
    onActionClick: (Action) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val authState by viewModel.state.collectAsState()
    val user = (authState as? AuthViewModel.State.LoggedIn)?.user

    MenuScreenContent(
        user = user,
        onActionClick = onActionClick,
        onLogout = onLogout
    )
}

@Composable
fun MenuScreenContent(
    user: EmployeeEntity?,
    onActionClick: (Action) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val checker = PermissionChecker()
    val allowedActions = if (user != null) {
        Action.entries.filter { checker.can(user.role, it) }
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
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Меню",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    user?.let {
                        Text(
                            text = "${it.name} · ${it.role}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Выйти",
                        tint = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allowedActions) { action ->
                        MenuCardItem(action = action, onClick = { onActionClick(action) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCardItem(action: Action, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Меню — менеджер")
@Composable
fun MenuScreenManagerPreview() {
    MenuScreenContent(
        user = EmployeeEntity(
            name = "admin",
            role = Role.MANAGER,
            pinHash = ""
        ),
        onActionClick = {},
        onLogout = {}
    )
}

@Preview(showBackground = true, name = "Меню — без входа")
@Composable
fun MenuScreenLoadingPreview() {
    MenuScreenContent(
        user = null,
        onActionClick = {},
        onLogout = {}
    )
}