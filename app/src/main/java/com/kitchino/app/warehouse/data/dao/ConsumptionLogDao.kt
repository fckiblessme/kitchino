package com.kitchino.app.warehouse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kitchino.app.warehouse.data.entity.ConsumptionLogEntity
import kotlinx.coroutines.flow.Flow

// Вспомогательный класс для результата агрегации по дням
data class DailyConsumption(
    val dayStartMillis: Long,
    val totalAmount: Double
)

// Доступ к журналу списаний
@Dao
interface ConsumptionLogDao {
    // Записать списание
    @Insert
    suspend fun insertNewLog(log: ConsumptionLogEntity)
    // Записать несколько списаний
    @Insert
    suspend fun insertAllLogs(logs: List<ConsumptionLogEntity>)
    // Все записи журнала
    @Query("select * from consumption_log order by timestamp desc")
    fun getAllLogs(): Flow<List<ConsumptionLogEntity>>

    // Суммарный расход по дням для одного ингредиента
    @Query("""
        select (timestamp / 86400000) * 86400000 as dayStartMillis,
               SUM(amount) as totalAmount
        from consumption_log
        where idIngredient = :ingredientId
        group by dayStartMillis
        order by dayStartMillis
    """)
    suspend fun getDailyConsumption(ingredientId: Long): List<DailyConsumption>

    // Суммарный расход по дням с определенной даты
    @Query("""
        select (timestamp / 86400000) * 86400000 as dayStartMillis,
               SUM(amount) as totalAmount
        from consumption_log
        where idIngredient = :ingredientId and timestamp >= :sinceMillis
        group by dayStartMillis
        order by dayStartMillis
    """)
    suspend fun getDailyConsumptionSince(
        ingredientId: Long,
        sinceMillis: Long
    ): List<DailyConsumption>
}