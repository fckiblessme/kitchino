package com.kitchino.app.dishbatch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ScreenHorizontalPadding = 16.dp
private val ScreenVerticalPadding = 12.dp
private val CardSpacing = 10.dp
private val CardCorner = RoundedCornerShape(18.dp)
private val CardInnerPadding = 16.dp
private val ButtonCorner = RoundedCornerShape(12.dp)
private val ChipRowPadding = 4.dp

private val UrgentThresholdMs = 60 * 60 * 1000L
private val UrgentBorderColor = Color(0xFFD32F2F)
private val DayThresholdMs = 24 * 60 * 60 * 1000L

private const val SAMPLE_MADE_AT = 1_760_000_000_000L
private const val SAMPLE_LIFETIME = 3_600_000L

private enum class BatchFilter(val label: String) {
    ALL("Все"),
    FRESH("Свежие"),
    DISCOUNTED("Со скидкой"),
    WRITE_OFF("На списание")
}

private enum class BatchSort(val label: String) {
    BY_EXPIRY("По истечении"),
    BY_QUANTITY("По количеству"),
    BY_NAME("По названию")
}

@Composable
fun DishBatchScreen(viewModel: DishBatchViewModel) {
    val batches by viewModel.activeBatchesWithDecisions.collectAsState()
    DishBatchContent(
        batches = batches,
        onApply = { item -> viewModel.applyDecision(item) },
        onSell = { item, qty -> viewModel.recordSale(item, qty) },
        onWriteOff = { item -> viewModel.writeOffBatch(item) }
    )
}

@Composable
fun DishBatchContent(
    batches: List<BatchUI>,
    onApply: (BatchUI) -> Unit,
    onSell: (BatchUI, Int) -> Unit,
    onWriteOff: (BatchUI) -> Unit
) {
    if (batches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет партий блюд")
        }
        return
    }

    var searchText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(BatchFilter.ALL) }
    var sort by remember { mutableStateOf(BatchSort.BY_EXPIRY) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val visibleBatches = remember(batches, searchText, filter, sort) {
        val query = searchText.trim()
        batches
            .filter { item ->
                val matchesText = item.dishName.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    BatchFilter.ALL -> true
                    BatchFilter.FRESH -> item.discountDecision.decisionType == DecisionType.NO_ACTION
                    BatchFilter.DISCOUNTED -> item.discountDecision.decisionType == DecisionType.DISCOUNT
                    BatchFilter.WRITE_OFF -> item.discountDecision.decisionType == DecisionType.WRITE_OFF
                }
                matchesText && matchesFilter
            }
            .sortedWith(
                when (sort) {
                    BatchSort.BY_EXPIRY -> compareBy { it.dishBatchEntity.expiresAt }
                    BatchSort.BY_QUANTITY -> compareByDescending { it.dishBatchEntity.quantity }
                    BatchSort.BY_NAME -> compareBy { it.dishName.lowercase(Locale.getDefault()) }
                }
            )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = ChipRowPadding),
                label = { Text("Поиск по названию блюда") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = ScreenHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatchFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = ScreenHorizontalPadding, vertical = ChipRowPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Сортировка:", style = MaterialTheme.typography.bodySmall)
                BatchSort.entries.forEach { s ->
                    FilterChip(
                        selected = sort == s,
                        onClick = { sort = s },
                        label = { Text(s.label) }
                    )
                }
            }

            val expiringSoon = visibleBatches.count {
                val remaining = it.dishBatchEntity.expiresAt - nowMillis
                remaining in 1..DayThresholdMs
            }
            val discountCount = visibleBatches.count {
                it.discountDecision.decisionType == DecisionType.DISCOUNT
            }
            Text(
                "Всего партий: ${visibleBatches.size} · Со скидкой: $discountCount · Истекает сегодня: $expiringSoon",
                modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (visibleBatches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ничего не найдено по заданным фильтрам")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = ScreenHorizontalPadding,
                        vertical = ScreenVerticalPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(CardSpacing)
                ) {
                    items(
                        visibleBatches,
                        key = { it.dishBatchEntity.idDishBatch }
                    ) { item ->
                        SwipeableBatchCard(
                            item = item,
                            nowMillis = nowMillis,
                            snackbarHostState = snackbarHostState,
                            onApply = onApply,
                            onSell = onSell,
                            onWriteOff = onWriteOff
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


private fun decisionMessage(item: BatchUI): String = when (item.discountDecision.decisionType) {
    DecisionType.NO_ACTION ->
        "Партия #${item.dishBatchEntity.idDishBatch} оставлена без изменений"
    DecisionType.DISCOUNT ->
        "Скидка ${item.discountDecision.discountPercent?.toInt()}% применена к партии «${item.dishName}»"
    DecisionType.WRITE_OFF ->
        "Партия «${item.dishName}» списана"
}

private fun remainingText(remainingMs: Long): String = when {
    remainingMs <= 0 -> "просрочено"
    remainingMs < 60_000L -> "менее минуты"
    remainingMs < UrgentThresholdMs -> "через ${remainingMs / 60_000L} мин"
    else -> "через ${remainingMs / 3_600_000L} ч ${(remainingMs % 3_600_000L) / 60_000L} мин"
}

@Composable
private fun SwipeableBatchCard(
    item: BatchUI,
    nowMillis: Long,
    snackbarHostState: SnackbarHostState,
    onApply: (BatchUI) -> Unit,
    onSell: (BatchUI, Int) -> Unit,
    onWriteOff: (BatchUI) -> Unit
) {
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (item.discountDecision.decisionType == DecisionType.NO_ACTION) {
                        scope.launch {
                            snackbarHostState.showSnackbar("По этой партии скидка не рекомендована")
                        }
                        false
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(decisionMessage(item)) }
                        onApply(item)
                        true
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Партия «${item.dishName}» списана")
                    }
                    onWriteOff(item)
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val background: Color
            val label: String
            val icon: androidx.compose.ui.graphics.vector.ImageVector
            val contentAlignment: Alignment
            when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    background = LightRed
                    label = "Применить скидку"
                    icon = Icons.Filled.Check
                    contentAlignment = Alignment.CenterStart
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    background = LightOrange
                    label = "Списать"
                    icon = Icons.Filled.Delete
                    contentAlignment = Alignment.CenterEnd
                }
                else -> {
                    background = Color.Transparent
                    label = ""
                    icon = Icons.Filled.Check
                    contentAlignment = Alignment.CenterStart
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = ChipRowPadding)
                    .clip(CardCorner)
                    .background(background),
                contentAlignment = contentAlignment
            ) {
                if (label.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(label)
                    }
                }
            }
        }
    ) {
        DishBatchCard(
            item = item,
            nowMillis = nowMillis,
            onApply = {
                scope.launch { snackbarHostState.showSnackbar(decisionMessage(item)) }
                onApply(item)
            },
            onSell = { qty -> onSell(item, qty) }
        )
    }
}

