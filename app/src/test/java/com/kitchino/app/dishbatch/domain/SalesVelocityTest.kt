package com.kitchino.app.dishbatch.domain

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SalesVelocityTest {
    private companion object {
        const val NOW = 10_000_000L
        const val HOUR = 3_600_000L
        const val MINUTE = 60_000L
    }

    private fun velocityAgo(sold: Int?, millisAgo: Long) =
        SalesVelocity.calculatePortionsPerHour(sold,
            NOW - millisAgo, NOW)

    private fun sellOut(remaining: Int, velocity: Double?) =
        SalesVelocity.calculateHoursToSellOut(remaining, velocity)

    // ---------- calculatePortionsPerHour — классы эквивалентности ----------
    @Test
    fun normalObservation_computesRate() {
        assertEquals(5.0,velocityAgo(10, 2 * HOUR)!!, 0.0)
    }

    @Test
    fun longObservation_computesRate(){
        assertEquals(4.0, velocityAgo(24, 6 * HOUR)!!, 0.0)
    }

    @Test
    fun shortObservation_isFlooredToOneHour() {
        assertEquals(1.0, velocityAgo(1, 5 * MINUTE)!!, 0.0)
    }

    // ----------- Граничные значения ----------
    @Test
    fun boundaryExactlyOneHour_isRate() {
        assertEquals(2.0, velocityAgo(2, HOUR)!!, 0.0)
    }

    @Test
    fun boundaryJustBelowOneHour_isFloored() {
        assertEquals(1.0, velocityAgo(1, HOUR - 1)!!, 0.0)
    }

    @Test
    fun zeroInterval_isFlooredNotDivisionByZero() {
        assertEquals(1.0, velocityAgo(1,0)!!, 0.0)
    }

    @Test
    fun oneMillisecondInterval_isFloored() {
        assertEquals(1.0, velocityAgo(1,1)!!, 0.0)
    }

    // ---------- Ошибочный вход ----------
    @Test
    fun futureSale_isNull(){
        assertNull(velocityAgo(1, -1))
    }

    @Test
    fun futureSalesByDay_isNull(){
        assertNull(velocityAgo(5, -24 * HOUR))
    }

    @Test
    fun nullSoldQuantity_isNull() {
        assertNull(velocityAgo(null, HOUR))
    }

    @Test
    fun zeroSoldQuantity_isNull() {
        assertNull(velocityAgo(0, HOUR))
    }

    @Test
    fun negativeSoldQuantity_isNull() {
        assertNull(velocityAgo(-5, HOUR))
    }

    @Test
    fun nullEarliestSale_isNull() {
        assertNull(SalesVelocity.calculatePortionsPerHour(5, null, NOW))
    }

    // ---------- calculateHoursToSellOut ----------
    @Test
    fun sellOut_computesHours() {
        assertEquals(2.0, sellOut(10, 5.0)!!, 0.0)
    }

    @Test
    fun sellOut_fractionalResult() {
        assertEquals(1.5, sellOut(3, 2.0)!!, 0.0)
    }

    @Test
    fun sellOut_nullVelocity_isNull() {
        assertNull(sellOut(10, null))
    }

    @Test
    fun sellOut_zeroVelocity_isNull() {
        assertNull(sellOut(10, 0.0))
    }

    @Test
    fun sellOut_negativeVelocity_isNull() {
        assertNull(sellOut(10, -1.0))
    }

    @Test
    fun sellOut_zeroRemaining_isZero() {
        assertEquals(0.0, sellOut(0, 5.0)!!, 0.0)
    }

    @Test
    fun sellOut_negativeRemaining_isZero() {
        assertEquals(0.0, sellOut(-3, 5.0)!!, 0.0)
    }

    // ---------- Устойчивость ----------
    @Test
    fun sameInputGivesSameResult() {
        val first = velocityAgo(10, 2 * HOUR)
        val second = velocityAgo(10, 2 * HOUR)
        assertEquals(first, second)
    }

    @Test
    fun repeatedCallsOnBoundariesAreStable() {
        listOf(HOUR, HOUR - 1, 0L, -1L).forEach { millisAgo ->
            assertEquals(
                "нестабильный результат при сдвиге $millisAgo",
                velocityAgo(5, millisAgo),
                velocityAgo(5, millisAgo)
            )
        }
    }

    @Test
    fun validInput_neverNegative() {
        listOf(
            velocityAgo(1, HOUR),
            velocityAgo(10, 2 * HOUR),
            velocityAgo(24, 6 * HOUR)
        ).forEach { result ->
            assertTrue(
                "скорость не должна быть отрицательной, получено $result",
                result != null && result > 0.0
            )
        }
    }
}