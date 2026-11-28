package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

class ExpiryDiscountDecision(
    private val thresholds: DiscountThresholds = DiscountThresholds.DEFAULT
) : DiscountDecisionProvider {

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
            partOfRemainingTime < thresholds.writeoff ->
                DiscountDecision(
                    DecisionType.WRITE_OFF, null,
                    "Менее ${thresholds.writeoffPercentText()}% от срока жизни блюда"
                )
            partOfRemainingTime < thresholds.heavy ->
                DiscountDecision(
                    DecisionType.DISCOUNT, 50.0,
                    "Менее ${thresholds.heavyPercentText()}% от срока жизни блюда"
                )
            partOfRemainingTime < thresholds.light ->
                DiscountDecision(
                    DecisionType.DISCOUNT, 20.0,
                    "Менее ${thresholds.lightPercentText()}% от срока жизни блюда"
                )
            else ->
                DiscountDecision(DecisionType.NO_ACTION, null, "Срок годности еще не подходит к концу")
        }
    }
}
