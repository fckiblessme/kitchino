package com.kitchino.app.warehouse.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.core.contracts.ConsumeResult
import com.kitchino.app.core.contracts.WarehouseApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MakeDishUiState(
    val selectedRecipeId: Long? = null,
    val batchSize: Int = 1,
    val isSubmitting: Boolean = false,
    val result: ConsumeResult? = null
)

class MakeDishViewModel(
    private val warehouseApi: WarehouseApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(MakeDishUiState())
    val uiState: StateFlow<MakeDishUiState> = _uiState.asStateFlow()

    fun selectRecipe(recipeId: Long) {
        _uiState.value = _uiState.value.copy(selectedRecipeId = recipeId, result = null)
    }

    fun setBatchSize(size: Int) {
        _uiState.value = _uiState.value.copy(batchSize = size, result = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null)
    }

    fun reset() {
        _uiState.value = MakeDishUiState()
    }

    fun makeDish(onSuccess: (ConsumeResult) -> Unit) {
        val recipeId = _uiState.value.selectedRecipeId ?: return
        val batchSize = _uiState.value.batchSize
        if (batchSize < 1) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, result = null)
            val result = warehouseApi.consumeIngredients(recipeId, batchSize)
            _uiState.value = _uiState.value.copy(isSubmitting = false, result = result)
            onSuccess(result)
        }
    }
}

class MakeDishViewModelFactory(
    private val warehouseApi: WarehouseApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MakeDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MakeDishViewModel(warehouseApi) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel: ${modelClass.name}")
    }
}