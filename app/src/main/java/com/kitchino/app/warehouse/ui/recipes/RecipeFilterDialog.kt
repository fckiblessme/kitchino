package com.kitchino.app.warehouse.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecipeFilterDialog(
    currentFilter: RecipeFilter,
    onApply: (RecipeFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var dishTypes by remember { mutableStateOf(currentFilter.dishTypes) }
    var cookingTechnologies by remember { mutableStateOf(currentFilter.cookingTechnologies) }
    var ingredientCategories by remember { mutableStateOf(currentFilter.ingredientCategories) }
    var ingredientSubcategories by remember { mutableStateOf(currentFilter.ingredientSubcategories) }

    val types = listOf("Закуски", "Салаты", "Супы", "Основные блюда", "Гарнир", "Соус", "Выпечка", "Десерты")
    val technologies = listOf("Варка", "Жарка", "Тушение", "Запекание", "Гриль", "На пару", "Сырое")

    val meat = listOf("Говядина", "Свинина", "Баранина", "Телятина", "Курица", "Индейка")
    val seafood = listOf("Лосось", "Тунец", "Треска", "Форель", "Креветки", "Кальмары")
    val plants = listOf("Овощи", "Грибы", "Картофель", "Фрукты", "Орехи")
    val other = listOf("Яйца", "Сыр", "Молочные продукты", "Крупы", "Макаронные")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Фильтрация рецептов", fontWeight = FontWeight.Bold, color = Color.Black)
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Вид блюда", fontWeight = FontWeight.Bold, color = Color.Black)
                }
                items(types) { type ->
                    FilterChip(
                        selected = dishTypes.contains(type),
                        onClick = {
                            dishTypes = if (dishTypes.contains(type)) dishTypes - type else dishTypes + type
                        },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Технология", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
                }
                items(technologies) { tech ->
                    FilterChip(
                        selected = cookingTechnologies.contains(tech),
                        onClick = {
                            cookingTechnologies = if (cookingTechnologies.contains(tech)) cookingTechnologies - tech else cookingTechnologies + tech
                        },
                        label = { Text(tech) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("Ингредиенты", fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 8.dp))
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        title = "Мясо",
                        categoryName = "Мясо",
                        subcategories = meat,
                        ingredientCategories = ingredientCategories,
                        onCategoryChange = { ingredientCategories = it },
                        onSubcategoryChange = { ingredientSubcategories = it }
                    )
                }
                items(meat) { sub ->
                    FilterChip(
                        selected = ingredientSubcategories.contains(sub),
                        onClick = {
                            if (ingredientSubcategories.contains(sub)) {
                                ingredientSubcategories = ingredientSubcategories - sub
                            } else {
                                ingredientSubcategories = ingredientSubcategories + sub
                                ingredientCategories = ingredientCategories - "Мясо"
                            }
                        },
                        label = { Text(sub) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        title = "Морепродукты",
                        categoryName = "Морепродукты",
                        subcategories = seafood,
                        ingredientCategories = ingredientCategories,
                        onCategoryChange = { ingredientCategories = it },
                        onSubcategoryChange = { ingredientSubcategories = it }
                    )
                }
                items(seafood) { sub ->
                    FilterChip(
                        selected = ingredientSubcategories.contains(sub),
                        onClick = {
                            if (ingredientSubcategories.contains(sub)) {
                                ingredientSubcategories = ingredientSubcategories - sub
                            } else {
                                ingredientSubcategories = ingredientSubcategories + sub
                                ingredientCategories = ingredientCategories - "Морепродукты"
                            }
                        },
                        label = { Text(sub) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        title = "Растительные продукты",
                        categoryName = "Растительные продукты",
                        subcategories = plants,
                        ingredientCategories = ingredientCategories,
                        onCategoryChange = { ingredientCategories = it },
                        onSubcategoryChange = { ingredientSubcategories = it }
                    )
                }
                items(plants) { sub ->
                    FilterChip(
                        selected = ingredientSubcategories.contains(sub),
                        onClick = {
                            if (ingredientSubcategories.contains(sub)) {
                                ingredientSubcategories = ingredientSubcategories - sub
                            } else {
                                ingredientSubcategories = ingredientSubcategories + sub
                                ingredientCategories = ingredientCategories - "Растительные продукты"
                            }
                        },
                        label = { Text(sub) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoryHeader(
                        title = "Прочее",
                        categoryName = "Прочее",
                        subcategories = other,
                        ingredientCategories = ingredientCategories,
                        onCategoryChange = { ingredientCategories = it },
                        onSubcategoryChange = { ingredientSubcategories = it }
                    )
                }
                items(other) { sub ->
                    FilterChip(
                        selected = ingredientSubcategories.contains(sub),
                        onClick = {
                            if (ingredientSubcategories.contains(sub)) {
                                ingredientSubcategories = ingredientSubcategories - sub
                            } else {
                                ingredientSubcategories = ingredientSubcategories + sub
                                ingredientCategories = ingredientCategories - "Прочее"
                            }
                        },
                        label = { Text(sub) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(RecipeFilter(dishTypes, cookingTechnologies, ingredientCategories, ingredientSubcategories))
                }
            ) {
                Text("Применить", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        onApply(RecipeFilter())
                    }
                ) {
                    Text("Сбросить", color = Color.DarkGray)
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = Color.DarkGray)
                }
            }
        }
    )
}

@Composable
private fun CategoryHeader(
    title: String,
    categoryName: String,
    subcategories: List<String>,
    ingredientCategories: Set<String>,
    onCategoryChange: (Set<String>) -> Unit,
    onSubcategoryChange: (Set<String>) -> Unit
) {
    val isSelected = ingredientCategories.contains(categoryName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.DarkGray,
            fontWeight = FontWeight.SemiBold
        )
        FilterChip(
            selected = isSelected,
            onClick = {
                if (isSelected) {
                    onCategoryChange(ingredientCategories - categoryName)
                } else {
                    onCategoryChange(ingredientCategories + categoryName)
                    onSubcategoryChange(emptySet())
                }
            },
            label = { Text("Все", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color.Black,
                selectedLabelColor = Color.White
            )
        )
    }
}