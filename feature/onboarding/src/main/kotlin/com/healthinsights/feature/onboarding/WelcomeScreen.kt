package com.healthinsights.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Bar heights from the design mockup (0..1, relative to chart height).
// Index 6 = today (highlighted); others are dimmed.
private val mockBarWeights = listOf(0.40f, 0.70f, 0.55f, 0.85f, 0.50f, 0.90f, 0.65f)
private val mockDayLabels  = listOf("S", "T", "Q", "Q", "S", "S", "D")

// brand-tint-2 ≈ oklch(0.92 0.06 155) — lighter sage for non-active bars
private val BrandTint2 = Color(0xFFCBE3CF)

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            // ── Status bar inset ──────────────────────────────────────
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(12.dp))

            // ── Wordmark ──────────────────────────────────────────────
            WordmarkRow()

            // ── Content area: card + headline, vertically centered ────
            // BoxWithConstraints captures the available height so that
            // Arrangement.Center works on large phones while verticalScroll
            // prevents clipping on small screens or large font sizes.
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val minH = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minH)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(Modifier.height(24.dp))
                    PreviewCard()
                    Spacer(Modifier.height(28.dp))
                    HeadlineSection()
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── Pinned footer: button → privacy ───────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Começar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }

                Spacer(Modifier.height(16.dp))
                PrivacyNote()
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wordmark
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WordmarkRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TrendLineIcon(size = 20.dp, tint = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Health Insights",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.16).sp,   // ≈ -0.01em at 16sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * Trend-line glyph derived from the design SVG (viewBox 0 0 28 28):
 *   path  : M3 18 L9 12 L13 16 L19 8 L25 14
 *   circle: cx=19 cy=8 r=2
 */
@Composable
private fun TrendLineIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val sx = this.size.width / 28f
        val sy = this.size.height / 28f

        val linePath = Path().apply {
            moveTo(3f * sx, 18f * sy)
            lineTo(9f * sx, 12f * sy)
            lineTo(13f * sx, 16f * sy)
            lineTo(19f * sx, 8f * sy)
            lineTo(25f * sx, 14f * sy)
        }
        drawPath(
            path = linePath,
            color = tint,
            style = Stroke(
                width = 2.4f * sx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        // Accent dot at peak (19, 8)
        drawCircle(
            color = tint,
            radius = 2f * sx,
            center = Offset(19f * sx, 8f * sy),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreviewCard() {
    val accentColor  = MaterialTheme.colorScheme.secondary    // BrandSage = deficit
    val captionColor = MaterialTheme.colorScheme.tertiary     // Ink3
    val onSurface    = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(22.dp),
        color           = MaterialTheme.colorScheme.surface,
        border          = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Label row: dot + "Hoje · prévia"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(6.dp)) {
                    drawCircle(color = accentColor, radius = size.minDimension / 2)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Hoje · prévia",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = captionColor,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Hero number: "−482" at 64sp + "kcal" at 20sp in captionColor
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = "−482",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize      = 64.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = (-2.56).sp,  // -0.04em at 64sp
                        lineHeight    = 64.sp,
                    ),
                    color = onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text      = "kcal",
                    style     = MaterialTheme.typography.bodyLarge.copy(
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color     = captionColor,
                    modifier  = Modifier.padding(bottom = 8.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            // "Você está em déficit hoje."
            Text(
                text = buildAnnotatedString {
                    append("Você está em ")
                    withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                        append("déficit")
                    }
                    append(" hoje.")
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = onSurfaceVar,
            )

            Spacer(Modifier.height(20.dp))

            // Mini bar chart — 7 days, last bar = today (BrandSage), rest = BrandTint2
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                val count    = mockBarWeights.size
                val gapPx    = 6.dp.toPx()
                val barWidth = (size.width - gapPx * (count - 1)) / count
                val corner   = CornerRadius(4.dp.toPx())

                mockBarWeights.forEachIndexed { i, weight ->
                    val barColor  = if (i == count - 1) accentColor else BrandTint2
                    val barHeight = size.height * weight
                    val top       = size.height - barHeight
                    val x         = i * (barWidth + gapPx)

                    drawRoundRect(
                        color        = barColor,
                        topLeft      = Offset(x, top),
                        size         = Size(barWidth, barHeight),
                        cornerRadius = corner,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day labels (ink-3/tertiary ≈ ink-4 in design)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                mockDayLabels.forEach { day ->
                    Text(
                        text      = day,
                        style     = MaterialTheme.typography.labelSmall,
                        color     = captionColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Headline + subheadline
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeadlineSection() {
    Text(
        text  = "Seu balanço calórico,\nsem planilha.",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = (-0.8).sp,    // ≈ -0.025em at 34sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text  = "Lemos o Samsung Health e mostramos, em números, se você está perdendo, mantendo ou ganhando peso.",
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Privacy microcopy
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrivacyNote() {
    val captionColor = MaterialTheme.colorScheme.tertiary
    Row(
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        ShieldIcon(size = 14.dp, tint = captionColor)
        Spacer(Modifier.width(6.dp))
        Text(
            text      = "Seus dados ficam no aparelho. Sem nuvem, sem conta.",
            style     = MaterialTheme.typography.bodySmall,
            color     = captionColor,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Shield icon derived from the design SVG (viewBox 0 0 14 14):
 *   M7 1.5 L2.5 3.5 v3.2 c0 2.8 1.9 5.4 4.5 5.8 2.6-.4 4.5-3 4.5-5.8 V3.5 L7 1.5 z
 */
@Composable
private fun ShieldIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val sx = this.size.width  / 14f
        val sy = this.size.height / 14f

        val path = Path().apply {
            moveTo(7f * sx,    1.5f * sy)
            lineTo(2.5f * sx,  3.5f * sy)
            lineTo(2.5f * sx,  6.7f * sy)
            // First cubic: from (2.5,6.7) → (7.0,12.5)
            cubicTo(
                2.5f * sx,  9.5f  * sy,
                4.4f * sx,  12.1f * sy,
                7.0f * sx,  12.5f * sy,
            )
            // Second cubic: from (7.0,12.5) → (11.5,6.7)
            cubicTo(
                9.6f  * sx, 12.1f * sy,
                11.5f * sx,  9.5f * sy,
                11.5f * sx,  6.7f * sy,
            )
            lineTo(11.5f * sx, 3.5f * sy)
            close()
        }

        drawPath(
            path  = path,
            color = tint,
            style = Stroke(width = 1.2f * minOf(sx, sy)),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    // Inlines visual-system-v1 tokens — feature module cannot import :app theme
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary          = androidx.compose.ui.graphics.Color(0xFF0E1116),
            onPrimary        = androidx.compose.ui.graphics.Color.White,
            secondary        = androidx.compose.ui.graphics.Color(0xFF6FA47A),
            background       = androidx.compose.ui.graphics.Color(0xFFFAFAF7),
            surface          = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            surfaceVariant   = androidx.compose.ui.graphics.Color(0xFFF2F1EC),
            onBackground     = androidx.compose.ui.graphics.Color(0xFF0E1116),
            onSurface        = androidx.compose.ui.graphics.Color(0xFF0E1116),
            onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF3A3F47),
            tertiary         = androidx.compose.ui.graphics.Color(0xFF6B7079),
            outline          = androidx.compose.ui.graphics.Color(0x140E1116),
        ),
    ) {
        WelcomeScreen(onContinue = {})
    }
}
