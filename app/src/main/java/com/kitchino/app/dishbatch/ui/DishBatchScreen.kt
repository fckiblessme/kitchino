package com.kitchino.app.dishbatch.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.domain.DiscountDecision
import com.kitchino.app.ui.theme.DishButtonColor
import com.kitchino.app.ui.theme.KitchinoTheme
import com.kitchino.app.ui.theme.LightGreen
import com.kitchino.app.ui.theme.LightOrange
import com.kitchino.app.ui.theme.LightRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ScreenHorizontalPadding = 16.dp
private val ScreenVerticalPadding = 12.dp
private val CardSpacing = 10.dp
private val CardCorner = RoundedCornerShape(18.dp)
private val CardInnerPadding = 16.dp
private val ButtonCorner = RoundedCornerShape(12.dp)

private const val SAMPLE_MADE_AT = 1_760_000_000_000L
private const val SAMPLE_LIFETIME = 3_600_000L

@Composable
fun DishBatchScreen(viewModel: DishBatchViewModel) {
    val batches by viewModel.activeBatchesWithDecisions.collectAsState()
    DishBatchContent(
        batches = batches,
        onApply = { item -> viewModel.applyDecision(item) }
    )
}

@Composable
fun DishBatchContent(batches: List<BatchUI>, onApply: (BatchUI) -> Unit) {
    if (batches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет партий блюд")
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = ScreenHorizontalPadding,
            vertical = ScreenVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(CardSpacing)
    ) {
        items(batches) { item ->
            DishBatchCard(
                item = item,
                onApply = { onApply(item) }
            )
        }
    }
}

@Composable
private fun DishBatchCard(item: BatchUI, onApply: () -> Unit) {
    val (backgroundColor, badgeText) = when (item.discountDecision.decisionType) {
        DecisionType.NO_ACTION -> LightGreen to "Без действия"
        DecisionType.DISCOUNT -> LightRed to "Скидка ${item.discountDecision.discountPercent?.toInt()}%"
        DecisionType.WRITE_OFF -> LightOrange to "Списание"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardCorner,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardInnerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Партия #${item.dishBatchEntity.idDishBatch}")
            Text(badgeText)
            Text("Блюдо ID: ${item.dishBatchEntity.idDish}, сотрудник ID: ${item.dishBatchEntity.idEmployee}")
            Text("Количество: ${item.dishBatchEntity.quantity}")
            Text("Изготовлено: ${formatTime(item.dishBatchEntity.madeAt)}")
            Text("Истекает: ${formatTime(item.dishBatchEntity.expiresAt)}")
            Text("Рекомендация: ${item.discountDecision.discountReason}")

            Button(
                onClick = onApply,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = ButtonCorner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DishButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text("Применить")
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val formatterer = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatterer.format(Date(millis))
}


private fun sampleBatch(
    id: Long,
    decisionType: DecisionType,
    discountPercent: Double?,
    reason: String
) = BatchUI(
    dishBatchEntity = DishBatchEntity(
        idDishBatch = id,
        idDish = 1,
        idEmployee = 1,
        quantity = 5,
        madeAt = SAMPLE_MADE_AT,
        expiresAt = SAMPLE_MADE_AT + SAMPLE_LIFETIME,
        status = Status.ACTIVE
    ),
    discountDecision = DiscountDecision(decisionType, discountPercent, reason)
)

@Preview(showBackground = true, widthDp = 360, name = "Три решения")
@Composable
private fun DishBatchContentPreview() {
    KitchinoTheme {
        DishBatchContent(
            batches = listOf(
                sampleBatch(1, DecisionType.NO_ACTION, null, "Срок годности еще не подходит к концу"),
                sampleBatch(2, DecisionType.DISCOUNT, 20.0, "Менее 50% от срока жизни блюда"),
                sampleBatch(3, DecisionType.WRITE_OFF, null, "Менее 5% от срока жизни блюда")
            ),
            onApply = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Пустой список")
@Composable
private fun DishBatchEmptyPreview() {
    KitchinoTheme {
        DishBatchContent(batches = emptyList(), onApply = {})
    }
}
