package com.kitchino.app.warehouse.domain



import com.kitchino.app.warehouse.data.repository.WarehouseRepository

data class IngredientForecast(
    val ingredientId: Long,
    val name: String,
    val unit: String,
    val currentStock: Double,
    val daysUntilEmpty: Double?,
    val daysUntilExpiry: Double?,
    val usableDays: Double?,
    val shouldReorder: Boolean,
    val shouldUseUrgently: Boolean
)

// Расчёт прогноза по ингредиенту
class ForecastUseCase(
    private val repository: WarehouseRepository
) {

    companion object {
        private const val HISTORY_WINDOW_DAYS = 30
        private const val MILLIS_PER_DAY = 86_400_000L
    }

    suspend fun execute(ingredientId: Long): IngredientForecast? {
        // Паспорт ингредиента
        val ingredient = repository.getIngredientById(ingredientId)
            ?: return null

        // Текущее количество на складе
        val currentStock = repository.getTotalStock(ingredientId)

        // История списаний за последние 30 дней
        val since = System.currentTimeMillis() - HISTORY_WINDOW_DAYS * MILLIS_PER_DAY
        val daily = repository.getDailyConsumptionSince(ingredientId, since)

        // Средний дневной расход
        val dailyAmounts = daily.map { it.totalAmount }
        val averageDailyConsumption = Forecast.calculateAverageDailyConsumption(
            dailyAmounts,
            HISTORY_WINDOW_DAYS
        )

        // Хватит на N дней
        val daysUntilEmpty = Forecast.calculateDaysUntilEmpty(
            currentStock,
            averageDailyConsumption
        )

        // Ближайший срок годности среди партий
        val earliestExpiration = repository.getEarliestExpiration(ingredientId)
        val daysUntilExpiry = if (earliestExpiration == null) {
            null
        } else {
            Forecast.calculateDaysUntilExpiry(
                earliestExpiration,
                System.currentTimeMillis()
            )
        }

        val usableDays = Forecast.calculateUsableDays(daysUntilEmpty, daysUntilExpiry)

        // Пора ли заказывать / срочно ли использовать
        val reorder = Forecast.shouldReorder(daysUntilEmpty, ingredient.leadTimeDays)
        val urgent = Forecast.shouldUseUrgently(daysUntilEmpty, daysUntilExpiry)

        return IngredientForecast(
            ingredientId = ingredientId,
            name = ingredient.name,
            unit = ingredient.unit,
            currentStock = currentStock,
            daysUntilEmpty = daysUntilEmpty,
            daysUntilExpiry = daysUntilExpiry,
            usableDays = usableDays,
            shouldReorder = reorder,
            shouldUseUrgently = urgent
        )
    }
}