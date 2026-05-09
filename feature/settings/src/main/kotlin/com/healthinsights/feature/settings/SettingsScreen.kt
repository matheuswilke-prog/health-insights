package com.healthinsights.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthinsights.core.ui.theme.HealthInsightsSemantic
import com.healthinsights.core.ui.theme.HealthInsightsTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onOpenHealthConnect = onOpenHealthConnect,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsScreenContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            SettingsTopBar(onBack = onBack)
            when (uiState) {
                SettingsUiState.Loading -> LoadingState()
                is SettingsUiState.Content -> SettingsContent(
                    model = uiState.model,
                    onOpenHealthConnect = onOpenHealthConnect,
                )
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 12.dp, end = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "Privacidade",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("settings_loading"),
            color = MaterialTheme.colorScheme.onBackground,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ColumnScope.SettingsContent(
    model: SettingsUiModel,
    onOpenHealthConnect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IntroSection()
        ConsentSection(consents = model.consents)
        HealthConnectSection(onOpenHealthConnect = onOpenHealthConnect)
        PrivacyPolicySection()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun IntroSection() {
    SettingsCard {
        Text(
            text = "Seus dados ficam no aparelho",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "O Health Insights não usa conta, backend, analytics, anúncios ou telemetria. " +
                "Dados de saúde e dados derivados são processados localmente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ConsentSection(
    consents: List<ConsentItemUiModel>,
) {
    SettingsCard {
        SectionLabel("Consentimentos")
        consents.forEachIndexed { index, item ->
            ConsentRow(item = item)
            if (index < consents.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun ConsentRow(
    item: ConsentItemUiModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        StatusDot(granted = item.granted)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (item.granted) "Ativo" else "Desativado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.granted) HealthInsightsSemantic.deficit else HealthInsightsSemantic.ink4,
                )
            }
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            item.policyVersion?.let { version ->
                Text(
                    text = "Política: $version",
                    style = MaterialTheme.typography.labelSmall,
                    color = HealthInsightsSemantic.ink3,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusDot(
    granted: Boolean,
) {
    Box(
        modifier = Modifier
            .padding(top = 5.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(if (granted) HealthInsightsSemantic.deficit else HealthInsightsSemantic.ink4),
    )
}

@Composable
private fun HealthConnectSection(
    onOpenHealthConnect: () -> Unit,
) {
    SettingsCard {
        SectionLabel("Health Connect")
        Text(
            text = "As permissões do Android são gerenciadas pelo Health Connect. " +
                "Use este atalho para revisar ou alterar o acesso concedido ao app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onOpenHealthConnect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(56.dp)
                .testTag("open_health_connect_button"),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = "Abrir permissões",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun PrivacyPolicySection() {
    SettingsCard {
        SectionLabel("Política de privacidade")
        PolicyParagraph(
            "O app lê apenas os dados autorizados no Health Connect: calorias, ingestão, peso e treinos."
        )
        PolicyParagraph(
            "Perfil corporal, objetivo, meta calórica e consentimentos ficam em banco local criptografado."
        )
        PolicyParagraph(
            "Retenção padrão: 12 meses. Nenhum dado é vendido, compartilhado ou enviado para terceiros."
        )
        PolicyParagraph(
            "Para dúvidas sobre privacidade, use o contato informado na política completa do projeto."
        )
    }
}

@Composable
private fun PolicyParagraph(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun SectionLabel(
    text: String,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = HealthInsightsSemantic.ink3,
    )
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    HealthInsightsTheme {
        SettingsScreenContent(
            uiState = SettingsUiState.Content(
                SettingsUiModel(
                    consents = listOf(
                        ConsentItemUiModel(
                            dataType = "calories",
                            title = "Calorias",
                            description = "Gasto ativo e ingestão calórica do Health Connect.",
                            granted = true,
                            policyVersion = "consent-copy-v1.1",
                        ),
                        ConsentItemUiModel(
                            dataType = "weight",
                            title = "Peso",
                            description = "Peso corporal e data da medição.",
                            granted = false,
                            policyVersion = "consent-copy-v1.1",
                        ),
                    ),
                ),
            ),
            onBack = {},
            onOpenHealthConnect = {},
        )
    }
}
