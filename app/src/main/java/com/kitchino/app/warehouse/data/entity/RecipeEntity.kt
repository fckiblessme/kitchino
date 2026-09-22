package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Рецепт блюда
@Entity("recipe")
class RecipeEntity(
    // Уникальный номер рецепта
    @PrimaryKey(autoGenerate = true) val idRecipe: Long = 0,
    // Название
    val dishName: String,
    // Описание
    val description: String
)