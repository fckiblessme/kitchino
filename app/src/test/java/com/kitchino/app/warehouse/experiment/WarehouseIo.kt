package com.kitchino.app.warehouse.experiment

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object WarehouseIo {


    class Record(val id: Int, val payload: LongArray)

    fun blockingRead(id: Int, latencyMs: Long, payloadSize: Int): Record {
        Thread.sleep(latencyMs)
        val payload = LongArray(payloadSize)
        var seed = id.toLong() * 2_654_435_761L + 1L
        for (index in payload.indices) {
            seed = seed * 6_364_136_223_846_793_005L + 1_442_695_040_888_963_407L
            payload[index] = seed
        }
        return Record(id, payload)
    }

    fun readAllSynchronous(ids: IntArray, latencyMs: Long, payloadSize: Int): List<Record> {
        val result = ArrayList<Record>(ids.size)
        for (id in ids) {
            result.add(blockingRead(id, latencyMs, payloadSize))
        }
        return result
    }

    fun readAllAsynchronous(ids: IntArray, latencyMs: Long, payloadSize: Int): List<Record> {
        val executor: ExecutorService = Executors.newFixedThreadPool(ids.size)
        try {
            val futures = ArrayList<CompletableFuture<Record>>(ids.size)
            for (id in ids) {
                futures.add(
                    CompletableFuture.supplyAsync(
                        { blockingRead(id, latencyMs, payloadSize) },
                        executor
                    )
                )
            }
            return futures.map { it.join() }
        } finally {
            executor.shutdown()
        }
    }

    fun sameResult(expected: List<Record>, actual: List<Record>): Boolean {
        if (expected.size != actual.size) {
            return false
        }
        for (index in expected.indices) {
            if (expected[index].id != actual[index].id) {
                return false
            }
            if (!expected[index].payload.contentEquals(actual[index].payload)) {
                return false
            }
        }
        return true
    }
}