package com.kitchino.app.employee.ui.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchino.app.employee.data.AuditLogEntity
import com.kitchino.app.ui.theme.LightGreen
import com.kitchino.app.ui.theme.LightRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@Composable
fun AuditScreen(uiState: AuditUiState) {
    if (!uiState.canView) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Доступ только для менеджера")
        }
        return
    }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.tamperedId == null) LightGreen else LightRed
            )
        ) {
            Text(
                text = if (uiState.tamperedId == null) "Цепочка журнала цела"
                else "Нарушена запись #${uiState.tamperedId}",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Журнал пуст")
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.logs) { log ->
                AuditLogCard(log, uiState.employeeNames[log.actorId])
            }
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLogEntity, actorName: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("#${log.id}  ${timeFormat.format(Date(log.timestamp))}", fontWeight = FontWeight.Bold)
            Text("Сотрудник: ${actorName ?: "id=${log.actorId}"}")
            Text("Действие: ${log.action}")
            Text(log.payload)
        }
    }
}