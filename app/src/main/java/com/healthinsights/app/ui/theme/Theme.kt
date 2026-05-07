package com.healthinsights.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light scheme — Visual System v1 ──────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = Ink1,        // button background, active states
    onPrimary           = Color.White,
    primaryContainer    = BrandSage,
    onPrimaryContainer  = Color.White,
    secondary           = BrandSage,   // brand accent, deficit signal
    onSecondary         = Color.White,
    secondaryContainer  = BgSunken,
    onSecondaryContainer = Ink1,
    tertiary            = Ink3,        // captions, tertiary labels
    onTertiary          = Color.White,
    background          = BgBase,      // #FAFAF7
    onBackground        = Ink1,
    surface             = BgElevated,  // card surface
    onSurface           = Ink1,
    surfaceVariant      = BgSunken,    // chart tracks, chips
    onSurfaceVariant    = Ink2,        // secondary text
    outline             = Hairline,
    outlineVariant      = BgSunken,
)

// ── Dark scheme — inactive in MVP (visual-system-v1.md § 9) ──────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = BrandSage,
    onPrimary        = Color.White,
    background       = DarkSurface,
    onBackground     = DarkOnSurface,
    surface          = DarkSurface,
    onSurface        = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVar,
    onSurfaceVariant = DarkOnSurface,
)

@Composable
fun HealthInsightsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
