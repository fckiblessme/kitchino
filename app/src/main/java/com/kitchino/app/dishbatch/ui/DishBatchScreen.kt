package com.kitchino.app.dishbatch.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DishBatchScreen(viewModel: DishBatchViewModel){
    val batches by viewModel.activeBatchesWithDecisions.collectAsState()
    if (batches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет партий блюд")
        }
        return
    }
    LazyColumn() {
        items(batches) {
            item ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)){
                    Text("Партия #${item.dishBatchEntity.idDishBatch}")
                    Text("Блюдо ID: ${item.dishBatchEntity.idDish}, сотрудник ID: ${item.dishBatchEntity.idEmployee}")
                    Text("Количество: ${item.dishBatchEntity.quantity}")
                    Text("Изготовлено: ${formatTime(item.dishBatchEntity.madeAt)}")
                    Text("Истекает: ${formatTime(item.dishBatchEntity.expiresAt)}")
                    Text("Рекомендация: ${item.discountDecision.decisionType}, ${item.discountDecision.discountReason}")
                }
            }
        }
    }
}

fun formatTime(millis: Long) : String {
    val formatterer = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatterer.format(Date(millis))
}