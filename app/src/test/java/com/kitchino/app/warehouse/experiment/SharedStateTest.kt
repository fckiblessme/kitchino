package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedStateTest {

    private val threads = 8
    private val increments = 100_000
    private val expected = threads.toLong() * increments


    @Test
    fun raceIsReproducedAndEliminatedBySynchronization() {
        val trials = 20
        var lost = 0
        for (trial in 0 until trials) {
            val value = SharedStateExperiment.runUnsynchronized(threads, increments)
            if (value != expected) {
                lost++
            }
        }
        println("Ожидаемое значение итога: " + expected)
        println("Без синхронизации итог потерялся в " + lost + " из " + trials + " запусков")
        assertTrue("гонка не воспроизвелась ни разу", lost >= 1)

        val withLock = SharedStateExperiment.runWithLock(threads, increments)
        val withLocal = SharedStateExperiment.runWithLocalCounters(threads, increments)
        println("С блокировкой итог: " + withLock)
        println("С локальными счетчиками итог: " + withLocal)
        assertTrue("итог с блокировкой неточен", withLock == expected)
        assertTrue("итог с локальными счетчиками неточен", withLocal == expected)
    }


    @Test
    fun measurementSeries() {
        val protocol = TimeMeasurer(warmup = 2, repeats = 5)
        protocol.variant("без синхронизации (гонка)") {
            SharedStateExperiment.runUnsynchronized(threads, increments)
        }
        protocol.variant("с блокировкой") {
            SharedStateExperiment.runWithLock(threads, increments)
        }
        protocol.variant("локальные счетчики") {
            SharedStateExperiment.runWithLocalCounters(threads, increments)
        }
        protocol.run()

        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println("Исполнителей: " + threads + ", прибавлений на исполнитель: " + increments)
        println("Ожидаемый итог: " + expected)
        println("Ожидание до измерения: без синхронизации итог < ожидаемого (гонка);")
        println("с блокировкой и локальными счетчиками итог точный")
        println()
        protocol.print("Р4. Разделяемое состояние: гонка и способы её устранения")

        var lostTrials = 0
        for (trial in 0 until 20) {
            if (SharedStateExperiment.runUnsynchronized(threads, increments) != expected) {
                lostTrials++
            }
        }
        val notes = StringBuilder()
        notes.append("Ожидаемый итог: " + expected + "\n")
        notes.append("Гонка воспроизведена в " + lostTrials + " из 20 запусков\n")
        notes.append("Стоимость сравнивается только между корректными вариантами:\n")
        notes.append("  с блокировкой и локальными счетчиками\n")
        val lock = protocol.samples("с блокировкой").median
        val local = protocol.samples("локальные счетчики").median
        notes.append("С блокировкой (медиана): " +
                TimeMeasurer.format(lock, 1) + " мс\n")
        notes.append("Локальные счетчики (медиана): " +
                TimeMeasurer.format(local, 1) + " мс\n")
        notes.append("Отношение Tблокировка / Tлокальные = " +
                TimeMeasurer.format(lock / local, 2) + "\n")
        println()
        println(notes)

        protocol.save(
            name = "pz04-warehouse-state",
            title = "Р4. Разделяемое состояние: гонка и способы её устранения",
            conditions = TimeMeasurer.conditions() +
                    "Исполнителей: " + threads + ", прибавлений на исполнитель: " + increments + "\n" +
                    "Ожидаемый итог: " + expected + "\n" +
                    "Ожидание до измерения: без синхронизации итог < ожидаемого (гонка); " +
                    "с блокировкой и локальными счетчиками итог точный\n",
            notes = notes.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("с блокировкой").values.size == 5
        )
    }
}