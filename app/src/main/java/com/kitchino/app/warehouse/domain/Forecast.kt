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
    // Расчет на сколько дней хватит текущих запасов
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

    // Проверка сколько дней осталось до истечения срока годности партии
    fun calculateDaysUntilExpiry(
        expiryDateMillis: Long,
        currentTimeMillis: Long
    ): Double {
        val millisecondsPerDay = 86_400_000.0
        val difference = expiryDateMillis - currentTimeMillis
        return difference / millisecondsPerDay
    }

    // Расчет сколько дней продукт можно использовать
    fun calculateUsableDays(
        daysUntilEmpty: Double?,
        daysUntilExpiry: Double?
    ): Double? {
        if (daysUntilEmpty == null && daysUntilExpiry == null) {
            return null
        }
        if (daysUntilEmpty == null) {
            return daysUntilExpiry
        }
        if (daysUntilExpiry == null) {
            return daysUntilEmpty
        }
        return minOf(daysUntilEmpty, daysUntilExpiry)
    }

    // Срочно ли надо использовать продукт
    fun shouldUseUrgently(
        daysUntilEmpty: Double?,
        daysUntilExpiry: Double?
    ): Boolean {
        if (daysUntilExpiry == null) {
            return false
        }
        if (daysUntilEmpty == null) {
            return daysUntilExpiry < 3.0
        }
        return daysUntilExpiry < daysUntilEmpty
    }
}