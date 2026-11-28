package com.kitchino.app.dishbatch.domain

data class DiscountConfig(
    val thresholds: DiscountThresholds,
    val appliedFromFile: Boolean,
    val warning: String?
)
