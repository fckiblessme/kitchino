package com.kitchino.app.dishbatch.domain

// Расчет темпа продаж блюда по журналу продаж
object SalesVelocity {
    // Минимальное окно наблюдения в миллисекундах
    // Наблюдение короче часа считается как час: одна порция пять минут назад
    // это не 12 порций в час, а случайность. Занижение темпа безопаснее завышения:
    // по заниженному темпу скидка придет раньше, по завышенному партия уйдет в списание
    private const val MIN_OBSERVATION_MILLIS = 3_600_000.0
    private const val MILLISECONDS_PER_HOUR = 3_600_000.0

    // Скорость продаж в порциях в час
    // null означает что посчитать нельзя: нет продаж либо вход противоречив
    fun calculatePortionsPerHour(
        soldQuantity: Int?,
        earliestSaleMillis: Long?,
        currentTimeMillis: Long
    ): Double? {
        // Продаж не было, окна наблюдения не существует
        if (soldQuantity == null || soldQuantity <= 0) {
            return null
        }
        if (earliestSaleMillis == null) {
            return null
        }
        // Продажа позже текущего момента. Сбитые часы или испорченные данные,
        // а не короткое наблюдение, поэтому нижняя граница тут не применяется
        if (earliestSaleMillis > currentTimeMillis) {
            return null
        }
        val observedMillis = (currentTimeMillis - earliestSaleMillis).toDouble()
        // Знаменатель не бывает нулем: ноль миллисекунд поднимается до часа
        val denominatorMillis = maxOf(observedMillis, MIN_OBSERVATION_MILLIS)
        val observedHours = denominatorMillis / MILLISECONDS_PER_HOUR
        return soldQuantity / observedHours
    }

    // Сколько часов нужно чтобы продать остаток партии текущим темпом
    fun calculateHoursToSellOut(
        remainingQuantity: Int,
        portionsPerHour: Double?
    ): Double? {
        if (portionsPerHour == null || portionsPerHour <= 0.0) {
            return null
        }
        if (remainingQuantity <= 0) {
            return 0.0
        }
        return remainingQuantity / portionsPerHour
    }
}
