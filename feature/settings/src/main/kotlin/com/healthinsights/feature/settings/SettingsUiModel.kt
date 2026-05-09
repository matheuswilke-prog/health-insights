package com.healthinsights.feature.settings

data class ConsentItemUiModel(
    val dataType: String,
    val title: String,
    val description: String,
    val granted: Boolean,
    val policyVersion: String?,
)

data class SettingsUiModel(
    val consents: List<ConsentItemUiModel>,
    val exportInProgress: Boolean = false,
    val deleteInProgress: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(
        val model: SettingsUiModel,
    ) : SettingsUiState
}

sealed interface SettingsEvent {
    data class ExportReady(
        val fileName: String,
        val content: String,
    ) : SettingsEvent

    data object LocalDataDeleted : SettingsEvent
}
