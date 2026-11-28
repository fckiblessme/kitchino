package com.kitchino.app.experiment

import com.kitchino.app.warehouse.domain.Forecast
import com.kitchino.app.warehouse.domain.IngredientForecast
import java.util.Random
import kotlin.math.abs

class ConsumptionJournal(
    val ingredientId: Long,
    val name: String,
    val unit: String,
    val leadTimeDays: Int,
    val currentStock: Double,
    val earliestExpiration: Long?,
    val timestamps: LongArray,
    val amounts: DoubleArray
)

object ForecastProcessing {

    const val OBSERVATION_WINDOW_DAYS = 30
    private const val MILLIS_PER_DAY = 86_400_000L

    fun forecast(journal: ConsumptionJournal, nowMillis: Long): IngredientForecast {
        require(journal.timestamps.size == journal.amounts.size) {
            "Число отметок времени и количеств должно совпадать"
        }
        val windowStart = nowMillis - OBSERVATION_WINDOW_DAYS * MILLIS_PER_DAY
        val dailyTotals = DoubleArray(OBSERVATION_WINDOW_DAYS)
        val timestamps = journal.timestamps
        val amounts = journal.amounts
        var firstDayInWindow = -1
        for (index in timestamps.indices) {
            val day = ((timestamps[index] - windowStart) / MILLIS_PER_DAY).toInt()
            if (day < 0 || day >= OBSERVATION_WINDOW_DAYS) {
                continue
            }
            dailyTotals[day] += amounts[index]
            if (firstDayInWindow == -1 || day < firstDayInWindow) {
                firstDayInWindow = day
            }
        }
        val observationDays = if (firstDayInWindow == -1) {
            1
        } else {
            minOf(OBSERVATION_WINDOW_DAYS, maxOf(1, OBSERVATION_WINDOW_DAYS - firstDayInWindow))
        }
        val dailyAmounts = ArrayList<Double>(observationDays)
        for (day in (OBSERVATION_WINDOW_DAYS - observationDays) until OBSERVATION_WINDOW_DAYS) {
            dailyAmounts.add(dailyTotals[day])
        }
        val averageDailyConsumption = Forecast.calculateAverageDailyConsumption(
            dailyAmounts,
            observationDays
        )
        val daysUntilEmpty = Forecast.calculateDaysUntilEmpty(
            journal.currentStock,
            averageDailyConsumption
        )
        val daysUntilExpiry = if (journal.earliestExpiration == null) {
            null
        } else {
            Forecast.calculateDaysUntilExpiry(journal.earliestExpiration, nowMillis)
        }
        val usableDays = Forecast.calculateUsableDays(daysUntilEmpty, daysUntilExpiry)
        return IngredientForecast(
            ingredientId = journal.ingredientId,
            name = journal.name,
            unit = journal.unit,
            currentStock = journal.currentStock,
            daysUntilEmpty = daysUntilEmpty,
            daysUntilExpiry = daysUntilExpiry,
            usableDays = usableDays,
            shouldReorder = Forecast.shouldReorder(daysUntilEmpty, journal.leadTimeDays),
            shouldUseUrgently = Forecast.shouldUseUrgently(daysUntilEmpty, daysUntilExpiry)
        )
    }

    fun calculateSequential(
        journals: List<ConsumptionJournal>,
        nowMillis: Long
    ): List<IngredientForecast> {
        val result = ArrayList<IngredientForecast>(journals.size)
        for (journal in journals) {
            result.add(forecast(journal, nowMillis))
        }
        return result
    }

