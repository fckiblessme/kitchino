package com.kitchino.app.warehouse.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecipeIngredientDisplay(
    val name: String,
    val amountPerUnit: Double,
    val unit: String,
    val category: String,
    val subcategory: String
)

data class RecipeWithComposition(
    val recipeId: Long,
    val dishName: String,
    val description: String,
    val category: String,
    val cookingTechnology: String,
    val ingredients: List<RecipeIngredientDisplay>
)

data class RecipeFilter(
    val dishTypes: Set<String> = emptySet(),
    val cookingTechnologies: Set<String> = emptySet(),
    val ingredientCategories: Set<String> = emptySet(),
    val ingredientSubcategories: Set<String> = emptySet()
) {
    fun isEmpty(): Boolean {
        return dishTypes.isEmpty() && cookingTechnologies.isEmpty() &&
                ingredientCategories.isEmpty() && ingredientSubcategories.isEmpty()
    }
}

data class RecipesUiState(
    val isLoading: Boolean = true,
    val recipes: List<RecipeWithComposition> = emptyList(),
    val filteredRecipes: List<RecipeWithComposition> = emptyList(),
    val currentFilter: RecipeFilter = RecipeFilter()
)

class RecipesViewModel(
    private val repository: WarehouseRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(RecipeFilter())

    val uiState: StateFlow<RecipesUiState> = combine(
        repository.observeAllRecipes().map { recipes ->
            recipes.map { recipe ->
                val composition = repository.getRecipeComposition(recipe.idRecipe)
                val display = composition.mapNotNull { component ->
                    val ingredient = repository.getIngredientById(component.idIngredient)
                    if (ingredient == null) null
                    else RecipeIngredientDisplay(
                        name = ingredient.name,
                        amountPerUnit = component.amountPerUnit,
                        unit = ingredient.unit,
                        category = ingredient.category,
                        subcategory = ingredient.subcategory
                    )
                }
                RecipeWithComposition(
                    recipeId = recipe.idRecipe,
                    dishName = recipe.dishName,
                    description = recipe.description,
                    category = recipe.category,
                    cookingTechnology = recipe.cookingTechnology,
                    ingredients = display
                )
            }
        },
        _filter
    ) { allRecipes, filter ->
        val filtered = allRecipes.filter { recipe ->
            val matchesType = filter.dishTypes.isEmpty() || recipe.category in filter.dishTypes
            val matchesTech = filter.cookingTechnologies.isEmpty() || recipe.cookingTechnology in filter.cookingTechnologies

            val matchesCat = filter.ingredientCategories.isEmpty() || filter.ingredientCategories.all { cat ->
                recipe.ingredients.any { it.category == cat }
            }

            val matchesSub = filter.ingredientSubcategories.isEmpty() || filter.ingredientSubcategories.all { sub ->
                recipe.ingredients.any { it.subcategory == sub }
            }

            matchesType && matchesTech && matchesCat && matchesSub
        }

        RecipesUiState(
            isLoading = false,
            recipes = allRecipes,
            filteredRecipes = filtered,
            currentFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipesUiState()
    )

    fun applyFilter(filter: RecipeFilter) {
        _filter.value = filter
    }
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