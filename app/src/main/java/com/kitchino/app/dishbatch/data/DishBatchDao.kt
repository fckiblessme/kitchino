package com.kitchino.app.dishbatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DishBatchDao {
    @Query("SELECT * FROM dish_batch WHERE status IN ('ACTIVE', 'WITH_SALE') ORDER BY expiresAt ASC")
    fun getLiveBatches(): Flow<List<DishBatchEntity>>

    @Query("SELECT COUNT(*) FROM dish_batch")
    suspend fun countBatches(): Int

    @Insert
    suspend fun insertNewBatch(batch: DishBatchEntity): Long

    @Update
    suspend fun updateExistingBatch(batch: DishBatchEntity)
}