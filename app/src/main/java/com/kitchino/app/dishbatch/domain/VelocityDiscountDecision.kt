package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

// Реализация 2: решение по сроку годности с усилением на темп продаж.
// Скорость передаётся через конструктор (точка сборки), контракт тот же,
// что у ExpiryDiscountDecision — потребитель не различает реализации.
class VelocityDiscountDecision(
    private val portionsPerHour: Double?,
    private val base: DiscountDecisionProvider = ExpiryDiscountDecision()
) : DiscountDecisionProvider {

    override fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime: Long): DiscountDecision {
        val baseDecision = base.recommendAction(dishBatchEntity, currentTime)

        val hoursToSellOut = SalesVelocity.calculateHoursToSellOut(
            dishBatchEntity.quantity, portionsPerHour
        )
        if (hoursToSellOut == null) return baseDecision

        val hoursUntilExpiry = (dishBatchEntity.expiresAt - currentTime) / 3_600_000.0

        // Скорость только усиливает скидку, но не ослабляет.
        // Усиление, если по текущему темпу партия не успевает продаться до истечения срока.
        return if (hoursToSellOut > hoursUntilExpiry) {
            when (baseDecision.decisionType) {
                DecisionType.NO_ACTION ->
                    DiscountDecision(DecisionType.DISCOUNT, 20.0, "Не успеет продаться по текущему темпу")
                DecisionType.DISCOUNT ->
                    if (baseDecision.discountPercent == 50.0) {
                        baseDecision
                    } else {
                        DiscountDecision(DecisionType.DISCOUNT, 50.0, "Не успеет продаться по текущему темпу")
                    }
                DecisionType.WRITE_OFF -> baseDecision
            }
        } else {
            baseDecision
        }
    }
}
