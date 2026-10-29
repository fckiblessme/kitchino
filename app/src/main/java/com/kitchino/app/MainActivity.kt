package com.kitchino.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.ui.DishBatchScreen
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.dishbatch.ui.DishBatchViewModelFactory
import com.kitchino.app.ui.theme.KitchinoTheme
import com.kitchino.app.warehouse.WarehouseDependencies
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity
import com.kitchino.app.warehouse.data.entity.IngredientEntity
import com.kitchino.app.warehouse.data.entity.RecipeEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import com.kitchino.app.warehouse.domain.ConsumeIngredientsUseCase
import com.kitchino.app.warehouse.ui.ingredients.IngredientsScreen
import com.kitchino.app.warehouse.ui.ingredients.IngredientsViewModel
import com.kitchino.app.warehouse.ui.ingredients.IngredientsViewModelFactory
import com.kitchino.app.warehouse.ui.recipes.MakeDishViewModel
import com.kitchino.app.warehouse.ui.recipes.MakeDishViewModelFactory
import com.kitchino.app.warehouse.ui.recipes.RecipesScreen
import com.kitchino.app.warehouse.ui.recipes.RecipesViewModel
import com.kitchino.app.warehouse.ui.recipes.RecipesViewModelFactory
import kotlinx.coroutines.launch
import com.kitchino.app.BuildConfig
import com.kitchino.app.dishbatch.domain.DiscountDecisionProvider
import com.kitchino.app.dishbatch.domain.ExpiryDiscountDecision
import com.kitchino.app.dishbatch.domain.VelocityDiscountDecision

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }

    private val dishBatchViewModel: DishBatchViewModel by viewModels {
        // Вариант 1 — базовая реализация (только срок годности)
        val decisionProvider: DiscountDecisionProvider = ExpiryDiscountDecision()

        // Вариант 2 — реализация с учётом темпа продаж
        // val decisionProvider: DiscountDecisionProvider = VelocityDiscountDecision(portionsPerHour = 5.0)

        DishBatchViewModelFactory(
            database.returnDishBatchDao(),
            database.returnDiscountDecisionDao(),
            database.returnSalesLogDao(),
            decisionProvider
        )
    }

    private val warehouseDependencies by lazy { WarehouseDependencies(database) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Kitchino", "Запуск: версия ${BuildConfig.VERSION_NAME} " +
                "(${BuildConfig.BUILD_TYPE}), SHA ${BuildConfig.GIT_SHA}")
        enableEdgeToEdge()
        lifecycleScope.launch {
            database.returnDishBatchDao().insertNewBatch(
                DishBatchEntity(
                    idDish = 1, idEmployee = 1, quantity = 5,
                    madeAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + 1000 * 60 * 60,
                    status = Status.ACTIVE
                )
            )

            // Тестовые данные
            val ingredientDao = database.returnIngredientDao()
            val batchDao = database.returnIngredientBatchDao()
            val recipeDao = database.returnRecipeDao()

            if (ingredientDao.getIngredientsByIds(listOf(1L)).isEmpty()) {
                val potatoId = ingredientDao.insertNewIngredient(IngredientEntity(name = "Картофель", unit = "г", leadTimeDays = 2))
                val beefId = ingredientDao.insertNewIngredient(IngredientEntity(name = "Говядина", unit = "г", leadTimeDays = 3))

                val now = System.currentTimeMillis()
                batchDao.insertNewBatch(IngredientBatchEntity(idIngredient = potatoId, initialVolume = 10000.0, remainingVolume = 10000.0, receivedAt = now, expirationDate = now + 10 * 86400000L, supplyPricePerUnit = 0.05))
                batchDao.insertNewBatch(IngredientBatchEntity(idIngredient = beefId, initialVolume = 5000.0, remainingVolume = 5000.0, receivedAt = now, expirationDate = now + 5 * 86400000L, supplyPricePerUnit = 0.45))


                val recipeId = recipeDao.insertNewRecipe(RecipeEntity(dishName = "Борщ", description = "Классический суп"))
                recipeDao.insertRecipeComposition(listOf(
                    RecipeIngredientEntity(recipeId, potatoId, 200.0),
                    RecipeIngredientEntity(recipeId, beefId, 150.0)
                ))
            }

            // Тест логов
            val useCase = ConsumeIngredientsUseCase(database)
            try {
                // Штатный запуск
                useCase.execute(recipeId = 1, batchSize = 5)
            } catch (e: Exception) {
                Log.e("WarehouseDiag", "Ошибка: ${e.message}")
            }

            try {
                // Запуск с ошибкой
                useCase.execute(recipeId = 1, batchSize = 0)
            } catch (e: Exception) {
                Log.e("WarehouseDiag", "Исключение: ${e.message}")
            }
        }

        setContent {
            KitchinoTheme {
                var currentTab by remember { mutableStateOf(0) }

                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        Row {
                            TextButton(onClick = { currentTab = 0 }) { Text("Партии") }
                            TextButton(onClick = { currentTab = 1 }) { Text("Склад") }
                            TextButton(onClick = { currentTab = 2 }) { Text("Рецепты") }
                        }

                        when (currentTab) {
                            0 -> DishBatchScreen(viewModel = dishBatchViewModel)

                            1 -> {
                                val vm: IngredientsViewModel = viewModel(
                                    factory = IngredientsViewModelFactory(
                                        repository = warehouseDependencies.repository,
                                        forecastUseCase = warehouseDependencies.forecastUseCase
                                    )
                                )
                                val state by vm.uiState.collectAsState()
                                IngredientsScreen(uiState = state)
                            }

                            2 -> {
                                val recipesVm: RecipesViewModel = viewModel(
                                    factory = RecipesViewModelFactory(
                                        repository = warehouseDependencies.repository
                                    )
                                )
                                val makeDishVm: MakeDishViewModel = viewModel(
                                    factory = MakeDishViewModelFactory(
                                        warehouseApi = warehouseDependencies.warehouseApi
                                    )
                                )
                                val state by recipesVm.uiState.collectAsState()
                                RecipesScreen(
                                    uiState = state,
                                    makeDishViewModel = makeDishVm
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}