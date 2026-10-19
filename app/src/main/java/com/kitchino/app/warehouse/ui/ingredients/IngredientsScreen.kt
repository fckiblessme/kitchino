package com.kitchino.app.warehouse.ui.ingredients

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitchino.app.warehouse.domain.IngredientForecast


@Composable
fun IngredientsScreen(
    uiState: IngredientsUiState
) {

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }


    if (uiState.forecasts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("На складе пока ничего нет")
        }
        return
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.forecasts) { forecast ->
            IngredientCard(forecast)
        }
    }
}


@Composable
private fun IngredientCard(forecast: IngredientForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = forecast.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatNumber(forecast.currentStock)} ${forecast.unit}",
                    style = MaterialTheme.typography.titleMedium
                )
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