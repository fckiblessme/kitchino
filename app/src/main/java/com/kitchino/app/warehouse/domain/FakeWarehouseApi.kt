package com.kitchino.app.warehouse.domain

import com.kitchino.app.core.contracts.ConsumeResult
import com.kitchino.app.core.contracts.MissingIngredient
import com.kitchino.app.core.contracts.RecipeInfo
import com.kitchino.app.core.contracts.WarehouseApi

/**
 * Вторая реализация контракта [WarehouseApi] без обращения к базе Room.
 *
 * Назначение: проверка поведения потребителя ([MakeDishViewModel]) в
 * воспроизводимых сценариях без эмулятора и заполненной базы. Та же операция
 * [consumeIngredients], тот же контракт [ConsumeResult], но данные хранятся
 * в памяти.
 *
 * Ограничения (по сравнению с [WarehouseApiImpl]):
 *  - не обращается к базе Room и не пишет журнал списаний;
 *  - остатки фиксированы в конструкторе и не изменяются между вызовами
 *    (каждый вызов оценивается по начальному запасу);
 *  - не моделирует распределение по партиям и срокам годности.
 *
 * Поэтому FakeWarehouseApi подтверждает работу потребителя с контрактом, но
 * не подтверждает целостность базы: эту проверку выполняет WarehouseApiImpl.
 */
class FakeWarehouseApi(
    private val recipes: List<FakeRecipe> = emptyList(),
    private val stock: Map<Long, Double> = emptyMap()
) : WarehouseApi {

    override suspend fun consumeIngredients(recipeId: Long, batchSize: Int): ConsumeResult {
        require(batchSize > 0) { "batchSize должен быть положительным" }

        val recipe = recipes.firstOrNull { it.id == recipeId }
            ?: return ConsumeResult.RecipeNotFound

        val missing = mutableListOf<MissingIngredient>()
        for (component in recipe.components) {
            val requiredAmount = component.amountPerUnit * batchSize
            val availableAmount = stock[component.ingredientId] ?: 0.0
            if (availableAmount < requiredAmount) {
                missing.add(
                    MissingIngredient(
                        ingredientId = component.ingredientId,
                        name = component.name,
                        missingAmount = requiredAmount - availableAmount,
                        unit = component.unit
                    )
                )
            }
        }
        return if (missing.isEmpty()) ConsumeResult.Success
        else ConsumeResult.InsufficientStock(missing)
    }

    override suspend fun getRecipeInfo(recipeId: Long): RecipeInfo? {
        return recipes.firstOrNull { it.id == recipeId }?.let {
            RecipeInfo(id = it.id, dishName = it.dishName)
        }
    }

    override suspend fun listRecipes(): List<RecipeInfo> {
        return recipes.map { RecipeInfo(id = it.id, dishName = it.dishName) }
    }

    companion object {
        /**
         * Демонстрационный набор: один рецепт, при порциях <= 5 хватает,
         * при порциях > 5 дефицит говядины; id 999 - несуществующий рецепт.
         */
        fun demo(): FakeWarehouseApi = FakeWarehouseApi(
            recipes = listOf(
                FakeRecipe(
                    id = 1,
                    dishName = "Борщ (демо)",
                    components = listOf(
                        FakeComponent(1L, "Картофель", 200.0, "г"),
                        FakeComponent(2L, "Говядина", 150.0, "г")
                    )
                )
            ),
            stock = mapOf(1L to 10000.0, 2L to 750.0)
        )
    }
}

data class FakeRecipe(
    val id: Long,
    val dishName: String,
    val components: List<FakeComponent>
)

data class FakeComponent(
    val ingredientId: Long,
    val name: String,
    val amountPerUnit: Double,
    val unit: String
)