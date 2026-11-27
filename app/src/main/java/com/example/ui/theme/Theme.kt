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
    secondary = NovaDarkSecondary,
    onSecondary = NovaDarkOnSecondary,
    secondaryContainer = NovaDarkSecondaryContainer,
    onSecondaryContainer = NovaDarkOnSecondaryContainer,
    tertiary = NovaDarkTertiary,
    onTertiary = NovaDarkOnTertiary,
    tertiaryContainer = NovaDarkTertiaryContainer,
    onTertiaryContainer = NovaDarkOnTertiaryContainer,
    background = NovaDarkBackground,
    onBackground = NovaDarkOnSurface,
    surface = NovaDarkSurface,
    onSurface = NovaDarkOnSurface,
    surfaceVariant = NovaDarkSurfaceVariant,
    onSurfaceVariant = NovaDarkOnSurfaceVariant,
    outline = NovaDarkOutline,
    outlineVariant = NovaDarkOutlineVariant,
    error = NovaDarkError,
    onError = NovaDarkOnError,
    errorContainer = NovaDarkErrorContainer,
    onErrorContainer = NovaDarkOnErrorContainer,
    surfaceTint = NovaDarkSurfaceTint
)

private val NovaLightColorScheme = lightColorScheme(
    primary = NovaLightPrimary,
    onPrimary = NovaLightOnPrimary,
    primaryContainer = NovaLightPrimaryContainer,
    onPrimaryContainer = NovaLightOnPrimaryContainer,
    secondary = NovaLightSecondary,
    onSecondary = NovaLightOnSecondary,
    secondaryContainer = NovaLightSecondaryContainer,
    onSecondaryContainer = NovaLightOnSecondaryContainer,
    tertiary = NovaLightTertiary,
    onTertiary = NovaLightOnTertiary,
    tertiaryContainer = NovaLightTertiaryContainer,
    onTertiaryContainer = NovaLightOnTertiaryContainer,
    background = NovaLightBackground,
    onBackground = NovaLightOnSurface,
    surface = NovaLightSurface,
    onSurface = NovaLightOnSurface,
    surfaceVariant = NovaLightSurfaceVariant,
    onSurfaceVariant = NovaLightOnSurfaceVariant,
    outline = NovaLightOutline,
    outlineVariant = NovaLightOutlineVariant,
    error = NovaLightError,
    onError = NovaLightOnError,
    errorContainer = NovaLightErrorContainer,
    onErrorContainer = NovaLightOnErrorContainer,
    surfaceTint = NovaLightSurfaceTint
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
