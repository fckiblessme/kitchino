package com.kitchino.app.employee.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface TrainingDao {
    @Insert
    suspend fun insertCard(card: TrainingCardEntity) : Long

    @Query("Select * from cards where recipeId = :recipeId order by orderIndex ")
    suspend fun  getRecipeCard(recipeId : Long) :List<TrainingCardEntity>

    @Insert
    suspend fun insertProgress(progress: TrainingProgressEntity) : Long

    @Update
    suspend fun updateProgress(progress: TrainingProgressEntity)

    @Query("Select * from progress where employeeId = :employeeId and cardId = :cardId")
    suspend fun findProgress(employeeId: Long, cardId: Long) : TrainingProgressEntity?

    @Query("Select * from progress where employeeId = :employeeId")
    suspend fun getProgress(employeeId: Long) : List<TrainingProgressEntity>







}
