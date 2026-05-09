package com.healthinsights.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.HealthConnectRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import com.healthinsights.core.domain.usecase.CalculateBmrUseCase
import com.healthinsights.core.domain.usecase.CalculateDailyTargetUseCase
import com.healthinsights.core.domain.usecase.GetDailyBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val consentRepository: ConsentRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val getDailyBalance: GetDailyBalanceUseCase,
    private val calculateBmr: CalculateBmrUseCase,
    private val calculateDailyTarget: CalculateDailyTargetUseCase,
    private val zone: ZoneId,
) : ViewModel() {
    private val formatter = DashboardFormatter()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private var cache: Cache? = null
    private var loadJob: Job? = null

    init {
        load(force = true)
    }

    fun retry() {
        val current = _uiState.value
        if (current is DashboardUiState.Error) {
            _uiState.value = current.copy(isRetrying = true)
        }
        load(force = true)
    }

    fun onForeground() {
        load(force = false)
    }

    fun onSettingsClick() {
        viewModelScope.launch { _events.emit(DashboardEvent.NavigateToSettings) }
    }

    fun onBannerAction(action: BannerAction) {
        viewModelScope.launch {
            _events.emit(
                when (action) {
                    BannerAction.OpenHealthConnect -> DashboardEvent.OpenHealthConnect
                    BannerAction.OpenPermissions -> DashboardEvent.OpenPermissions
                },
            )
        }
    }

    fun onReconfigureClick() {
        viewModelScope.launch { _events.emit(DashboardEvent.ReconfigureOnboarding) }
    }

    private fun load(force: Boolean) {
        val today = LocalDate.now(zone)
        val cached = cache
        if (!force && cached != null && cached.isFreshFor(today)) {
            _uiState.value = cached.state
            return
        }
        if (loadJob?.isActive == true) return

        loadJob =
            viewModelScope.launch {
                if (_uiState.value !is DashboardUiState.Content) {
                    _uiState.value = DashboardUiState.Loading
                }
                val loadedState = runCatching { loadState(today) }
                    .getOrElse { DashboardUiState.Error() }
                cache = Cache(date = today, loadedAt = Instant.now(), state = loadedState)
                _uiState.value = loadedState
            }
    }

    private suspend fun loadState(today: LocalDate): DashboardUiState {
        val profile = userProfileRepository.get().first() ?: return DashboardUiState.LocalStateInvalid
        val consents = consentRepository.getAll().first()
        if (consents.isEmpty()) return DashboardUiState.LocalStateInvalid

        val localCaloriesGranted = consents.any { it.dataType == "calories" && it.granted }
        val localWeightGranted = consents.any { it.dataType == "weight" && it.granted }
        val availability = healthConnectRepository.getAvailability()
        val healthConnectAvailable = availability == HealthConnectAvailability.Available
        val granted = if (healthConnectAvailable) {
            healthConnectRepository.getGrantedPermissions()
        } else {
            emptySet()
        }

        val goal = GoalUiModel(
            targetKcal = profile.persistedOrCalculatedTarget(),
            goal = profile.goal,
        )
        val bmr = calculateBmr(profile)

        if (!healthConnectAvailable) {
            return DashboardUiState.Content(
                DashboardUiModel(
                    date = today,
                    goal = goal,
                    balance = BalanceUiModel.Unavailable("Saldo depende dos dados do Health Connect."),
                    intake = IntakeUiModel.HealthConnectUnavailable,
                    expenditure = ExpenditureUiModel.HealthConnectUnavailable,
                    weight = WeightUiModel.HealthConnectUnavailable,
                    banner = BannerUiModel(
                        tone = BannerTone.Warning,
                        title = "Health Connect indisponível",
                        body = "Não conseguimos ler calorias, ingestão e peso agora. Sua meta local segue ativa.",
                        action = BannerAction.OpenHealthConnect,
                    ),
                ),
            )
        }

        val canReadCalories = localCaloriesGranted &&
            HealthDataPermission.CALORIES_BURNED in granted &&
            HealthDataPermission.CALORIE_INTAKE in granted
        val canReadWeight = localWeightGranted && HealthDataPermission.WEIGHT in granted

        val balance = if (canReadCalories) getDailyBalance(today).first() else null
        val latestWeight = if (canReadWeight) healthConnectRepository.getLatestWeight() else null

        return DashboardUiState.Content(
            DashboardUiModel(
                date = today,
                goal = goal,
                balance = balanceUi(balance, canReadCalories),
                intake = intakeUi(balance, canReadCalories),
                expenditure = expenditureUi(balance, canReadCalories, bmr),
                weight = weightUi(latestWeight, canReadWeight, today),
                banner = bannerUi(balance, canReadCalories),
            ),
        )
    }

    private fun UserProfile.persistedOrCalculatedTarget(): Int =
        dailyCalorieTarget.takeIf { it > 0 } ?: calculateDailyTarget(this)

    private fun balanceUi(
        balance: com.healthinsights.core.domain.model.DailyCaloricBalance?,
        canReadCalories: Boolean,
    ): BalanceUiModel {
        if (!canReadCalories) {
            return BalanceUiModel.Unavailable(
                "Calorias dependem de permissão no Health Connect.",
            )
        }
        return when (balance?.status) {
            BalanceStatus.Deficit,
            BalanceStatus.Maintain,
            BalanceStatus.Surplus,
            -> BalanceUiModel.Available(balance.balance, balance.status)
            BalanceStatus.NoIntakeData -> BalanceUiModel.Unavailable(
                "Sem ingestão registrada hoje, não calculamos o saldo. Meta e gasto seguem abaixo.",
            )
            BalanceStatus.HealthConnectUnavailable,
            null,
            -> BalanceUiModel.Unavailable("Não conseguimos acessar os dados de calorias agora.")
        }
    }

    private fun intakeUi(
        balance: com.healthinsights.core.domain.model.DailyCaloricBalance?,
        canReadCalories: Boolean,
    ): IntakeUiModel =
        when {
            !canReadCalories -> IntakeUiModel.PermissionMissing
            balance?.status == BalanceStatus.HealthConnectUnavailable -> IntakeUiModel.HealthConnectUnavailable
            balance == null || balance.intake <= 0 -> IntakeUiModel.Empty
            else -> IntakeUiModel.Available(balance.intake)
        }

    private fun expenditureUi(
        balance: com.healthinsights.core.domain.model.DailyCaloricBalance?,
        canReadCalories: Boolean,
        bmr: Int,
    ): ExpenditureUiModel =
        when {
            !canReadCalories -> ExpenditureUiModel.PermissionMissing
            balance?.status == BalanceStatus.HealthConnectUnavailable -> ExpenditureUiModel.HealthConnectUnavailable
            balance == null -> ExpenditureUiModel.BasalOnly(bmr)
            balance.activeBurned <= 0 -> ExpenditureUiModel.BasalOnly(balance.bmr)
            else -> ExpenditureUiModel.Available(
                bmrKcal = balance.bmr,
                activeKcal = balance.activeBurned,
            )
        }

    private fun weightUi(
        latestWeight: com.healthinsights.core.domain.model.LatestWeight?,
        canReadWeight: Boolean,
        today: LocalDate,
    ): WeightUiModel =
        when {
            !canReadWeight -> WeightUiModel.PermissionMissing
            latestWeight == null -> WeightUiModel.Empty
            latestWeight.valueKg <= 0f -> WeightUiModel.Empty
            else -> WeightUiModel.Available(
                kg = latestWeight.valueKg,
                measuredAtLabel = formatter.measuredAtLabel(latestWeight.measuredAt, today, zone),
            )
        }

    private fun bannerUi(
        balance: com.healthinsights.core.domain.model.DailyCaloricBalance?,
        canReadCalories: Boolean,
    ): BannerUiModel? =
        when {
            !canReadCalories -> BannerUiModel(
                tone = BannerTone.Info,
                title = "Permissões parciais",
                body = "Calorias dependem de consentimento e permissão do Health Connect.",
                action = BannerAction.OpenPermissions,
            )
            balance?.status == BalanceStatus.HealthConnectUnavailable -> BannerUiModel(
                tone = BannerTone.Warning,
                title = "Calorias indisponíveis",
                body = "Não conseguimos atualizar ingestão e calorias ativas agora.",
            )
            else -> null
        }

    private data class Cache(
        val date: LocalDate,
        val loadedAt: Instant,
        val state: DashboardUiState,
    ) {
        fun isFreshFor(today: LocalDate): Boolean =
            date == today && Duration.between(loadedAt, Instant.now()) < CACHE_TTL
    }

    private companion object {
        val CACHE_TTL: Duration = Duration.ofMinutes(2)
    }
}
