package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.CardelyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: CardelyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prefs = context.getSharedPreferences("cardely_security_prefs", Context.MODE_PRIVATE)

    var isProtected by remember { mutableStateOf(prefs.getBoolean("is_protected", false)) }
    var saveUsername by remember { mutableStateOf(prefs.getString("username", "admin") ?: "admin") }
    var savePassword by remember { mutableStateOf(prefs.getString("password", "1234") ?: "1234") }

    var importJsonInput by remember { mutableStateOf("") }
    var showSecurityEditor by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("settings_dialog")
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Configurações & Backup",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    // SEÇÃO SEGURANÇA & ACESSO (LOGIN/SENHA)
                    Text(
                        text = "🔒 Segurança & Acesso",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bloqueio por Senha", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "Exigir login e senha para entrar no Cardely",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isProtected,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            showSecurityEditor = true
                                        } else {
                                            prefs.edit().putBoolean("is_protected", false).apply()
                                            isProtected = false
                                            Toast.makeText(context, "Proteção desativada!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("security_protect_switch")
                                )
                            }

                            if (isProtected || showSecurityEditor) {
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Text("Editar Credenciais de Acesso", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                OutlinedTextField(
                                    value = saveUsername,
                                    onValueChange = { saveUsername = it },
                                    label = { Text("Nome de Usuário") },
                                    modifier = Modifier.fillMaxWidth().testTag("settings_username_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                OutlinedTextField(
                                    value = savePassword,
                                    onValueChange = { savePassword = it },
                                    label = { Text("Senha de Acesso") },
                                    modifier = Modifier.fillMaxWidth().testTag("settings_password_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (saveUsername.isNotBlank() && savePassword.isNotBlank()) {
                                            prefs.edit().apply {
                                                putString("username", saveUsername.trim())
                                                putString("password", savePassword.trim())
                                                putBoolean("is_protected", true)
                                                apply()
                                            }
                                            isProtected = true
                                            showSecurityEditor = false
                                            Toast.makeText(context, "Credenciais salvas com sucesso!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Insira credenciais válidas!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("settings_save_credentials_btn")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Salvar & Ativar Segurança")
                                }
                            }
                        }
                    }

                    // SEÇÃO BACKUP DE DADOS (EXPORTAR/IMPORTAR)
                    Text(
                        text = "📥 Backup & Sincronização",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Faça o backup de todos os cartões cadastrados e da timeline financeira para guardar ou migrar para outro aparelho.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // EXPORTAR
                                Button(
                                    onClick = {
                                        val backupStr = viewModel.exportBackup()
                                        if (backupStr.isNotBlank()) {
                                            // Copiar para a área de transferência
                                            clipboardManager.setText(AnnotatedString(backupStr))
                                            Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show()

                                            // Compartilhar Real via Intent
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, backupStr)
                                                type = "application/json"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Exportar Backup Cardely")
                                            context.startActivity(shareIntent)
                                        } else {
                                            Toast.makeText(context, "Sem dados para exportar!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("settings_export_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Exportar JSON", fontSize = 12.sp)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                            Text("Importar / Restaurar Backup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                "Cole a string JSON de backup gerada anteriormente no campo abaixo:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = importJsonInput,
                                onValueChange = { importJsonInput = it },
                                placeholder = { Text("Cole o JSON de backup aqui...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("settings_import_input"),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                maxLines = 6
                            )

                            Button(
                                onClick = {
                                    if (importJsonInput.isNotBlank()) {
                                        viewModel.importBackup(
                                            jsonStr = importJsonInput,
                                            onSuccess = {
                                                Toast.makeText(context, "Backup restaurado com sucesso!", Toast.LENGTH_LONG).show()
                                                importJsonInput = ""
                                            },
                                            onError = { error ->
                                                Toast.makeText(context, "Erro na importação: $error", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "Por favor cole um JSON válido primeiro!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_import_btn")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carregar & Sobrescrever")
                            }
                        }
                    }

                    // LIMPEZA E DADOS OPCIONAIS
                    Text(
                        text = "⚙️ Limpeza & Manutenção",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Deixamos o app de fábrica totalmente limpo. Caso queira redefinir a base de dados ou gerar cartões e lançamentos de teste para demonstração, use os recursos abaixo:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Gerar Dados de Exemplo (Opcional)
                            OutlinedButton(
                                onClick = {
                                    viewModel.seedDemoData()
                                    Toast.makeText(context, "Dados demonstrativos gerados! Verifique a Dashboard.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_seed_demo_btn"),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.BatchPrediction, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Semear Dados de Simulação", color = MaterialTheme.colorScheme.secondary)
                            }

                            // Limpeza Completa
                            Button(
                                onClick = {
                                    viewModel.clearAllUserData()
                                    Toast.makeText(context, "Banco de dados local limpo!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().testTag("settings_clear_db_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Zerar Banco de Dados", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
