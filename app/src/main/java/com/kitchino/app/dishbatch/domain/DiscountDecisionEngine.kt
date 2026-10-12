package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

data class DiscountDecision (
    val decisionType: DecisionType,
    val discountPercent: Double?,
    val discountReason: String
)


object DiscountDecisionEngine {
    private const val WRITEOFF_THRESHOLD = 0.05
    private const val HEAVY_DISCOUNT_THRESHOLD = 0.20
    private const val LIGHT_DISCOUNT_THRESHOLD = 0.50

    fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime : Long) : DiscountDecision {
        val liveTimeDishBatch = dishBatchEntity.expiresAt - dishBatchEntity.madeAt
        if (liveTimeDishBatch <= 0) {
            return DiscountDecision(DecisionType.WRITE_OFF, null, "Не корректный срок партии. Не может быть меньше или равен 0")
        } else {
            val remainingTimeDishBatch = dishBatchEntity.expiresAt - currentTime
            val partOfRemainingTime = remainingTimeDishBatch.toDouble() / liveTimeDishBatch
            return when {
                partOfRemainingTime <= 0.0 -> DiscountDecision(DecisionType.WRITE_OFF, null, "Срок истек")
                partOfRemainingTime < WRITEOFF_THRESHOLD -> DiscountDecision(DecisionType.WRITE_OFF, null, "Менее 5% от срока жизни блюда")
                partOfRemainingTime < HEAVY_DISCOUNT_THRESHOLD -> DiscountDecision(DecisionType.DISCOUNT, 50.0, "Менее 20% от срока жизни блюда")
                partOfRemainingTime < LIGHT_DISCOUNT_THRESHOLD -> DiscountDecision(DecisionType.DISCOUNT, 20.0, "Менее 50% от срока жизни блюда")
                else -> {
                    DiscountDecision(DecisionType.NO_ACTION, null, "Срок годности еще не подходит к концу")
                }
            }

        }
    }

    fun recommendActionWithSalesVelocity(
        dishBatchEntity: DishBatchEntity,
        currentTime: Long,
        portionsPerHour: Double?
    ) : DiscountDecision {
        val base = recommendAction(dishBatchEntity, currentTime)
        val hoursToSellOut = SalesVelocity.calculateHoursToSellOut(
            dishBatchEntity.quantity, portionsPerHour
        )
        if (hoursToSellOut == null) return base
        val hoursUntilExpiry = (dishBatchEntity.expiresAt - currentTime) / 3_600_000.0
        return if (hoursToSellOut > hoursUntilExpiry){
            when (base.decisionType){
                DecisionType.NO_ACTION -> DiscountDecision(DecisionType.DISCOUNT, 20.0, "")
                DecisionType.DISCOUNT -> {
                    if (base.discountPercent == 50.0){
                        base
                    }
                    else {
                        DiscountDecision(DecisionType.DISCOUNT, 50.0, "")
                    }
                }
                DecisionType.WRITE_OFF -> base
            }
        } else {
            base
        }
    }
}