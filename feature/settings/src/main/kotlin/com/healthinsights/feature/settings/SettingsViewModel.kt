package com.healthinsights.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.repository.ConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    consentRepository: ConsentRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = consentRepository.getAll()
        .map { records ->
            SettingsUiState.Content(
                SettingsUiModel(
                    consents = SettingsConsentMapper.map(records),
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )
}

internal object SettingsConsentMapper {
    fun map(records: List<ConsentRecord>): List<ConsentItemUiModel> =
        dataTypes.map { definition ->
            val record = records.lastOrNull { it.dataType == definition.dataType }
            definition.toUiModel(record)
        }

    private fun ConsentDefinition.toUiModel(record: ConsentRecord?): ConsentItemUiModel =
        ConsentItemUiModel(
            dataType = dataType,
            title = title,
            description = description,
            granted = record?.granted == true,
            policyVersion = record?.policyVersion,
        )

    private val dataTypes = listOf(
        ConsentDefinition(
            dataType = "calories",
            title = "Calorias",
            description = "Gasto ativo e ingestão calórica do Health Connect.",
        ),
        ConsentDefinition(
            dataType = "weight",
            title = "Peso",
            description = "Peso corporal e data da medição.",
        ),
        ConsentDefinition(
            dataType = "exercise",
            title = "Treinos",
            description = "Tipo de atividade e duração, sem GPS ou rota.",
        ),
    )

    private data class ConsentDefinition(
        val dataType: String,
        val title: String,
        val description: String,
    )
}
