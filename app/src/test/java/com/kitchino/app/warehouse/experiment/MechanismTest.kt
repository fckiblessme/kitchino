package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.ForecastProcessing
import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test


class MechanismTest {

    private val now = 1_766_000_000_000L
    private val passes = 10


    @Test
    fun timeScalesWithJournalSize() {
        val small = ForecastProcessing.buildCatalog(
            ingredients = 200,
            recordsPerIngredient = 5000,
            seed = 42L,
            nowMillis = now
        )
        val large = ForecastProcessing.buildCatalog(
            ingredients = 200,
            recordsPerIngredient = 50000,
            seed = 42L,
            nowMillis = now
        )

        val protocol = TimeMeasurer(warmup = 2, repeats = 5)
        protocol.variant("журнал 5000 записей на ингредиент") {
            for (pass in 0 until passes) {
                ForecastProcessing.calculateSequential(small, now)
            }
        }
        protocol.variant("журнал 50000 записей на ингредиент") {
            for (pass in 0 until passes) {
                ForecastProcessing.calculateSequential(large, now)
            }
        }
        protocol.run()

        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println("Ингредиентов: " + small.size + ", число записей журнала меняется в 10 раз")
        println("Проходов в одном прогоне: " + passes)
        println("Ожидание до измерения: при линейной зависимости время ~ 10x")
        println()
        protocol.print("Р5. Гипотеза: время прогноза зависит от числа записей журнала")

        val tSmall = protocol.samples("журнал 5000 записей на ингредиент").median
        val tLarge = protocol.samples("журнал 50000 записей на ингредиент").median
        val ratio = tLarge / tSmall
        val verdict = if (ratio >= 7.0) "близка к линейной" else "сублинейная"
        val notes = StringBuilder()
        notes.append("Медиана при 5000 записей: " + TimeMeasurer.format(tSmall, 1) + " мс\n")
        notes.append("Медиана при 50000 записей: " + TimeMeasurer.format(tLarge, 1) + " мс\n")
        notes.append("Отношение времени: " + TimeMeasurer.format(ratio, 2) + "\n")
        notes.append("Ожидание при линейной зависимости: 10.00\n")
        notes.append("Вывод: зависимость " + verdict + "\n")
        println()
        println(notes)

        protocol.save(
            name = "pz04-warehouse-mechanism",
            title = "Р5. Гипотеза: время прогноза зависит от числа записей журнала",
            conditions = TimeMeasurer.conditions() +
                    "Ингредиентов: " + small.size + ", число записей журнала меняется в 10 раз\n" +
                    "Проходов в одном прогоне: " + passes + "\n" +
                    "Ожидание до измерения: при линейной зависимости время ~ 10x\n",
            notes = notes.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("журнал 5000 записей на ингредиент").values.size == 5
        )
    }
}