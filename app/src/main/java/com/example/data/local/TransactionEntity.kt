package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double, // Valor total (se crédito à vista) ou valor da parcela (se parcelado)
    val totalAmount: Double, // Valor total da compra original
    val type: String, // CREDIT, DEBIT, PIX_SENT, PIX_RECEIVED, CASH_EXPENSE, CASH_INCOME, TRANSFER
    val cardId: Int? = null, // ID do cartão, se for crédito
    val category: String, // Alimentação, Combustível, Aluguel do veículo, Manutenção, Lavagem, Pedágio, etc.
    val timestamp: Long, // Data e hora
    val isParcelado: Boolean = false,
    val totalInstallments: Int = 1,
    val installmentNumber: Int = 1,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
    val status: String = "APPROVED" // APPROVED, FUTURE, CANCELLED
)
