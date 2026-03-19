package com.stephanmeijer.fileconverter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue, onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer, onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryAmber, onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer, onSecondaryContainer = OnSecondaryContainer,
    error = ErrorRed, errorContainer = ErrorContainer,
    background = BackgroundLight, surface = SurfaceLight, surfaceVariant = SurfaceVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryContainer, onPrimary = OnPrimaryContainer,
    primaryContainer = PrimaryBlueDark, onPrimaryContainer = PrimaryContainer,
    secondary = SecondaryContainer, onSecondary = OnSecondaryContainer,
    secondaryContainer = SecondaryAmberDark, onSecondaryContainer = SecondaryContainer,
    error = ErrorContainer, errorContainer = ErrorRed,
    background = BackgroundDark, surface = SurfaceDark, surfaceVariant = SurfaceVariantDark,
)

@Composable
fun FileConverterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
