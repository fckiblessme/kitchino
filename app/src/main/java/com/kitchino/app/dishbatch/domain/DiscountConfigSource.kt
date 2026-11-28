package com.kitchino.app.dishbatch.domain

interface DiscountConfigSource {
    fun load(): DiscountConfig
}
