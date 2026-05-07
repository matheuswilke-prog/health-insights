package com.healthinsights.feature.onboarding

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.usecase.CalculateDailyTargetUseCase
import com.healthinsights.core.ui.theme.HealthInsightsSemantic
import com.healthinsights.core.ui.theme.HealthInsightsTheme
import java.util.Locale

@Composable
fun GoalScreen(
    profileData: ProfileFormData,
    onBack: () -> Unit,
    onContinue: (UserGoal) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedGoal by rememberSaveable { mutableStateOf(UserGoal.MAINTAIN) }

    val targetKcal = rememberDailyTarget(profileData, selectedGoal)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StepDots(currentStep = 3, totalSteps = 5)
                }
                Spacer(Modifier.size(44.dp))
            }

            // ── Scrollable content ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Qual é o seu objetivo?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Você pode mudar quando quiser.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(20.dp))

                // ── Goal cards ────────────────────────────────────────────
                GoalCard(
                    title = "Perder peso",
                    description = "≈ 0,5 kg por semana",
                    deltaKcal = -500,
                    isSelected = selectedGoal == UserGoal.LOSE,
                    onClick = { selectedGoal = UserGoal.LOSE },
                )
                Spacer(Modifier.height(12.dp))
                GoalCard(
                    title = "Manter peso",
                    description = "Ingestão alinhada ao gasto",
                    deltaKcal = 0,
                    isSelected = selectedGoal == UserGoal.MAINTAIN,
                    onClick = { selectedGoal = UserGoal.MAINTAIN },
                )
                Spacer(Modifier.height(12.dp))
                GoalCard(
                    title = "Ganhar peso",
                    description = "≈ 0,3 kg por semana",
                    deltaKcal = +300,
                    isSelected = selectedGoal == UserGoal.GAIN,
                    onClick = { selectedGoal = UserGoal.GAIN },
                )

                Spacer(Modifier.height(24.dp))

                // ── Daily target preview ──────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Sua meta diária".uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.72.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "%,d", targetKcal),
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "kcal/dia",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Disclaimer ────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Acompanhamento calórico. Não substitui orientação de nutricionista.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        textAlign = TextAlign.Start,
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Footer ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { onContinue(selectedGoal) },
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
                        text = "Continuar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GoalCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GoalCard(
    title: String,
    description: String,
    deltaKcal: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deltaColor = when {
        deltaKcal < 0 -> HealthInsightsSemantic.deficit
        deltaKcal > 0 -> HealthInsightsSemantic.surplus
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val deltaLabel = when {
        deltaKcal == 0 -> "±0 kcal/dia"
        deltaKcal < 0 -> "−${Math.abs(deltaKcal)} kcal/dia"
        else -> "+$deltaKcal kcal/dia"
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Radio indicator
            Surface(
                modifier = Modifier.size(22.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    2.dp,
                    if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
            ) {
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxSize(),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.onBackground,
                    ) {}
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = deltaLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = deltaColor,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Computes daily calorie target for the given profile + goal. Pure, no side effects. */
@Composable
private fun rememberDailyTarget(profileData: ProfileFormData, goal: UserGoal): Int {
    val useCase = CalculateDailyTargetUseCase()
    val profile = UserProfile(
        weightKg = profileData.weightKg,
        heightCm = profileData.heightCm,
        ageYears = profileData.ageYears,
        sex = profileData.sex,
        goal = goal,
        dailyCalorieTarget = 0,
    )
    return useCase(profile)
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GoalScreenPreview() {
    HealthInsightsTheme {
        GoalScreen(
            profileData = ProfileFormData(
                sex = BiologicalSex.MALE,
                weightKg = 80f,
                heightCm = 180,
                ageYears = 30,
            ),
            onBack = {},
            onContinue = {},
        )
    }
}
