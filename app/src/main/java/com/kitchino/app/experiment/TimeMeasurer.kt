package com.kitchino.app.experiment

import java.io.File
import java.util.Locale
import kotlin.collections.iterator

class TimeMeasurer(
    private val warmup: Int = 2,
    private val repeats: Int = 5
) {

    class Samples(val label: String, val values: List<Double>) {
        val minimum: Double = values.minOrNull() ?: Double.NaN
        val maximum: Double = values.maxOrNull() ?: Double.NaN
        val median: Double = medianOf(values)

        val spread: Double
            get() = if (median > 0.0) (maximum - minimum) / median * 100.0 else 0.0

        fun raw(): String = values.joinToString(separator = ", ") { format(it, 3) }
    }

    private val blocks = LinkedHashMap<String, () -> Unit>()
    private val collected = LinkedHashMap<String, MutableList<Double>>()
    private val order = ArrayList<String>()

    fun variant(label: String, block: () -> Unit) {
        blocks[label] = block
        collected[label] = ArrayList(repeats)
        order.add(label)
    }

    fun run() {
        for (pass in 0 until warmup) {
            for (block in blocks.values) {
                block()
            }
        }
        for (repeat in 0 until repeats) {
            for ((label, block) in blocks) {
                val startedAt = System.nanoTime()
                block()
                collected.getValue(label).add((System.nanoTime() - startedAt) / 1_000_000.0)
            }
        }
    }

    fun samples(label: String): Samples = Samples(label, collected[label] ?: emptyList())

    fun labels(): List<String> = order

    fun tableText(): String {
        val text = StringBuilder()
        text.append(
            String.format(
                Locale.ROOT,
                "%-34s %11s %11s %11s %10s",
                "вариант", "мин, мс", "медиана", "макс, мс", "размах"
            )
        ).append('\n')
        text.append("-".repeat(108)).append('\n')
        for (label in order) {
            val samples = samples(label)
            text.append(
                String.format(
                    Locale.ROOT,
                    "%-34s %11s %11s %11s %10s",
                    label,
                    format(samples.minimum, 3),
                    format(samples.median, 3),
                    format(samples.maximum, 3),
                    format(samples.spread, 2) + "%"
                )
            ).append('\n')
            text.append("    исходная серия: ").append(samples.raw()).append('\n')
        }
        text.append("-".repeat(108)).append('\n')
        text.append("Прогрев: ").append(warmup).append(" прогона, серия: ").append(repeats)
            .append(" повторов, варианты запускаются поочередно\n")
        text.append("Размах = (максимум - минимум) / медиана; время в миллисекундах\n")
        return text.toString()
    }

    fun print(title: String) {
        println("=".repeat(108))
        println(title)
        println("=".repeat(108))
        println(tableText())
    }

    fun save(name: String, title: String, conditions: String, notes: String = ""): File {
        val directory = outputDir()
        directory.mkdirs()
        val protocolFile = File(directory, name + ".txt")
        protocolFile.writeText(
            title + "\n" + "=".repeat(108) + "\n" + conditions + tableText() +
                    (if (notes.isEmpty()) "" else "\n" + notes)
        )
        val csv = StringBuilder()
        csv.append("variant;")
        for (index in 1..repeats) {
            csv.append("repeat").append(index).append(';')
        }
        csv.append("min;median;max;spread_percent\n")
        for (label in order) {
            val samples = samples(label)
            csv.append(label).append(';')
            for (value in samples.values) {
                csv.append(format(value, 3)).append(';')
            }
            csv.append(format(samples.minimum, 3)).append(';')
            csv.append(format(samples.median, 3)).append(';')
            csv.append(format(samples.maximum, 3)).append(';')
            csv.append(format(samples.spread, 2)).append('\n')
        }
        File(directory, name + "-series.csv").writeText(csv.toString())
        return protocolFile
    }

    companion object {
        fun format(value: Double, digits: Int): String =
            String.format(Locale.ROOT, "%.${digits}f", value)

        fun medianOf(values: List<Double>): Double {
            if (values.isEmpty()) {
                return Double.NaN
            }
            val sorted = values.sorted()
            val middle = sorted.size / 2
            return if (sorted.size % 2 == 1) {
                sorted[middle]
            } else {
                (sorted[middle - 1] + sorted[middle]) / 2.0
            }
        }

        fun speedup(sequentialMillis: Double, parallelMillis: Double): Double =
            if (parallelMillis > 0.0) sequentialMillis / parallelMillis else Double.NaN

        fun efficiency(speedup: Double, p: Int): Double = speedup / p

        fun sequentialShare(speedup: Double, p: Int): Double {
            if (p <= 1 || speedup <= 0.0) {
                return Double.NaN
            }
            return (1.0 / speedup - 1.0 / p) / (1.0 - 1.0 / p)
        }

        fun amdahl(f: Double, p: Int): Double = 1.0 / (f + (1.0 - f) / p)

        fun amdahlLimit(f: Double): Double = if (f > 0.0) 1.0 / f else Double.POSITIVE_INFINITY

        fun conditions(): String {
            val text = StringBuilder()
            text.append("Логических процессоров: ")
                .append(Runtime.getRuntime().availableProcessors()).append('\n')
            text.append("Операционная система: ")
                .append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append('\n')
            text.append("Среда выполнения: ")
                .append(System.getProperty("java.vm.name")).append(' ')
                .append(System.getProperty("java.version")).append('\n')
            text.append("Режим: модульные тесты модуля, без отладчика, прогрев обязателен\n")
            return text.toString()
        }

        fun outputDir(): File {
            val root = File("").absoluteFile
            val candidates = listOf(
                File(root, "docs/experiments"),
                File(root.parentFile, "docs/experiments")
            )
            for (candidate in candidates) {
                if (candidate.parentFile?.isDirectory == true) {
                    return candidate
                }
            }
            return candidates.first()
        }
    }
}