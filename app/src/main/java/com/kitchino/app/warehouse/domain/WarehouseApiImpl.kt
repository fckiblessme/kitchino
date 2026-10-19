package com.kitchino.app.warehouse.domain

import com.kitchino.app.core.contracts.ConsumeResult
import com.kitchino.app.core.contracts.RecipeInfo
import com.kitchino.app.core.contracts.WarehouseApi
import com.kitchino.app.warehouse.data.repository.WarehouseRepository

class WarehouseApiImpl(
    private val consumeIngredientsUseCase: ConsumeIngredientsUseCase,
    private val repository: WarehouseRepository
) : WarehouseApi {

    // Списать ингредиенты по рецепту
    override suspend fun consumeIngredients(
        recipeId: Long,
        batchSize: Int
    ): ConsumeResult {
        return consumeIngredientsUseCase.execute(recipeId, batchSize)
    }

    // Информация о рецепте
    override suspend fun getRecipeInfo(recipeId: Long): RecipeInfo? {
        val recipe = repository.getRecipeById(recipeId) ?: return null
        return RecipeInfo(
            id = recipe.idRecipe,
            dishName = recipe.dishName
        )
    }

    // Список всех рецептов
    override suspend fun listRecipes(): List<RecipeInfo> {
        val recipes = repository.getAllRecipesOnce()
        return recipes.map { recipe ->
            RecipeInfo(
                id = recipe.idRecipe,
                dishName = recipe.dishName
            )
        }
    }

}