package com.kitchino.app.warehouse.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.core.contracts.ConsumeResult
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import com.kitchino.app.warehouse.domain.ConsumeIngredientsUseCase
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
    private val repository: WarehouseRepository,
    private val consumeIngredientsUseCase: ConsumeIngredientsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MakeDishUiState())
    val uiState: StateFlow<MakeDishUiState> = _uiState.asStateFlow()

    fun selectRecipe(recipeId: Long) {
        _uiState.value = _uiState.value.copy(selectedRecipeId = recipeId, result = null)
    }

    fun setBatchSize(size: Int) {
        val safeSize = if (size < 1) 1 else size
        _uiState.value = _uiState.value.copy(batchSize = safeSize, result = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null)
    }

    fun makeDish() {
        val recipeId = _uiState.value.selectedRecipeId ?: return
        val batchSize = _uiState.value.batchSize

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, result = null)
            val result = consumeIngredientsUseCase.execute(recipeId, batchSize)
            _uiState.value = _uiState.value.copy(isSubmitting = false, result = result)
        }
    }
}

class MakeDishViewModelFactory(
    private val repository: WarehouseRepository,
    private val consumeIngredientsUseCase: ConsumeIngredientsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MakeDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MakeDishViewModel(repository, consumeIngredientsUseCase) as T
        }
        throw IllegalArgumentException("Неизвестный класс ViewModel: ${modelClass.name}")
    }
}