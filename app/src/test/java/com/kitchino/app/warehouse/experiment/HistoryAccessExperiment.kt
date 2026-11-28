package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.ConsumptionJournal
import com.kitchino.app.experiment.ForecastProcessing
import com.kitchino.app.warehouse.domain.Forecast
import com.kitchino.app.warehouse.domain.IngredientForecast

object HistoryAccessExperiment {

    private const val OBSERVATION_WINDOW_DAYS = ForecastProcessing.OBSERVATION_WINDOW_DAYS
    private const val MILLIS_PER_DAY = 86_400_000L

    fun forecastWithSecondScan(journal: ConsumptionJournal, nowMillis: Long): IngredientForecast {
        val windowStart = nowMillis - OBSERVATION_WINDOW_DAYS * MILLIS_PER_DAY

        val dailyTotals = DoubleArray(OBSERVATION_WINDOW_DAYS)
        val timestamps = journal.timestamps
        val amounts = journal.amounts
        for (index in timestamps.indices) {
            val day = ((timestamps[index] - windowStart) / MILLIS_PER_DAY).toInt()
            if (day < 0 || day >= OBSERVATION_WINDOW_DAYS) {
                continue
            }
            dailyTotals[day] += amounts[index]
        }

        var firstDayInWindow = -1
        for (index in timestamps.indices) {
            val day = ((timestamps[index] - windowStart) / MILLIS_PER_DAY).toInt()
            if (day < 0 || day >= OBSERVATION_WINDOW_DAYS) {
                continue
            }
            if (firstDayInWindow == -1 || day < firstDayInWindow) {
                firstDayInWindow = day
            }
        }

        return buildForecast(journal, nowMillis, dailyTotals, firstDayInWindow)
    }

    fun forecastWithSingleScan(journal: ConsumptionJournal, nowMillis: Long): IngredientForecast {
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
        return buildForecast(journal, nowMillis, dailyTotals, firstDayInWindow)
    }

    private fun buildForecast(
        journal: ConsumptionJournal,
        nowMillis: Long,
        dailyTotals: DoubleArray,
        firstDayInWindow: Int
    ): IngredientForecast {
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

    fun catalogWithSecondScan(
        journals: List<ConsumptionJournal>,
        nowMillis: Long
    ): List<IngredientForecast> {
        val result = ArrayList<IngredientForecast>(journals.size)
        for (journal in journals) {
            result.add(forecastWithSecondScan(journal, nowMillis))
        }
        return result
    }

    fun catalogWithSingleScan(
        journals: List<ConsumptionJournal>,
        nowMillis: Long
    ): List<IngredientForecast> {
        val result = ArrayList<IngredientForecast>(journals.size)
        for (journal in journals) {
            result.add(forecastWithSingleScan(journal, nowMillis))
        }
        return result
    }
}