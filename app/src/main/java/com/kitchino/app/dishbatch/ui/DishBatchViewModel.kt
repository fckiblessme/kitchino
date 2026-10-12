package com.kitchino.app.dishbatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DiscountDecisionDao
import com.kitchino.app.dishbatch.data.DiscountDecisionEntity
import com.kitchino.app.dishbatch.data.DishBatchDao
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.SalesLogDao
import com.kitchino.app.dishbatch.data.SalesLogEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.domain.DiscountDecision
import com.kitchino.app.dishbatch.domain.DiscountDecisionEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

data class BatchUI (
    val dishBatchEntity: DishBatchEntity,
    val discountDecision: DiscountDecision
)

class DishBatchViewModel (
    private val batchDao: DishBatchDao,
    private val decisionDao: DiscountDecisionDao,
    private val salesLogDao: SalesLogDao
) : ViewModel() {
    val activeBatchesWithDecisions: StateFlow<List<BatchUI>> = batchDao.getAllBatches()
        .map { batches -> val now = System.currentTimeMillis()
        batches.map {batch -> BatchUI(batch, DiscountDecisionEngine.recommendAction(batch, now)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun applyDecision(item: BatchUI) = viewModelScope.launch {
        decisionDao.insertNewDecision(DiscountDecisionEntity(0, item.dishBatchEntity.idDishBatch, item.discountDecision.decisionType, item.discountDecision.discountPercent,
            System.currentTimeMillis(), "System", item.discountDecision.discountReason))
        when(item.discountDecision.decisionType) {
            DecisionType.WRITE_OFF -> batchDao.updateExistingBatch(item.dishBatchEntity.copy(status = Status.WRITTEN_OFF))
            DecisionType.DISCOUNT -> batchDao.updateExistingBatch(item.dishBatchEntity.copy(status = Status.WITH_SALE))
            DecisionType.NO_ACTION -> {}
        }
    }

    fun recordSale(item: BatchUI, soldQuantity: Int) {
        if(soldQuantity <= 0){
            return
        }
        val sold = minOf(soldQuantity, item.dishBatchEntity.quantity)
        viewModelScope.launch {
            salesLogDao.insertNewSale(
                SalesLogEntity(
                    idDish = item.dishBatchEntity.idDish,
                    quantity = sold,
                    timestamp = System.currentTimeMillis()
                )
            )
            val newQuantity = item.dishBatchEntity.quantity - sold
            val newStatus = if (newQuantity == 0) Status.SOLD_OUT else
                item.dishBatchEntity.status
            batchDao.updateExistingBatch(
                item.dishBatchEntity.copy(quantity = newQuantity, status = newStatus)
            )
        }

    }
}

class DishBatchViewModelFactory(
    private val batchDao: DishBatchDao,
    private val decisionDao: DiscountDecisionDao,
    private val salesLogDao: SalesLogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class <T>) : T {
        @Suppress("UNCHECKED_CAST")
        return DishBatchViewModel(batchDao, decisionDao, salesLogDao) as T
    }
}