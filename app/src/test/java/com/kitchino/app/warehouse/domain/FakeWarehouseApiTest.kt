package com.kitchino.app.warehouse.domain

import com.kitchino.app.core.contracts.ConsumeResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeWarehouseApiTest {

    private fun api(): FakeWarehouseApi = FakeWarehouseApi(
        recipes = listOf(
            FakeRecipe(
                id = 1,
                dishName = "Борщ",
                components = listOf(
                    FakeComponent(1L, "Картофель", 200.0, "г"),
                    FakeComponent(2L, "Говядина", 150.0, "г")
                )
            )
        ),
        stock = mapOf(1L to 1000.0, 2L to 750.0)
    )

    // Штатный случай: рецепт есть, ингредиентов хватает
    @Test
    fun normalCase_enoughStock_returnsSuccess() = runBlocking {
        val result = api().consumeIngredients(recipeId = 1, batchSize = 1)
        assertEquals(ConsumeResult.Success, result)
    }

    // Граничный случай: запас ровно равен требуемому количеству
    @Test
    fun boundaryCase_stockExactlyEqualsRequired_returnsSuccess() = runBlocking {
        // 5 порций: картофель 200*5=1000 (есть 1000), говядина 150*5=750 (есть 750)
        val result = api().consumeIngredients(recipeId = 1, batchSize = 5)
        assertEquals(ConsumeResult.Success, result)
    }

    // Ошибочный случай: рецепт не найден
    @Test
    fun errorCase_recipeNotFound_returnsRecipeNotFound() = runBlocking {
        val result = api().consumeIngredients(recipeId = 999, batchSize = 1)
        assertEquals(ConsumeResult.RecipeNotFound, result)
    }

    // Ошибочный случай: не хватает ингредиентов
    @Test
    fun errorCase_insufficientStock_returnsInsufficientStock() = runBlocking {

        val result = api().consumeIngredients(recipeId = 1, batchSize = 6)
        assertTrue(result is ConsumeResult.InsufficientStock)
        val missing = (result as ConsumeResult.InsufficientStock).missing
        assertEquals(2, missing.size)
        val potatoMissing = missing.first { it.name == "Картофель" }
        assertEquals(200.0, potatoMissing.missingAmount, 0.001)
        val beefMissing = missing.first { it.name == "Говядина" }
        assertEquals(150.0, beefMissing.missingAmount, 0.001)
    }
}