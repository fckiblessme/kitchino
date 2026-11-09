package com.kitchino.app.employee.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitchino.app.employee.data.TrainingCardEntity

@Composable
fun TrainingScreen() {
    val cards = remember {
        listOf(
            TrainingCardEntity(1, 1, "Какой главный ингредиент в борще?", "Свекла", 1),
            TrainingCardEntity(2, 1, "Сколько варить говядину для борща?", "1.5 - 2 часа", 2),
            TrainingCardEntity(3, 1, "Что добавляют в борщ для кислинки?", "Лимон или уксус", 3)
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentIndex < cards.size) {
            val card = cards[currentIndex]

            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(card.question, style = MaterialTheme.typography.headlineSmall)

                    if (showAnswer) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(card.answer, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!showAnswer) {
                Button(onClick = { showAnswer = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Показать ответ")
                }
            } else {
                Button(onClick = {
                    showAnswer = false
                    currentIndex++
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Далее")
                }
            }
        } else {
            Text("Обучение завершено!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                currentIndex = 0
                showAnswer = false
            }) {
                Text("Пройти заново")
            }
        }
    }
}