package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = Slate950,
    primaryContainer = Cyan900,
    onPrimaryContainer = Cyan300,
    secondary = Indigo400,
    onSecondary = Slate950,
    tertiary = Emerald400,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800,
    error = Red400,
    onError = Slate950
)

private val LightColorScheme = lightColorScheme(
    primary = TharaRed,
    onPrimary = Color.White,
    primaryContainer = TharaRedLight,
    onPrimaryContainer = TharaRedDark,
    secondary = TharaBlueText,
    onSecondary = Color.White,
    tertiary = Emerald600,
    onTertiary = Color.White,
    background = TharaBackground,
    onBackground = LightTextPrimary,
    surface = TharaSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = TharaCardBorder,
    outlineVariant = TharaRedBorder,
    error = TharaRed,
    onError = Color.White
)

@Composable
fun TharaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
