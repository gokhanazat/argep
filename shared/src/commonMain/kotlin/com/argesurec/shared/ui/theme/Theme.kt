package com.argesurec.shared.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = ArgepColors.Navy700,
    onPrimary = ArgepColors.White,
    primaryContainer = ArgepColors.Navy100,
    onPrimaryContainer = ArgepColors.Navy900,
    secondary = ArgepColors.Navy500,
    onSecondary = ArgepColors.White,
    surface = ArgepColors.White,
    onSurface = ArgepColors.Navy900,
    background = ArgepColors.Slate100,
    onBackground = ArgepColors.Slate900,
    error = ArgepColors.Error,
    onError = ArgepColors.White,
    outline = ArgepColors.Slate300,
)

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = ArgepColors.Navy400,
    onPrimary = ArgepColors.Navy900,
    primaryContainer = ArgepColors.Navy800,
    onPrimaryContainer = ArgepColors.Navy100,
    secondary = ArgepColors.Navy300,
    onSecondary = ArgepColors.Navy900,
    surface = ArgepColors.Navy900,
    onSurface = ArgepColors.Slate100,
    background = ArgepColors.Navy900,
    onBackground = ArgepColors.Slate100,
    error = ArgepColors.Error,
    onError = ArgepColors.White,
    outline = ArgepColors.Navy600,
)

private val ArgepTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = ArgepColors.Navy900
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun ArgepTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArgepTypography,
        content = content
    )
}
