package com.example.sportsgearreserve.models

import java.io.Serializable

data class Equipment(
    val equipmentId: String,
    val name: String,
    val category: String,
    val description: String,
    val totalQuantity: Int,
    val maxPerStudent: Int = 2,
    val maxDurationHours: Int = 4,
    val imageUrl: String = "",
    val isActive: Boolean = true
) : Serializable
