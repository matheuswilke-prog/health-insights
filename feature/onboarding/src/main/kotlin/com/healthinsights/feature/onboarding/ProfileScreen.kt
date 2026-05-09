package com.healthinsights.feature.onboarding

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthinsights.core.domain.model.BiologicalSex
import com.healthinsights.core.ui.theme.HealthInsightsSemantic
import com.healthinsights.core.ui.theme.HealthInsightsTheme

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Lightweight data holder for the profile form.
 * NOT persisted to the database here — persistence happens only after
 * ConsentScreen (S1.3) is completed, satisfying the LGPD base-legal requirement.
 */
data class ProfileFormData(
    val sex: BiologicalSex,
    val weightKg: Float,
    val heightCm: Int,
    val ageYears: Int,
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onContinue: (ProfileFormData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sex by rememberSaveable { mutableStateOf(BiologicalSex.MALE) }
    var weightRaw by rememberSaveable { mutableStateOf("") }
    var heightRaw by rememberSaveable { mutableStateOf("") }
    var ageRaw by rememberSaveable { mutableStateOf("") }
    var showDataUseDialog by rememberSaveable { mutableStateOf(false) }

    // Validation — only show errors after first submit attempt
    var submitted by rememberSaveable { mutableStateOf(false) }

    val weightError = validateWeight(weightRaw).takeIf { submitted }
    val heightError = validateHeight(heightRaw).takeIf { submitted }
    val ageError = validateAge(ageRaw).takeIf { submitted }

    val isFormValid =
        validateWeight(weightRaw) == null &&
            validateHeight(heightRaw) == null &&
            validateAge(ageRaw) == null

    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // ── Header: back + step dots ──────────────────────────────────
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepDots(currentStep = 2, totalSteps = 5)
                }
                // Spacer to balance the back button width
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
                    text = "Vamos calcular\nseu metabolismo basal.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Esses dados ficam no seu aparelho e podem ser editados depois.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))

                // ── Sexo biológico ────────────────────────────────────────
                FieldLabel(text = "Sexo biológico")
                Spacer(Modifier.height(8.dp))
                SexSegmentedControl(
                    selected = sex,
                    onSelect = { sex = it },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Usado apenas na fórmula de Mifflin-St Jeor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                Spacer(Modifier.height(20.dp))

                // ── Peso ──────────────────────────────────────────────────
                NumericField(
                    label = "Peso atual",
                    value = weightRaw,
                    onValueChange = { weightRaw = it },
                    suffix = "kg",
                    error = weightError,
                    hint = "30 – 300",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    testTag = "field_weight",
                )

                // ── Altura ────────────────────────────────────────────────
                NumericField(
                    label = "Altura",
                    value = heightRaw,
                    onValueChange = { heightRaw = it },
                    suffix = "cm",
                    error = heightError,
                    hint = "100 – 250",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    testTag = "field_height",
                )

                // ── Idade ─────────────────────────────────────────────────
                NumericField(
                    label = "Idade",
                    value = ageRaw,
                    onValueChange = { ageRaw = it },
                    suffix = "anos",
                    error = ageError,
                    hint = "13 – 120",
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    testTag = "field_age",
                )

                // ── Privacy link ──────────────────────────────────────────
                Text(
                    text = "Como tratamos esses dados →",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { showDataUseDialog = true }
                        .testTag("profile_data_use_link"),
                )

                Spacer(Modifier.height(24.dp))
            }

            // ── Pinned footer ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        submitted = true
                        if (isFormValid) {
                            onContinue(
                                ProfileFormData(
                                    sex = sex,
                                    weightKg = weightRaw.replace(",", ".").toFloat(),
                                    heightCm = heightRaw.toInt(),
                                    ageYears = ageRaw.toInt(),
                                ),
                            )
                        }
                    },
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

    if (showDataUseDialog) {
        ProfileDataUseDialog(onDismiss = { showDataUseDialog = false })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Step progress dots. Completed steps are filled ink-1, current step is
 * wider (18 dp), future steps use hairline color.
 */
@Composable
internal fun StepDots(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isCurrent = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep
            Surface(
                modifier = Modifier.size(
                    width = if (isCurrent) 18.dp else 6.dp,
                    height = 6.dp,
                ),
                shape = RoundedCornerShape(999.dp),
                color = if (isCompleted || isCurrent) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.outline
                },
            ) {}
        }
    }
}

@Composable
private fun SexSegmentedControl(
    selected: BiologicalSex,
    onSelect: (BiologicalSex) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf(BiologicalSex.MALE to "Masculino", BiologicalSex.FEMALE to "Feminino")
                .forEach { (sex, label) ->
                    val isSelected = selected == sex
                    Surface(
                        onClick = { onSelect(sex) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        } else {
                            null
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ProfileDataUseDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Como tratamos esses dados",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = "Peso, altura, idade e sexo biológico são usados apenas " +
                    "para estimar seu metabolismo basal e sua meta calórica. " +
                    "Esses dados ficam salvos somente no aparelho, protegidos " +
                    "localmente, e não são enviados para servidor, analytics " +
                    "ou terceiros.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendi")
            }
        },
    )
}

@Composable
private fun NumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    error: String?,
    hint: String,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
) {
    val hasError = error != null
    Column(modifier = modifier.padding(bottom = 18.dp)) {
        FieldLabel(text = label)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // Allow digits, one comma or dot for weight decimal
                val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                onValueChange(filtered)
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            suffix = {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            supportingText = {
                Text(
                    text = error ?: hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasError) {
                        HealthInsightsSemantic.surplus
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            },
            isError = hasError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() },
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = HealthInsightsSemantic.surplus,
                errorCursorColor = HealthInsightsSemantic.surplus,
            ),
            singleLine = true,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Validation
// ─────────────────────────────────────────────────────────────────────────────

internal fun validateWeight(raw: String): String? {
    val v = raw.replace(",", ".").toFloatOrNull() ?: return "Informe um peso válido"
    return if (v < 30f || v > 300f) "Deve ser entre 30 e 300 kg" else null
}

internal fun validateHeight(raw: String): String? {
    val v = raw.toIntOrNull() ?: return "Informe uma altura válida"
    return if (v < 100 || v > 250) "Deve ser entre 100 e 250 cm" else null
}

internal fun validateAge(raw: String): String? {
    val v = raw.toIntOrNull() ?: return "Informe uma idade válida"
    return if (v < 13 || v > 120) "Deve ser entre 13 e 120 anos" else null
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    HealthInsightsTheme {
        ProfileScreen(onBack = {}, onContinue = {})
    }
}
