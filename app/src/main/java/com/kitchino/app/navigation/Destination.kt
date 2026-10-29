package com.kitchino.app.navigation

import android.net.NetworkInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed class Destination(val route: String) {
    data object DishBatchScreen : Destination(ROUTE_DISHBATCH_SCREEN)
    data object StartScreen: Destination(ROUTE_START_SCREEN)
    data object LoginScreen: Destination(ROUTE_LOGIN_SCREEN)
    data object RegistrationScreen: Destination(ROUTE_REGISTRATION_SCREEN)
    data object MenuScreen: Destination(ROUTE_MENU_SCREEN)
    data object IngredientsScreen: Destination(ROUTE_INGREDIENTS_SCREEN)
    data object RecipesScreen: Destination(ROUTE_RECIPES_SCREEN)

    companion object{
        private const val ROUTE_START_SCREEN = "start_screen"
        private const val ROUTE_LOGIN_SCREEN = "login_screen"
        private const val ROUTE_REGISTRATION_SCREEN = "registration_screen"
        private const val ROUTE_MENU_SCREEN = "menu_screen"
        private const val ROUTE_DISHBATCH_SCREEN = "dishbatch_screen"
        private const val ROUTE_INGREDIENTS_SCREEN = "ingredients_screen"
        private const val ROUTE_RECIPES_SCREEN = "recipes_screen"
    }
}
