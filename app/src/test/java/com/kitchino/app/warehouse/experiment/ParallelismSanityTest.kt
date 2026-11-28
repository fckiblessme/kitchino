package com.kitchino.app.warehouse.experiment

import com.kitchino.app.experiment.TimeMeasurer
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale


class ParallelismSanityTest {
    private val totalIterations = 40_000_000L
    private fun runWork(threads: Int): Long {
        val perThread = totalIterations / threads
        val results = LongArray(threads)
        val failures = arrayOfNulls<Throwable>(threads)
        val workers = ArrayList<Thread>(threads)
        for (worker in 0 until threads) {
            val index = worker
            val thread = Thread {
                try {
                    var accumulator = index.toLong() * 2_654_435_761L + 1L
                    for (step in 0 until perThread) {
                        accumulator = accumulator * 6_364_136_223_846_793_005L +
                                1_442_695_040_888_963_407L
                        accumulator = accumulator xor (accumulator ushr 29)
                    }
                    results[index] = accumulator
                } catch (error: Throwable) {
                    failures[index] = error
                }
            }
            thread.name = "sanity-worker-" + worker
            workers.add(thread)
        }
        for (thread in workers) {
            thread.start()
        }
        for (thread in workers) {
            thread.join()
        }
        for (failure in failures) {
            if (failure != null) {
                throw IllegalStateException("Исполнитель завершился ошибкой", failure)
            }
        }
        var sum = 0L
        for (value in results) {
            sum += value
        }
        return sum
    }

    @Test
    fun threadsRunInParallel() {
        val processors = Runtime.getRuntime().availableProcessors()
        val counts = intArrayOf(1, 2, 4, processors)
        val protocol = TimeMeasurer(warmup = 2, repeats = 5)
        for (p in counts) {
            protocol.variant(if (p == 1) "1 исполнитель" else "исполнителей: $p") {
                runWork(p)
            }
        }
        protocol.run()

        val baseline = protocol.samples("1 исполнитель").median
        println("Условия измерения:")
        println(TimeMeasurer.conditions())
        println("Шагов счета всего: " + totalIterations + ", арифметика без обращения к памяти")
        println()
        protocol.print("Проверка среды: масштабируется ли чистая арифметика")

        val table = StringBuilder()
        table.append(
            String.format(Locale.ROOT, "%-20s %10s %8s %8s", "вариант", "медиана", "S", "E")
        ).append('\n')
        table.append("-".repeat(50)).append('\n')
        for (p in counts) {
            val label = if (p == 1) "1 исполнитель" else "исполнителей: $p"
            val samples = protocol.samples(label)
            val speedup = TimeMeasurer.speedup(baseline, samples.median)
            table.append(
                String.format(
                    Locale.ROOT,
                    "%-20s %10s %8s %8s",
                    label,
                    TimeMeasurer.format(samples.median, 1),
                    TimeMeasurer.format(speedup, 2),
                    TimeMeasurer.format(
                        TimeMeasurer.efficiency(speedup, p) * 100.0, 0
                    ) + "%"
                )
            ).append('\n')
        }
        table.append("-".repeat(50)).append('\n')
        println(table)

        protocol.save(
            name = "pz04-warehouse-sanity",
            title = "Проверка среды: масштабируется ли чистая арифметика",
            conditions = TimeMeasurer.conditions() +
                    "Шагов счета всего: " + totalIterations + ", арифметика без обращения к памяти\n",
            notes = table.toString()
        )

        assertTrue(
            "серия должна содержать пять повторов",
            protocol.samples("1 исполнитель").values.size == 5
        )
    }
}