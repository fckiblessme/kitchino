package com.kitchino.app.dishbatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("dish_batch")
data class DishBatchEntity (
    @PrimaryKey(autoGenerate = true) val idDishBatch: Long = 0,
    val idDish: Long,
    val idEmployee: Long,
    val quantity: Int,
    val madeAt: Long,
    val expiresAt: Long,
    val status: Status
)

enum class Status {ACTIVE, WITH_SALE, WRITTEN_OFF, SOLD_OUT}
