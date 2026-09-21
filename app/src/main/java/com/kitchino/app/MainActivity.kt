package com.kitchino.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.kitchino.app.data.AppDatabase
import com.kitchino.app.dishbatch.data.DishBatchEntity
import com.kitchino.app.dishbatch.data.Status
import com.kitchino.app.dishbatch.ui.DishBatchScreen
import com.kitchino.app.dishbatch.ui.DishBatchViewModel
import com.kitchino.app.dishbatch.ui.DishBatchViewModelFactory
import com.kitchino.app.ui.theme.KitchinoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dishBatchViewModel: DishBatchViewModel by viewModels {
        DishBatchViewModelFactory(database.returnDishBatchDao(), database.returnDiscountDecisionDao())
    }
    @Suppress("UNUSED_PARAMETER")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            database.returnDishBatchDao().insertNewBatch(
                DishBatchEntity(
                    idDish = 1,
                    idEmployee = 1,
                    quantity = 5,
                    madeAt = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + 1000 * 60 * 60,
                    status = Status.ACTIVE
                )
            )
        }
        setContent {
            KitchinoTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    DishBatchScreen(viewModel = dishBatchViewModel)
                }
            }
        }
    }
}

