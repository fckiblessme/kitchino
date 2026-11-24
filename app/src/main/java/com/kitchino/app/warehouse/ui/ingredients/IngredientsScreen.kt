package com.kitchino.app.warehouse.ui.ingredients

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.kitchino.app.ui.theme.StartScreenColor
import com.kitchino.app.warehouse.domain.IngredientForecast

@Composable
fun IngredientsScreen(
    uiState: IngredientsUiState,
    viewModel: IngredientsViewModel
) {
    val supplyState by viewModel.supplyState.collectAsState()
    val context = LocalContext.current
    var ingredientToSupply by remember { mutableStateOf<IngredientForecast?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StartScreenColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
                uiState.forecasts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("На складе пока ничего нет", color = Color.DarkGray)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.forecasts) { forecast ->
                            IngredientCard(
                                forecast = forecast,
                                onSupplyClick = { ingredientToSupply = forecast }
                            )
                        }
                    }
                }
            }
        }
    }

    ingredientToSupply?.let { forecast ->
        SupplyDialog(
            forecast = forecast,
            isSupplying = supplyState.isSupplying,
            errorMessage = supplyState.error,
            onDismiss = {
                ingredientToSupply = null
                viewModel.clearSupplyState()
            },
            onConfirm = { amount, days, price ->
                viewModel.supplyIngredient(
                    ingredientId = forecast.ingredientId,
                    amount = amount,
                    expirationDays = days,
                    pricePerUnit = price,
                    onComplete = { success, message ->
                        if (success) {
                            Toast.makeText(context, "Запас пополнен", Toast.LENGTH_SHORT).show()
                            ingredientToSupply = null
                        } else {
                            Toast.makeText(context, message ?: "Ошибка", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun IngredientCard(
    forecast: IngredientForecast,
    onSupplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = forecast.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatNumber(forecast.currentStock)} ${forecast.unit}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onSupplyClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Пополнить",
                            tint = Color.Black
                        )
                    }
                }
            }
            Text(
                text = buildDaysUntilEmptyText(forecast.daysUntilEmpty),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = buildDaysUntilExpiryText(forecast.daysUntilExpiry),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = buildUsableDaysText(forecast.usableDays),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            if (forecast.shouldUseUrgently) {
                AlertBadge(
                    text = "Срочно использовать — скоро испортится",
                    color = Color(0xFFD32F2F)
                )
            }
            if (forecast.shouldReorder) {
                AlertBadge(
                    text = "Пора заказывать",
                    color = Color(0xFFF57C00)
                )
            }
        }
    }
}

@Composable
private fun SupplyDialog(
    forecast: IngredientForecast,
    isSupplying: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, expirationDays: Int, pricePerUnit: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val days = daysText.toIntOrNull() ?: 0
    val price = priceText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Пополнить запас",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ингредиент: ${forecast.name}",
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() || it == '.' || it == ',' }
                            .replace(',', '.')
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Количество (${forecast.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { input ->
                        daysText = input.filter { it.isDigit() }.take(4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Срок годности (дней)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        priceText = input.filter { it.isDigit() || it == '.' || it == ',' }
                            .replace(',', '.')
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Цена за единицу") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = Color(0xFFD32F2F))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount, days, price) },
                enabled = !isSupplying && amount > 0 && days > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(if (isSupplying) "Пополняем..." else "Пополнить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.DarkGray)
            }
        }
    )
}

@Composable
private fun AlertBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatNumber(value: Double): String {
    return ((value * 100).toLong() / 100.0).toString()
}

private fun formatDays(days: Double): String {
    return days.toInt().toString()
}

private fun buildDaysUntilEmptyText(daysUntilEmpty: Double?): String {
    if (daysUntilEmpty == null) {
        return "Расход по нулю — продукт не используется"
    }
    return "Хватит по расходу: ${formatDays(daysUntilEmpty)} дней"
}

private fun buildDaysUntilExpiryText(daysUntilExpiry: Double?): String {
    if (daysUntilExpiry == null) {
        return "Срок годности: партий с остатком нет"
    }
    if (daysUntilExpiry < 0.0) {
        return "Срок годности: ПРОСРОЧЕНО"
    }
    return "До истечения срока: ${formatDays(daysUntilExpiry)} дней"
}

private fun buildUsableDaysText(usableDays: Double?): String {
    if (usableDays == null) {
        return "Реальное использование: неизвестно"
    }
    return "Реальное использование: ${formatDays(usableDays)} дней"
}