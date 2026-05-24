package com.example.data.repository

import com.example.data.local.CardDao
import com.example.data.local.CardEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CardelyRepository(
    private val cardDao: CardDao,
    private val transactionDao: TransactionDao
) {
    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByCard(cardId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCard(cardId)

    suspend fun getCardById(id: Int): CardEntity? = cardDao.getCardById(id)

    suspend fun insertCard(card: CardEntity): Long = cardDao.insertCard(card)

    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)

    suspend fun deleteCard(card: CardEntity) {
        // Excluir transações do cartão primeiro
        transactionDao.deleteTransactionsByCard(card.id)
        cardDao.deleteCard(card)
    }

    suspend fun getTransactionById(id: Int): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        if (transaction.type == "CREDIT" && transaction.isParcelado && transaction.totalInstallments > 1) {
            val idx = mutableListOf<TransactionEntity>()
            val baseCal = Calendar.getInstance().apply {
                timeInMillis = transaction.timestamp
            }
            // A cada parcela aumentamos o mês correspondente
            for (i in 1..transaction.totalInstallments) {
                val instCal = Calendar.getInstance().apply {
                    timeInMillis = baseCal.timeInMillis
                    add(Calendar.MONTH, i - 1)
                }
                idx.add(
                    transaction.copy(
                        installmentNumber = i,
                        timestamp = instCal.timeInMillis,
                        status = if (i == 1) "APPROVED" else "FUTURE"
                    )
                )
            }
            transactionDao.insertTransactions(idx)
            return 1L
        } else {
            return transactionDao.insertTransaction(transaction)
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)
}
