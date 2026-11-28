package com.kitchino.app.dishbatch.domain

data class DiscountThresholds(
    val writeoff: Double,
    val heavy: Double,
    val light: Double
) {
    fun isValid(): Boolean =
        writeoff >= 0.0 && writeoff < heavy && heavy < light && light <= 1.0

    fun writeoffPercentText(): String = percentText(writeoff)

    fun heavyPercentText(): String = percentText(heavy)

    fun lightPercentText(): String = percentText(light)

    private fun percentText(value: Double): String {
        val rounded = Math.round(value * 1000.0) / 10.0
        return if (rounded == Math.floor(rounded)) rounded.toLong().toString() else rounded.toString()
    }

    companion object {
        val DEFAULT = DiscountThresholds(0.05, 0.20, 0.50)
    }
}
