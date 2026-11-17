package com.kitchino.app.employee.domain

import androidx.navigation.NavController
import com.kitchino.app.core.contracts.TrainingApi

class TrainingApiImpl(private val navController: NavController) : TrainingApi {
    override fun startTraining(recipeId: Long) {

        navController.navigate("training_screen/$recipeId")
    }
}