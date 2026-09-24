package com.kitchino.app.warehouse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kitchino.app.warehouse.data.entity.RecipeEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // Выборка всех рецептов
    @Query("select * from recipe order by dishName")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    // Разовый список рецептов
    @Query("select * from recipe")
    suspend fun getAllRecipesOnce(): List<RecipeEntity>

    // Поиск рецепта по номеру
    @Query("select * from recipe where idRecipe = :recipeId")
    suspend fun getRecipeById(recipeId: Long): RecipeEntity?

    // Получение состава рецепта
    @Query("select * from recipe_ingredient where idRecipe = :recipeId")
    suspend fun getRecipeComposition(recipeId: Long): List<RecipeIngredientEntity>

    // Добавление нового рецепта
    @Insert
    suspend fun insertNewRecipe(recipe: RecipeEntity): Long

    // Добавление нескольких строк состава
    @Insert
    suspend fun insertRecipeComposition(items: List<RecipeIngredientEntity>)

    // Обновление рецепта
    @Update
    suspend fun updateExistingRecipe(recipe: RecipeEntity)

    // Удаление состава рецепта
    @Query("delete from recipe_ingredient where idRecipe = :recipeId")
    suspend fun clearRecipeComposition(recipeId: Long)

    // Удаление рецепта целиком
    @Query("delete from recipe where idRecipe = :recipeId")
    suspend fun deleteRecipe(recipeId: Long)
}