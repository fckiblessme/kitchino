package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Журнал списаний ингредиентов
@Entity(
    tableName = "consumption_log",
    indices = [Index("idIngredient")]
)
data class ConsumptionLogEntity(
    // Уникальный номер записи в журнале
    @PrimaryKey(autoGenerate = true) val idConsumptionLog: Long = 0,
    // Списанный ингредиент
    val idIngredient: Long,
    // Время списания
    val timestamp: Long,
    // Количество
    val amount: Double,
    // Причина
    val reason: String,
    // По какому рецепту было списание
    val idRecipe: Long?,
    // Количество порций
    val batchSize: Int?
)