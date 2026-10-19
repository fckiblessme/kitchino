package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscountDecisionEngineTest {

    private companion object {
        const val LIVE_MILLIS = 1000L
        const val MADE_AT = 1_000_000L
        const val EXPIRES_AT = MADE_AT + LIVE_MILLIS
    }

    private fun batch(madeAt: Long = MADE_AT, expiresAt: Long = EXPIRES_AT) = DishBatchEntity(
        idDish = 1,
        idEmployee = 1,
        quantity = 1,
        madeAt = madeAt,
        expiresAt = expiresAt,
        status = Status.ACTIVE
    )

    private fun decideWithRemaining(remaining: Long, entity: DishBatchEntity = batch()) =
        DiscountDecisionEngine.recommendAction(entity, entity.expiresAt - remaining)

    // ------------------- классы эквивалентности -------------------

    @Test
    fun freshBatch_isNoAction() {
        val decision = decideWithRemaining(LIVE_MILLIS)
        assertEquals(DecisionType.NO_ACTION, decision.decisionType)
        assertNull(decision.discountPercent)
    }

    @Test
    fun classAboveHalf_isNoAction() {
        assertEquals(DecisionType.NO_ACTION, decideWithRemaining(750).decisionType)
    }

    @Test
    fun classTwentyToFifty_isDiscount20() {
        val decision = decideWithRemaining(300)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(20.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun classFiveToTwenty_isDiscount50() {
        val decision = decideWithRemaining(100)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(50.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun classBelowFive_isWriteOff() {
        val decision = decideWithRemaining(30)
        assertEquals(DecisionType.WRITE_OFF, decision.decisionType)
        assertNull(decision.discountPercent)
    }

    @Test
    fun expiredBatch_isWriteOff() {
        assertEquals(DecisionType.WRITE_OFF, decideWithRemaining(-100).decisionType)
    }

    // ------------------- граничные значения -------------------

    @Test
    fun boundaryExactly50_isNoAction() {
        assertEquals(DecisionType.NO_ACTION, decideWithRemaining(500).decisionType)
    }

    @Test
    fun boundaryJustBelow50_isDiscount20() {
        val decision = decideWithRemaining(499)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(20.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun boundaryExactly20_isDiscount20() {
        val decision = decideWithRemaining(200)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(20.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun boundaryJustBelow20_isDiscount50() {
        val decision = decideWithRemaining(199)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(50.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun boundaryExactly5_isDiscount50() {
        val decision = decideWithRemaining(50)
        assertEquals(DecisionType.DISCOUNT, decision.decisionType)
        assertEquals(50.0, decision.discountPercent!!, 0.0)
    }

    @Test
    fun boundaryJustBelow5_isWriteOff() {
        assertEquals(DecisionType.WRITE_OFF, decideWithRemaining(49).decisionType)
    }

    @Test
    fun boundaryExactlyZeroRemaining_isWriteOff() {
        assertEquals(DecisionType.WRITE_OFF, decideWithRemaining(0).decisionType)
    }

    @Test
    fun boundaryJustExpired_isWriteOff() {
        assertEquals(DecisionType.WRITE_OFF, decideWithRemaining(-1).decisionType)
    }

    // ------------------- существенный ошибочный исход, FR-09 -------------------

    @Test
    fun zeroLifetime_isWriteOffWithoutException() {
        val invalid = batch(madeAt = MADE_AT, expiresAt = MADE_AT)
        val decision = DiscountDecisionEngine.recommendAction(invalid, MADE_AT)
        assertEquals(DecisionType.WRITE_OFF, decision.decisionType)
        assertTrue(decision.discountReason.isNotBlank())
    }

    @Test
    fun negativeLifetime_isWriteOffWithoutException() {
        val invalid = batch(madeAt = MADE_AT, expiresAt = MADE_AT - 500)
        val decision = DiscountDecisionEngine.recommendAction(invalid, MADE_AT)
        assertEquals(DecisionType.WRITE_OFF, decision.decisionType)
        assertTrue(decision.discountReason.isNotBlank())
    }

    @Test
    fun invalidLifetimeIgnoresCurrentTime() {
        val invalid = batch(madeAt = MADE_AT, expiresAt = MADE_AT - 500)
        val early = DiscountDecisionEngine.recommendAction(invalid, MADE_AT - 10_000)
        val late = DiscountDecisionEngine.recommendAction(invalid, MADE_AT + 10_000)
        assertEquals(DecisionType.WRITE_OFF, early.decisionType)
        assertEquals(DecisionType.WRITE_OFF, late.decisionType)
    }

    // ------------------- содержание результата -------------------

    @Test
    fun reasonIsFilledForEveryClass() {
        val samples = listOf(1000L, 750L, 300L, 100L, 30L, -100L)
        samples.forEach { remaining ->
            assertTrue(
                "пустая причина для остатка $remaining",
                decideWithRemaining(remaining).discountReason.isNotBlank()
            )
        }
    }

    @Test
    fun discountPercentIsNullForNoActionAndWriteOff() {
        assertNull(decideWithRemaining(750).discountPercent)
        assertNull(decideWithRemaining(30).discountPercent)
        assertNull(decideWithRemaining(-100).discountPercent)
    }

    @Test
    fun discountPercentMatchesDecisionKind() {
        assertEquals(20.0, decideWithRemaining(499).discountPercent!!, 0.0)
        assertEquals(50.0, decideWithRemaining(50).discountPercent!!, 0.0)
    }

    // ------------------- NFR-03: устойчивость результата -------------------

    @Test
    fun sameInputGivesSameResult() {
        val entity = batch()
        val currentTime = entity.expiresAt - 300
        val first = DiscountDecisionEngine.recommendAction(entity, currentTime)
        val second = DiscountDecisionEngine.recommendAction(entity, currentTime)
        assertEquals(first, second)
    }

    @Test
    fun repeatedCallsOnBoundariesAreStable() {
        listOf(500L, 200L, 50L, 0L).forEach { remaining ->
            val entity = batch()
            val currentTime = entity.expiresAt - remaining
            assertEquals(
                "нестабильный результат при остатке $remaining",
                DiscountDecisionEngine.recommendAction(entity, currentTime),
                DiscountDecisionEngine.recommendAction(entity, currentTime)
            )
        }
    }
}
