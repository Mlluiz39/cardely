package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.ui.CardelyViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(viewModel: CardelyViewModel) {
    val cardsWithSummary by viewModel.cardsWithSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedCard by remember { mutableStateOf<CardEntity?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    // Form states
    var cardName by remember { mutableStateOf("") }
    var selectedCardBrand by remember { mutableStateOf("Visa") }
    var totalLimitStr by remember { mutableStateOf("") }
    var closingDayStr by remember { mutableStateOf("5") }
    var dueDayStr by remember { mutableStateOf("12") }
    var selectedCardColor by remember { mutableStateOf("#1E88E5") } // Default Blue

    // Month Selector for virtual invoice
    var selectedMonthOffset by remember { mutableStateOf(0) } // 0 = Current, 1 = Next month, etc.

    val brLocale = Locale("pt", "BR")
    val currencyFormatter = NumberFormat.getCurrencyInstance(brLocale)

    // Sync selected card when list updates
    LaunchedEffect(cardsWithSummary) {
        if (selectedCard == null && cardsWithSummary.isNotEmpty()) {
            selectedCard = cardsWithSummary.first().card
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("cards_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. HEADER
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Controle de Cartões",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Simuladores & Faturas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { showAddForm = !showAddForm },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .testTag("toggle_add_card_form_button")
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                        contentDescription = "Form",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 2. FORMULARIO SE EXPANDIDO
        item {
            AnimatedVisibility(
                visible = showAddForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_card_form")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Cadastrar Novo Cartão Simulator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Nome do Cartão / Dono") },
                            placeholder = { Text("Ex: Cartão da Mãe, Nubank Compartilhado") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_name_input"),
                            singleLine = true
                        )

                        // Bandeira
                        Text("Bandeira do Cartão", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        val brands = listOf("Visa", "Mastercard", "Elo", "Hipercard", "American Express")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            brands.forEach { brand ->
                                val isSelected = selectedCardBrand == brand
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedCardBrand = brand }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(brand, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        // Limites e Datas
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = totalLimitStr,
                                onValueChange = { totalLimitStr = it },
                                label = { Text("Limite Total R$") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("card_limit_input"),
                                singleLine = true
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = closingDayStr,
                                onValueChange = { closingDayStr = it },
                                label = { Text("Fechamento (Dia)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = dueDayStr,
                                onValueChange = { dueDayStr = it },
                                label = { Text("Vencimento (Dia)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Cores
                        Text("Cor Personalizada", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        val colorsList = listOf(
                            "#2196F3" to "Azul",
                            "#9C27B0" to "Roxo",
                            "#FF9800" to "Laranja",
                            "#4CAF50" to "Verde",
                            "#E91E63" to "Rosa",
                            "#3F51B5" to "Indigo"
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            colorsList.forEach { (hexVal, colorName) ->
                                val isSelected = selectedCardColor == hexVal
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hexVal)))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedCardColor = hexVal },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text("✔", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val limit = totalLimitStr.toDoubleOrNull() ?: 0.0
                                val close = closingDayStr.toIntOrNull() ?: 5
                                val due = dueDayStr.toIntOrNull() ?: 12
                                if (cardName.isNotBlank() && limit > 0) {
                                    viewModel.addCard(cardName, selectedCardBrand, limit, close, due, selectedCardColor)
                                    // Reset
                                    cardName = ""
                                    totalLimitStr = ""
                                    showAddForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_card_button"),
                            enabled = cardName.isNotBlank() && (totalLimitStr.toDoubleOrNull() ?: 0.0) > 0.0
                        ) {
                            Text("Salvar Cartão Simulador", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. CARDS DIGITAIS SELECIONÁVEIS
        if (cardsWithSummary.isNotEmpty()) {
            item {
                Text("Selecione um Cartão para Fatura Virtual", fontWeight = FontWeight.SemiBold)
            }

            items(cardsWithSummary) { summary ->
                val card = summary.card
                val isSelected = selectedCard?.id == card.id
                val cardColor = try {
                    Color(android.graphics.Color.parseColor(card.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.secondary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCard = card }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) cardColor else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        cardColor.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(card.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(cardColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = card.brand.uppercase(),
                                            color = cardColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Text(
                                    text = "Fechamento dia ${card.closingDay} • Vencimento dia ${card.dueDay}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("LIMITE LIVRE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(currencyFormatter.format(summary.availableLimit), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column {
                                        Text("RECONSTRUÍDO", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(currencyFormatter.format(summary.usedLimit), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteCard(card)
                                    if (selectedCard?.id == card.id) {
                                        selectedCard = null
                                    }
                                },
                                modifier = Modifier.testTag("delete_card_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir Cartão", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }

        // 4. DETALHES DA FATURA VIRTUAL DO CARTÃO SELECIONADO
        item {
            if (cardsWithSummary.isNotEmpty() && selectedCard != null) {
                val activeCard = selectedCard!!
                val activeCardColor = try {
                    Color(android.graphics.Color.parseColor(activeCard.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.secondary
                }

                // Filtrar transações para este mês simulado
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, selectedMonthOffset)
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH) // 0 to 11

                val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
                val displayMonthName = monthNames[currentMonth]

                // Buscar transações de CRÉDITO para este cartão, que correspondam ao período do mês selecionado
                val creditTransactions = transactions.filter {
                    it.type == "CREDIT" &&
                    it.cardId == activeCard.id &&
                    it.status != "CANCELLED"
                }

                // No controle de faturas, filtramos as transações cujo mês e ano correspondam ao mês selecionado!
                val invoiceTransactionsForMonth = creditTransactions.filter { t ->
                    val tCal = Calendar.getInstance().apply { timeInMillis = t.timestamp }
                    tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
                }

                val invoiceTotalValue = invoiceTransactionsForMonth.sumOf { it.amount }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("virtual_invoice_container"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Seletor de mês dinâmico
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { selectedMonthOffset-- }) {
                                Text("◀ Mês Ant.", fontSize = 12.sp)
                            }
                            Text(
                                "$displayMonthName / $currentYear",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = activeCardColor
                            )
                            TextButton(onClick = { selectedMonthOffset++ }) {
                                Text("Próx. Mês ▶", fontSize = 12.sp)
                            }
                        }

                        // Detalhes sintéticos da fatura
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fatura Virtual Reconstruída", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Simulado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = activeCardColor)
                                }
                                Text(
                                    text = currencyFormatter.format(invoiceTotalValue),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = activeCardColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Fecha dia: ${activeCard.closingDay} | Vence dia: ${activeCard.dueDay}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Transações: ${invoiceTransactionsForMonth.size}",
                                        fontSize = 11.sp,
                                        color = activeCardColor
                                    )
                                }
                            }
                        }

                        // Lista de compras integrantes da fatura
                        Text("Detalhamento da Fatura", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        if (invoiceTransactionsForMonth.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Nenhuma compra parcelada ou à vista registrada para esta fatura.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                invoiceTransactionsForMonth.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                if (item.isParcelado) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 6.dp)
                                                            .background(activeCardColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            "${item.installmentNumber}/${item.totalInstallments}x",
                                                            fontSize = 9.sp,
                                                            color = activeCardColor,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = item.category,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = currencyFormatter.format(item.amount),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = activeCardColor
                                            )
                                            if (item.isParcelado) {
                                                Text(
                                                    "Total: ${currencyFormatter.format(item.totalAmount)}",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (cardsWithSummary.isNotEmpty() && selectedCard == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Escolha um cartão na lista acima.")
                }
            }
        }
    }
}
