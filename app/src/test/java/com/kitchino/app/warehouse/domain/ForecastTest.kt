package com.kitchino.app.warehouse.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastTest {

    // FR-05: средний дневной расход
    @Test
    fun emptyHistory_givesZeroAverage() {
        assertEquals(0.0, Forecast.calculateAverageDailyConsumption(emptyList(), 30), 0.0001)
    }

    // 6 дней по 2 кг за 30 дней - 12/30 = 0,4
    @Test
    fun sparseConsumption_dividesByCalendarWindow() {
        val avg = Forecast.calculateAverageDailyConsumption(List(6) { 2.0 }, 30)
        assertEquals(0.4, avg, 0.0001)
    }

    // 6 кг за 2 дня - 3 кг/день, а не 6/30
    @Test
    fun youngProduct_dividesByActualObservationDays() {
        val avg = Forecast.calculateAverageDailyConsumption(listOf(3.0, 3.0), 2)
        assertEquals(3.0, avg, 0.0001)
    }

    // Первое списание сегодня
    @Test
    fun firstConsumptionToday_dividesByOne() {
        assertEquals(2.5, Forecast.calculateAverageDailyConsumption(listOf(2.5), 1), 0.0001)
    }

    // Ограничено 30 днями, даже если списаний в истории больше
    @Test
    fun longHistory_windowCappedAtThirty() {
        val avg = Forecast.calculateAverageDailyConsumption(List(10) { 3.0 }, 30)
        assertEquals(1.0, avg, 0.0001) // 30 / 30
    }

    // FR-05, FR-04

    @Test
    fun daysUntilEmpty_isStockDividedByAverage() {
        assertEquals(4.0, Forecast.calculateDaysUntilEmpty(10.0, 2.0)!!, 0.0001)
    }

    // Нулевой и отрицательный расход
    @Test
    fun zeroOrNegativeConsumption_givesNull() {
        assertNull(Forecast.calculateDaysUntilEmpty(10.0, 0.0))
        assertNull(Forecast.calculateDaysUntilEmpty(10.0, -1.0))
    }

    // Нулевой и отрицательный запас
    @Test
    fun nonPositiveStock_givesZeroDays() {
        assertEquals(0.0, Forecast.calculateDaysUntilEmpty(0.0, 2.0)!!, 0.0001)
        assertEquals(0.0, Forecast.calculateDaysUntilEmpty(-5.0, 2.0)!!, 0.0001)
    }

    // FR-04

    @Test
    fun shouldReorder_whenDaysAreLessThanLeadTime() {
        assertTrue(Forecast.shouldReorder(2.0, 3))
        assertFalse(Forecast.shouldReorder(5.0, 3))
    }

    // Запас ровно равен сроку поставки
    @Test
    fun boundaryDaysEqualToLeadTime_doesNotReorder() {
        assertFalse(Forecast.shouldReorder(3.0, 3))
    }

    // Признак заказа не выставляется
    @Test
    fun noHistory_doesNotReorder() {
        assertFalse(Forecast.shouldReorder(null, 3))
    }

    // Дни до истечения срока годности ----

    @Test
    fun daysUntilExpiry_countsDaysBetween() {
        val day = 86_400_000L
        val now = 1_000_000_000_000L
        assertEquals(2.0, Forecast.calculateDaysUntilExpiry(now + 2 * day, now), 0.0001)
        assertEquals(0.0, Forecast.calculateDaysUntilExpiry(now, now), 0.0001)
        assertEquals(-1.0, Forecast.calculateDaysUntilExpiry(now - day, now), 0.0001)
    }

    // Реальный срок использования ----

    @Test
    fun usableDays_isMinimumOfBothLimits() {
        assertEquals(3.0, Forecast.calculateUsableDays(5.0, 3.0)!!, 0.0001)
        assertEquals(2.0, Forecast.calculateUsableDays(2.0, 3.0)!!, 0.0001)
        assertEquals(4.0, Forecast.calculateUsableDays(4.0, 4.0)!!, 0.0001)
    }

    // Один из сроков неизвестен
    @Test
    fun usableDays_withOneUnknownLimit() {
        assertEquals(5.0, Forecast.calculateUsableDays(5.0, null)!!, 0.0001)
        assertEquals(3.0, Forecast.calculateUsableDays(null, 3.0)!!, 0.0001)
        assertNull(Forecast.calculateUsableDays(null, null))
    }

    // Срок годности истекает раньше, чем кончается запас
    @Test
    fun urgently_whenExpiryIsSoonerThanConsumption() {
        assertTrue(Forecast.shouldUseUrgently(5.0, 3.0))
        assertFalse(Forecast.shouldUseUrgently(5.0, 7.0))
    }

    // Сроки равны - срочности нет
    @Test
    fun boundaryExpiryEqualToConsumption_notUrgent() {
        assertFalse(Forecast.shouldUseUrgently(3.0, 3.0))
    }

    // Без истории расходов срочность считается по сроку годности
    @Test
    fun urgently_withoutConsumptionHistory_usesThreeDayThreshold() {
        assertTrue(Forecast.shouldUseUrgently(null, 2.9))
        assertFalse(Forecast.shouldUseUrgently(null, 3.0))
        assertFalse(Forecast.shouldUseUrgently(null, 10.0))
    }

    // Партий нет
    @Test
    fun noBatches_notUrgent() {
        assertFalse(Forecast.shouldUseUrgently(null, null))
        assertFalse(Forecast.shouldUseUrgently(5.0, null))
    }

    // Повторный вызов с теми же данными даёт тот же результат ----

    @Test
    fun repeatedCalls_returnSameResult() {
        val data = listOf(2.0, 2.0, 2.0)
        assertEquals(
            Forecast.calculateAverageDailyConsumption(data, 30),
            Forecast.calculateAverageDailyConsumption(data, 30),
            0.0
        )
        assertEquals(
            Forecast.calculateDaysUntilEmpty(10.0, 2.0),
            Forecast.calculateDaysUntilEmpty(10.0, 2.0)
        )
        assertEquals(Forecast.shouldReorder(2.0, 3), Forecast.shouldReorder(2.0, 3))
        assertEquals(Forecast.shouldUseUrgently(5.0, 3.0), Forecast.shouldUseUrgently(5.0, 3.0))
    }
}