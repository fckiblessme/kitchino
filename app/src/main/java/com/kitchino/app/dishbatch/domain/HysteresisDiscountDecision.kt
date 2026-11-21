package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity

// Реализация 3: гистерезис против дребезга скидки.
// Свежий расчёт колеблется на границе порога (темп продаж меняется, когда старые
// продажи выходят из окна наблюдения), но цена на ценнике обратно не меняется.
// Если по партии уже было решение DISCOUNT, ослабить его нельзя — решение либо
// сохраняется, либо усиливается. Списание проходит всегда, оно сильнее любой скидки.
class HysteresisDiscountDecision(
    private val previous: DiscountDecision?,
    private val source: DiscountDecisionProvider
) : DiscountDecisionProvider {

    override fun recommendAction(dishBatchEntity: DishBatchEntity, currentTime: Long): DiscountDecision {
        val fresh = source.recommendAction(dishBatchEntity, currentTime)
        if (previous?.decisionType != DecisionType.DISCOUNT) return fresh
        return when (fresh.decisionType) {
            DecisionType.DISCOUNT ->
                if ((fresh.discountPercent ?: 0.0) >= (previous.discountPercent ?: 0.0)) fresh else previous
            DecisionType.NO_ACTION -> previous
            DecisionType.WRITE_OFF -> fresh
        }
    }
}
