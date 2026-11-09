package com.kitchino.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.ui.DishBatchScreen
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.ui.LoginScreen
import com.kitchino.app.ui.MenuScreen
import com.kitchino.app.ui.RegistrationScreen
import com.kitchino.app.ui.StartScreen
import com.kitchino.app.ui.SubScreen
import com.kitchino.app.warehouse.WarehouseDependencies
import com.kitchino.app.warehouse.ui.ingredients.IngredientsScreen
import com.kitchino.app.warehouse.ui.ingredients.IngredientsViewModel
import com.kitchino.app.warehouse.ui.ingredients.IngredientsViewModelFactory
import com.kitchino.app.warehouse.ui.recipes.MakeDishViewModel
import com.kitchino.app.warehouse.ui.recipes.MakeDishViewModelFactory
import com.kitchino.app.warehouse.ui.recipes.RecipesScreen
import com.kitchino.app.warehouse.ui.recipes.RecipesViewModel
import com.kitchino.app.warehouse.ui.recipes.RecipesViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    dishBatchViewModel: DishBatchViewModel,
    warehouseDependencies: WarehouseDependencies,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(context).returnEmployeeDao()
                return AuthViewModel(dao) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Destination.StartScreen.route
    ) {
        composable(Destination.StartScreen.route) {
            StartScreen(
                onLoginClick = {
                    navController.navigate(Destination.LoginScreen.route)
                },
                onRegisterClick = {
                    navController.navigate(Destination.RegistrationScreen.route)
                }
            )
        }

        composable(Destination.LoginScreen.route) {
            LoginScreen(
                viewModel = authViewModel,
                onMenuClick = {
                    navController.navigate(Destination.MenuScreen.route) {
                        popUpTo(Destination.StartScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.RegistrationScreen.route) {
            RegistrationScreen(
                viewModel = authViewModel,
                onLoginClick = {
                    onLogout()
                    navController.navigate(Destination.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        composable(Destination.MenuScreen.route) {
            MenuScreen(
                viewModel = authViewModel,
                onActionClick = { action ->
                    when (action) {
                        Action.MAKE_DISH ->
                            navController.navigate(Destination.Recipes.route)
                        Action.EDIT_RECIPE ->
                            navController.navigate(Destination.Recipes.route)
                        Action.WRITE_OFF_PRODUCTS ->
                            navController.navigate(Destination.IngredientsScreen.route)
                        Action.APPLICATION_DISCOUNT ->
                            navController.navigate(Destination.DishBatchScreen.route)
                        Action.VIEW_AUDIT -> { /* TODO: экран аудита */ }
                        Action.EMPLOYEE_MANAGEMENT -> { /* TODO: экран сотрудников */ }
                    }
                },
                onLogout = {
                    onLogout()
                    authViewModel.reset()
                    navController.navigate(Destination.StartScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.DishBatchScreen.route) {
            SubScreen(title = "Партии блюд", onBack = { navController.popBackStack() }) {
                DishBatchScreen(viewModel = dishBatchViewModel)
            }
        }

        composable(Destination.IngredientsScreen.route) {
            SubScreen(title = "Склад", onBack = { navController.popBackStack() }) {
                val vm: IngredientsViewModel = viewModel(
                    factory = IngredientsViewModelFactory(
                        repository = warehouseDependencies.repository,
                        forecastUseCase = warehouseDependencies.forecastUseCase
                    )
                )
                val state by vm.uiState.collectAsState()
                IngredientsScreen(uiState = state)
            }
        }

        composable(Destination.Recipes.route) {
            SubScreen(title = "Рецепты", onBack = { navController.popBackStack() }) {
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
                RecipesScreen(uiState = state, makeDishViewModel = makeDishVm)
            }
        }
    }
}