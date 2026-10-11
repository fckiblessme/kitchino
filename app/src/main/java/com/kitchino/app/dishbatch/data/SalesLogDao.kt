package com.kitchino.app.dishbatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SalesLogDao {
    // Записать продажу
    @Insert
    suspend fun insertNewSale(log: SalesLogEntity)

    // Записать несколько продаж
    @Insert
    suspend fun insertAllSales(logs: List<SalesLogEntity>)

    // Сумма проданных порций блюда начиная с момента времени
    @Query("""select SUM(quantity) from sales_log where idDish = :dishId 
            and timestamp >= :sinceMillis""")
    suspend fun getSoldQuantitySince(dishId: Long, sinceMillis: Long): Int?

    // Самая ранняя продажа блюда не старше границы окна
    @Query("""select min(timestamp) from sales_log where idDish = :dishId 
        and timestamp >= :sinceMillis""")
    suspend fun getEarliestSale(dishId: Long, sinceMillis: Long): Long?

}