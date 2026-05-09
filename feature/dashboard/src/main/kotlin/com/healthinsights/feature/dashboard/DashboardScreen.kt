package com.healthinsights.feature.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.ui.theme.HealthInsightsSemantic
import com.healthinsights.core.ui.theme.HealthInsightsTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"

@Composable
fun DashboardScreen(
    onReconfigureOnboarding: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.onForeground()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                DashboardEvent.NavigateToSettings -> onSettingsClick()
                DashboardEvent.ReconfigureOnboarding -> onReconfigureOnboarding()
                DashboardEvent.OpenHealthConnect,
                DashboardEvent.OpenPermissions,
                -> openHealthConnect(context = context)
            }
        }
    }

    DashboardScreenContent(
        uiState = uiState,
        onRetry = viewModel::retry,
        onSettingsClick = viewModel::onSettingsClick,
        onBannerAction = viewModel::onBannerAction,
        onReconfigure = viewModel::onReconfigureClick,
        modifier = modifier,
    )
}

@Composable
internal fun DashboardScreenContent(
    uiState: DashboardUiState,
    onRetry: () -> Unit,
    onSettingsClick: () -> Unit,
    onBannerAction: (BannerAction) -> Unit,
    onReconfigure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (uiState) {
            DashboardUiState.Loading -> DashboardShell(onSettingsClick = onSettingsClick) {
                DashboardSkeleton()
            }
            is DashboardUiState.Content -> DashboardShell(
                date = uiState.model.date,
                onSettingsClick = onSettingsClick,
            ) {
                uiState.model.banner?.let { banner ->
                    GlobalBanner(
                        banner = banner,
                        onAction = onBannerAction,
                    )
                }
                BalanceHeroCard(balance = uiState.model.balance)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DailyGoalCard(
                        goal = uiState.model.goal,
                        modifier = Modifier.weight(1f),
                    )
                    LatestWeightCard(
                        weight = uiState.model.weight,
                        modifier = Modifier.weight(1f),
                    )
                }
                IntakeCard(intake = uiState.model.intake)
                EstimatedExpenditureCard(expenditure = uiState.model.expenditure)
            }
            is DashboardUiState.Error -> ErrorState(
                isRetrying = uiState.isRetrying,
                onRetry = onRetry,
                onSettingsClick = onSettingsClick,
            )
            DashboardUiState.LocalStateInvalid -> LocalInvalidState(
                onReconfigure = onReconfigure,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun DashboardShell(
    modifier: Modifier = Modifier,
    date: LocalDate = LocalDate.now(),
    onSettingsClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        DashboardTopBar(date = date, onSettingsClick = onSettingsClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
        PrivacyFootnote()
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun DashboardTopBar(
    date: LocalDate,
    onSettingsClick: () -> Unit,
) {
    val formatter = DashboardFormatter()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 20.dp, end = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Eyebrow(text = formatter.dayLabel(date).uppercase())
            Text(
                text = "Hoje",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.displayMedium.fontSize),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Configurações",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BalanceHeroCard(balance: BalanceUiModel) {
    val formatter = DashboardFormatter()
    DashboardCard {
        Eyebrow("Saldo do dia")
        when (balance) {
            is BalanceUiModel.Available -> {
                Text(
                    text = "${formatter.signedKcal(balance.balanceKcal)} kcal",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("balance_value"),
                )
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusPill(status = balance.status)
                    Text(
                        text = statusSupportText(balance.status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = HealthInsightsSemantic.ink3,
                    )
                }
            }
            is BalanceUiModel.Unavailable -> {
                Text(
                    text = "- kcal",
                    style = MaterialTheme.typography.displayLarge,
                    color = HealthInsightsSemantic.ink4,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = balance.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun DailyGoalCard(
    goal: GoalUiModel,
    modifier: Modifier = Modifier,
) {
    val formatter = DashboardFormatter()
    DashboardCard(modifier = modifier) {
        Eyebrow("Meta diária")
        Text(
            text = "${formatter.kcal(goal.targetKcal)} kcal",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Objetivo: ${formatter.goalLabel(goal.goal)}",
            style = MaterialTheme.typography.bodyMedium,
            color = HealthInsightsSemantic.ink3,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LatestWeightCard(
    weight: WeightUiModel,
    modifier: Modifier = Modifier,
) {
    val formatter = DashboardFormatter()
    DashboardCard(modifier = modifier) {
        Eyebrow("Peso mais recente")
        when (weight) {
            is WeightUiModel.Available -> {
                Text(
                    text = "${formatter.kg(weight.kg)} kg",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = weight.measuredAtLabel?.let { "Medido $it" } ?: "Data indisponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthInsightsSemantic.ink3,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            WeightUiModel.Empty -> MetricUnavailable("Sem registro de peso no Health Connect.")
            WeightUiModel.PermissionMissing -> MetricUnavailable("Permissão de peso necessária.")
            WeightUiModel.HealthConnectUnavailable -> MetricUnavailable("Health Connect indisponível.")
        }
    }
}

@Composable
private fun IntakeCard(intake: IntakeUiModel) {
    val formatter = DashboardFormatter()
    DashboardCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow("Ingestão registrada")
            Text(
                text = "Health Connect",
                style = MaterialTheme.typography.labelSmall,
                color = HealthInsightsSemantic.ink4,
            )
        }
        when (intake) {
            is IntakeUiModel.Available -> {
                Text(
                    text = "${formatter.kcal(intake.kcal)} kcal",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            IntakeUiModel.Empty -> {
                MetricDash()
                Text(
                    text = "Ainda não há ingestão calórica registrada hoje no Health Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "Quando esse dado aparecer, calculamos o balanço completo do dia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthInsightsSemantic.ink3,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            IntakeUiModel.PermissionMissing -> InlineNote(
                text = "Permissão de calorias está negada ou ausente.",
            )
            IntakeUiModel.HealthConnectUnavailable -> MetricUnavailable("Indisponível sem Health Connect.")
        }
    }
}

@Composable
private fun EstimatedExpenditureCard(expenditure: ExpenditureUiModel) {
    val formatter = DashboardFormatter()
    DashboardCard {
        Eyebrow(
            text = when (expenditure) {
                is ExpenditureUiModel.BasalOnly -> "Gasto do dia"
                else -> "Gasto estimado"
            },
        )
        when (expenditure) {
            is ExpenditureUiModel.Available -> {
                Text(
                    text = "${formatter.kcal(expenditure.bmrKcal + expenditure.activeKcal)} kcal",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(Modifier.height(14.dp))
                MetricLine("Metabolismo basal estimado", "${formatter.kcal(expenditure.bmrKcal)} kcal")
                MetricLine("Calorias ativas", "+${formatter.kcal(expenditure.activeKcal)} kcal")
            }
            is ExpenditureUiModel.BasalOnly -> {
                Text(
                    text = "${formatter.kcal(expenditure.bmrKcal)} kcal",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Metabolismo basal estimado. Calorias ativas indisponíveis hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthInsightsSemantic.ink3,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            ExpenditureUiModel.PermissionMissing -> InlineNote(
                text = "Sem calorias ativas, não calculamos o gasto total do dia.",
            )
            ExpenditureUiModel.HealthConnectUnavailable -> MetricUnavailable("Indisponível sem Health Connect.")
        }
    }
}

@Composable
private fun GlobalBanner(
    banner: BannerUiModel,
    onAction: (BannerAction) -> Unit,
) {
    val background = when (banner.tone) {
        BannerTone.Info -> MaterialTheme.colorScheme.surfaceVariant
        BannerTone.Warning -> Color(0xFFFFF4D8)
        BannerTone.Error -> Color(0xFFFFEEE7)
    }
    val accent = when (banner.tone) {
        BannerTone.Info -> HealthInsightsSemantic.maintain
        BannerTone.Warning -> Color(0xFFC9983B)
        BannerTone.Error -> HealthInsightsSemantic.surplus
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(80.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = banner.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = banner.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            banner.action?.let { action ->
                OutlinedButton(
                    onClick = { onAction(action) },
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text(action.label)
                }
            }
        }
    }
}

@Composable
private fun DashboardSkeleton() {
    repeat(4) {
        DashboardCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (it == 0) 132.dp else 84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun ErrorState(
    isRetrying: Boolean,
    onRetry: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    DashboardShell(onSettingsClick = onSettingsClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Algo não saiu como esperado.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Não conseguimos carregar seus dados de hoje. Tente novamente em instantes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            Button(
                onClick = onRetry,
                enabled = !isRetrying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(56.dp)
                    .testTag("retry_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (isRetrying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Tentar novamente")
                }
            }
        }
    }
}

@Composable
private fun LocalInvalidState(
    onReconfigure: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    DashboardShell(onSettingsClick = onSettingsClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 80.dp),
        ) {
            Text(
                text = "Precisamos refazer sua configuração local para continuar.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Sua meta, perfil e consentimentos serão criados de novo. Nenhum dado sai do aparelho.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            InlineNote(text = "Dados ficam no aparelho. Sem conta, sem nuvem.")
            Button(
                onClick = onReconfigure,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(56.dp),
            ) {
                Text("Refazer configuração")
            }
        }
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content,
        )
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = HealthInsightsSemantic.ink3,
    )
}

@Composable
private fun StatusPill(status: BalanceStatus) {
    val formatter = DashboardFormatter()
    val color = when (status) {
        BalanceStatus.Deficit -> HealthInsightsSemantic.deficit
        BalanceStatus.Maintain -> HealthInsightsSemantic.maintain
        BalanceStatus.Surplus -> HealthInsightsSemantic.surplus
        else -> HealthInsightsSemantic.ink3
    }
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatter.statusLabel(status),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun MetricUnavailable(text: String) {
    MetricDash()
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = HealthInsightsSemantic.ink3,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun MetricDash() {
    Text(
        text = "-",
        style = MaterialTheme.typography.headlineMedium,
        color = HealthInsightsSemantic.ink4,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun InlineNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
    )
}

@Composable
private fun PrivacyFootnote() {
    Text(
        text = "Dados ficam no aparelho",
        style = MaterialTheme.typography.labelSmall,
        color = HealthInsightsSemantic.ink3,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
    )
}

private val BannerAction.label: String
    get() =
        when (this) {
            BannerAction.OpenHealthConnect -> "Abrir Health Connect"
            BannerAction.OpenPermissions -> "Ajustar permissões"
        }

private fun statusSupportText(status: BalanceStatus): String =
    when (status) {
        BalanceStatus.Deficit -> "Você está em déficit."
        BalanceStatus.Maintain -> "Dentro da faixa de manutenção."
        BalanceStatus.Surplus -> "Você está acima da meta."
        else -> ""
    }

private fun openHealthConnect(context: android.content.Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(HEALTH_CONNECT_PACKAGE)
    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL))
    try {
        context.startActivity(launchIntent ?: fallbackIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(fallbackIntent)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreviewComplete() {
    HealthInsightsTheme {
        DashboardScreenContent(
            uiState = DashboardUiState.Content(
                DashboardUiModel(
                    date = LocalDate.of(2026, 5, 8),
                    goal = GoalUiModel(2200, UserGoal.LOSE),
                    balance = BalanceUiModel.Available(-470, BalanceStatus.Deficit),
                    intake = IntakeUiModel.Available(1850),
                    expenditure = ExpenditureUiModel.Available(1700, 620),
                    weight = WeightUiModel.Available(82.4f, "hoje"),
                ),
            ),
            onRetry = {},
            onSettingsClick = {},
            onBannerAction = {},
            onReconfigure = {},
        )
    }
}
