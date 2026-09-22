package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
// Состав рецепта
@Entity(
    tableName = "recipe_ingredient",
    primaryKeys = ["idRecipe", "idIngredient"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["idRecipe"],
            childColumns = ["idRecipe"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["idIngredient"],
            childColumns = ["idIngredient"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("idIngredient")]
)
data class RecipeIngredientEntity(
    // Номер рецепта
    val idRecipe: Long,
    // Номер ингредиента
    val idIngredient: Long,
    // Количество ингредиентов
    val amountPerUnit: Double
)