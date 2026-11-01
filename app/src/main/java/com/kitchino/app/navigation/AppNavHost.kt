package com.kitchino.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.ui.LoginScreen
import com.kitchino.app.ui.StartScreen

@Composable
fun AppNavHost(navController: NavHostController) {

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

        composable(route = "login_screen") {
            LoginScreen(
                viewModel = authViewModel,
                onMenuClick = {
                    navController.navigate(Destination.MenuScreen.route) {
                        // Чтобы кнопка «Назад» не возвращала на экран входа
                        popUpTo(Destination.StartScreen.route) { inclusive = true }
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