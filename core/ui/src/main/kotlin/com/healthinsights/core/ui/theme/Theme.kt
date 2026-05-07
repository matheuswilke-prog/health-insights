package com.healthinsights.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Health Insights — Visual System v1
// Source of truth: docs/design/visual-system-v1.md
// Mockups: Anthropic Designer project "health-insights" → Health Insights.html

private val InkPrimary    = Color(0xFF0E1116)
private val InkSecondary  = Color(0xFF3A3F47)
private val InkTertiary   = Color(0xFF6B7079)
private val InkDisabled   = Color(0xFF9AA0A8)
private val Bg            = Color(0xFFFAFAF7)
private val BgElevated    = Color(0xFFFFFFFF)
private val BgSunken      = Color(0xFFF2F1EC)
private val Hairline      = Color(0x140E1116)
private val BrandSage     = Color(0xFF6FA47A)

private val HealthInsightsLightColors = lightColorScheme(
    background        = Bg,
    surface           = BgElevated,
    surfaceVariant    = BgSunken,
    onBackground      = InkPrimary,
    onSurface         = InkPrimary,
    onSurfaceVariant  = InkSecondary,
    primary           = InkPrimary,        // CTA primário usa ink-1
    onPrimary         = Color.White,
    secondary         = BrandSage,         // brand para CTAs secundários
    onSecondary       = Color.White,
    outline           = Hairline,
    outlineVariant    = Hairline,
)

object HealthInsightsSemantic {
    val deficit  = Color(0xFF6FA47A)  // verde sage — emagrecendo
    val surplus  = Color(0xFFD68B6A)  // coral — ganhando
    val maintain = Color(0xFF6F8AB5)  // azul muted — mantendo
    val ink3     = InkTertiary
    val ink4     = InkDisabled
    val sunken   = BgSunken
}

// Inter Tight + Inter — adicionar como assets em :core:ui/src/main/res/font
private val InterTight = FontFamily.Default
private val Inter      = FontFamily.Default

private val HealthInsightsTypography = Typography(
    displayLarge   = TextStyle(fontFamily = InterTight, fontSize = 44.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1.5).sp, lineHeight = 48.sp),
    displayMedium  = TextStyle(fontFamily = InterTight, fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.85).sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = InterTight, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, lineHeight = 30.sp),
    titleMedium    = TextStyle(fontFamily = Inter,      fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.16).sp),
    bodyLarge      = TextStyle(fontFamily = Inter,      fontSize = 16.sp, fontWeight = FontWeight.Medium,   lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Inter,      fontSize = 14.sp, fontWeight = FontWeight.Normal,   lineHeight = 20.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.66.sp),
)

private val HealthInsightsShapes = Shapes(
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HealthInsightsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HealthInsightsLightColors,
        typography  = HealthInsightsTypography,
        shapes      = HealthInsightsShapes,
        content     = content,
    )
}
