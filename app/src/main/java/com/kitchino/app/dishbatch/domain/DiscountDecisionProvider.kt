package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DishBatchEntity

interface DiscountDecisionProvider {
    fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime: Long): DiscountDecision
}
