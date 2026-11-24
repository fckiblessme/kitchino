package com.kitchino.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.ui.DishBatchScreen
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.employee.EmployeeDependencies
import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.ui.TrainingScreen
import com.kitchino.app.employee.ui.audit.AuditScreen
import com.kitchino.app.employee.ui.audit.AuditViewModel
import com.kitchino.app.employee.ui.audit.AuditViewModelFactory
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
    employeeDependencies: EmployeeDependencies,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.getDatabase(context).returnEmployeeDao()
                return AuthViewModel(dao) as T
            }
        }
    )

    val authState by authViewModel.state.collectAsState()
    LaunchedEffect(authState) {
        val s = authState
        if (s is AuthViewModel.State.LoggedIn) {
            employeeDependencies.sessionManager.set(s.user)
        } else {
            employeeDependencies.sessionManager.clear()
        }
    }

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
                        Action.VIEW_AUDIT -> navController.navigate(Destination.AuditScreen.route)
                        Action.EMPLOYEE_MANAGEMENT -> { /* TODO */ }
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
                IngredientsScreen(uiState = state, viewModel = vm)
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
                RecipesScreen(
                    uiState = state,
                    makeDishViewModel = makeDishVm,
                    onStartTraining = { recipeId ->
                        navController.navigate(Destination.trainingRoute(recipeId))
                    },
                    onApplyFilter = { filter ->
                        recipesVm.applyFilter(filter)
                    }
                )
            }
        }


        composable(Destination.AuditScreen.route) {
            SubScreen(title = "Журнал аудита", onBack = { navController.popBackStack() }) {
                val auditVm: AuditViewModel = viewModel(
                    factory = AuditViewModelFactory(
                        auditLogDao = database.returnAuditLogDao(),
                        employeeDao = database.returnEmployeeDao(),
                        sessionManager = employeeDependencies.sessionManager,
                        permissionChecker = employeeDependencies.permissionChecker
                    )
                )
                val state by auditVm.uiState.collectAsState()
                AuditScreen(uiState = state)
            }
        }

        composable(
            route = Destination.TRAINING_SCREEN_PATTERN,
            arguments = listOf(navArgument(Destination.ARG_RECIPE_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(Destination.ARG_RECIPE_ID) ?: 0L
            SubScreen(title = "Обучение", onBack = { navController.popBackStack() }) {
                TrainingScreen(recipeId = recipeId)
            }
        }
    }
}