    fun calculateParallel(
        journals: List<ConsumptionJournal>,
        nowMillis: Long,
        p: Int
    ): List<IngredientForecast> {
        require(p >= 1) { "Число исполнителей должно быть не меньше 1" }
        if (p == 1 || journals.size < 2) {
            return calculateSequential(journals, nowMillis)
        }
        val result = arrayOfNulls<IngredientForecast>(journals.size)
        val threads = ArrayList<Thread>(p)
        val failures = arrayOfNulls<Throwable>(p)
        for (worker in 0 until p) {
            val assigned = worker
            val thread = Thread {
                try {
                    var index = assigned
                    while (index < journals.size) {
                        result[index] = forecast(journals[index], nowMillis)
                        index += p
                    }
                } catch (error: Throwable) {
                    failures[assigned] = error
                }
            }
            thread.name = "forecast-worker-" + worker
            threads.add(thread)
        }
        for (thread in threads) {
            thread.start()
        }
        for (thread in threads) {
            try {
                thread.join()
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        for (failure in failures) {
            if (failure != null) {
                throw IllegalStateException("Расчет в исполнителе завершился ошибкой", failure)
            }
        }
        val forecasts = ArrayList<IngredientForecast>(journals.size)
        for (index in journals.indices) {
            forecasts.add(result[index] ?: forecast(journals[index], nowMillis))
        }
        return forecasts
    }

    fun sameResult(
        expected: List<IngredientForecast>,
        actual: List<IngredientForecast>
    ): Boolean {
        if (expected.size != actual.size) {
            return false
        }
        for (index in expected.indices) {
            val left = expected[index]
            val right = actual[index]
            if (left.ingredientId != right.ingredientId) {
                return false
            }
            if (left.name != right.name || left.unit != right.unit) {
                return false
            }
            if (left.shouldReorder != right.shouldReorder) {
                return false
            }
            if (left.shouldUseUrgently != right.shouldUseUrgently) {
                return false
            }
            if (!sameDouble(left.currentStock, right.currentStock)) {
                return false
            }
            if (!sameNullable(left.daysUntilEmpty, right.daysUntilEmpty)) {
                return false
            }
            if (!sameNullable(left.daysUntilExpiry, right.daysUntilExpiry)) {
                return false
            }
            if (!sameNullable(left.usableDays, right.usableDays)) {
                return false
            }
        }
        return true
    }

    private fun sameDouble(left: Double, right: Double): Boolean {
        val scale = maxOf(1.0, abs(left), abs(right))
        return abs(left - right) <= 1e-12 * scale
    }

    private fun sameNullable(left: Double?, right: Double?): Boolean {
        if (left == null && right == null) {
            return true
        }
        if (left == null || right == null) {
            return false
        }
        return sameDouble(left, right)
    }

    fun buildCatalog(
        ingredients: Int,
        recordsPerIngredient: Int,
        seed: Long,
        nowMillis: Long
    ): List<ConsumptionJournal> {
        val random = Random(seed)
        val result = ArrayList<ConsumptionJournal>(ingredients)
        val horizonDays = 365L
        for (index in 0 until ingredients) {
            val count = recordsPerIngredient
            val timestamps = LongArray(count)
            val amounts = DoubleArray(count)
            var level = 0.5 + random.nextDouble() * 20.0
            for (position in 0 until count) {
                val dayOffset = (random.nextDouble() * horizonDays).toLong()
                timestamps[position] = nowMillis - dayOffset * MILLIS_PER_DAY -
                        (random.nextDouble() * MILLIS_PER_DAY * 0.9).toLong()
                level = level * (0.9 + random.nextDouble() * 0.2)
                if (level < 1e-3) {
                    level = 1e-3
                }
                amounts[position] = if (random.nextInt(10) == 0) 0.0 else level
            }
            val currentStock = level * (1.0 + random.nextDouble() * 60.0)
            result.add(
                ConsumptionJournal(
                    ingredientId = index.toLong() + 1,
                    name = "Ингредиент " + (index + 1),
                    unit = if (index % 2 == 0) "кг" else "шт",
                    leadTimeDays = 1 + random.nextInt(7),
                    currentStock = currentStock,
                    earliestExpiration = nowMillis + (1 + random.nextInt(40)) * MILLIS_PER_DAY,
                    timestamps = timestamps,
                    amounts = amounts
                )
            )
        }
        return result
    }
}