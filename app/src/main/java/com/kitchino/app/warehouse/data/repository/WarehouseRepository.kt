package com.kitchino.app.warehouse.data.repository

import com.kitchino.app.warehouse.data.dao.ConsumptionLogDao
import com.kitchino.app.warehouse.data.dao.DailyConsumption
import com.kitchino.app.warehouse.data.dao.IngredientBatchDao
import com.kitchino.app.warehouse.data.dao.IngredientDao
import com.kitchino.app.warehouse.data.dao.RecipeDao
import com.kitchino.app.warehouse.data.entity.ConsumptionLogEntity
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity
import com.kitchino.app.warehouse.data.entity.IngredientEntity
import com.kitchino.app.warehouse.data.entity.RecipeEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

class WarehouseRepository(
    private val ingredientDao: IngredientDao,
    private val ingredientBatchDao: IngredientBatchDao,
    private val recipeDao: RecipeDao,
    private val consumptionLogDao: ConsumptionLogDao
    ){
    // Список всех ингредиентов
    fun observeAllIngredients(): Flow<List<IngredientEntity>> {
        return ingredientDao.getAllIngredients()
    }
    // Получение ингредиента по идентификатору
    suspend fun getIngredientById(ingredientId:Long):IngredientEntity?{
        return ingredientDao.getIngredientById(ingredientId)
    }
    // Получение списка ингредиентов по их идентификаторам
    suspend fun getIngredientsByIds(ids:List<Long>): List<IngredientEntity>{
        return ingredientDao.getIngredientsByIds(ids)
    }
    // Вставка нового ингредиента
    suspend fun insertIngredient(ingredient: IngredientEntity): Long {
        return ingredientDao.insertNewIngredient(ingredient)
    }
    // Вставка списка ингредиентов
    suspend fun insertAllIngredients(ingredients: List<IngredientEntity>): Unit {
        ingredientDao.insertAllIngredients(ingredients)
    }
    // Обновление существующего ингредиента
    suspend fun updateIngredient(ingredient: IngredientEntity): Unit {
        ingredientDao.updateExistingIngredient(ingredient)
    }
    // Удаление ингредиентов
    suspend fun deleteIngredient(ingredient: IngredientEntity): Unit {
        ingredientDao.deleteIngredient(ingredient)
    }


    // Вставка новой партии ингредиентов
    suspend fun insertBatch(batch: IngredientBatchEntity): Long {
        return ingredientBatchDao.insertNewBatch(batch)
    }
    // Вставка списка партий продуктов
    suspend fun insertAllBatches(batches: List<IngredientBatchEntity>): Unit {
        ingredientBatchDao.insertAllBatches(batches)
    }
    // Получение доступных партий конкретного ингредиента
    suspend fun getAvailableBatches(ingredientId: Long): List<IngredientBatchEntity> {
        return ingredientBatchDao.getAvailableBatches(ingredientId)
    }
    // Получение общего остатка указанного ингредиента
    suspend fun getTotalStock(ingredientId: Long): Double {
        return ingredientBatchDao.getTotalStock(ingredientId)
    }
    // Получение ближайшей даты истечения срока конкретного ингредиента
    suspend fun getEarliestExpiration(ingredientId: Long): Long? {
        return ingredientBatchDao.getEarliestExpiration(ingredientId)
    }
    // Уменьшение объема партии продукта
    suspend fun decreaseBatchVolume(batchId: Long, amount: Double): Unit {
        ingredientBatchDao.decreaseBatchVolume(batchId, amount)
    }

    // Список всех рецептов
    fun observeAllRecipes(): Flow<List<RecipeEntity>> {
        return recipeDao.getAllRecipes()
    }

    suspend fun getAllRecipesOnce(): List<RecipeEntity> {
        return recipeDao.getAllRecipesOnce()
    }
    // Получение рецепта по идентификатору
    suspend fun getRecipeById(recipeId: Long): RecipeEntity? {
        return recipeDao.getRecipeById(recipeId)
    }
    // Получение состава рецепта
    suspend fun getRecipeComposition(recipeId: Long): List<RecipeIngredientEntity> {
        return recipeDao.getRecipeComposition(recipeId)
    }
    // Вставка нового рецепта
    suspend fun insertRecipe(recipe: RecipeEntity): Long {
        return recipeDao.insertNewRecipe(recipe)
    }
    // Вставка состава рецепта
    suspend fun insertRecipeComposition(items: List<RecipeIngredientEntity>): Unit {
        recipeDao.insertRecipeComposition(items)
    }
    // Обновление рецепта
    suspend fun updateRecipe(recipe: RecipeEntity): Unit {
        recipeDao.updateExistingRecipe(recipe)
    }
    // Удаление состава рецепта
    suspend fun clearRecipeComposition(recipeId: Long): Unit {
        recipeDao.clearRecipeComposition(recipeId)
    }
    // Удаление рецепта
    suspend fun deleteRecipe(recipeId: Long): Unit {
        recipeDao.deleteRecipe(recipeId)
    }

    // Вставка записи в журнал списаний
    suspend fun insertConsumptionLog(log: ConsumptionLogEntity): Unit {
        consumptionLogDao.insertNewLog(log)
    }
    // Дневное потребление по указанному рецепту
    suspend fun getDailyConsumption(ingredientId: Long): List<DailyConsumption> {
        return consumptionLogDao.getDailyConsumption(ingredientId)
    }
    // Дневное потребление по указанному рецепту с конкретной даты
    suspend fun getDailyConsumptionSince(
        ingredientId: Long,
        sinceMillis: Long
    ): List<DailyConsumption> {
        return consumptionLogDao.getDailyConsumptionSince(ingredientId, sinceMillis)
    }
}