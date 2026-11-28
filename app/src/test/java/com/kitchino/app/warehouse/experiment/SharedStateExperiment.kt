package com.kitchino.app.warehouse.experiment

import java.util.concurrent.locks.ReentrantLock

class SharedStock {

    private val lock = ReentrantLock()

    @Volatile
    private var total: Long = 0
    fun addOneUnsynchronized() {
        val current = total
        Thread.onSpinWait()
        total = current + 1
    }

    fun addOneWithLock() {
        lock.lock()
        try {
            total += 1
        } finally {
            lock.unlock()
        }
    }

    fun reset() {
        total = 0
    }

    fun snapshot(): Long = total
}

object SharedStateExperiment {

    fun runUnsynchronized(threads: Int, increments: Int): Long {
        val stock = SharedStock()
        runWorkers(threads, increments) { stock.addOneUnsynchronized() }
        return stock.snapshot()
    }

    fun runWithLock(threads: Int, increments: Int): Long {
        val stock = SharedStock()
        runWorkers(threads, increments) { stock.addOneWithLock() }
        return stock.snapshot()
    }

    fun runWithLocalCounters(threads: Int, increments: Int): Long {
        val partial = LongArray(threads)
        val failures = arrayOfNulls<Throwable>(threads)
        val workers = ArrayList<Thread>(threads)
        for (worker in 0 until threads) {
            val index = worker
            workers.add(
                Thread {
                    try {
                        var local = 0L
                        for (step in 0 until increments) {
                            local += 1
                        }
                        partial[index] = local
                    } catch (error: Throwable) {
                        failures[index] = error
                    }
                }
            )
        }
        startAndJoin(workers, failures)
        var total = 0L
        for (value in partial) {
            total += value
        }
        return total
    }

    private fun runWorkers(threads: Int, increments: Int, action: () -> Unit) {
        val failures = arrayOfNulls<Throwable>(threads)
        val workers = ArrayList<Thread>(threads)
        for (worker in 0 until threads) {
            val index = worker
            workers.add(
                Thread {
                    try {
                        for (step in 0 until increments) {
                            action()
                        }
                    } catch (error: Throwable) {
                        failures[index] = error
                    }
                }
            )
        }
        startAndJoin(workers, failures)
    }

    private fun startAndJoin(workers: List<Thread>, failures: Array<Throwable?>) {
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
    }
}