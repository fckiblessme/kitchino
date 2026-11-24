package com.kitchino.app.warehouse.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitchino.app.ui.theme.StartScreenColor

@Composable
fun RecipesScreen(
    uiState: RecipesUiState,
    makeDishViewModel: MakeDishViewModel,
    onStartTraining: (recipeId: Long) -> Unit,
    onApplyFilter: (RecipeFilter) -> Unit
) {
    var selectedRecipeId by remember { mutableStateOf<Long?>(null) }
    var recipeToCookId by remember { mutableStateOf<Long?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val selectedRecipe = uiState.recipes.find { it.recipeId == selectedRecipeId }

    if (selectedRecipe != null) {
        RecipeDetailScreen(
            recipe = selectedRecipe,
            onBack = { selectedRecipeId = null },
            onStartTraining = { onStartTraining(selectedRecipe.recipeId) },
            onMakeDish = { recipeToCookId = selectedRecipe.recipeId }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StartScreenColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Все рецепты",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    OutlinedButton(
                        onClick = { showFilterDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Фильтры",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = if (uiState.currentFilter.isEmpty()) "Фильтры" else "Фильтры (вкл)",
                            color = Color.Black
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Black)
                        }
                    }
                    uiState.filteredRecipes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Рецепты не найдены", color = Color.DarkGray)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.filteredRecipes) { recipe ->
                                CompactRecipeCard(
                                    recipe = recipe,
                                    onClick = { selectedRecipeId = recipe.recipeId },
                                    onQuickCook = { recipeToCookId = recipe.recipeId }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        RecipeFilterDialog(
            currentFilter = uiState.currentFilter,
            onApply = { filter ->
                onApplyFilter(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    recipeToCookId?.let { recipeId ->
        val recipe = uiState.recipes.find { it.recipeId == recipeId }
        if (recipe != null) {
            key(recipeId) {
                MakeDishDialog(
                    recipeId = recipeId,
                    recipeName = recipe.dishName,
                    viewModel = makeDishViewModel,
                    onDismiss = {
                        recipeToCookId = null
                        makeDishViewModel.reset()
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactRecipeCard(
    recipe: RecipeWithComposition,
    onClick: () -> Unit,
    onQuickCook: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.dishName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
            IconButton(onClick = onQuickCook) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = "Приготовить",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}