package com.kitchino.app.warehouse.domain

import androidx.room.withTransaction
import com.kitchino.app.core.contracts.ConsumeResult
import com.kitchino.app.core.contracts.MissingIngredient
import com.kitchino.app.core.contracts.EmployeeApi
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.warehouse.data.entity.ConsumptionLogEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import kotlin.collections.isNotEmpty
import android.util.Log
import com.kitchino.app.BuildConfig

class ConsumeIngredientsUseCase(
    private val database: AppDatabase,
    private val employeeApi: EmployeeApi,
    private val reportWriter: ConsumptionReportWriter? = null
) {

    // Срок жизни приготовленного блюда (2 часа) — вынесен в константу, чтобы легко менять
    private val dishShelfLifeMs: Long = 2 * 60 * 60 * 1000L

    suspend fun execute(recipeId: Long, batchSize: Int): ConsumeResult {
        // Проверка количества порций
        require(batchSize > 0) { "batchSize должен быть положительным" }

        // Диагностика
        Log.d("WarehouseDiag", "Запуск списания: версия ${BuildConfig.VERSION_NAME}, SHA ${BuildConfig.GIT_SHA}, рецепт=$recipeId, порций=$batchSize")

        // Проверка существования рецепта
        val recipe = database.returnRecipeDao().getRecipeById(recipeId)
        if (recipe == null) {
            return ConsumeResult.RecipeNotFound
        }

        // Состав рецепта
        val composition = database.returnRecipeDao().getRecipeComposition(recipeId)
        if (composition.isEmpty()) {
            // Рецепт без состава
            return ConsumeResult.Success()
        }

        // Проверка хватает ли ингредиентов
        val missingList = findMissingIngredients(composition, batchSize)
        if (missingList.isNotEmpty()) {
            return ConsumeResult.InsufficientStock(missingList)
        }

        // Списание ингредиентов
        val now = System.currentTimeMillis()
        val employeeId = employeeApi.getCurrentEmployeeId()
        database.withTransaction {
            for (component in composition) {
                // Вычисление размера списания
                val requiredAmount = component.amountPerUnit * batchSize
                // Списание с той партии, где меньше срок годности
                consumeFromBatches(component.idIngredient, requiredAmount)
                // Запись в журнал о списании
                database.returnConsumptionLogDao().insertNewLog(
                    ConsumptionLogEntity(
                        idIngredient = component.idIngredient,
                        timestamp = now,
                        amount = requiredAmount,
                        reason = "consume_recipe",
                        idRecipe = recipeId,
                        batchSize = batchSize
                    )
                )
            }

            // Создание партии готовых блюд — именно она попадает на экран DishBatchScreen
            database.returnDishBatchDao().insertNewBatch(
                DishBatchEntity(
                    idDish = recipeId, // в текущей схеме idDish = id рецепта
                    idEmployee = employeeId,
                    quantity = batchSize,
                    madeAt = now,
                    expiresAt = now + dishShelfLifeMs,
                    status = Status.ACTIVE
                )
            )
        }

        var reportPath: String? = null
        var reportError: String? = null
        val writer = reportWriter
        if (writer != null) {
            val rows = composition.map { component ->
                ReportRow(
                    ingredient = component.idIngredient.toString(),
                    amount = component.amountPerUnit * batchSize,
                    timestamp = now
                )
            }
            when (val reportResult = writer.write(rows)) {
                is ReportWriteResult.Ok -> reportPath = reportResult.path
                is ReportWriteResult.Error -> reportError = reportResult.message
            }
        }

        return ConsumeResult.Success(reportPath = reportPath, reportError = reportError)
    }

    // Проверка чего и сколько не хватает для списания
    private suspend fun findMissingIngredients(
        composition: List<RecipeIngredientEntity>,
        batchSize: Int
    ): List<MissingIngredient> {
        val missing = mutableListOf<MissingIngredient>()

        for (component in composition) {
            // Расчет необходимого числа ингредиентов
            val requiredAmount = component.amountPerUnit * batchSize
            // Получение всех ингредиентов на складе
            val availableAmount = database.returnIngredientBatchDao()
                .getTotalStock(component.idIngredient)
            // При нехватке добавление в список
            if (availableAmount < requiredAmount) {
                // Информация об ингредиенте для отображения информации пользователю
                val ingredient = database.returnIngredientDao()
                    .getIngredientById(component.idIngredient)

                val ingredientName: String
                val ingredientUnit: String
                if (ingredient != null) {
                    ingredientName = ingredient.name
                    ingredientUnit = ingredient.unit
                } else {
                    ingredientName = "неизвестный ингредиент"
                    ingredientUnit = ""
                }

                missing.add(
                    MissingIngredient(
                        ingredientId = component.idIngredient,
                        name = ingredientName,
                        missingAmount = requiredAmount - availableAmount,
                        unit = ingredientUnit
                    )
                )
            }
        }

        return missing
    }
    //  Списание со склада
    private suspend fun consumeFromBatches(ingredientId: Long, amount: Double) {
        // Количество для списания
        var remaining = amount
        // Список партий, сортированный по мере истечения срока годности
        val batches = database.returnIngredientBatchDao()
            .getAvailableBatches(ingredientId)
        val batchDao = database.returnIngredientBatchDao()

        for (batch in batches) {
            // Выход из цикла по окончанию списания
            if (remaining <= 0.0) {
                break
            }
            // Сравнение количества в партии и количества для списания
            val takeFromBatch = minOf(batch.remainingVolume, remaining)
            batchDao.decreaseBatchVolume(batch.idBatch, takeFromBatch)
            remaining -= takeFromBatch
        }
    }
}