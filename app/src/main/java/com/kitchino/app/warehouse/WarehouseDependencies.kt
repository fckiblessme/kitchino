package com.kitchino.app.warehouse

import com.kitchino.app.data.AppDatabase
import com.kitchino.app.warehouse.data.repository.WarehouseRepository
import com.kitchino.app.warehouse.domain.ConsumeIngredientsUseCase
import com.kitchino.app.warehouse.domain.ForecastUseCase
import com.kitchino.app.warehouse.domain.WarehouseApiImpl


class WarehouseDependencies(database: AppDatabase) {
    // DAO склада
    val repository: WarehouseRepository = WarehouseRepository(
        ingredientDao = database.returnIngredientDao(),
        ingredientBatchDao = database.returnIngredientBatchDao(),
        recipeDao = database.returnRecipeDao(),
        consumptionLogDao = database.returnConsumptionLogDao()
    )

    val consumeIngredientsUseCase: ConsumeIngredientsUseCase =
        ConsumeIngredientsUseCase(database)

    // Прогноз по ингредиентам
    val forecastUseCase: ForecastUseCase =
        ForecastUseCase(repository)

    // Реализация контракта для других модулей (партии, сотрудники)
    val warehouseApi = WarehouseApiImpl(
        consumeIngredientsUseCase = consumeIngredientsUseCase,
        repository = repository
    )
}