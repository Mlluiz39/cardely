package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.ui.CardelyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(viewModel: CardelyViewModel) {
    val cards by viewModel.cards.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var currentMonthOffset by remember { mutableStateOf(0) } // 0 = Current, etc.

    val brLocale = Locale("pt", "BR")
    val currencyFormatter = NumberFormat.getCurrencyInstance(brLocale)

    val currentCal = Calendar.getInstance()
    currentCal.add(Calendar.MONTH, currentMonthOffset)
    val year = currentCal.get(Calendar.YEAR)
    val month = currentCal.get(Calendar.MONTH) // 0 to 11

    val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val displayMonthName = monthNames[month]

    // Days count in current selected month
    val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calculate events for the active month
    // Events can be:
    // 1. Due day of a card: day == card.dueDay
    // 2. Closing day of a card: day == card.closingDay
    // 3. Transactions linked to CREDIT/DEBIT/PIX on their specific day of month
    data class CalendarEvent(
        val type: String, // CARD_DUE, CARD_CLOSING, TRANSACTION_ITEM
        val title: String,
        val details: String,
        val amount: Double?,
        val colorHex: String
    )

    fun getEventsForDay(day: Int): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        // 1. Card closing/due dates (recurrent monthly)
        cards.forEach { card ->
            if (card.closingDay == day) {
                events.add(
                    CalendarEvent(
                        type = "CARD_CLOSING",
                        title = "Fechamento: ${card.name}",
                        details = "Fatura virtual fecha hoje. Próximas compras entram na seguinte.",
                        amount = null,
                        colorHex = card.colorHex
                    )
                )
            }
            if (card.dueDay == day) {
                events.add(
                    CalendarEvent(
                        type = "CARD_DUE",
                        title = "🚨 Vencimento: ${card.name}",
                        details = "Prazo limite para prestar contas do cartão de terceiros.",
                        amount = null,
                        colorHex = "#F44336"
                    )
                )
            }
        }

        // 2. Transactions matched to this calendar day and month
        transactions.forEach { t ->
            val tCal = Calendar.getInstance().apply { timeInMillis = t.timestamp }
            if (tCal.get(Calendar.DAY_OF_MONTH) == day &&
                tCal.get(Calendar.MONTH) == month &&
                tCal.get(Calendar.YEAR) == year &&
                t.status != "CANCELLED"
            ) {
                val isIncome = t.type == "PIX_RECEIVED" || t.type == "CASH_INCOME" || t.type == "TRANSFER_RECEIVED"
                events.add(
                    CalendarEvent(
                        type = "TRANSACTION_ITEM",
                        title = t.title,
                        details = "${t.category} (${if (isIncome) "Entrada" else "Saída"})",
                        amount = t.amount,
                        colorHex = if (isIncome) "#00E676" else "#90A4AE"
                    )
                )
            }
        }

        return events
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. HEADER
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "Calendário Financeiro",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Compromissos & Datas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 2. CONTROLE DO MÊS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonthOffset-- }) {
                    Text("◀", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "$displayMonthName $year".uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { currentMonthOffset++ }) {
                    Text("▶", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // 3. GRADE DO CALENDÁRIO (DIAS)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Monday to Sunday indicators
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { dayLable ->
                            Text(
                                text = dayLable,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simplified Grid representing monthly days
                    val rowsCount = (daysInMonth + 6) / 7
                    for (row in 0 until rowsCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val currentDayIdx = row * 7 + col + 1
                                if (currentDayIdx <= daysInMonth) {
                                    val dayEvents = getEventsForDay(currentDayIdx)
                                    val isSelected = selectedDay == currentDayIdx
                                    val hasEvents = dayEvents.isNotEmpty()

                                    // Check if card limits close/due on this day
                                    val hasDue = dayEvents.any { it.type == "CARD_DUE" }
                                    val hasClosing = dayEvents.any { it.type == "CARD_CLOSING" }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    hasDue -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                                    hasClosing -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDay = currentDayIdx }
                                            .border(
                                                width = if (hasEvents && !isSelected) 1.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = currentDayIdx.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    hasDue -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Little indicator dots for multiple events
                                            if (hasEvents && !isSelected) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    dayEvents.take(3).forEach { ev ->
                                                        val dotColor = try {
                                                            Color(android.graphics.Color.parseColor(ev.colorHex))
                                                        } catch (e: Exception) {
                                                            MaterialTheme.colorScheme.primary
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(dotColor)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. LISTA DE EVENTOS DO DIA DE CALENDÁRIO SELECIONADO
        item {
            Text(
                text = "Compromissos para o dia $selectedDay de $displayMonthName",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val activeDayEvents = getEventsForDay(selectedDay)

        if (activeDayEvents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Nenhum fechamento, vencimento ou transação registrada para esta data.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeDayEvents) { event ->
                val primaryColor = try {
                    Color(android.graphics.Color.parseColor(event.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(primaryColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.type == "TRANSACTION_ITEM") Icons.Default.CalendarMonth else Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = event.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = event.details,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (event.amount != null) {
                            Text(
                                text = currencyFormatter.format(event.amount),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
