package com.kitchino.app.dishbatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kitchino.app.core.contracts.AuditApi
import com.kitchino.app.core.contracts.EmployeeApi
import com.kitchino.app.dishbatch.data.DecisionType
import com.kitchino.app.dishbatch.data.DiscountDecisionDao
import com.kitchino.app.dishbatch.data.DiscountDecisionEntity
import com.kitchino.app.dishbatch.data.DishBatchDao
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.SalesLogDao
import com.kitchino.app.dishbatch.data.SalesLogEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.domain.DiscountDecision
import com.kitchino.app.dishbatch.domain.DiscountDecisionProvider
import com.kitchino.app.dishbatch.domain.HysteresisDiscountDecision
import com.kitchino.app.dishbatch.domain.SalesVelocity
import com.kitchino.app.dishbatch.domain.VelocityDiscountDecision
import com.kitchino.app.employee.data.EmployeeDao
import com.kitchino.app.warehouse.data.dao.RecipeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BatchUI (
    val dishBatchEntity: DishBatchEntity,
    val discountDecision: DiscountDecision,
    val dishName: String,
    val employeeName: String
)

private fun DiscountDecisionEntity.toDomainDecision() = DiscountDecision(
    decisionType = decisionType,
    discountPercent = discountPercent,
    discountReason = reason.orEmpty()
)

class DishBatchViewModel(
    private val batchDao: DishBatchDao,
    private val decisionDao: DiscountDecisionDao,
    private val salesLogDao: SalesLogDao,
    private val decisionProvider: DiscountDecisionProvider,
    private val auditApi: AuditApi,
    private val employeeApi: EmployeeApi,
    private val recipeDao: RecipeDao,
    private val employeeDao: EmployeeDao
) : ViewModel() {

    val activeBatchesWithDecisions: StateFlow<List<BatchUI>> = batchDao.getAllBatches()
        .map { batches ->
            val now = System.currentTimeMillis()
            val windowStart = now - SalesVelocity.OBSERVATION_WINDOW_MILLIS.toLong()
            batches.map { batch ->
                val dish = recipeDao.getRecipeById(batch.idDish)
                val employee = employeeDao.findById(batch.idEmployee)
                val sold = salesLogDao.getSoldQuantitySince(batch.idDish, windowStart)
                val earliestSale = salesLogDao.getEarliestSale(batch.idDish, windowStart)
                val velocity = SalesVelocity.calculatePortionsPerHour(
                    sold, earliestSale, now, SalesVelocity.OBSERVATION_WINDOW_MILLIS
                )
                // Цепочка решений: базовая политика -> усиление по темпу -> гистерезис.
                // Темп собирается здесь из журнала — точка сборки для VelocityDiscountDecision;
                // инжектируемый provider остаётся базовой политикой, при отсутствии продаж
                // декоратор возвращает его решение без изменений.
                // Гистерезис держит последнее решение по партии: свежий расчёт может
                // колебаться на границе порога, а ослаблять применённую скидку нельзя
                val previous = decisionDao.getLatestDecisionByBatch(batch.idDishBatch)?.toDomainDecision()
                val decision = HysteresisDiscountDecision(
                    previous,
                    VelocityDiscountDecision(velocity, base = decisionProvider)
                ).recommendAction(batch, now)
                BatchUI(
                    dishBatchEntity = batch,
                    discountDecision = decision,
                    dishName = dish?.dishName ?: "Блюдо #${batch.idDish}",
                    employeeName = employee?.name ?: "Сотрудник #${batch.idEmployee}"
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun applyDecision(item: BatchUI) = viewModelScope.launch {
        decisionDao.insertNewDecision(
            DiscountDecisionEntity(
                0, item.dishBatchEntity.idDishBatch,
                item.discountDecision.decisionType,
                item.discountDecision.discountPercent,
                System.currentTimeMillis(), "System",
                item.discountDecision.discountReason
            )
        )
        when (item.discountDecision.decisionType) {
            DecisionType.WRITE_OFF ->
                batchDao.updateExistingBatch(item.dishBatchEntity.copy(status = Status.WRITTEN_OFF))
            DecisionType.DISCOUNT ->
                batchDao.updateExistingBatch(item.dishBatchEntity.copy(status = Status.WITH_SALE))
            DecisionType.NO_ACTION -> {}
        }

        val empId = employeeApi.getCurrentEmployeeId()
        auditApi.logAction(
            actorId = empId,
            action = item.discountDecision.decisionType.name,
            payload = "batch=${item.dishBatchEntity.idDishBatch} reason=${item.discountDecision.discountReason}"
        )
    }

    fun writeOffBatch(item: BatchUI) = viewModelScope.launch {
        decisionDao.insertNewDecision(
            DiscountDecisionEntity(
                0, item.dishBatchEntity.idDishBatch,
                DecisionType.WRITE_OFF, null,
                System.currentTimeMillis(), "System",
                "Списано вручную (свайп)"
            )
        )
        batchDao.updateExistingBatch(item.dishBatchEntity.copy(status = Status.WRITTEN_OFF))
        val empId = employeeApi.getCurrentEmployeeId()
        auditApi.logAction(
            actorId = empId,
            action = DecisionType.WRITE_OFF.name,
            payload = "batch=${item.dishBatchEntity.idDishBatch} reason=manual_swipe"
        )
    }

    fun recordSale(item: BatchUI, soldQuantity: Int) {
        if (soldQuantity <= 0) return
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
            val newStatus = if (newQuantity == 0) Status.SOLD_OUT else item.dishBatchEntity.status
            batchDao.updateExistingBatch(
                item.dishBatchEntity.copy(quantity = newQuantity, status = newStatus)
            )
        }
    }
}

class DishBatchViewModelFactory(
    private val batchDao: DishBatchDao,
    private val decisionDao: DiscountDecisionDao,
    private val salesLogDao: SalesLogDao,
    private val decisionProvider: DiscountDecisionProvider,
    private val auditApi: AuditApi,
    private val employeeApi: EmployeeApi,
    private val recipeDao: RecipeDao,
    private val employeeDao: EmployeeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DishBatchViewModel(
            batchDao, decisionDao, salesLogDao, decisionProvider, auditApi, employeeApi,
            recipeDao, employeeDao
        ) as T
    }
}