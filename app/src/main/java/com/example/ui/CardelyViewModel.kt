package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.CardelyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CardelyViewModel(private val repository: CardelyRepository) : ViewModel() {

    val cards: StateFlow<List<CardEntity>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modo Motorista de App Ativado
    private val _driverModeEnabled = MutableStateFlow(true)
    val driverModeEnabled: StateFlow<Boolean> = _driverModeEnabled.asStateFlow()

    fun toggleDriverMode() {
        _driverModeEnabled.value = !_driverModeEnabled.value
    }

    // Calculadores de saldo
    val accountBalance: StateFlow<Double> = transactions
        .combine(cards) { transactionList, _ ->
            calculateBalance(transactionList)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Detalhes estendidos dos cartões (limite usado e livre)
    data class CardWithSummary(
        val card: CardEntity,
        val usedLimit: Double,
        val availableLimit: Double,
        val totalLimit: Double
    )

    val cardsWithSummary: StateFlow<List<CardWithSummary>> = cards
        .combine(transactions) { cardList, transactionList ->
            cardList.map { card ->
                // O limite usado acumula todas as parcelas ativas e compras de crédito para esse cartão
                val used = transactionList
                    .filter { it.type == "CREDIT" && it.cardId == card.id && it.status != "CANCELLED" }
                    .sumOf { it.amount }
                CardWithSummary(
                    card = card,
                    usedLimit = used,
                    availableLimit = (card.totalLimit - used).coerceAtLeast(0.0),
                    totalLimit = card.totalLimit
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Métricas do Motorista de App: Lucro diário, semanal, mensal
    data class DriverMetrics(
        val dailyProfit: Double,
        val dailyGains: Double,
        val dailyExpenses: Double,
        val weeklyProfit: Double,
        val weeklyGains: Double,
        val weeklyExpenses: Double,
        val monthlyProfit: Double,
        val monthlyGains: Double,
        val monthlyExpenses: Double
    )

    private val driverEarningsCategories = setOf("Uber", "99", "iFood", "Ganhos", "Corrida")
    private val driverExpensesCategories = setOf("Combustível", "Aluguel do veículo", "Manutenção", "Lavagem", "Alimentação", "Pedágios", "Pedágio")

    val driverMetrics: StateFlow<DriverMetrics> = transactions
        .combine(driverModeEnabled) { transactionList, isEnabled ->
            if (!isEnabled) {
                DriverMetrics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            } else {
                val now = System.currentTimeMillis()
                val oneDayMs = 24 * 60 * 60 * 1000L
                val oneWeekMs = 7 * oneDayMs
                val oneMonthMs = 30 * oneDayMs

                // Diário
                val dailyTrans = filterByTime(transactionList, now, oneDayMs)
                val dailyGains = sumGains(dailyTrans)
                val dailyExpenses = sumExpenses(dailyTrans)

                // Semanal
                val weeklyTrans = filterByTime(transactionList, now, oneWeekMs)
                val weeklyGains = sumGains(weeklyTrans)
                val weeklyExpenses = sumExpenses(weeklyTrans)

                // Mensal
                val monthlyTrans = filterByTime(transactionList, now, oneMonthMs)
                val monthlyGains = sumGains(monthlyTrans)
                val monthlyExpenses = sumExpenses(monthlyTrans)

                DriverMetrics(
                    dailyProfit = dailyGains - dailyExpenses,
                    dailyGains = dailyGains,
                    dailyExpenses = dailyExpenses,
                    weeklyProfit = weeklyGains - weeklyExpenses,
                    weeklyGains = weeklyGains,
                    weeklyExpenses = weeklyExpenses,
                    monthlyProfit = monthlyGains - monthlyExpenses,
                    monthlyGains = monthlyGains,
                    monthlyExpenses = monthlyExpenses
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DriverMetrics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

    // Métricas para Gráficos: Categorias de Gastos e Tendências
    data class CategorySpending(
        val category: String,
        val amount: Double
    )

    val expenseSpendingByCategory: StateFlow<List<CategorySpending>> = transactions
        .combine(driverModeEnabled) { transactionList, _ ->
            transactionList
                .filter { it.status != "CANCELLED" && isExpense(it.type) }
                .groupBy { it.category }
                .map { CategorySpending(it.key, it.value.sumOf { t -> t.amount }) }
                .sortedByDescending { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ações do usuário
    fun addCard(name: String, brand: String, totalLimit: Double, closingDay: Int, dueDay: Int, colorHex: String) {
        viewModelScope.launch {
            repository.insertCard(
                CardEntity(
                    name = name,
                    brand = brand,
                    totalLimit = totalLimit,
                    closingDay = closingDay,
                    dueDay = dueDay,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        totalAmount: Double,
        type: String,
        cardId: Int?,
        category: String,
        isParcelado: Boolean,
        totalInstallments: Int,
        notes: String?,
        locationName: String?,
        latitude: Double? = null,
        longitude: Double? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val amountPerInstallment = if (isParcelado && totalInstallments > 0) {
                amount // se o usuário já digitou o valor da parcela, ou totalAmount/totalInstallments
            } else {
                amount
            }

            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amountPerInstallment,
                    totalAmount = totalAmount,
                    type = type,
                    cardId = cardId,
                    category = category,
                    timestamp = timestamp,
                    isParcelado = isParcelado,
                    totalInstallments = totalInstallments,
                    installmentNumber = 1,
                    notes = notes,
                    locationName = if (locationName.isNullOrBlank()) null else locationName,
                    latitude = latitude,
                    longitude = longitude,
                    status = "APPROVED"
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun cancelTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(status = "CANCELLED"))
        }
    }

    // Auxiliares
    private fun calculateBalance(transactions: List<TransactionEntity>): Double {
        var balance = 0.0
        for (t in transactions) {
            if (t.status == "CANCELLED") continue
            when (t.type) {
                "PIX_RECEIVED", "CASH_INCOME", "TRANSFER_RECEIVED" -> {
                    balance += t.amount
                }
                "DEBIT", "PIX_SENT", "CASH_EXPENSE", "TRANSFER_SENT" -> {
                    balance -= t.amount
                }
                // CREDIT não afeta o saldo bancário líquido, somente o limite do cartão
            }
        }
        return balance
    }

    private fun filterByTime(list: List<TransactionEntity>, baseMs: Long, rangeMs: Long): List<TransactionEntity> {
        return list.filter { it.status != "CANCELLED" && (baseMs - it.timestamp) < rangeMs && it.timestamp <= baseMs }
    }

    private fun sumGains(transactions: List<TransactionEntity>): Double {
        return transactions
            .filter { (it.type == "PIX_RECEIVED" || it.type == "CASH_INCOME" || it.type == "TRANSFER_RECEIVED") && driverEarningsCategories.contains(it.category) }
            .sumOf { it.amount }
    }

    private fun sumExpenses(transactions: List<TransactionEntity>): Double {
        return transactions
            .filter { isExpense(it.type) && driverExpensesCategories.contains(it.category) }
            .sumOf { it.amount }
    }

    private fun isExpense(type: String): Boolean {
        return type == "DEBIT" || type == "PIX_SENT" || type == "CASH_EXPENSE" || type == "CREDIT" || type == "TRANSFER_SENT"
    }
}

class CardelyViewModelFactory(private val repository: CardelyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardelyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardelyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
