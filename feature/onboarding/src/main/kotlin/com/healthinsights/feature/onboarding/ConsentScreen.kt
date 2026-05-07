package com.healthinsights.feature.onboarding

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.ui.theme.HealthInsightsTheme

@Composable
fun ConsentScreen(
    profileData: ProfileFormData,
    goal: UserGoal,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsentViewModel = hiltViewModel(),
) {
    // Toggle states — all OFF by default (LGPD Art. 8 §3 — no pre-selection)
    var caloriesGranted by rememberSaveable { mutableStateOf(false) }
    var weightGranted by rememberSaveable { mutableStateOf(false) }
    var exerciseGranted by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val hasScrolledToBottom by remember {
        derivedStateOf { scrollState.value >= scrollState.maxValue - 8 }
    }
    // Auto-unlock if content is shorter than screen
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue == 0) {
            // content fits without scrolling — treat as scrolled
        }
    }
    val ctaEnabled = hasScrolledToBottom || scrollState.maxValue == 0

    val anyGranted = caloriesGranted || weightGranted || exerciseGranted

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
                IconButton(onClick = onSkip, modifier = Modifier.size(44.dp)) {
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
                    StepDots(currentStep = 4, totalSteps = 5)
                }
                Spacer(Modifier.size(44.dp))
            }

            // ── Scrollable consent copy ───────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Antes de começar — seus dados de saúde",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "O Health Insights é operado por Matheus Wilke " +
                        "(matheus.wilke@gmail.com), responsável pelo tratamento " +
                        "dos seus dados de saúde neste aplicativo.\n\n" +
                        "Para mostrar seu balanço calórico diário e a evolução " +
                        "do seu peso, o app precisa ler dados do Android Health " +
                        "Connect. Você decide cada permissão separadamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))

                // ── Toggle: Calorias ──────────────────────────────────────
                ConsentToggle(
                    title = "Calorias gastas e ingeridas",
                    description = "Gasto calórico diário (ativo + basal) e ingestão " +
                        "de calorias dos alimentos registrados no Health Connect. " +
                        "Usado para calcular seu balanço calórico.",
                    notRead = "Frequência cardíaca, passos, sono, GPS ou macros detalhados.",
                    checked = caloriesGranted,
                    onCheckedChange = { caloriesGranted = it },
                    switchTestTag = "switch_calories",
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // ── Toggle: Peso ──────────────────────────────────────────
                ConsentToggle(
                    title = "Peso corporal",
                    description = "Registros de peso (kg) e data de cada medição. " +
                        "Usado para mostrar a evolução do seu peso ao longo do tempo.",
                    notRead = "Composição corporal, % de gordura ou circunferências.",
                    checked = weightGranted,
                    onCheckedChange = { weightGranted = it },
                    switchTestTag = "switch_weight",
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // ── Toggle: Treinos ───────────────────────────────────────
                ConsentToggle(
                    title = "Treinos",
                    description = "Tipo de atividade e duração de cada sessão. " +
                        "Usado para contextualizar seu gasto calórico ativo.",
                    notRead = "GPS, rota, localização, velocidade ou frequência cardíaca.",
                    checked = exerciseGranted,
                    onCheckedChange = { exerciseGranted = it },
                    switchTestTag = "switch_exercise",
                )

                Spacer(Modifier.height(24.dp))

                // ── On-device guarantee ───────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Seus dados nunca saem do seu aparelho. " +
                            "Sem servidores. Sem upload. Sem terceiros. " +
                            "Tudo processado e guardado só aqui, com criptografia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Retention + rights ────────────────────────────────────
                Text(
                    text = "Dados ficam guardados por até 12 meses. " +
                        "Você pode ver, exportar e apagar tudo em " +
                        "Configurações → Privacidade (LGPD Art. 18).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Você pode cancelar esta autorização a qualquer " +
                        "momento em Configurações → Privacidade → Revogar Acesso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))
            }

            // ── Scroll hint ───────────────────────────────────────────────
            if (!ctaEnabled) {
                Text(
                    text = "Role até o final para continuar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }

            // ── Footer ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        val granted = buildSet {
                            if (caloriesGranted) add(ConsentDataType.CALORIES)
                            if (weightGranted) add(ConsentDataType.WEIGHT)
                            if (exerciseGranted) add(ConsentDataType.EXERCISE)
                        }
                        viewModel.confirmConsent(
                            profileData = profileData,
                            goal = goal,
                            grantedTypes = granted,
                            onComplete = onContinue,
                        )
                    },
                    enabled = ctaEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = if (anyGranted) {
                            "Concordo — permitir acesso"
                        } else {
                            "Continuar sem dados de saúde"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Agora não — usar com funções limitadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ConsentToggle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConsentToggle(
    title: String,
    description: String,
    notRead: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    switchTestTag: String = "",
) {
    Column(
        modifier = modifier
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = if (switchTestTag.isNotEmpty()) Modifier.testTag(switchTestTag) else Modifier,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "O app NÃO lê: $notRead",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConsentScreenPreview() {
    HealthInsightsTheme {
        // Preview without ViewModel — compose-preview can't inject Hilt
        ConsentScreenContent(
            profileData = ProfileFormData(
                sex = BiologicalSex.MALE,
                weightKg = 80f,
                heightCm = 180,
                ageYears = 30,
            ),
            goal = UserGoal.LOSE,
            onConfirm = { _, _ -> },
            onSkip = {},
        )
    }
}

/**
 * Stateless content extracted for preview and testing without Hilt.
 * [ConsentScreen] delegates to this composable.
 */
@Composable
internal fun ConsentScreenContent(
    profileData: ProfileFormData,
    goal: UserGoal,
    onConfirm: (ProfileFormData, Set<ConsentDataType>) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var caloriesGranted by rememberSaveable { mutableStateOf(false) }
    var weightGranted by rememberSaveable { mutableStateOf(false) }
    var exerciseGranted by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val hasScrolledToBottom by remember {
        derivedStateOf { scrollState.value >= scrollState.maxValue - 8 }
    }
    val ctaEnabled = hasScrolledToBottom || scrollState.maxValue == 0
    val anyGranted = caloriesGranted || weightGranted || exerciseGranted

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onSkip, modifier = Modifier.size(44.dp)) {
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
                    StepDots(currentStep = 4, totalSteps = 5)
                }
                Spacer(Modifier.size(44.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Antes de começar — seus dados de saúde",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "O Health Insights é operado por Matheus Wilke " +
                        "(matheus.wilke@gmail.com), responsável pelo tratamento " +
                        "dos seus dados de saúde neste aplicativo.\n\n" +
                        "Para mostrar seu balanço calórico diário e a evolução " +
                        "do seu peso, o app precisa ler dados do Android Health " +
                        "Connect. Você decide cada permissão separadamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                ConsentToggle(
                    title = "Calorias gastas e ingeridas",
                    description = "Gasto calórico diário (ativo + basal) e ingestão " +
                        "de calorias dos alimentos registrados no Health Connect. " +
                        "Usado para calcular seu balanço calórico.",
                    notRead = "Frequência cardíaca, passos, sono, GPS ou macros detalhados.",
                    checked = caloriesGranted,
                    onCheckedChange = { caloriesGranted = it },
                    switchTestTag = "switch_calories",
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ConsentToggle(
                    title = "Peso corporal",
                    description = "Registros de peso (kg) e data de cada medição. " +
                        "Usado para mostrar a evolução do seu peso ao longo do tempo.",
                    notRead = "Composição corporal, % de gordura ou circunferências.",
                    checked = weightGranted,
                    onCheckedChange = { weightGranted = it },
                    switchTestTag = "switch_weight",
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ConsentToggle(
                    title = "Treinos",
                    description = "Tipo de atividade e duração de cada sessão. " +
                        "Usado para contextualizar seu gasto calórico ativo.",
                    notRead = "GPS, rota, localização, velocidade ou frequência cardíaca.",
                    checked = exerciseGranted,
                    onCheckedChange = { exerciseGranted = it },
                    switchTestTag = "switch_exercise",
                )

                Spacer(Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Seus dados nunca saem do seu aparelho. " +
                            "Sem servidores. Sem upload. Sem terceiros. " +
                            "Tudo processado e guardado só aqui, com criptografia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Dados ficam guardados por até 12 meses. " +
                        "Você pode ver, exportar e apagar tudo em " +
                        "Configurações → Privacidade (LGPD Art. 18).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Você pode cancelar esta autorização a qualquer " +
                        "momento em Configurações → Privacidade → Revogar Acesso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
            }

            if (!ctaEnabled) {
                Text(
                    text = "Role até o final para continuar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        val granted = buildSet {
                            if (caloriesGranted) add(ConsentDataType.CALORIES)
                            if (weightGranted) add(ConsentDataType.WEIGHT)
                            if (exerciseGranted) add(ConsentDataType.EXERCISE)
                        }
                        onConfirm(profileData, granted)
                    },
                    enabled = ctaEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = if (anyGranted) {
                            "Concordo — permitir acesso"
                        } else {
                            "Continuar sem dados de saúde"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Agora não — usar com funções limitadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
