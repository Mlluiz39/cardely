package com.example

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.CardelyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ServiceLocator {
    private var database: AppDatabase? = null
    var repository: CardelyRepository? = null
        private set

    fun init(context: Context) {
        if (database == null) {
            val db = AppDatabase.getInstance(context)
            database = db
            val repo = CardelyRepository(db.cardDao(), db.transactionDao())
            repository = repo

            // Semear dados iniciais se a tabela de cartões estiver vazia
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val cards = repo.allCards.first()
                    if (cards.isEmpty()) {
                        seedInitialData(repo)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun seedInitialData(repo: CardelyRepository) {
        // Enviar os cartões padrão
        val maeCardId = repo.insertCard(
            CardEntity(
                name = "Cartão da Mãe",
                brand = "Visa",
                totalLimit = 3000.0,
                closingDay = 5,
                dueDay = 12,
                colorHex = "#2196F3" // Azul
            )
        ).toInt()

        val compCardId = repo.insertCard(
            CardEntity(
                name = "Nubank Dividido",
                brand = "Mastercard",
                totalLimit = 5000.0,
                closingDay = 10,
                dueDay = 17,
                colorHex = "#9C27B0" // Roxo Nubank
            )
        ).toInt()

        val today = System.currentTimeMillis()
        val yesterday = today - 24 * 60 * 60 * 1000L
        val twoDaysAgo = today - 2 * 24 * 60 * 60 * 1000L

        // Ganhos (Ganhos de App e Pix)
        repo.insertTransaction(
            TransactionEntity(
                title = "Ganhos Semanais Uber",
                amount = 1250.0,
                totalAmount = 1250.0,
                type = "PIX_RECEIVED",
                category = "Uber",
                timestamp = yesterday,
                isParcelado = false
            )
        )

        repo.insertTransaction(
            TransactionEntity(
                title = "Entregas iFood",
                amount = 450.0,
                totalAmount = 450.0,
                type = "CASH_INCOME",
                category = "iFood",
                timestamp = today,
                isParcelado = false
            )
        )

        // Despesas normais e de motorista
        repo.insertTransaction(
            TransactionEntity(
                title = "Posto Ipiranga",
                amount = 180.0,
                totalAmount = 180.0,
                type = "DEBIT",
                category = "Combustível",
                timestamp = today,
                isParcelado = false
            )
        )

        repo.insertTransaction(
            TransactionEntity(
                title = "Almoço Prato Feito",
                amount = 35.0,
                totalAmount = 35.0,
                type = "CASH_EXPENSE",
                category = "Alimentação",
                timestamp = today,
                isParcelado = false
            )
        )

        // Compra Parcelada no Cartão da Mãe (exemplo: pneu comprado em 6x)
        repo.insertTransaction(
            TransactionEntity(
                title = "Pneus AutoCenter",
                amount = 120.0, // R$ 120 por parcela (total R$ 720)
                totalAmount = 720.0,
                type = "CREDIT",
                cardId = maeCardId,
                category = "Manutenção",
                timestamp = twoDaysAgo,
                isParcelado = true,
                totalInstallments = 6,
                installmentNumber = 1,
                notes = "Pneus novos para trabalhar"
            )
        )

        // Compra à vista no cartão dividido
        repo.insertTransaction(
            TransactionEntity(
                title = "Supermercado Carrefour",
                amount = 320.0,
                totalAmount = 320.0,
                type = "CREDIT",
                cardId = compCardId,
                category = "Alimentação",
                timestamp = yesterday,
                isParcelado = false,
                notes = "Compras do mês divididas"
            )
        )
    }
}
