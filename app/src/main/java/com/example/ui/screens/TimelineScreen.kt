package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.ui.CardelyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: CardelyViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val cards by viewModel.cards.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") }
    var selectedReceiptTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val brLocale = Locale("pt", "BR")
    val currencyFormatter = NumberFormat.getCurrencyInstance(brLocale)
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", brLocale)

    val categories = listOf("TODAS") + listOf("Combustível", "Aluguel do veículo", "Manutenção", "Lavagem", "Alimentação", "Pedágios", "Uber", "99", "iFood", "Lazer", "Outros")

    // Filtered transaction list
    val filteredTransactions = transactions.filter { t ->
        val matchesSearch = t.title.contains(searchQuery, ignoreCase = true) || t.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "TODAS" || t.category.equals(selectedCategoryFilter, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("timeline_screen"),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(bottom = 8.dp)) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Timeline Financeira",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Barra de busca
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquise por estabelecimento, produto...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("timeline_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Filtro horizontal por pilulas de categorias
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedCategoryFilter = cat }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "Nenhuma transação localizada.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tente limpar os filtros ou faça um novo lançamento.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { t ->
                    val linkedCard = cards.find { it.id == t.cardId }
                    val typeSign = when (t.type) {
                        "PIX_RECEIVED", "CASH_INCOME", "TRANSFER_RECEIVED" -> "+"
                        else -> "-"
                    }
                    val typeColor = when (t.type) {
                        "PIX_RECEIVED", "CASH_INCOME", "TRANSFER_RECEIVED" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReceiptTransaction = t },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ícone por categoria
                                val categoryIcon = when (t.category) {
                                    "Combustível" -> "⛽"
                                    "Aluguel do veículo" -> "🚗"
                                    "Manutenção" -> "🔧"
                                    "Lavagem" -> "🚿"
                                    "Alimentação" -> "🍔"
                                    "Pedágios", "Pedágio" -> "🛣️"
                                    "Uber" -> "📱"
                                    "99" -> "🚕"
                                    "iFood" -> "🛵"
                                    else -> "🛒"
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(categoryIcon, fontSize = 20.sp)
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = t.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            textDecoration = if (t.status == "CANCELLED") TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        if (t.isParcelado) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    "${t.installmentNumber}/${t.totalInstallments}x",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${t.category} • ${dateFormatter.format(Date(t.timestamp))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (t.type == "CREDIT" && linkedCard != null) {
                                        Text(
                                            text = "💳 ${linkedCard.name}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Valor e Status
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$typeSign ${currencyFormatter.format(t.amount)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (t.status == "CANCELLED") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else typeColor,
                                    fontFamily = FontFamily.Monospace,
                                    textDecoration = if (t.status == "CANCELLED") TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val statusBadge = when (t.status) {
                                    "CANCELLED" -> "❌ Cancelado"
                                    "FUTURE" -> "⏳ Parcela"
                                    else -> "✅ Aprovada"
                                }
                                val statusColor = when (t.status) {
                                    "CANCELLED" -> MaterialTheme.colorScheme.error
                                    "FUTURE" -> Color(0xFF90A4AE)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Text(statusBadge, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG COMPROVANTE TÉRMICO DE COMPRA
    if (selectedReceiptTransaction != null) {
        val trans = selectedReceiptTransaction!!
        val linkedCard = cards.find { it.id == trans.cardId }

        ThermalPOSReceiptDialog(
            transaction = trans,
            card = linkedCard,
            currencyFormatter = currencyFormatter,
            dateTimeFormatter = dateFormatter,
            onDismiss = { selectedReceiptTransaction = null },
            onCancelTransaction = {
                viewModel.cancelTransaction(trans)
                selectedReceiptTransaction = null
            }
        )
    }
}

// COMPONENTE VISUAL COMPROVANTE TÉRMICO (INSPIRADO EM MAQUININHAS / POS)
@Composable
fun ThermalPOSReceiptDialog(
    transaction: TransactionEntity,
    card: CardEntity?,
    currencyFormatter: NumberFormat,
    dateTimeFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onCancelTransaction: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("thermal_receipt_dialog"),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Bobina do Recibo (Papel Térmico)
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .background(Color(0xFFFAFAEE), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E2C9), shape = RoundedCornerShape(8.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Serrilhamento Superior Mock
                        SerratedPaperEdge(modifier = Modifier.fillMaxWidth().height(6.dp))

                        Text(
                            text = "CARDELY POS TERMINAL",
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF333333),
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "RECIBO DA TRANSAÇÃO",
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            color = Color(0xFF111111)
                        )

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )

                        // Beneficiário e data
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VIA CLIENTE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text(dateTimeFormatter.format(Date(transaction.timestamp)), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )

                        // ESTABELECIMENTO
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = transaction.title.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "CATEGORIA: ${transaction.category.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF555555)
                            )
                        }

                        // STATUS BADGE
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (transaction.status) {
                                        "CANCELLED" -> Color(0xFFE53935).copy(alpha = 0.15f)
                                        "FUTURE" -> Color(0xFFB0BEC5)
                                        else -> Color(0xFF43A047).copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (transaction.status) {
                                    "CANCELLED" -> "CANCELADA"
                                    "FUTURE" -> "FUTURA / PARCELADA"
                                    else -> "APROVADA"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = when (transaction.status) {
                                    "CANCELLED" -> Color(0xFFD32F2F)
                                    "FUTURE" -> Color(0xFF37474F)
                                    else -> Color(0xFF2E7D32)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // VALOR PRINCIPAL DO RECIBO
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL COMPRA:" ,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF555555)
                            )
                            Text(
                                text = currencyFormatter.format(transaction.amount),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            )
                            if (transaction.isParcelado) {
                                Text(
                                    text = "PARCELA ${transaction.installmentNumber} DE ${transaction.totalInstallments}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF444444)
                                )
                                Text(
                                    text = "VALOR DA COMPRA: ${currencyFormatter.format(transaction.totalAmount)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )

                        // DETALHES DO MEIO DE PAGAMENTO
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ReceiptLine(label = "TIPO:", value = when(transaction.type) {
                                "CREDIT" -> "CRÉDITO"
                                "DEBIT" -> "DÉBITO"
                                "PIX_SENT", "PIX_RECEIVED" -> "TRANSF. PIX"
                                "CASH_EXPENSE", "CASH_INCOME" -> "EFE. DINHEIRO"
                                else -> "TRANSFERÊNCIA"
                            })

                            if (transaction.type == "CREDIT" && card != null) {
                                ReceiptLine(label = "BANCO/CONTA:", value = card.name.uppercase())
                                ReceiptLine(label = "BANDEIRA:", value = card.brand.uppercase())
                                ReceiptLine(label = "FECHAMENTO:", value = "DIA ${card.closingDay}")
                            }

                            if (transaction.locationName != null) {
                                ReceiptLine(label = "ESTADO/LOC:", value = transaction.locationName.uppercase())
                            }

                            if (transaction.notes != null) {
                                ReceiptLine(label = "OBSERVAÇÕES:", value = transaction.notes.uppercase())
                            }
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF999999),
                            fontSize = 13.sp
                        )

                        // MAPA GEOGRÁFICO MOCKUP (SE POSSUIR LOCALIZAÇÃO MOCK)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFE4E8D1), RoundedCornerShape(6.dp)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🌍 COORDENADAS GPS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4C573C)
                            )
                            Text(
                                "LAT: -23.5505 | LNG: -46.6333",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF5E6D4E)
                            )
                        }

                        // BARCODE MOCKUP COM CANVAS
                        Spacer(modifier = Modifier.height(6.dp))
                        Canvas(
                            modifier = Modifier
                                .width(220.dp)
                                .height(32.dp)
                        ) {
                            var x = 0f
                            val strokeWidths = listOf(2f, 4f, 1f, 3f, 5f, 1f, 2f, 4f, 1f, 5f, 3f, 1f, 4f, 2f, 5f, 1f, 3f, 2f, 4f, 1f, 6f, 2f)
                            for (w in strokeWidths) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = w * 2.5f
                                )
                                x += w * 5f
                            }
                        }

                        Text(
                            text = "CARDELY-75892-04825-91285",
                            color = Color(0xFF555555),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Serrilhamento Inferior Mock
                        Spacer(modifier = Modifier.height(4.dp))
                        SerratedPaperEdge(modifier = Modifier.fillMaxWidth().height(6.dp))
                    }
                }

                // Botões de Ação para o Recibo
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.width(320.dp)
                ) {
                    // Compartilhar Real usando Intent
                    Button(
                        onClick = {
                            val shareBody = """
                                *Cardely Comprovante Financeiro Virtual*
                                
                                Estabelecimento: ${transaction.title}
                                Categoria: ${transaction.category}
                                Valor: ${currencyFormatter.format(transaction.amount)}
                                Data: ${dateTimeFormatter.format(Date(transaction.timestamp))}
                                Tipo: ${transaction.type}
                                ${if (card != null) "Cartão: ${card.name} (${card.brand})" else ""}
                                Status: ${transaction.status}
                                
                                Gerado em Cardely — Controle Paralelo Sem Banco.
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Enviar Comprovante Cardely")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartilhar", fontSize = 12.sp)
                    }

                    // Cancelar Lançamento (se não cancelado já)
                    if (transaction.status != "CANCELLED") {
                        Button(
                            onClick = onCancelTransaction,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancelar Compra", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.width(320.dp)
                ) {
                    Text("Remover Tela")
                }
            }
        }
    }
}

@Composable
fun ReceiptLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 160.dp)
        )
    }
}

@Composable
fun SerratedPaperEdge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        drawLine(
            color = Color(0xFFCCCCCC),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}
