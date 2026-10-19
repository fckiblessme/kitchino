package com.kitchino.app.warehouse.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import com.kitchino.app.warehouse.domain.ForecastUseCase
import com.kitchino.app.warehouse.domain.IngredientForecast
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


data class IngredientsUiState(
    val isLoading: Boolean = true,
    val forecasts: List<IngredientForecast> = emptyList(),
    val errorMessage: String? = null
)


class IngredientsViewModel(
    private val repository: WarehouseRepository,
    private val forecastUseCase: ForecastUseCase
) : ViewModel() {


    val uiState: StateFlow<IngredientsUiState> =
        repository.observeAllIngredients()
            .map { ingredients ->
                val forecasts = ingredients.mapNotNull { ingredient ->
                    forecastUseCase.execute(ingredient.idIngredient)
                }
                IngredientsUiState(
                    isLoading = false,
                    forecasts = forecasts,
                    errorMessage = null
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = IngredientsUiState()
            )
}


class IngredientsViewModelFactory(
    private val repository: WarehouseRepository,
    private val forecastUseCase: ForecastUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IngredientsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IngredientsViewModel(repository, forecastUseCase) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel: ${modelClass.name}")
    }
}