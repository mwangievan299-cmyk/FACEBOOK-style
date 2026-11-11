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
    primary = FacebookBlue,
    onPrimary = Color.White,
    primaryContainer = FacebookSurfaceDarkElevated,
    onPrimaryContainer = FacebookTextPrimaryDark,
    secondary = FacebookBlueLight,
    onSecondary = FacebookTextPrimaryDark,
    background = FacebookBgDark,
    onBackground = FacebookTextPrimaryDark,
    surface = FacebookSurfaceDark,
    onSurface = FacebookTextPrimaryDark,
    surfaceVariant = FacebookSurfaceDarkElevated,
    onSurfaceVariant = FacebookTextSecondaryDark,
    outline = FacebookDividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = FacebookBlue,
    onPrimary = Color.White,
    primaryContainer = FacebookBlueLight,
    onPrimaryContainer = FacebookDarkBlue,
    secondary = FacebookBlueLight,
    onSecondary = FacebookDarkBlue,
    background = FacebookBgLight,
    onBackground = FacebookTextPrimaryLight,
    surface = FacebookSurfaceLight,
    onSurface = FacebookTextPrimaryLight,
    surfaceVariant = FacebookInputBgLight,
    onSurfaceVariant = FacebookTextSecondaryLight,
    outline = FacebookDividerLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use authentic Facebook brand styling by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
