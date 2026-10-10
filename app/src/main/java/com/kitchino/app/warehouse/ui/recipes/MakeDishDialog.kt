package com.kitchino.app.warehouse.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchino.app.core.contracts.ConsumeResult


@Composable
fun MakeDishDialog(
    recipes: List<RecipeWithComposition>,
    viewModel: MakeDishViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Приготовить блюдо") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {


                Text("Выберите рецепт:", fontWeight = FontWeight.Medium)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(recipes) { recipe ->
                        val isSelected = state.selectedRecipeId == recipe.recipeId
                        TextButton(
                            onClick = { viewModel.selectRecipe(recipe.recipeId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSelected) "✓ ${recipe.dishName}" else recipe.dishName,
                                color = if (isSelected) Color(0xFF2E7D32) else Color.Unspecified
                            )
                        }
                    }
                }


                OutlinedTextField(
                    value = state.batchSize.toString(),
                    onValueChange = { text ->
                        val number = text.toIntOrNull()
                        if (number != null) {
                            viewModel.setBatchSize(number)
                        }
                    },
                    label = { Text("Порций") },
                    modifier = Modifier.fillMaxWidth()
                )


                when (val result = state.result) {
                    null -> Unit
                    is ConsumeResult.Success -> {
                        Text(
                            text = "Готово! Ингредиенты списаны со склада.",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is ConsumeResult.RecipeNotFound -> {
                        Text(
                            text = "Рецепт не найден.",
                            color = Color(0xFFD32F2F)
                        )
                    }
                    is ConsumeResult.InsufficientStock -> {
                        Text(
                            text = "Не хватает ингредиентов:",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                        result.missing.forEach { missing ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${missing.name}",
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "не хватает ${missing.missingAmount} ${missing.unit}",
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.makeDish() },
                enabled = state.selectedRecipeId != null && !state.isSubmitting
            ) {
                Text(if (state.isSubmitting) "Готовим..." else "Приготовить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}