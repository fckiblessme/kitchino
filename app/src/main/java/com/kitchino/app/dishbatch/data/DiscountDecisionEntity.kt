package com.kitchino.app.dishbatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("discount_decision")
data class DiscountDecisionEntity(
    @PrimaryKey(autoGenerate = true) val idDecision: Long = 0,
    val idBatch : Long,
    val decisionType: DecisionType,
    val discountPercent: Double?,
    val decidedAt: Long,
    val decidedBy: String,
    val reason: String?
)

enum class DecisionType { NO_ACTION, DISCOUNT, WRITE_OFF}