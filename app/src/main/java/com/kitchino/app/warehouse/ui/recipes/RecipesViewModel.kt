package com.kitchino.app.warehouse.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecipeIngredientDisplay(
    val name: String,
    val amountPerUnit: Double,
    val unit: String
)


data class RecipeWithComposition(
    val recipeId: Long,
    val dishName: String,
    val description: String,
    val ingredients: List<RecipeIngredientDisplay>
)

data class RecipesUiState(
    val isLoading: Boolean = true,
    val recipes: List<RecipeWithComposition> = emptyList()
)


class RecipesViewModel(
    private val repository: WarehouseRepository
) : ViewModel() {

    val uiState: StateFlow<RecipesUiState> =
        repository.observeAllRecipes()
            .map { recipes ->
                val withComposition = recipes.map { recipe ->
                    val composition = repository.getRecipeComposition(recipe.idRecipe)
                    val display = composition.mapNotNull { component ->
                        val ingredient = repository.getIngredientById(component.idIngredient)
                        if (ingredient == null) {
                            null
                        } else {
                            RecipeIngredientDisplay(
                                name = ingredient.name,
                                amountPerUnit = component.amountPerUnit,
                                unit = ingredient.unit
                            )
                        }
                    }
                    RecipeWithComposition(
                        recipeId = recipe.idRecipe,
                        dishName = recipe.dishName,
                        description = recipe.description,
                        ingredients = display
                    )
                }
                RecipesUiState(isLoading = false, recipes = withComposition)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RecipesUiState()
            )
}

class RecipesViewModelFactory(
    private val repository: WarehouseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipesViewModel(repository) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel: ${modelClass.name}")
    }
}