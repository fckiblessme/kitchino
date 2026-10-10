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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kitchino.app.dishbatch.data.Converters
import com.kitchino.app.dishbatch.data.DiscountDecisionDao
import com.kitchino.app.dishbatch.data.DiscountDecisionEntity
import com.kitchino.app.dishbatch.data.DishBatchDao
import com.kitchino.app.dishbatch.data.DishBatchEntity

import com.kitchino.app.employee.data.AuditLogEntity
import com.kitchino.app.employee.data.EmployeeEntity
import com.kitchino.app.employee.data.TrainingCardEntity
import com.kitchino.app.employee.data.TrainingProgressEntity
import com.kitchino.app.employee.data.EmployeeDao
import com.kitchino.app.employee.data.TrainingDao
import com.kitchino.app.employee.data.AuditLogDao


val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingredient` (`idIngredient` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `unit` TEXT NOT NULL, `leadTimeDays` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingredient_batch` (`idBatch` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idIngredient` INTEGER NOT NULL, `initialVolume` REAL NOT NULL, `remainingVolume` REAL NOT NULL, `receivedAt` INTEGER NOT NULL, `expirationDate` INTEGER NOT NULL, `supplyPricePerUnit` REAL NOT NULL, FOREIGN KEY(`idIngredient`) REFERENCES `ingredient`(`idIngredient`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingredient_batch_idIngredient` ON `ingredient_batch` (`idIngredient`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingredient_batch_expirationDate` ON `ingredient_batch` (`expirationDate`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `recipe` (`idRecipe` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dishName` TEXT NOT NULL, `description` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `recipe_ingredient` (`idRecipe` INTEGER NOT NULL, `idIngredient` INTEGER NOT NULL, `amountPerUnit` REAL NOT NULL, PRIMARY KEY(`idRecipe`, `idIngredient`), FOREIGN KEY(`idRecipe`) REFERENCES `recipe`(`idRecipe`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`idIngredient`) REFERENCES `ingredient`(`idIngredient`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_recipe_ingredient_idIngredient` ON `recipe_ingredient` (`idIngredient`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `consumption_log` (`idConsumptionLog` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idIngredient` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `amount` REAL NOT NULL, `reason` TEXT NOT NULL, `idRecipe` INTEGER, `batchSize` INTEGER)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_consumption_log_idIngredient` ON `consumption_log` (`idIngredient`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `audit_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `actorId` INTEGER NOT NULL, `action` TEXT NOT NULL, `payload` TEXT NOT NULL, `prevHash` TEXT NOT NULL, `hash` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `employees` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `pinHash` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER NOT NULL, `question` TEXT NOT NULL, `answer` TEXT NOT NULL, `orderId` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `progress` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `employeeId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL, `interval` INTEGER NOT NULL, `factor` REAL NOT NULL, `nextReview` INTEGER NOT NULL, `repetition` INTEGER NOT NULL)")
    }
}

@Database(entities = [DishBatchEntity::class, DiscountDecisionEntity::class,
    IngredientEntity::class, IngredientBatchEntity::class, RecipeEntity::class,
    RecipeIngredientEntity::class,ConsumptionLogEntity::class, AuditLogEntity::class,
    EmployeeEntity::class, TrainingCardEntity::class, TrainingProgressEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase  : RoomDatabase(){
    abstract fun returnDishBatchDao() : DishBatchDao
    abstract fun returnDiscountDecisionDao() : DiscountDecisionDao

    abstract fun returnIngredientDao(): IngredientDao
    abstract fun returnIngredientBatchDao(): IngredientBatchDao
    abstract fun returnRecipeDao(): RecipeDao
    abstract fun returnConsumptionLogDao(): ConsumptionLogDao
    abstract fun returnEmployeeDao(): EmployeeDao
    abstract fun returnTrainingDao(): TrainingDao
    abstract fun returnAuditLogDao(): AuditLogDao


    companion object{
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database"
                    ).addMigrations(MIGRATION_1_2)
                        .build()
                }
                return instance!!
            }
        }
    }
}