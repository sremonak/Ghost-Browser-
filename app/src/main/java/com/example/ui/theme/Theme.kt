package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ChromeDarkPrimary,
    onPrimary = ChromeDarkOnPrimary,
    primaryContainer = ChromeDarkPrimaryContainer,
    onPrimaryContainer = ChromeDarkOnPrimaryContainer,
    background = ChromeDarkBackground,
    surface = ChromeDarkSurface,
    surfaceVariant = ChromeDarkSurfaceVariant,
    onBackground = ChromeDarkOnSurface,
    onSurface = ChromeDarkOnSurface,
    onSurfaceVariant = ChromeDarkOnSurfaceVariant,
    outline = ChromeDarkBorder,
    tertiary = PurpleGhostGlow
)

private val LightColorScheme = lightColorScheme(
    primary = ChromeLightPrimary,
    onPrimary = ChromeLightOnPrimary,
    primaryContainer = ChromeLightPrimaryContainer,
    onPrimaryContainer = ChromeLightOnPrimaryContainer,
    background = ChromeLightBackground,
    surface = ChromeLightSurface,
    surfaceVariant = ChromeLightSurfaceVariant,
    onBackground = ChromeLightOnSurface,
    onSurface = ChromeLightOnSurface,
    onSurfaceVariant = ChromeLightOnSurfaceVariant,
    outline = ChromeLightBorder,
    tertiary = PurpleGhostGlow
)

private val IncognitoColorScheme = darkColorScheme(
    primary = IncognitoAccent,
    onPrimary = IncognitoBackground,
    primaryContainer = IncognitoBar,
    onPrimaryContainer = ChromeDarkOnPrimaryContainer,
    background = IncognitoBackground,
    surface = IncognitoSurface,
    surfaceVariant = ChromeDarkSurfaceVariant,
    onBackground = ChromeDarkOnSurface,
    onSurface = ChromeDarkOnSurface,
    onSurfaceVariant = ChromeDarkOnSurfaceVariant,
    outline = ChromeDarkBorder,
    tertiary = PurpleGhostGlow
)

private val HackerColorScheme = darkColorScheme(
    primary = HackerGreen,
    onPrimary = HackerOnGreen,
    primaryContainer = HackerGreenContainer,
    onPrimaryContainer = HackerGreen,
    background = HackerDarkBg,
    surface = HackerSurface,
    surfaceVariant = HackerSurfaceVariant,
    onBackground = HackerGreen,
    onSurface = HackerGreen,
    onSurfaceVariant = HackerGreen.copy(alpha = 0.8f),
    outline = HackerBorder,
    tertiary = ShieldActiveGreen
)

@Composable
fun GhostBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isIncognito: Boolean = false,
    isHackerTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isHackerTheme -> HackerColorScheme
        isIncognito -> IncognitoColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
