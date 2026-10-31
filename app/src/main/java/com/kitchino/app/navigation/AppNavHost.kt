package com.kitchino.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kitchino.app.ui.StartScreen


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destination.StartScreen.route
    ) {
        composable(route = "start_screen") {
            StartScreen(onLoginClick = {
                navController.navigate(Destination.LoginScreen.route)
            },
            onRegisterClick = {
                    navController.navigate(Destination.RegistrationScreen.route)
            })
      }
    }

}

