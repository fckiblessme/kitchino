package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.ForecastProcessing
import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test


class CpuBenchmarkTest {

    private val now = 1_766_000_000_000L

    private val passes = 10

    @Test
    fun sequentialAndParallelVersionsGiveSameResult() {
        val catalog = ForecastProcessing.buildCatalog(
            ingredients = 200,
            recordsPerIngredient = 2000,
            seed = 42L,
            nowMillis = now
        )
        val expected = ForecastProcessing.calculateSequential(catalog, now)
        for (p in intArrayOf(2, 4, 8, Runtime.getRuntime().availableProcessors())) {
            val actual = ForecastProcessing.calculateParallel(catalog, now, p)
            assertTrue(
                "результаты не совпали при p = $p",
                ForecastProcessing.sameResult(expected, actual)
            )
        }
        val repeated = ForecastProcessing.calculateSequential(catalog, now)
        assertTrue(
            "повторный последовательный расчет дал другой результат",
            ForecastProcessing.sameResult(expected, repeated)
        )
    }

    @Test
    fun measurementSeries() {
        val catalog = ForecastProcessing.buildCatalog(
            ingredients = 200,
            recordsPerIngredient = 30000,
            seed = 42L,
            nowMillis = now
        )
        val processors = Runtime.getRuntime().availableProcessors()
        val protocol = TimeMeasurer(warmup = 3, repeats = 5)
        protocol.variant("последовательная версия") {
            for (pass in 0 until passes) {
                ForecastProcessing.calculateSequential(catalog, now)
            }
        }
        for (p in intArrayOf(2, 4, processors)) {
            protocol.variant("параллельная, p = $p") {
                for (pass in 0 until passes) {
                    ForecastProcessing.calculateParallel(catalog, now, p)
                }
            }
        }
        protocol.run()

        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println(
            "Входные данные: ингредиентов " + catalog.size +
                    ", записей журнала на ингредиент " + catalog.first().timestamps.size +
                    ", начальное значение генератора 42"
        )
        println("Окно наблюдения: " + ForecastProcessing.OBSERVATION_WINDOW_DAYS + " дней")
        println("Проходов в одном прогоне: " + passes)
        println()
        protocol.print("Р1, Р2. CPU-bound: пересчет прогноза по каталогу ингредиентов")

        val sequential = protocol.samples("последовательная версия").median
        val derived = StringBuilder()
        derived.append(
            String.format(
                java.util.Locale.ROOT,
                "%-10s %10s %8s %8s %8s %12s",
                "вариант", "медиана", "S", "E", "f", "предел 1/f"
            )
        ).append('\n')
        derived.append("-".repeat(58)).append('\n')
        println()
        for (p in intArrayOf(2, 4, processors)) {
            val samples = protocol.samples("параллельная, p = $p")
            val speedup = TimeMeasurer.speedup(sequential, samples.median)
            val efficiency = TimeMeasurer.efficiency(speedup, p)
            val share = TimeMeasurer.sequentialShare(speedup, p)
            val limit = TimeMeasurer.amdahlLimit(share)
            derived.append(
                String.format(
                    java.util.Locale.ROOT,
                    "p = %-6d %10s %8s %8s %8s %12s",
                    p,
                    TimeMeasurer.format(samples.median, 2),
                    TimeMeasurer.format(speedup, 2),
                    TimeMeasurer.format(efficiency * 100.0, 1) + "%",
                    TimeMeasurer.format(share, 3),
                    if (limit.isFinite() && limit > 0.0) {
                        TimeMeasurer.format(limit, 1)
                    } else {
                        "не ограничен"
                    }
                )
            ).append('\n')
        }
        derived.append("-".repeat(58)).append('\n')
        derived.append("S = T1 / Tp, E = S / p, f = (1/S - 1/p) / (1 - 1/p), предел = 1/f").append('\n')
        derived.append("T1 - медиана отдельной последовательной версии").append('\n')
        println(derived)

        protocol.save(
            name = "pz04-warehouse-cpu",
            title = "Р1, Р2. CPU-bound: пересчет прогноза по каталогу ингредиентов",
            conditions = TimeMeasurer.conditions() +
                    "Входные данные: ингредиентов " + catalog.size +
                    ", записей журнала на ингредиент " + catalog.first().timestamps.size +
                    ", начальное значение генератора 42\n" +
                    "Окно наблюдения: " + ForecastProcessing.OBSERVATION_WINDOW_DAYS + " дней\n" +
                    "Проходов в одном прогоне: " + passes + "\n",
            notes = derived.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("последовательная версия").values.size == 5
        )
    }
}