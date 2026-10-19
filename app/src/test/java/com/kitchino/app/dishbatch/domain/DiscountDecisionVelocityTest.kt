package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DiscountDecisionVelocityTest {

    private companion object {
        const val LIVE_MILLIS = 3_600_000L
        const val MADE_AT = 1_000_000_000L
        const val EXPIRES_AT = MADE_AT + LIVE_MILLIS
    }

    private fun batch(madeAt: Long = MADE_AT, expiresAt: Long = EXPIRES_AT, quantity: Int = 1) = DishBatchEntity(
        idDish = 1,
        idEmployee = 1,
        quantity = quantity,
        madeAt = madeAt,
        expiresAt = expiresAt,
        status = Status.ACTIVE
    )
    private fun decision(
        remaining: Long,
        quantity: Int = 10,
        portionsPerHour: Double?
    ) : DiscountDecision = DiscountDecisionEngine.recommendActionWithSalesVelocity(
        batch(quantity = quantity), batch().expiresAt - remaining, portionsPerHour
    )

    // ---------- Базовое поведение ----------
    @Test
    fun nullVelocity_usesBaseDecision(){
        assertEquals(20.0 , decision(remaining = 1_080_000L, portionsPerHour = null).discountPercent!!, 0.0)
    }

    @Test
    fun zeroVelocity_usesBaseDecision() {
        assertEquals(20.0, decision(remaining = 1_080_000L, portionsPerHour = 0.0).discountPercent!!, 0.0)
    }

    @Test
    fun negativeVelocity_usesBaseDecision() {
        assertEquals(20.0, decision(remaining = 1_080_000L, portionsPerHour = -1.0).discountPercent!!, 0.0)
    }

    @Test
    fun badShelfLife_isWriteOff() {
        val invalid = batch(madeAt = MADE_AT, expiresAt = MADE_AT - 500)
        val decision = DiscountDecisionEngine.recommendActionWithSalesVelocity(invalid, MADE_AT, 5.0)
        assertEquals(DecisionType.WRITE_OFF, decision.decisionType)
        assertTrue(decision.discountReason.isNotBlank())
    }

    // База не меняется, если успевает продаться
    @Test
    fun sellsOutInTime_keepsBaseDecision() {
        assertEquals(DecisionType.NO_ACTION, decision(remaining = 2_700_000L, 10, 20.0).decisionType)
    }

    @Test
    fun boundaryExactlyOnTime_keepsBaseDecision(){
        assertEquals(DecisionType.NO_ACTION, decision(1_800_000L, 10, 20.0).decisionType)
    }

    @Test
    fun sellsOutInTime_doesNotWeakenDiscount(){
        assertEquals(20.0, decision(1_080_000L, 10, 100.0).discountPercent!!, 0.0)
    }

    // Не успевает - усиление скидки
    @Test
    fun tooSlowFromNoAction_becomesDiscount20(){
        assertEquals(20.0, decision(1_800_000L, 10, 5.0).discountPercent!!, 0.0)
    }

    @Test
    fun tooSlowFromDiscount20_becomesDiscount50() {
        assertEquals(50.0, decision(1_080_000L, 10, 5.0).discountPercent!!, 0.0)
    }

    @Test
    fun tooSlowFromDiscount50_staysDiscount50() {
        assertEquals(50.0, decision(360_000L, 10, 5.0).discountPercent!!, 0.0)
    }

    @Test
    fun tooSlowFromWriteOff_staysWriteOff() {
        assertEquals(DecisionType.WRITE_OFF, decision(108_000L, 10, 5.0).decisionType)
    }

    // Устойчивость
    @Test
    fun sameInputGivesSameResult() {
        val firstInput = decision(300, 10, 5.0)
        val secondInput = decision(300, 10, 5.0)
        assertEquals(firstInput, secondInput)
    }
}