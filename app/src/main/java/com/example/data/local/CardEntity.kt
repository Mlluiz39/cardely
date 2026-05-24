package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String, // Visa, Mastercard, Elo, Hipercard, American Express
    val totalLimit: Double,
    val closingDay: Int,
    val dueDay: Int,
    val colorHex: String
)
