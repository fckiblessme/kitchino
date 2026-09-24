package com.kitchino.app.warehouse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity


@Dao
interface IngredientBatchDao {

    // Добавление партии
    @Insert
    suspend fun insertNewBatch(batch: IngredientBatchEntity): Long

    // Добавление нескольких партий
    @Insert
    suspend fun insertAllBatches(batches: List<IngredientBatchEntity>)

    // Все партии продукта, отсортированные по сроку годности
    @Query("""
        select * from ingredient_batch
        where idIngredient = :ingredientId and remainingVolume > 0
        order by expirationDate asc
    """)
    suspend fun getAvailableBatches(ingredientId: Long): List<IngredientBatchEntity>

    // Суммарный остаток продукта на складе
    @Query("""
        select COALESCE(SUM(remainingVolume), 0.0) from ingredient_batch
        where idIngredient = :ingredientId
    """)
    suspend fun getTotalStock(ingredientId: Long): Double

    // Ближайшая дата истечения среди партий
    @Query("""
        select MIN(expirationDate) from ingredient_batch
        where idIngredient = :ingredientId and remainingVolume > 0
    """)
    suspend fun getEarliestExpiration(ingredientId: Long): Long?

    // Уменьшение остатка конкретной партии
    @Query("""
        update ingredient_batch set remainingVolume = remainingVolume - :amount
        where idBatch = :batchId
    """)
    suspend fun decreaseBatchVolume(batchId: Long, amount: Double)

    // Удаление всех партий с истёкшим сроком
    @Query("""
        delete from ingredient_batch
        where expirationDate < :nowMillis and remainingVolume <= 0.0
    """)
    suspend fun deleteExpiredEmptyBatches(nowMillis: Long)
}