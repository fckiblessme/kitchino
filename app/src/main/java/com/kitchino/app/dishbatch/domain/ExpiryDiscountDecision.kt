package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

// Реализация 1: решение только по доле остатка срока годности (FR-08).
// Не учитывает темп продаж.
class ExpiryDiscountDecision : DiscountDecisionProvider {

    private val writeoffThreshold = 0.05
    private val heavyDiscountThreshold = 0.20
    private val lightDiscountThreshold = 0.50

    override fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime: Long): DiscountDecision {
        val liveTimeDishBatch = dishBatchEntity.expiresAt - dishBatchEntity.madeAt
        if (liveTimeDishBatch <= 0) {
            return DiscountDecision(
                DecisionType.WRITE_OFF, null,
                "Не корректный срок партии. Не может быть меньше или равен 0"
            )
        }
        val remainingTimeDishBatch = dishBatchEntity.expiresAt - currentTime
        val partOfRemainingTime = remainingTimeDishBatch.toDouble() / liveTimeDishBatch
        return when {
            partOfRemainingTime <= 0.0 ->
                DiscountDecision(DecisionType.WRITE_OFF, null, "Срок истек")
            partOfRemainingTime < writeoffThreshold ->
                DiscountDecision(DecisionType.WRITE_OFF, null, "Менее 5% от срока жизни блюда")
            partOfRemainingTime < heavyDiscountThreshold ->
                DiscountDecision(DecisionType.DISCOUNT, 50.0, "Менее 20% от срока жизни блюда")
            partOfRemainingTime < lightDiscountThreshold ->
                DiscountDecision(DecisionType.DISCOUNT, 20.0, "Менее 50% от срока жизни блюда")
            else ->
                DiscountDecision(DecisionType.NO_ACTION, null, "Срок годности еще не подходит к концу")
        }
    }
}
