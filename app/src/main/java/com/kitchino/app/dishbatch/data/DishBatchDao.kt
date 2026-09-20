package com.kitchino.app.dishbatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DishBatchDao {
    @Query("SELECT * FROM dish_batch WHERE status = 'ACTIVE' ORDER BY expiresAt ASC")
    fun getAllBatches(): Flow<List<DishBatchEntity>>

    @Insert
    suspend fun insertNewBatch(batch: DishBatchEntity): Long

    @Update
    suspend fun updateExistingBatch(batch: DishBatchEntity)
}