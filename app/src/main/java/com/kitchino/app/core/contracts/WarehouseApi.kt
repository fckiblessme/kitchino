package com.kitchino.app.core.contracts
interface WarehouseApi  {
    // Списание ингредиентов со склада
    suspend fun consumeIngredients(recipeId: Long, batchSize: Int): ConsumeResult
    // Получение информации о рецепте по его номеру
    suspend fun getRecipeInfo(recipeId: Long): RecipeInfo?
    // Получение списка рецептов
    suspend fun listRecipes(): List<RecipeInfo>
}

// Результат спсиания ингредиентов
sealed interface ConsumeResult{
    // Успешное списание
    data object Success: ConsumeResult
    // Нехватка ингредиентов
    data class InsufficientStock(
        val missing: List<MissingIngredient>
    ): ConsumeResult
    // Рецепт не найден
    data object RecipeNotFound: ConsumeResult
}

// Информация о рецепте для отображения
data class RecipeInfo(
    val id:Long,
    val dishName:String
)
// Описание недостающего ингредиента
data class MissingIngredient(
    val ingredientId: Long,
    val name: String,
    val missingAmount: Double,
    val unit: String
)



