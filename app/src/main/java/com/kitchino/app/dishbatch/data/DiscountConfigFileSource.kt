package com.kitchino.app.dishbatch.data

import com.kitchino.app.dishbatch.domain.DiscountConfig
import com.kitchino.app.dishbatch.domain.DiscountConfigSource
import com.kitchino.app.dishbatch.domain.DiscountThresholds
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class DiscountConfigFileSource(private val file: File) : DiscountConfigSource {

    override fun load(): DiscountConfig {
        if (!file.exists() || !file.canRead()) {
            return DiscountConfig(
                DiscountThresholds.DEFAULT, false,
                "Файл конфигурации скидок отсутствует или недоступен, применены пороги по умолчанию"
            )
        }
        val values = HashMap<String, Double>()
        val failedLine = try {
            read(values)
        } catch (e: IOException) {
            return DiscountConfig(
                DiscountThresholds.DEFAULT, false,
                "Ошибка чтения файла конфигурации скидок, применены пороги по умолчанию"
            )
        }
        val thresholds = thresholdsOf(values)
        if (!thresholds.isValid()) {
            return DiscountConfig(
                DiscountThresholds.DEFAULT, false,
                if (failedLine == null) {
                    "Файл конфигурации скидок повреждён, применены пороги по умолчанию"
                } else {
                    "Строка $failedLine файла конфигурации повреждена, применены пороги по умолчанию"
                }
            )
        }
        return DiscountConfig(thresholds, true, null)
    }

    private fun thresholdsOf(values: Map<String, Double>): DiscountThresholds = DiscountThresholds(
        values[KEY_WRITEOFF] ?: Double.NaN,
        values[KEY_HEAVY] ?: Double.NaN,
        values[KEY_LIGHT] ?: Double.NaN
    )

    private fun read(values: MutableMap<String, Double>): Int? {
        file.inputStream().use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                lines.forEachIndexed { index, raw ->
                    val line = raw.trim()
                    if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed
                    val sep = line.indexOf('=')
                    if (sep <= 0) return index + 1
                    val key = line.substring(0, sep).trim()
                    val value = line.substring(sep + 1).trim().toDoubleOrNull()
                    if (key !in KEYS || value == null) return index + 1
                    values[key] = value
                }
            }
        }
        return if (KEYS.all { it in values }) null else 0
    }

    private companion object {
        const val KEY_WRITEOFF = "writeoff"
        const val KEY_HEAVY = "heavy"
        const val KEY_LIGHT = "light"
        val KEYS = setOf(KEY_WRITEOFF, KEY_HEAVY, KEY_LIGHT)
    }
}
