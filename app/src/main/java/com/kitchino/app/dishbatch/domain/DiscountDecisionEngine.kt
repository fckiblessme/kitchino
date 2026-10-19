package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

data class DiscountDecision (
    val decisionType: DecisionType,
    val discountPercent: Double?,
    val discountReason: String
)


// Объект-фасад над двумя реализациями DiscountDecisionProvider.
// Сохранён для обратной совместимости со старыми тестами; делегирует в
// ExpiryDiscountDecision и VelocityDiscountDecision.
object DiscountDecisionEngine : DiscountDecisionProvider {

    private val expiryImpl = ExpiryDiscountDecision()

    override fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime : Long) : DiscountDecision {
        return expiryImpl.recommendAction(dishBatchEntity, currentTime)
    }

    fun recommendActionWithSalesVelocity(
        dishBatchEntity: DishBatchEntity,
        currentTime: Long,
        portionsPerHour: Double?
    ) : DiscountDecision {
        return VelocityDiscountDecision(portionsPerHour).recommendAction(dishBatchEntity, currentTime)
    }
}
