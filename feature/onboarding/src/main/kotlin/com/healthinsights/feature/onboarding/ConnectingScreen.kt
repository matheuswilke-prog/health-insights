package com.healthinsights.feature.onboarding

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthinsights.core.ui.theme.HealthInsightsSemantic
import com.healthinsights.core.ui.theme.HealthInsightsTheme

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"

@Composable
fun ConnectingScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConnectingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val permissionsToRequest by viewModel.permissionsToRequest.collectAsState()

    val launcher = rememberLauncherForActivityResult(viewModel.permissionContract) { granted ->
        viewModel.onPermissionResult(granted)
    }

    // Auto-launch permission dialog when state transitions to ReadyToRequest
    LaunchedEffect(uiState) {
        if (uiState is ConnectingUiState.ReadyToRequest && permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        }
    }

    // Auto-navigate to dashboard on full success
    LaunchedEffect(uiState) {
        val state = uiState
        if (state is ConnectingUiState.PermissionResult &&
            state.totalRequested == 0
        ) {
            // Nothing was consented — bypass dashboard
            onNavigateToDashboard()
        }
    }

    ConnectingScreenContent(
        uiState = uiState,
        onNavigateToDashboard = onNavigateToDashboard,
        onRetry = viewModel::retryPermissions,
        onLaunchPermissions = { launcher.launch(permissionsToRequest) },
        modifier = modifier,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless content (testable without Hilt)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ConnectingScreenContent(
    uiState: ConnectingUiState,
    onNavigateToDashboard: () -> Unit,
    onRetry: () -> Unit,
    onLaunchPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
        context.startActivity(intent)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // ── Wordmark header ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "Health Insights",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // ── Content area ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when (uiState) {
                    ConnectingUiState.Checking -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("loading_indicator"),
                            color = MaterialTheme.colorScheme.onBackground,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Verificando Health Connect…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    ConnectingUiState.ReadyToRequest -> {
                        // Dialog launches automatically via LaunchedEffect
                        Text(
                            text = "Abrindo Health Connect…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("ready_to_request_label"),
                        )
                    }

                    ConnectingUiState.NotAvailable -> {
                        NotAvailableCard(
                            onOpenPlayStore = ::openPlayStore,
                            onSkip = onNavigateToDashboard,
                        )
                    }

                    ConnectingUiState.UpdateRequired -> {
                        UpdateRequiredCard(
                            onOpenPlayStore = ::openPlayStore,
                            onSkip = onNavigateToDashboard,
                        )
                    }

                    is ConnectingUiState.PermissionResult -> {
                        PermissionResultCard(
                            state = uiState,
                            onContinue = onNavigateToDashboard,
                            onRetry = onRetry,
                        )
                    }
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// State-specific sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotAvailableCard(
    onOpenPlayStore: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder stripe
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(2.dp),
        ) {}

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Health Connect não está instalado",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("not_available_title"),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "O Health Insights usa o Health Connect do Google para " +
                "ler seus dados de saúde com segurança. " +
                "Instale-o gratuitamente na Play Store.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onOpenPlayStore,
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
                text = "Instalar Health Connect",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pular por agora — usar sem dados de saúde",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UpdateRequiredCard(
    onOpenPlayStore: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(2.dp),
        ) {}

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Atualize o Health Connect",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("update_required_title"),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "A versão instalada do Health Connect é muito antiga. " +
                "Atualize na Play Store para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onOpenPlayStore,
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
                text = "Atualizar na Play Store",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pular por agora — usar sem dados de saúde",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionResultCard(
    state: ConnectingUiState.PermissionResult,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFullSuccess = state.grantedCount == state.totalRequested && state.totalRequested > 0
    val isPartial = state.grantedCount in 1 until state.totalRequested
    val noneGranted = state.grantedCount == 0 && state.totalRequested > 0

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isFullSuccess) {
            Text(
                text = "${state.grantedCount} permissões concedidas",
                style = MaterialTheme.typography.headlineMedium,
                color = HealthInsightsSemantic.deficit,
                modifier = Modifier.testTag("full_success_title"),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Perfeito! O Health Insights agora pode ler seus dados de saúde.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (isPartial) {
            Text(
                text = "${state.grantedCount} de ${state.totalRequested} permissões concedidas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("partial_success_title"),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Você pode continuar com os dados disponíveis " +
                    "ou tentar conceder as permissões restantes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (noneGranted) {
            Text(
                text = "Nenhuma permissão concedida",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("none_granted_title"),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Sem permissões, o app não consegue mostrar dados reais. " +
                    "Você pode tentar novamente ou usar com funções limitadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── Permission summary row ─────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Permissões concedidas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${state.grantedCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (state.grantedCount > 0) {
                            HealthInsightsSemantic.deficit
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (state.totalRequested > state.grantedCount) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Negadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${state.totalRequested - state.grantedCount}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = HealthInsightsSemantic.surplus,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("continue_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = "Ir para o Dashboard",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        if (!isFullSuccess) {
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retry_button"),
            ) {
                Text(
                    text = "Tentar novamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConnectingPreview_NotAvailable() {
    HealthInsightsTheme {
        ConnectingScreenContent(
            uiState = ConnectingUiState.NotAvailable,
            onNavigateToDashboard = {},
            onRetry = {},
            onLaunchPermissions = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConnectingPreview_PartialSuccess() {
    HealthInsightsTheme {
        ConnectingScreenContent(
            uiState = ConnectingUiState.PermissionResult(grantedCount = 2, totalRequested = 5),
            onNavigateToDashboard = {},
            onRetry = {},
            onLaunchPermissions = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConnectingPreview_FullSuccess() {
    HealthInsightsTheme {
        ConnectingScreenContent(
            uiState = ConnectingUiState.PermissionResult(grantedCount = 5, totalRequested = 5),
            onNavigateToDashboard = {},
            onRetry = {},
            onLaunchPermissions = {},
        )
    }
}
