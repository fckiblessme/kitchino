package com.kitchino.app.warehouse.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "ingredient_batch",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["idIngredient"],
            childColumns = ["idIngredient"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idIngredient"), Index("expirationDate")]
)
data class IngredientBatchEntity(
    // Уникальный номер партии
    @PrimaryKey(autoGenerate = true) val idBatch: Long = 0,

    // Какой продукт в партии
    val idIngredient: Long,

    // Количество в партии при поставке
    val initialVolume: Double,

    // Остаток в партии
    val remainingVolume: Double,

    // Когда партия пришла на склад
    val receivedAt: Long,

    // До какой даты продукт годен
    val expirationDate: Long,

    // Цена за единицу именно в этой партии
    val supplyPricePerUnit: Double
)