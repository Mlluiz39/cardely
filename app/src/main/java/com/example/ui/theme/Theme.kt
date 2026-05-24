package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreenAccent,
    secondary = ElectricBlue,
    tertiary = Pink80,
    background = SlateDarkBackground,
    surface = SlateCardSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = SlateInnerBorder,
    onSurfaceVariant = Color(0xFFB0BEC5),
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E88E5), // Azul Premium
    secondary = Color(0xFF43A047), // Verde
    tertiary = Color(0xFFD81B60),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1D21),
    onSurface = Color(0xFF2C2D35),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF5C6D7F),
    error = Color(0xFFD32F2F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Forçar a identidade premium escura por padrão!
    dynamicColor: Boolean = false, // Desativar cor dinâmica para manter a identidade impecável da marca
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
