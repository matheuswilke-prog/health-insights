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
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(
        val model: SettingsUiModel,
    ) : SettingsUiState
}
