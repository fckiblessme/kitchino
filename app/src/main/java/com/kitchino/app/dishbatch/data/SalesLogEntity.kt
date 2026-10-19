package com.kitchino.app.dishbatch.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Журнал продаж блюд
@Entity(
    tableName = "sales_log",
    indices = [Index("idDish")]
)
data class SalesLogEntity(
    // Уникальный номер записи в журнале
    @PrimaryKey(autoGenerate = true) val idSale: Long = 0,
    // Проданное блюдо
    val idDish: Long,
    // Количество
    val quantity: Int,
    // Время продажи
    val timestamp: Long
)


