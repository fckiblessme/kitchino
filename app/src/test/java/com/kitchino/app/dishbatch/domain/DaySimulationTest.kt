package com.kitchino.app.dishbatch.domain

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DaySimulationTest {

    private companion object {
        val HOUR = 3_600_000L
    }

    // Модельный день: медленное блюдо (за срок жизни успевает продаться только частично),
    // быстрое и среднее (успевают целиком)
    private val day = listOf(
        SimDish("Суп дня", price = 300.0, portions = 150, shelfLifeMillis = 6 * HOUR, basePortionsPerHour = 10.0),
        SimDish("Паста", price = 450.0, portions = 60, shelfLifeMillis = 6 * HOUR, basePortionsPerHour = 20.0),
        SimDish("Салат", price = 350.0, portions = 40, shelfLifeMillis = 4 * HOUR, basePortionsPerHour = 25.0)
    )

    @Test
    fun slowDish_engineReducesWriteOffLosses() {
        val naive = DaySimulation.runNaiveDay(day)
        val engine = DaySimulation.runEngineDay(day)

        val soupNaive = naive.dishes.first { it.name == "Суп дня" }
        val soupEngine = engine.dishes.first { it.name == "Суп дня" }
        assertTrue(
            "движок должен сократить списания супа: ${soupEngine.lostMoney} против ${soupNaive.lostMoney}",
            soupEngine.lostMoney < soupNaive.lostMoney
        )

        println("=== Симуляция дня ресторана ===")
        (naive.dishes zip engine.dishes).forEach { (n, e) ->
            println(
                "${n.name}: без движка списано ${n.writtenOffPortions} порций на ${n.lostMoney}₽, " +
                    "с движком — ${e.writtenOffPortions} на ${e.lostMoney}₽"
            )
        }
        val reduction = (1.0 - engine.lostMoney / naive.lostMoney) * 100.0
        println(
            "Итого за день: потери без движка ${naive.lostMoney}₽, с движком ${engine.lostMoney}₽, " +
                "снижение на ${reduction.toInt()}%"
        )
        println(
            "Выручка за день: без движка ${naive.revenue}₽, с движком ${engine.revenue}₽ " +
                "(скидки слегка удешевляют продажи быстрых блюд)"
        )
    }

    @Test
    fun fastDish_engineDoesNotCreateLosses() {
        val fast = listOf(SimDish("Салат", price = 350.0, portions = 40, shelfLifeMillis = 4 * HOUR, basePortionsPerHour = 25.0))
        val naive = DaySimulation.runNaiveDay(fast)
        val engine = DaySimulation.runEngineDay(fast)

        assertEquals(0.0, naive.dishes.first().lostMoney, 0.0)
        assertEquals(0.0, engine.dishes.first().lostMoney, 0.0)
        assertEquals(naive.dishes.first().soldPortions, engine.dishes.first().soldPortions)
    }

    @Test
    fun overproducedDish_engineCutsLossesButCannotSellEverything() {
        // Переготовка: спрос заведомо не покрывает партию. Движок сокращает потери,
        // но часть партии всё равно не успевает продаться — чудес не бывает
        val soup = listOf(SimDish("Суп дня", price = 300.0, portions = 150, shelfLifeMillis = 6 * HOUR, basePortionsPerHour = 10.0))
        val naive = DaySimulation.runNaiveDay(soup)
        val engine = DaySimulation.runEngineDay(soup)

        assertTrue(
            "движок не должен распродать переготовленную партию в ноль",
            engine.dishes.first().lostMoney > 0.0
        )
        assertTrue(
            "но потери должны упасть: ${engine.dishes.first().lostMoney} против ${naive.dishes.first().lostMoney}",
            engine.dishes.first().lostMoney < naive.dishes.first().lostMoney
        )
    }

    @Test
    fun simulation_isDeterministic() {
        val first = DaySimulation.runEngineDay(day)
        val second = DaySimulation.runEngineDay(day)
        assertEquals(first.dishes, second.dishes)
    }
}
