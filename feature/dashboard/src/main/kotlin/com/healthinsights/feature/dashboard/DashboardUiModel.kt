package com.healthinsights.feature.dashboard

import com.healthinsights.core.domain.model.BalanceStatus
import com.healthinsights.core.domain.model.UserGoal
import java.time.LocalDate

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Content(
        val model: DashboardUiModel,
        val isRefreshing: Boolean = false,
    ) : DashboardUiState

    data class Error(
        val isRetrying: Boolean = false,
    ) : DashboardUiState

    data object LocalStateInvalid : DashboardUiState
}

data class DashboardUiModel(
    val date: LocalDate,
    val goal: GoalUiModel,
    val balance: BalanceUiModel,
    val intake: IntakeUiModel,
    val expenditure: ExpenditureUiModel,
    val weight: WeightUiModel,
    val banner: BannerUiModel? = null,
)

data class GoalUiModel(
    val targetKcal: Int,
    val goal: UserGoal,
)

sealed interface BalanceUiModel {
    data class Available(
        val balanceKcal: Int,
        val status: BalanceStatus,
    ) : BalanceUiModel

    data class Unavailable(
        val reason: String,
    ) : BalanceUiModel
}

sealed interface IntakeUiModel {
    data class Available(val kcal: Int) : IntakeUiModel
    data object Empty : IntakeUiModel
    data object PermissionMissing : IntakeUiModel
    data object HealthConnectUnavailable : IntakeUiModel
}

sealed interface ExpenditureUiModel {
    data class Available(
        val bmrKcal: Int,
        val activeKcal: Int,
    ) : ExpenditureUiModel

    data class BasalOnly(
        val bmrKcal: Int,
    ) : ExpenditureUiModel

    data object PermissionMissing : ExpenditureUiModel
    data object HealthConnectUnavailable : ExpenditureUiModel
}

sealed interface WeightUiModel {
    data class Available(
        val kg: Float,
        val measuredAtLabel: String?,
    ) : WeightUiModel

    data object Empty : WeightUiModel
    data object PermissionMissing : WeightUiModel
    data object HealthConnectUnavailable : WeightUiModel
}

data class BannerUiModel(
    val tone: BannerTone,
    val title: String,
    val body: String,
    val action: BannerAction? = null,
)

enum class BannerTone {
    Info,
    Warning,
    Error,
}

enum class BannerAction {
    OpenHealthConnect,
    OpenPermissions,
}

sealed interface DashboardEvent {
    data object NavigateToSettings : DashboardEvent
    data object OpenHealthConnect : DashboardEvent
    data object OpenPermissions : DashboardEvent
    data object ReconfigureOnboarding : DashboardEvent
}
