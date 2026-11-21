package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HysteresisDiscountDecisionTest {

    private companion object {
        const val LIVE_MILLIS = 3_600_000L
        const val MADE_AT = 1_000_000_000L
        const val EXPIRES_AT = MADE_AT + LIVE_MILLIS
    }

    private fun batch() = DishBatchEntity(
        idDish = 1,
        idEmployee = 1,
        quantity = 10,
        madeAt = MADE_AT,
        expiresAt = EXPIRES_AT,
        status = Status.ACTIVE
    )

    private fun decide(
        previous: DiscountDecision?,
        portionsPerHour: Double?,
        remaining: Long = 1_800_000L
    ) = HysteresisDiscountDecision(previous, VelocityDiscountDecision(portionsPerHour))
        .recommendAction(batch(), EXPIRES_AT - remaining)

    private fun discount(percent: Double) =
        DiscountDecision(DecisionType.DISCOUNT, percent, "прошлое решение по партии")

    // ---------- Пропуск без прошлого решения ----------
    @Test
    fun noPreviousDecision_passesFreshThrough() {
        assertEquals(DecisionType.NO_ACTION, decide(null, 20.1).decisionType)
    }

    @Test
    fun previousNoAction_doesNotHoldAnything() {
        val previous = DiscountDecision(DecisionType.NO_ACTION, null, "было без скидки")
        assertEquals(20.0, decide(previous, 19.9).discountPercent!!, 0.0)
    }

    // ---------- Держим скидку против ослабления ----------
    @Test
    fun freshNoAction_holdsPreviousDiscount() {
        assertEquals(20.0, decide(discount(20.0), 20.1).discountPercent!!, 0.0)
    }

    @Test
    fun freshWeakerDiscount_holdsPreviousDiscount() {
        assertEquals(50.0, decide(discount(50.0), 20.1).discountPercent!!, 0.0)
    }

    @Test
    fun freshSameDiscount_passesFresh() {
        assertEquals(20.0, decide(discount(20.0), 19.9).discountPercent!!, 0.0)
    }

    // ---------- Усиление проходит ----------
    @Test
    fun freshStrongerDiscount_passesFresh() {
        // 10% срока жизни + медленные продажи: свежий расчёт даёт 50%
        assertEquals(50.0, decide(discount(20.0), 5.0, remaining = 360_000L).discountPercent!!, 0.0)
    }

    @Test
    fun freshWriteOff_notBlockedByDiscount() {
        // 3% срока жизни: свежий расчёт — списание, оно сильнее любой скидки
        val decision = decide(discount(50.0), 20.1, remaining = 108_000L)
        assertEquals(DecisionType.WRITE_OFF, decision.decisionType)
    }

    // ---------- Регрессия: тот же ряд темпа, что в воспроизведении дребезга ----------
    // Решение по партии запоминается после каждого шага, ослаблений быть не должно:
    // после первого DISCOUNT ряд монотонно неубывающий
    @Test
    fun velocityNoiseSeries_isMonotonicUnderHysteresis() {
        val wobble = listOf(19.9, 20.1, 19.9, 20.1, 19.9)
        var previous: DiscountDecision? = null
        val decisions = wobble.map { velocity ->
            val fresh = HysteresisDiscountDecision(previous, VelocityDiscountDecision(velocity))
                .recommendAction(batch(), EXPIRES_AT - 1_800_000L)
            previous = fresh
            fresh
        }

        assertEquals(DecisionType.DISCOUNT, decisions.first().decisionType)
        decisions.forEach { decision ->
            assertEquals(DecisionType.DISCOUNT, decision.decisionType)
            assertTrue(
                "скидка не должна ослабляться, получено ${decision.discountPercent}",
                decision.discountPercent!! >= 20.0
            )
        }
    }
}
