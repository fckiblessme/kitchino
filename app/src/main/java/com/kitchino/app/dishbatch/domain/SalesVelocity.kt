package com.kitchino.app.dishbatch.domain

object SalesVelocity {
    private const val MIN_OBSERVATION_MILLIS = 3_600_000.0
    private const val MILLISECONDS_PER_HOUR = 3_600_000.0

    const val OBSERVATION_WINDOW_MILLIS = 12 * 3_600_000.0

    fun calculatePortionsPerHour(
        soldQuantity: Int?,
        earliestSaleMillis: Long?,
        currentTimeMillis: Long,
        observationWindowMillis: Double = MIN_OBSERVATION_MILLIS
    ): Double? {
        if (soldQuantity == null || soldQuantity <= 0) {
            return null
        }
        if (earliestSaleMillis == null) {
            return null
        }
        if (earliestSaleMillis > currentTimeMillis) {
            return null
        }
        val observedMillis = (currentTimeMillis - earliestSaleMillis).toDouble()
        val denominatorMillis = maxOf(observedMillis, observationWindowMillis, MIN_OBSERVATION_MILLIS)
        val observedHours = denominatorMillis / MILLISECONDS_PER_HOUR
        return soldQuantity / observedHours
    }

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
