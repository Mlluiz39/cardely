package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CardelyViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: CardelyViewModel,
    onNavigateToCards: () -> Unit,
    onNavigateToTimeline: () -> Unit
) {
    val balance by viewModel.accountBalance.collectAsState()
    val cardsWithSummary by viewModel.cardsWithSummary.collectAsState()
    val driverMode by viewModel.driverModeEnabled.collectAsState()
    val driverMetrics by viewModel.driverMetrics.collectAsState()
    val spendingByCategory by viewModel.expenseSpendingByCategory.collectAsState()

    val brLocale = Locale("pt", "BR")
    val currencyFormatter = NumberFormat.getCurrencyInstance(brLocale)

    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Cabeçalho de Perfil com Saudação
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Olá, Motorista Parceiro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Cardely",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Modo Motorista Switch e Configurações
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { viewModel.toggleDriverMode() }
                    ) {
                        Icon(
                            imageVector = if (driverMode) Icons.Default.DirectionsCar else Icons.Default.Person,
                            contentDescription = "Driver Mode Info",
                            tint = if (driverMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (driverMode) "Modo Motorista" else "Modo Pessoal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (driverMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                            .testTag("dashboard_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Card de Saldo em Conta (Simulado)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Saldo Líquido",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Lançado manual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = currencyFormatter.format(balance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Considera Pix, Dinheiro e Débito lançado. Não inclui limite de cartões.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // 3. Carousel de Limites de Cartões de Crédito Virtuais
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cartões Compartilhados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ver todos",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToCards() }
                    )
                }

                if (cardsWithSummary.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "Nenhum cartão simulador cadastrado.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = onNavigateToCards,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Criar Cartão", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(cardsWithSummary) { summary ->
                            val parsedColor = try {
                                Color(android.graphics.Color.parseColor(summary.card.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.secondary
                            }

                            Card(
                                modifier = Modifier
                                    .width(260.dp)
                                    .clickable { onNavigateToCards() },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = summary.card.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = parsedColor
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(parsedColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = summary.card.brand.uppercase(),
                                                color = parsedColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("LIVRE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                currencyFormatter.format(summary.availableLimit),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("UTILIZADO", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                currencyFormatter.format(summary.usedLimit),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    // Barra de progresso do limite
                                    val progress = if (summary.totalLimit > 0) {
                                        (summary.usedLimit / summary.totalLimit).toFloat().coerceIn(0f, 1f)
                                    } else 0f

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        LinearProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = parsedColor,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Limite: ${currencyFormatter.format(summary.totalLimit)}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${(progress * 100).toInt()}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = parsedColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Seção Dinâmica - Modo Motorista de App (Indicadores de Lucro)
        item {
            AnimatedVisibility(
                visible = driverMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Desempenho Motorista (Uber / 99 / iFood)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Diário
                        DriverStatCard(
                            period = "HOJE",
                            profit = driverMetrics.dailyProfit,
                            gains = driverMetrics.dailyGains,
                            expenses = driverMetrics.dailyExpenses,
                            formatter = currencyFormatter,
                            modifier = Modifier.weight(1f)
                        )
                        // Semanal
                        DriverStatCard(
                            period = "7 DIAS",
                            profit = driverMetrics.weeklyProfit,
                            gains = driverMetrics.weeklyGains,
                            expenses = driverMetrics.weeklyExpenses,
                            formatter = currencyFormatter,
                            modifier = Modifier.weight(1f)
                        )
                        // Mensal
                        DriverStatCard(
                            period = "30 DIAS",
                            profit = driverMetrics.monthlyProfit,
                            gains = driverMetrics.monthlyGains,
                            expenses = driverMetrics.monthlyExpenses,
                            formatter = currencyFormatter,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Inteligência Financeira (Alertas e Insights Automatizados)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Insight Logo",
                            tint = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "Inteligência & Alertas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Logica de Alertas Reais baseados no estado
                    val alerts = mutableListOf<String>()

                    val limitAlerts = cardsWithSummary.filter { it.totalLimit > 0 && (it.usedLimit / it.totalLimit) > 0.85 }
                    limitAlerts.forEach { summary ->
                        alerts.add("⚠️ Cartão '${summary.card.name}' está com o limite quase esgotado (${(summary.usedLimit/summary.totalLimit * 100).toInt()}%). Evite novas compras parceladas nele.")
                    }

                    if (balance < 0) {
                        alerts.add("🚨 Alerta: Seu saldo restante em conta está negativo (${currencyFormatter.format(balance)}). Faça lançamentos de ganhos ou reduza compras em dinheiro e débito.")
                    }

                    if (spendingByCategory.isNotEmpty()) {
                        val topSpender = spendingByCategory.first()
                        alerts.add("💡 Seu maior acumulado de gastos é em '${topSpender.category}', somando ${currencyFormatter.format(topSpender.amount)}. Tente otimizar essa despesa.")
                    }

                    if (cardsWithSummary.isNotEmpty()) {
                        val totalCardsUsed = cardsWithSummary.sumOf { it.usedLimit }
                        if (totalCardsUsed > 0) {
                            alerts.add("📊 Simulador: Você possui ${currencyFormatter.format(totalCardsUsed)} ocupados nos cartões de terceiros. Seu controle paralelo está ativo e livre de juros.")
                        }
                    }

                    if (alerts.isEmpty()) {
                        Text(
                            text = "Nenhum ponto crítico detectado! Continue registrando suas corridas, abastecimentos e parcelas para manter o histórico blindado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            alerts.take(3).forEach { alert ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("•", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = alert,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Gráfico de Despesas por Categoria (Progress de Comparação Asimétrica)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Gastos por Categoria (Mês)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (spendingByCategory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sem lançamentos de gastos cadastrados este mês.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        val maxSpending = spendingByCategory.maxOf { it.amount }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            spendingByCategory.take(5).forEach { spec ->
                                val pct = if (maxSpending > 0) (spec.amount / maxSpending).toFloat() else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = spec.category,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = currencyFormatter.format(spec.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = pct,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverStatCard(
    period: String,
    profit: Double,
    gains: Double,
    expenses: Double,
    formatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = period,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatter.format(profit),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (profit >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("GANHOS", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(
                        text = formatter.format(gains),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("CUSTOS", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Text(
                        text = formatter.format(expenses),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
