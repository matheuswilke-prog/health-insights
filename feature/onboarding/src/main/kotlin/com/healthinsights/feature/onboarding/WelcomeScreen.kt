package com.healthinsights.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mock values for the preview card — replaced by real data on Dashboard
private val mockBarWeights  = listOf(0.45f, 0.30f, 0.72f, 0.50f, 0.65f, 0.38f, 0.55f)
private val mockDayLabels   = listOf("S", "T", "Q", "Q", "S", "S", "D")

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Scrollable content ────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(Modifier.height(20.dp))

                // Wordmark
                Text(
                    text = "Health Insights",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                )

                Spacer(Modifier.height(24.dp))

                // ── Preview card — Variante A (Apple Health minimal) ──
                PreviewCard()

                Spacer(Modifier.height(32.dp))

                // Headline
                Text(
                    text = "Saiba se está em déficit ou superávit calórico.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(12.dp))

                // Subheadline
                Text(
                    text = "O Health Insights conecta seus dados do Samsung Health e mostra, em números, se você está perdendo, mantendo ou ganhando peso.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(40.dp))
            }

            // ── Pinned footer ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Privacy microcopy
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Seus dados ficam só no seu aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(12.dp))

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

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PreviewCard() {
    val accentColor   = MaterialTheme.colorScheme.secondary   // BrandSage
    val trackColor    = MaterialTheme.colorScheme.surfaceVariant // BgSunken
    val captionColor  = MaterialTheme.colorScheme.tertiary      // Ink3
    val onSurface     = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar  = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier       = Modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(22.dp),
        color          = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation  = 0.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Label row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier
                    .width(6.dp)
                    .height(6.dp)) {
                    drawCircle(color = accentColor, radius = size.minDimension / 2)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "Hoje · prévia",
                    style = MaterialTheme.typography.bodySmall,
                    color = captionColor,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Hero number
            Text(
                text  = "−482 kcal",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize     = 40.sp,
                    fontWeight   = FontWeight.Bold,
                    letterSpacing = (-1.5).sp,
                ),
                color = onSurface,
            )

            Spacer(Modifier.height(4.dp))

            // Annotation with semantic color on "déficit"
            Text(
                text = buildAnnotatedString {
                    append("Você está em ")
                    withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Medium)) {
                        append("déficit")
                    }
                    append(".")
                },
                style = MaterialTheme.typography.bodyLarge,
                color = onSurfaceVar,
            )

            Spacer(Modifier.height(20.dp))

            // Mini bar chart — 7 days
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                val count    = mockBarWeights.size
                val gapPx    = 6.dp.toPx()
                val barWidth = (size.width - gapPx * (count - 1)) / count
                val corner   = CornerRadius(3.dp.toPx())

                mockBarWeights.forEachIndexed { i, weight ->
                    val x         = i * (barWidth + gapPx)
                    val barHeight = size.height * weight
                    val top       = size.height - barHeight

                    // Background track
                    drawRoundRect(
                        color       = trackColor,
                        topLeft     = Offset(x, 0f),
                        size        = Size(barWidth, size.height),
                        cornerRadius = corner,
                    )
                    // Filled bar
                    drawRoundRect(
                        color       = accentColor,
                        topLeft     = Offset(x, top),
                        size        = Size(barWidth, barHeight),
                        cornerRadius = corner,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Day labels
            Row(
                modifier            = Modifier.fillMaxWidth(),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onContinue = {})
    }
}
