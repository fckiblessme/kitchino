package com.kitchino.app.dishbatch.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun DishBatchScreen(viewModel: DishBatchViewModel){
    val batches by viewModel.activeBatchesWithDecisions.collectAsState()
    LazyColumn() {
        items(batches) {
            item -> Text("Партия #${item.dishBatchEntity.idDishBatch}")
        }
    }
}