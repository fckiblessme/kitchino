package com.kitchino.app.warehouse.domain

// Алгоритм прогноза расхода продуктов
object Forecast {
    //  Вычисление среднего арифметического по последним дням
    fun calculateAverageDailyConsumption(
        dailyConsumption: List<Double>,
        windowSize:Int
    ):Double{
        if(dailyConsumption.isEmpty()){
            return 0.0
        }
        val receptDays = dailyConsumption.takeLast(windowSize)
        return receptDays.average()
    }
    // Рассчет на сколько дней хватит текущих запасов
    fun calculateDaysUntilEmpty(
        currentStock: Double,
        averageDailyConsumption: Double)
    :Double? {
        if (averageDailyConsumption <= 0.0) {
            return null
        }
        val days = currentStock / averageDailyConsumption
        if (days < 0.0) {
            return 0.0
        }
        return days
    }

    // Проверка существует ли необходимость заказывать продукт
    fun shouldReorder(
        daysUntilEmpty:Double?,
        leadTimeDays: Int
    ): Boolean {
        if (daysUntilEmpty == null){
            return false
        }
        return daysUntilEmpty<leadTimeDays
    }
}