@Composable
private fun DishBatchCard(
    item: BatchUI,
    nowMillis: Long,
    onApply: () -> Unit,
    onSell: (Int) -> Unit
) {
    val (backgroundColor, badgeText) = when (item.discountDecision.decisionType) {
        DecisionType.NO_ACTION -> LightGreen to "Без действия"
        DecisionType.DISCOUNT -> LightRed to "Скидка ${item.discountDecision.discountPercent?.toInt()}%"
        DecisionType.WRITE_OFF -> LightOrange to "Списание"
    }
    val remaining = item.dishBatchEntity.expiresAt - nowMillis
    val isUrgent = remaining in 1..UrgentThresholdMs

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardCorner,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isUrgent) BorderStroke(2.dp, UrgentBorderColor) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardInnerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Партия #${item.dishBatchEntity.idDishBatch}")
            Text(badgeText)
            Text("Блюдо: ${item.dishName}, приготовил: ${item.employeeName}")
            Text("Количество: ${item.dishBatchEntity.quantity}")
            Text("Изготовлено: ${formatTime(item.dishBatchEntity.madeAt)}")
            Text("Истекает: ${formatTime(item.dishBatchEntity.expiresAt)} (${remainingText(remaining)})")
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

            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = ButtonCorner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DishButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text("Продать")
            }

            if (showDialog) {
                SellDialog(
                    item = item,
                    onConfirm = onSell,
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
private fun SellDialog(
    item: BatchUI,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var quantityText by remember { mutableStateOf("") }
    val quantity = quantityText.toIntOrNull() ?: 0
    val batchQuantity = item.dishBatchEntity.quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Продажа", modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Блюдо: ${item.dishName}, осталось: $batchQuantity")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            if ((digits.toIntOrNull() ?: 0) <= batchQuantity) {
                                quantityText = digits
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Количество") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    IconButton(
                        onClick = { quantityText = (quantity - 1).coerceAtLeast(0).toString() },
                        enabled = quantity > 0
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Минус один")
                    }
                    IconButton(
                        onClick = { quantityText = (quantity + 1).coerceAtMost(batchQuantity).toString() },
                        enabled = quantity < batchQuantity
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Плюс один")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(quantity)
                    onDismiss()
                },
                enabled = quantity in 1..batchQuantity
            ) {
                Text("Продать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun formatTime(millis: Long): String {
    val formatterer = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatterer.format(Date(millis))
}

private fun sampleBatch(
    id: Long,
    decisionType: DecisionType,
    discountPercent: Double?,
    reason: String,
    dishName: String,
    employeeName: String,
    expiresInMs: Long = SAMPLE_LIFETIME
) = BatchUI(
    dishBatchEntity = DishBatchEntity(
        idDishBatch = id,
        idDish = 1,
        idEmployee = 1,
        quantity = 5,
        madeAt = SAMPLE_MADE_AT,
        expiresAt = SAMPLE_MADE_AT + expiresInMs,
        status = Status.ACTIVE
    ),
    discountDecision = DiscountDecision(decisionType, discountPercent, reason),
    dishName = dishName,
    employeeName = employeeName
)

@Preview(showBackground = true, widthDp = 360, name = "Три решения")
@Composable
private fun DishBatchContentPreview() {
    KitchinoTheme {
        DishBatchContent(
            batches = listOf(
                sampleBatch(1, DecisionType.NO_ACTION, null, "Срок годности еще не подходит к концу", "Борщ", "Админ"),
                sampleBatch(2, DecisionType.DISCOUNT, 20.0, "Менее 50% от срока жизни блюда", "Паста Карбонара", "Иван Петров"),
                sampleBatch(3, DecisionType.WRITE_OFF, null, "Менее 5% от срока жизни блюда", "Салат Цезарь", "Мария Смирнова", expiresInMs = 25 * 60_000L)
            ),
            onApply = {},
            onSell = { _, _ -> },
            onWriteOff = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Пустой список")
@Composable
private fun DishBatchEmptyPreview() {
    KitchinoTheme {
        DishBatchContent(
            batches = emptyList(),
            onApply = {},
            onSell = { _, _ -> },
            onWriteOff = {}
        )
    }
}