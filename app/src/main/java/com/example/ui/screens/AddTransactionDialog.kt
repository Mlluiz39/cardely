package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.CardEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    cards: List<CardEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        totalAmount: Double,
        type: String,
        cardId: Int?,
        category: String,
        isParcelado: Boolean,
        totalInstallments: Int,
        notes: String?,
        locationName: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("DEBIT") } // CREDIT, DEBIT, PIX_SENT, PIX_RECEIVED, CASH_EXPENSE, CASH_INCOME
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var selectedCategory by remember { mutableStateOf("Alimentação") }
    var isParcelado by remember { mutableStateOf(false) }
    var installmentsCountStr by remember { mutableStateOf("2") }
    var notes by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }

    // Automatic Card Selection
    LaunchedEffect(selectedType, cards) {
        if (selectedType == "CREDIT" && selectedCardId == null && cards.isNotEmpty()) {
            selectedCardId = cards.first().id
        }
    }

    // Categories list based on dynamic types (including specialized driver categories)
    val appDriverCategories = listOf("Combustível", "Aluguel do veículo", "Manutenção", "Lavagem", "Alimentação", "Pedágios", "Uber", "99", "iFood", "Lazer", "Outros")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
                .testTag("add_transaction_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Registrar Transação", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_dialog_button")) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Valor em destaque
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Valor (R$)") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_input"),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Estabelecimento / Descrição
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Estabelecimento / Descrição") },
                        placeholder = { Text("Ex: Posto BR, Supermercado, Corrida...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_title_input"),
                        singleLine = true
                    )

                    // Tipo de Transação
                    Text("Tipo de Transação", fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val types = listOf(
                            "DEBIT" to "🏦 Débito (Conta)",
                            "CREDIT" to "💳 Crédito (Cartão)",
                            "PIX_SENT" to "📱 Pix Enviado",
                            "PIX_RECEIVED" to "🟢 Pix Recebido",
                            "CASH_EXPENSE" to "💵 Dinheiro (Gasto)",
                            "CASH_INCOME" to "💰 Dinheiro (Ganho)"
                        )

                        // 3 columns grid/wrap
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                types.take(3).forEach { (typeVal, labelText) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedType = typeVal }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = (selectedType == typeVal),
                                            onClick = { selectedType = typeVal }
                                        )
                                        Text(labelText, fontSize = 13.sp)
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                types.drop(3).forEach { (typeVal, labelText) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedType = typeVal }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = (selectedType == typeVal),
                                            onClick = { selectedType = typeVal }
                                        )
                                        Text(labelText, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Se for crédito, exibir seletor de cartões e parcelas
                    if (selectedType == "CREDIT") {
                        if (cards.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ Cadastre um cartão de crédito primeiro na aba de Cartões para usar o modo Crédito.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            Text("Selecione o Cartão Utilizado", fontWeight = FontWeight.SemiBold)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                cards.forEach { card ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedCardId = card.id }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = (selectedCardId == card.id),
                                            onClick = { selectedCardId = card.id }
                                        )
                                        Text("${card.brand.uppercase()} - ${card.name} (Lim: R$ ${card.totalLimit})", fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Parcelamento
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isParcelado,
                                    onCheckedChange = { isParcelado = it },
                                    modifier = Modifier.testTag("parcelado_checkbox")
                                )
                                Text("Compra Parcelada", fontWeight = FontWeight.Medium)
                            }

                            if (isParcelado) {
                                OutlinedTextField(
                                    value = installmentsCountStr,
                                    onValueChange = { installmentsCountStr = it },
                                    label = { Text("Número de Parcelas") },
                                    placeholder = { Text("Ex: 12") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("installments_count_input"),
                                    singleLine = true
                                )
                                val calculatedValue = (amountStr.toDoubleOrNull() ?: 0.0)
                                val count = installmentsCountStr.toIntOrNull() ?: 1
                                if (calculatedValue > 0 && count > 1) {
                                    val valPerInst = calculatedValue / count
                                    Text(
                                        "Serão geradas $count parcelas de R$ ${String.format("%.2f", valPerInst)} nos próximos meses.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Seletor de Categoria
                    Text("Categoria", fontWeight = FontWeight.SemiBold)
                    var expandedCat by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedCat = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_dropdown_button")
                        ) {
                            Text(selectedCategory)
                        }
                        DropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            appDriverCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }

                    // Localização (Opcional)
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Localização / Cidade (Opcional)") },
                        placeholder = { Text("Ex: Shopping Paulista, São Paulo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Observações (Opcional)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observação / Recado (Opcional)") },
                        placeholder = { Text("Escreva uma nota para se lembrar") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão confirmar
                    Button(
                        onClick = {
                            val originalAmt = amountStr.toDoubleOrNull() ?: 0.0
                            if (originalAmt > 0 && title.isNotBlank()) {
                                if (selectedType == "CREDIT" && isParcelado) {
                                    val parts = installmentsCountStr.toIntOrNull() ?: 2
                                    // No parcelamento, o valor principal vira o valor da parcela individual para inserção mensal,
                                    // e enviamos o originalAmt como totalAmount consolidado.
                                    val installmentValue = originalAmt / parts
                                    onConfirm(
                                        title,
                                        installmentValue,
                                        originalAmt,
                                        selectedType,
                                        selectedCardId,
                                        selectedCategory,
                                        true,
                                        parts,
                                        notes,
                                        locationName
                                    )
                                } else {
                                    onConfirm(
                                        title,
                                        originalAmt,
                                        originalAmt,
                                        selectedType,
                                        selectedCardId ?: if (selectedType == "CREDIT" && cards.isNotEmpty()) cards.first().id else null,
                                        selectedCategory,
                                        false,
                                        1,
                                        notes,
                                        locationName
                                    )
                                }
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_transaction_button"),
                        enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0 && (selectedType != "CREDIT" || cards.isNotEmpty())
                    ) {
                        Text("Registrar Lançamento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
