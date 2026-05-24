package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.CardelyViewModel
import com.example.ui.CardelyViewModelFactory
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TimelineScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Banco de dados e Repositório
        ServiceLocator.init(applicationContext)

        val factory = CardelyViewModelFactory(ServiceLocator.repository!!)
        val viewModel = ViewModelProvider(this, factory)[CardelyViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefs = remember { context.getSharedPreferences("cardely_security_prefs", android.content.Context.MODE_PRIVATE) }
                var isProtected by remember { mutableStateOf(prefs.getBoolean("is_protected", false)) }
                
                // Re-read when app comes to foreground or when status updates
                val checkProtection = {
                    isProtected = prefs.getBoolean("is_protected", false)
                }
                
                var isUnlocked by remember(isProtected) { mutableStateOf(!isProtected) }

                if (!isUnlocked) {
                    LoginScreen(
                        onUnlockSuccess = { isUnlocked = true }
                    )
                } else {
                    CardelyApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CardelyApp(viewModel: CardelyViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val cards by viewModel.cards.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard Screen") },
                    label = { Text("Resumo", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentRoute == "cards",
                    onClick = {
                        navController.navigate("cards") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Cards Screen") },
                    label = { Text("Faturas", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_cards")
                )
                NavigationBarItem(
                    selected = currentRoute == "timeline",
                    onClick = {
                        navController.navigate("timeline") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Timeline Screen") },
                    label = { Text("Timeline", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_timeline")
                )
                NavigationBarItem(
                    selected = currentRoute == "calendar",
                    onClick = {
                        navController.navigate("calendar") {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar Screen") },
                    label = { Text("Calendário", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_calendar")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nova Transação", modifier = Modifier.size(24.dp)) },
                text = { Text("Lançar", fontWeight = FontWeight.Black) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCards = { navController.navigate("cards") },
                    onNavigateToTimeline = { navController.navigate("timeline") }
                )
            }
            composable("cards") {
                CardsScreen(viewModel = viewModel)
            }
            composable("timeline") {
                TimelineScreen(viewModel = viewModel)
            }
            composable("calendar") {
                CalendarScreen(viewModel = viewModel)
            }
        }

        if (showAddDialog) {
            AddTransactionDialog(
                cards = cards,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, amount, totalAmount, type, cardId, category, isParcelado, totalInstallments, notes, locationName ->
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        totalAmount = totalAmount,
                        type = type,
                        cardId = cardId,
                        category = category,
                        isParcelado = isParcelado,
                        totalInstallments = totalInstallments,
                        notes = notes,
                        locationName = locationName
                    )
                }
            )
        }
    }
}
