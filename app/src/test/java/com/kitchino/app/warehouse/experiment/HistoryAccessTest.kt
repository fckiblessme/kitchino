package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.ForecastProcessing
import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test


class HistoryAccessTest {

    private val now = 1_766_000_000_000L
    private val passes = 5


    @Test
    fun bothFormsGiveSameForecast() {
        val catalog = ForecastProcessing.buildCatalog(
            ingredients = 50,
            recordsPerIngredient = 2000,
            seed = 42L,
            nowMillis = now
        )
        val second = HistoryAccessExperiment.catalogWithSecondScan(catalog, now)
        val single = HistoryAccessExperiment.catalogWithSingleScan(catalog, now)
        assertTrue(
            "прогноз двух форм не совпал",
            ForecastProcessing.sameResult(second, single)
        )
    }


    @Test
    fun measurementSeries() {
        val catalog = ForecastProcessing.buildCatalog(
            ingredients = 200,
            recordsPerIngredient = 50000,
            seed = 42L,
            nowMillis = now
        )

        val protocol = TimeMeasurer(warmup = 2, repeats = 5)
        protocol.variant("двойное чтение истории") {
            for (pass in 0 until passes) {
                HistoryAccessExperiment.catalogWithSecondScan(catalog, now)
            }
        }
        protocol.variant("одно чтение истории") {
            for (pass in 0 until passes) {
                HistoryAccessExperiment.catalogWithSingleScan(catalog, now)
            }
        }
        protocol.run()

        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println(
            "Ингредиентов: " + catalog.size + ", записей журнала на ингредиент: " +
                    catalog.first().timestamps.size
        )
        println("Проходов в одном прогоне: " + passes)
        println(
            "Ожидание до измерения: двойное чтение проходит журнал дважды, " +
                    "экономия ~ половина проходов"
        )
        println()
        protocol.print("Р6. Альтернатива: двойное и однократное чтение истории")

        val second = protocol.samples("двойное чтение истории").median
        val single = protocol.samples("одно чтение истории").median
        val ratio = second / single
        val saving = (second - single) / second * 100.0
        val notes = StringBuilder()
        notes.append("Двойное чтение (медиана): " + TimeMeasurer.format(second, 1) + " мс\n")
        notes.append("Одно чтение (медиана): " + TimeMeasurer.format(single, 1) + " мс\n")
        notes.append("Отношение Tдвойное / Tодно: " + TimeMeasurer.format(ratio, 2) + "\n")
        notes.append("Экономия времени: " + TimeMeasurer.format(saving, 1) + "%\n")
        notes.append("Ожидание до измерения: отношение около 2, экономия около 50%\n")
        notes.append("Результат прогноза у обеих форм совпадает\n")
        println()
        println(notes)

        protocol.save(
            name = "pz04-warehouse-alternative",
            title = "Р6. Альтернатива: двойное и однократное чтение истории",
            conditions = TimeMeasurer.conditions() +
                    "Ингредиентов: " + catalog.size + ", записей журнала на ингредиент: " +
                    catalog.first().timestamps.size + "\n" +
                    "Проходов в одном прогоне: " + passes + "\n" +
                    "Ожидание до измерения: двойное чтение проходит журнал дважды, " +
                    "экономия ~ половина проходов\n",
            notes = notes.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("двойное чтение истории").values.size == 5
        )
    }
}