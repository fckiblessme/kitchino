package com.kitchino.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kitchino.app.MainScreenContent
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.ui.LoginScreen
import com.kitchino.app.ui.StartScreen
import com.kitchino.app.warehouse.WarehouseDependencies

@Composable
fun AppNavHost(navController: NavHostController, dishBatchViewModel: DishBatchViewModel,
               warehouseDependencies: WarehouseDependencies, onLogout: () -> Unit) {

    // Создаём AuthViewModel через фабрику (требуется контекст для БД)
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
        composable(route = "start_screen") {
            StartScreen(
                onLoginClick = {
                    navController.navigate(Destination.LoginScreen.route)
                },
                onRegisterClick = {
                    navController.navigate(Destination.RegistrationScreen.route)
                }
            )
        }

        composable(route = Destination.LoginScreen.route) {
            LoginScreen(
                viewModel = authViewModel,
                onMenuClick = {
                    navController.navigate(Destination.MenuScreen.route) {
                        popUpTo(Destination.StartScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Destination.MenuScreen.route) {
            MainScreenContent(
                database = AppDatabase.getDatabase(context),
                dishBatchViewModel = dishBatchViewModel,
                warehouseDependencies = warehouseDependencies,
                onLogout = {
                    onLogout()
                    navController.navigate(Destination.StartScreen.route) {
                        popUpTo(0) { inclusive = true }  // чистим весь стек
                    }
                }
            )
        }

        // Регистрация пока закомментирована — без неё
//        composable(route = "registration_screen") {
//            RegistrationScreen(onLoginClick = {
//                navController.navigate(Destination.LoginScreen.route)
//            })
//        }
    }
}