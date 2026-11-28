package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test

class IoBenchmarkTest {

    private val recordCount = 10
    private val latencyMs = 20L
    private val payloadSize = 64

    @Test
    fun synchronousAndAsynchronousGiveSameResult() {
        val ids = IntArray(recordCount) { it + 1 }
        val synchronous = WarehouseIo.readAllSynchronous(ids, latencyMs, payloadSize)
        val asynchronous = WarehouseIo.readAllAsynchronous(ids, latencyMs, payloadSize)
        assertTrue(
            "результаты синхронной и асинхронной версий не совпали",
            WarehouseIo.sameResult(synchronous, asynchronous)
        )
    }


    @Test
    fun measurementSeries() {
        val ids = IntArray(recordCount) { it + 1 }
        val protocol = TimeMeasurer(warmup = 2, repeats = 5)
        protocol.variant("синхронная версия") {
            WarehouseIo.readAllSynchronous(ids, latencyMs, payloadSize)
        }
        protocol.variant("асинхронная версия") {
            WarehouseIo.readAllAsynchronous(ids, latencyMs, payloadSize)
        }
        protocol.run()

        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println("Записей: " + recordCount + ", задержка ответа: " + latencyMs + " мс")
        println("Ожидание до измерения: синхронная ~ " + (recordCount * latencyMs) +
                " мс, асинхронная ~ " + latencyMs + " мс, S ~ " + recordCount)
        println()
        protocol.print("Р3. I/O-bound: синхронное и асинхронное чтение записей")

        val synchronous = protocol.samples("синхронная версия").median
        val asynchronous = protocol.samples("асинхронная версия").median
        val speedup = TimeMeasurer.speedup(synchronous, asynchronous)
        val notes = StringBuilder()
        notes.append("Синхронная версия (медиана): " +
                TimeMeasurer.format(synchronous, 1) + " мс\n")
        notes.append("Асинхронная версия (медиана): " +
                TimeMeasurer.format(asynchronous, 1) + " мс\n")
        notes.append("Ускорение S = Tсинхр / Tасинхр = " +
                TimeMeasurer.format(speedup, 2) + "\n")
        notes.append("Ожидание до измерения: S ~ " + recordCount + "\n")
        println()
        println(notes)

        protocol.save(
            name = "pz04-warehouse-io",
            title = "Р3. I/O-bound: синхронное и асинхронное чтение записей",
            conditions = TimeMeasurer.conditions() +
                    "Записей: " + recordCount + ", задержка ответа: " + latencyMs + " мс\n" +
                    "Ожидание до измерения: синхронная ~ " + (recordCount * latencyMs) +
                    " мс, асинхронная ~ " + latencyMs + " мс, S ~ " + recordCount + "\n",
            notes = notes.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("синхронная версия").values.size == 5
        )
    }
}