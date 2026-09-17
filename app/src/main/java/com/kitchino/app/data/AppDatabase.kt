package com.kitchino.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.TypeConverters
import com.kitchino.app.dishbatch.data.Converters
import com.kitchino.app.dishbatch.data.DiscountDecisionDao
import com.kitchino.app.dishbatch.data.DiscountDecisionEntity
import com.kitchino.app.dishbatch.data.DishBatchDao
import com.kitchino.app.dishbatch.data.DishBatchEntity


@Database(entities = [DishBatchEntity::class, DiscountDecisionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase  : RoomDatabase(){
    abstract fun returnDishBatchDao() : DishBatchDao
    abstract fun returnDiscountDecisionDao() : DiscountDecisionDao
}