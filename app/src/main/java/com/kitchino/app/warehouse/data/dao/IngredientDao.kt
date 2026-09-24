package com.kitchino.app.warehouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kitchino.app.warehouse.data.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow
// Доступ к таблице с ингредиентами
@Dao
interface IngredientDao {
    // Все ингредиенты, отсортированные по имени
    @Query("select * from ingredient order by name")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    // Поиск ингредиента по номеру
    @Query("select * from ingredient where idIngredient = :ingredientId ")
    suspend fun getIngredientById(ingredientId:Long): IngredientEntity?

    // Поиск ингредиентов по списку номеров
    @Query("select * from ingredient where idIngredient in (:ids)")
    suspend fun getIngredientsByIds (ids: List<Long>): List<IngredientEntity>

    // Добавление нового ингредиента
    @Insert
    suspend fun insertNewIngredient(ingredient: IngredientEntity): Long

    // Добавление нескольких ингредиентов
    @Insert
    suspend fun insertAllIngredients(ingredients:List<IngredientEntity>)

    // Обновление существующего ингредиента
    @Update
    suspend fun updateExistingIngredient(ingredient: IngredientEntity)

    // Удаление ингредиента
    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

}