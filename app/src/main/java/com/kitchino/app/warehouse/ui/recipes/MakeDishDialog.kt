package com.kitchino.app.warehouse.ui.recipes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitchino.app.core.contracts.ConsumeResult

@Composable
fun MakeDishDialog(
    recipeId: Long,
    recipeName: String,
    viewModel: MakeDishViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var quantityText by remember(recipeId) { mutableStateOf("1") }

    LaunchedEffect(recipeId) {
        viewModel.selectRecipe(recipeId)
        viewModel.setBatchSize(1)
    }

    val quantity = quantityText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Приготовить блюдо",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Блюдо: $recipeName",
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Укажите количество порций",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            quantityText = digits
                            viewModel.setBatchSize(digits.toIntOrNull() ?: 0)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Порций") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            val newValue = (quantity - 1).coerceAtLeast(1)
                            quantityText = newValue.toString()
                            viewModel.setBatchSize(newValue)
                        },
                        enabled = quantity > 1
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Минус один")
                    }
                    IconButton(
                        onClick = {
                            val newValue = (quantity + 1).coerceAtMost(999)
                            quantityText = newValue.toString()
                            viewModel.setBatchSize(newValue)
                        },
                        enabled = quantity < 999
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Плюс один")
                    }
                }

                when (val result = state.result) {
                    null -> Unit
                    is ConsumeResult.Success -> Unit
                    is ConsumeResult.RecipeNotFound -> {
                        Text("Рецепт не найден.", color = Color(0xFFD32F2F))
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
                onClick = {
                    viewModel.makeDish { result ->
                        when (result) {
                            is ConsumeResult.Success -> {
                                Toast.makeText(
                                    context,
                                    "«$recipeName» приготовлено! Ингредиенты списаны",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            }
                            is ConsumeResult.RecipeNotFound -> {
                                Toast.makeText(
                                    context,
                                    "Рецепт не найден",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is ConsumeResult.InsufficientStock -> {
                                Toast.makeText(
                                    context,
                                    "Недостаточно ингредиентов на складе",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                enabled = state.selectedRecipeId != null && !state.isSubmitting && quantity > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(if (state.isSubmitting) "Готовим..." else "Приготовить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.DarkGray)
            }
        }
    )
}