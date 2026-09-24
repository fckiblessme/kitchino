package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Продукт на складе
@Entity("ingredient")
data class IngredientEntity (
    // Уникальный номер ингредиента
    @PrimaryKey(autoGenerate = true) val idIngredient: Long =0,
    // Название продукта
    val name:String,
    // Единица измерения
    val unit: String,
    // Сколько дней идет поставка
    val leadTimeDays: Int,
)