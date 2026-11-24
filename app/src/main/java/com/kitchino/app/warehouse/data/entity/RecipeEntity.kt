package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val idRecipe: Long = 0,
    val dishName: String,
    val description: String,
    val isReadyMade: Boolean = false,
    val category: String = "",
    val cookingTechnology: String = ""
)