package com.kitchino.app.employee.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
class TrainingCardEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val question: String,
    val answer: String,
    val orderId: Int = 0
)