package com.kitchino.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.kitchino.app.warehouse.data.dao.ConsumptionLogDao
import com.kitchino.app.warehouse.data.dao.IngredientDao
import com.kitchino.app.warehouse.data.dao.RecipeDao
import com.kitchino.app.warehouse.data.entity.ConsumptionLogEntity
import com.kitchino.app.warehouse.data.entity.IngredientEntity
import com.kitchino.app.warehouse.data.entity.RecipeEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import com.kitchino.app.warehouse.data.dao.IngredientBatchDao
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity

import androidx.room.TypeConverters
import com.kitchino.app.dishbatch.data.Converters
import com.kitchino.app.dishbatch.data.DiscountDecisionDao
import com.kitchino.app.dishbatch.data.DiscountDecisionEntity
import com.kitchino.app.dishbatch.data.DishBatchDao
import com.kitchino.app.dishbatch.data.DishBatchEntity



@Database(entities = [DishBatchEntity::class, DiscountDecisionEntity::class,  IngredientEntity::class, IngredientBatchEntity::class, RecipeEntity::class, RecipeIngredientEntity::class,ConsumptionLogEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase  : RoomDatabase(){
    abstract fun returnDishBatchDao() : DishBatchDao
    abstract fun returnDiscountDecisionDao() : DiscountDecisionDao

    abstract fun returnIngredientDao(): IngredientDao
    abstract fun returnIngredientBatchDao(): IngredientBatchDao
    abstract fun returnRecipeDao(): RecipeDao
    abstract fun returnConsumptionLogDao(): ConsumptionLogDao

    companion object{
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database"
                    ).build()
                }
                return instance!!
            }
        }
    }
}