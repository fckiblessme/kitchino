package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DishBatchEntity

// Контракт модуля принятия решения по партии блюда.
// Потребитель (DishBatchViewModel) зависит от контракта, а не от конкретной реализации,
// что позволяет подменять реализацию без изменения потребителя.
interface DiscountDecisionProvider {
    fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime: Long): DiscountDecision
}
