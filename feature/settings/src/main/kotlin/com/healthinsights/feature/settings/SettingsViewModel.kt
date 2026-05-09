package com.healthinsights.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.usecase.DeleteLocalDataUseCase
import com.healthinsights.core.domain.usecase.ExportLocalDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    consentRepository: ConsentRepository,
    private val exportLocalData: ExportLocalDataUseCase,
    private val deleteLocalData: DeleteLocalDataUseCase,
) : ViewModel() {

    private val actionState = MutableStateFlow(SettingsActionState())
    private val mutableEvents = MutableSharedFlow<SettingsEvent>()

    val events: SharedFlow<SettingsEvent> = mutableEvents

    val uiState: StateFlow<SettingsUiState> = consentRepository.getAll()
        .map(SettingsConsentMapper::map)
        .combine(actionState) { consents, actions ->
            SettingsUiState.Content(
                SettingsUiModel(
                    consents = consents,
                    exportInProgress = actions.exportInProgress,
                    deleteInProgress = actions.deleteInProgress,
                    errorMessage = actions.errorMessage,
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    fun onExportDataClick() {
        viewModelScope.launch {
            actionState.update { it.copy(exportInProgress = true, errorMessage = null) }
            try {
                mutableEvents.emit(
                    SettingsEvent.ExportReady(
                        fileName = EXPORT_FILE_NAME,
                        content = exportLocalData(),
                    ),
                )
            } catch (_: RuntimeException) {
                actionState.update { it.copy(errorMessage = GENERIC_ERROR_MESSAGE) }
            } finally {
                actionState.update { it.copy(exportInProgress = false) }
            }
        }
    }

    fun onConfirmDeleteLocalData() {
        viewModelScope.launch {
            actionState.update { it.copy(deleteInProgress = true, errorMessage = null) }
            try {
                deleteLocalData()
                mutableEvents.emit(SettingsEvent.LocalDataDeleted)
            } catch (_: RuntimeException) {
                actionState.update { it.copy(errorMessage = GENERIC_ERROR_MESSAGE) }
            } finally {
                actionState.update { it.copy(deleteInProgress = false) }
            }
        }
    }

    private companion object {
        const val EXPORT_FILE_NAME = "health-insights-export.json"
        const val GENERIC_ERROR_MESSAGE =
            "Nao foi possivel concluir a acao agora. Tente novamente."
    }
}

private data class SettingsActionState(
    val exportInProgress: Boolean = false,
    val deleteInProgress: Boolean = false,
    val errorMessage: String? = null,
)

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
