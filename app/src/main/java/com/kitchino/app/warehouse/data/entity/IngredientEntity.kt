package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val idIngredient: Long = 0,
    val name: String,
    val unit: String,
    val leadTimeDays: Int,
    val category: String = "",
    val subcategory: String = ""
)