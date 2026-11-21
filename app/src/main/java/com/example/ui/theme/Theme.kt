package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaDarkPrimary,
    onPrimary = NovaDarkOnPrimary,
    primaryContainer = NovaDarkPrimaryContainer,
    onPrimaryContainer = NovaDarkOnPrimaryContainer,
    background = NovaDarkBackground,
    surface = NovaDarkSurface,
    surfaceVariant = NovaDarkSurfaceVariant,
    onBackground = NovaDarkOnSurface,
    onSurface = NovaDarkOnSurface,
    onSurfaceVariant = NovaDarkOnSurfaceVariant,
    outline = NovaDarkBorder,
    outlineVariant = NovaDarkBorder,
    tertiary = NovaIncognitoAccent,
    error = NovaError
)

private val NovaLightColorScheme = lightColorScheme(
    primary = NovaLightPrimary,
    onPrimary = NovaLightOnPrimary,
    primaryContainer = NovaLightPrimaryContainer,
    onPrimaryContainer = NovaLightOnPrimaryContainer,
    background = NovaLightBackground,
    surface = NovaLightSurface,
    surfaceVariant = NovaLightSurfaceVariant,
    onBackground = NovaLightOnSurface,
    onSurface = NovaLightOnSurface,
    onSurfaceVariant = NovaLightOnSurfaceVariant,
    outline = NovaLightBorder,
    outlineVariant = NovaLightBorder,
    tertiary = NovaIncognitoAccent,
    error = NovaError
)

/**
 * Nova Browser theme. [darkTheme] forces the palette when the user picked an
 * explicit light / dark setting; otherwise the system preference is used.
 */
@Composable
fun NovaBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> NovaDarkColorScheme
        else -> NovaLightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovaTypography,
        content = content
    )
}
