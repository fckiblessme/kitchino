package com.kitchino.app.dishbatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DiscountDecisionDao {
    @Insert
    suspend fun insertNewDecision(discountDecisionEntity: DiscountDecisionEntity) : Long

    @Query("SELECT * FROM discount_decision WHERE idBatch = :batchId ORDER BY decidedAt ASC")
    suspend fun getDecisionByBatch(batchId: Long) :List<DiscountDecisionEntity>

}