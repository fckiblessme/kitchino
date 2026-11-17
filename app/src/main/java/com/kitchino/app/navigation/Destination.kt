package com.kitchino.app.navigation

sealed class Destination(val route: String) {
    data object DishBatchScreen : Destination(ROUTE_DISHBATCH_SCREEN)
    data object StartScreen: Destination(ROUTE_START_SCREEN)
    data object LoginScreen: Destination(ROUTE_LOGIN_SCREEN)
    data object RegistrationScreen: Destination(ROUTE_REGISTRATION_SCREEN)
    data object MenuScreen: Destination(ROUTE_MENU_SCREEN)
    data object IngredientsScreen: Destination(ROUTE_INGREDIENTS_SCREEN)
    data object Recipes: Destination(ROUTE_RECIPES_SCREEN)
    data object AuditScreen : Destination(ROUTE_AUDIT_SCREEN)
    data object TrainingScreen : Destination(TRAINING_SCREEN_PATTERN)

    companion object{
        private const val ROUTE_START_SCREEN = "start_screen"
        private const val ROUTE_LOGIN_SCREEN = "login_screen"
        private const val ROUTE_REGISTRATION_SCREEN = "registration_screen"
        private const val ROUTE_MENU_SCREEN = "menu_screen"
        private const val ROUTE_DISHBATCH_SCREEN = "dishbatch_screen"
        private const val ROUTE_INGREDIENTS_SCREEN = "ingredients_screen"
        private const val ROUTE_RECIPES_SCREEN = "recipes_screen"
        private const val ROUTE_AUDIT_SCREEN = "audit_screen"
        const val ARG_RECIPE_ID = "recipeId"
        const val TRAINING_SCREEN_PATTERN = "training_screen/{$ARG_RECIPE_ID}"
        fun trainingRoute(recipeId: Long): String = "training_screen/$recipeId"
    }
}
