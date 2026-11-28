package com.kitchino.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.kitchino.app.employee.EmployeeDependencies
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.dishbatch.ui.DishBatchViewModelFactory
import com.kitchino.app.ui.theme.KitchinoTheme
import com.kitchino.app.warehouse.WarehouseDependencies
import com.kitchino.app.warehouse.data.entity.IngredientBatchEntity
import com.kitchino.app.warehouse.data.entity.IngredientEntity
import com.kitchino.app.warehouse.data.entity.RecipeEntity
import com.kitchino.app.warehouse.data.entity.RecipeIngredientEntity
import com.kitchino.app.warehouse.domain.ConsumeIngredientsUseCase
import kotlinx.coroutines.launch
import com.kitchino.app.BuildConfig
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.dishbatch.data.DiscountConfigFileSource
import com.kitchino.app.dishbatch.domain.DiscountDecisionProvider
import com.kitchino.app.employee.data.EmployeeEntity
import com.kitchino.app.dishbatch.domain.ExpiryDiscountDecision
import com.kitchino.app.employee.data.Role
import com.kitchino.app.navigation.AppNavHost
import com.kitchino.app.audit.AuditEventSender
import java.io.File


class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val employeeDependencies by lazy { EmployeeDependencies(database) }
    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(database.returnEmployeeDao()) as T
        }
    }

    private val dishBatchViewModel: DishBatchViewModel by viewModels {
        val config = DiscountConfigFileSource(File(filesDir, "discounts.conf")).load()
        if (config.warning != null) {
            Toast.makeText(applicationContext, config.warning, Toast.LENGTH_LONG).show()
        }
        val decisionProvider: DiscountDecisionProvider = ExpiryDiscountDecision(config.thresholds)
        DishBatchViewModelFactory(
            database.returnDishBatchDao(),
            database.returnDiscountDecisionDao(),
            database.returnSalesLogDao(),
            decisionProvider,
            employeeDependencies.auditApi,
            employeeDependencies.employeeApi,
            database.returnRecipeDao(),
            database.returnEmployeeDao(),
            AuditEventSender(applicationContext)
        )
    }

    private val warehouseDependencies by lazy {
        WarehouseDependencies(database, employeeDependencies.employeeApi)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Kitchino", "Запуск: версия ${BuildConfig.VERSION_NAME} " +
                "(${BuildConfig.BUILD_TYPE}), SHA ${BuildConfig.GIT_SHA}")
        enableEdgeToEdge()

        lifecycleScope.launch {
            val employeeDao = database.returnEmployeeDao()
            if (employeeDao.findByName("admin") == null) {
                employeeDao.insertEmployee(
                    EmployeeEntity(
                        name = "admin",
                        role = Role.MANAGER,
                        pinHash = "\$2a\$10\$kA5Q6W7MgwBzbspxdI5g2urbnhfF2KhO7MfgFikUrG2GqZh9exy3m"
                    )
                )
                Log.d("Auth", "Создан менеджер admin/0000")
            }

            if (database.returnDishBatchDao().countBatches() == 0) {
                database.returnDishBatchDao().insertNewBatch(
                    DishBatchEntity(
                        idDish = 1, idEmployee = 1, quantity = 5,
                        madeAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 1000 * 60 * 60,
                        status = Status.ACTIVE
                    )
                )
            }

            val ingredientDao = database.returnIngredientDao()
            val batchDao = database.returnIngredientBatchDao()
            val recipeDao = database.returnRecipeDao()

            if (ingredientDao.getIngredientsByIds(listOf(1L)).isEmpty()) {
                val potatoId = ingredientDao.insertNewIngredient(
                    IngredientEntity(name = "Картофель", unit = "г", leadTimeDays = 2)
                )
                val beefId = ingredientDao.insertNewIngredient(
                    IngredientEntity(name = "Говядина", unit = "г", leadTimeDays = 3)
                )

                val now = System.currentTimeMillis()
                batchDao.insertNewBatch(IngredientBatchEntity(
                    idIngredient = potatoId, initialVolume = 10000.0,
                    remainingVolume = 10000.0, receivedAt = now,
                    expirationDate = now + 10 * 86400000L, supplyPricePerUnit = 0.05
                ))
                batchDao.insertNewBatch(IngredientBatchEntity(
                    idIngredient = beefId, initialVolume = 5000.0,
                    remainingVolume = 5000.0, receivedAt = now,
                    expirationDate = now + 5 * 86400000L, supplyPricePerUnit = 0.45
                ))

                val recipeId = recipeDao.insertNewRecipe(
                    RecipeEntity(dishName = "Борщ", description = "Классический суп")
                )
                recipeDao.insertRecipeComposition(listOf(
                    RecipeIngredientEntity(recipeId, potatoId, 200.0),
                    RecipeIngredientEntity(recipeId, beefId, 150.0)
                ))
            }

            val useCase = ConsumeIngredientsUseCase(database, employeeDependencies.employeeApi)
            try {
                useCase.execute(recipeId = 1, batchSize = 5)
            } catch (e: Exception) {
                Log.e("WarehouseDiag", "Ошибка: ${e.message}")
            }
            try {
                useCase.execute(recipeId = 1, batchSize = 0)
            } catch (e: Exception) {
                Log.e("WarehouseDiag", "Исключение: ${e.message}")
            }
        }

        setContent {
            KitchinoTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    dishBatchViewModel = dishBatchViewModel,
                    warehouseDependencies = warehouseDependencies,
                    employeeDependencies = employeeDependencies,
                    onLogout = { authViewModel.reset() }
                )
            }
        }
    }
}