package com.kitchino.app.warehouse.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import com.kitchino.app.warehouse.domain.ForecastUseCase
import com.kitchino.app.warehouse.domain.IngredientForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IngredientsUiState(
    val isLoading: Boolean = true,
    val forecasts: List<IngredientForecast> = emptyList(),
    val errorMessage: String? = null
)

data class SupplyState(
    val isSupplying: Boolean = false,
    val error: String? = null
)

class IngredientsViewModel(
    private val repository: WarehouseRepository,
    private val forecastUseCase: ForecastUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0L)

    val uiState: StateFlow<IngredientsUiState> = combine(
        repository.observeAllIngredients(),
        _refreshTrigger
    ) { ingredients, _ ->
        val forecasts = ingredients.mapNotNull { ingredient ->
            forecastUseCase.execute(ingredient.idIngredient)
        }
        IngredientsUiState(
            isLoading = false,
            forecasts = forecasts,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IngredientsUiState()
    )

    private val _supplyState = MutableStateFlow(SupplyState())
    val supplyState: StateFlow<SupplyState> = _supplyState.asStateFlow()

    fun clearSupplyState() {
        _supplyState.value = SupplyState()
    }

    fun supplyIngredient(
        ingredientId: Long,
        amount: Double,
        expirationDays: Int,
        pricePerUnit: Double,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (amount <= 0) {
            onComplete(false, "Количество должно быть больше нуля")
            return
        }
        if (expirationDays <= 0) {
            onComplete(false, "Срок годности должен быть больше нуля")
            return
        }
        viewModelScope.launch {
            _supplyState.value = SupplyState(isSupplying = true)
            try {
                val now = System.currentTimeMillis()
                val batch = IngredientBatchEntity(
                    idIngredient = ingredientId,
                    initialVolume = amount,
                    remainingVolume = amount,
                    receivedAt = now,
                    expirationDate = now + expirationDays * 24L * 60 * 60 * 1000,
                    supplyPricePerUnit = pricePerUnit.coerceAtLeast(0.0)
                )
                repository.insertBatch(batch)
                _refreshTrigger.value = _refreshTrigger.value + 1
                _supplyState.value = SupplyState(isSupplying = false)
                onComplete(true, null)
            } catch (e: Exception) {
                _supplyState.value = SupplyState(isSupplying = false, error = e.message)
                onComplete(false, e.message ?: "Ошибка пополнения")
            }
        }
    }
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