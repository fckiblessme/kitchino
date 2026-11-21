package com.kitchino.app.dishbatch.domain

import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status

// Сценарий: блюдо и его партия на модельный день
data class SimDish(
    val name: String,
    val price: Double,              // цена порции
    val portions: Int,              // размер партии
    val shelfLifeMillis: Long,      // срок годности партии
    val basePortionsPerHour: Double // сколько порций в час берут без скидки
)

// Результат по одному блюду
data class SimDishResult(
    val name: String,
    val soldPortions: Int,
    val writtenOffPortions: Int,
    val lostMoney: Double,
    val revenue: Double
)

// Итог модельного дня: сумма по блюдам
data class DayResult(val dishes: List<SimDishResult>) {
    val lostMoney: Double get() = dishes.sumOf { it.lostMoney }
    val revenue: Double get() = dishes.sumOf { it.revenue }
    val writtenOffPortions: Int get() = dishes.sumOf { it.writtenOffPortions }
}

// Симуляция дня ресторана: измеримое свойство движка скидок.
// Один и тот же спрос, два прогона: без движка (продаём по базовой скорости,
// остаток списываем по истечении срока) и с движком (скидки ускоряют продажи).
// Использует настоящий движок модуля: ExpiryDiscountDecision -> VelocityDiscountDecision
// -> HysteresisDiscountDecision и тот же расчёт темпа, что в DishBatchViewModel.
object DaySimulation {

    // Решения пересчитываются раз в 15 минут
    private const val STEP_MILLIS = 15 * 60_000L

    // Насколько скидка ускоряет продажи. Коэффициенты условные: в реальном
    // ресторане их измеряют, для модели важно что boost > 1 и растёт с процентом
    private fun rateBoost(discountPercent: Double?): Double = when (discountPercent) {
        20.0 -> 1.5
        50.0 -> 2.5
        else -> 1.0
    }

    fun runNaiveDay(dishes: List<SimDish>): DayResult =
        DayResult(dishes.map { simulateDish(it, withEngine = false) })

    fun runEngineDay(dishes: List<SimDish>): DayResult =
        DayResult(dishes.map { simulateDish(it, withEngine = true) })

    private fun simulateDish(dish: SimDish, withEngine: Boolean): SimDishResult {
        val batch = DishBatchEntity(
            idDish = 1,
            idEmployee = 0,
            quantity = dish.portions,
            madeAt = 0,
            expiresAt = dish.shelfLifeMillis,
            status = Status.ACTIVE
        )

        var remaining = dish.portions
        var sold = 0
        var revenue = 0.0
        var carry = 0.0 // дробные порции копятся между шагами
        var previous: DiscountDecision? = null
        // Журнал продаж (время, порции) — аналог SalesLogEntity, для расчёта темпа
        val sales = mutableListOf<Pair<Long, Int>>()

        var t = 0L
        while (t < dish.shelfLifeMillis && remaining > 0) {
            var rate = dish.basePortionsPerHour
            var priceNow = dish.price

            if (withEngine) {
                // Темп из журнала — тот же расчёт, что в DishBatchViewModel:
                // продажи внутри окна наблюдения и знаменатель не короче окна.
                // В первые часы темп занижен консервативно — движок
                // перестраховывается скидкой, занижение безопаснее завышения
                val windowStart = t - SalesVelocity.OBSERVATION_WINDOW_MILLIS.toLong()
                val soldInWindow = sales.filter { it.first >= windowStart }.sumOf { it.second }
                val earliestInWindow = sales.firstOrNull { it.first >= windowStart }?.first
                val velocity = SalesVelocity.calculatePortionsPerHour(
                    soldInWindow.takeIf { it > 0 },
                    earliestInWindow,
                    t,
                    SalesVelocity.OBSERVATION_WINDOW_MILLIS
                )
                val decision = HysteresisDiscountDecision(
                    previous,
                    VelocityDiscountDecision(velocity, base = ExpiryDiscountDecision())
                ).recommendAction(batch.copy(quantity = remaining), t)
                previous = decision

                when (decision.decisionType) {
                    DecisionType.DISCOUNT -> {
                        rate *= rateBoost(decision.discountPercent)
                        priceNow *= 1.0 - decision.discountPercent!! / 100.0
                    }
                    // Партия снята с продажи, остаток идёт в списание
                    DecisionType.WRITE_OFF -> break
                    DecisionType.NO_ACTION -> {}
                }
            }

            // Продажи за шаг
            carry += rate * STEP_MILLIS / 3_600_000.0
            val whole = carry.toInt()
            if (whole > 0) {
                carry -= whole
                val soldNow = minOf(whole, remaining)
                remaining -= soldNow
                sold += soldNow
                revenue += soldNow * priceNow
                sales += t to soldNow
            }
            t += STEP_MILLIS
        }

        return SimDishResult(
            name = dish.name,
            soldPortions = sold,
            writtenOffPortions = remaining,
            lostMoney = remaining * dish.price,
            revenue = revenue
        )
    }
}
