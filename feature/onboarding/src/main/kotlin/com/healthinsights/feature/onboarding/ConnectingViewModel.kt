package com.healthinsights.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthinsights.core.domain.healthconnect.HealthConnectAvailability
import com.healthinsights.core.domain.healthconnect.HealthDataPermission
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.feature.healthconnect.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// UiState
// ─────────────────────────────────────────────────────────────────────────────

sealed interface ConnectingUiState {
    /** Reading DataStore + checking HC availability. */
    data object Checking : ConnectingUiState

    /** HC app is not installed on the device. */
    data object NotAvailable : ConnectingUiState

    /** HC app is installed but requires an update. */
    data object UpdateRequired : ConnectingUiState

    /**
     * HC is available and we have permissions to request.
     * The screen should auto-launch the permission dialog.
     */
    data object ReadyToRequest : ConnectingUiState

    /**
     * Permission flow completed.
     *
     * @param grantedCount how many individual HC permission strings were granted.
     * @param totalRequested how many we requested (0 means user consented to nothing).
     */
    data class PermissionResult(
        val grantedCount: Int,
        val totalRequested: Int,
    ) : ConnectingUiState
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ConnectingViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val consentRepository: ConsentRepository,
) : ViewModel() {

    /** ActivityResultContract exposed so the composable can call rememberLauncherForActivityResult. */
    val permissionContract = healthConnectManager.createPermissionRequestContract()

    private val _uiState = MutableStateFlow<ConnectingUiState>(ConnectingUiState.Checking)
    val uiState: StateFlow<ConnectingUiState> = _uiState.asStateFlow()

    private val _permissionsToRequest = MutableStateFlow<Set<String>>(emptySet())
    val permissionsToRequest: StateFlow<Set<String>> = _permissionsToRequest.asStateFlow()

    init {
        checkAvailability()
    }

    /** Checks HC availability and, if available, loads the permissions to request. */
    fun checkAvailability() {
        viewModelScope.launch {
            _uiState.value = ConnectingUiState.Checking
            when (healthConnectManager.availability) {
                HealthConnectAvailability.Available -> preparePermissions()
                HealthConnectAvailability.UpdateRequired ->
                    _uiState.value = ConnectingUiState.UpdateRequired
                HealthConnectAvailability.NotAvailable ->
                    _uiState.value = ConnectingUiState.NotAvailable
            }
        }
    }

    /**
     * Called when the permission dialog returns.
     *
     * @param granted the set of HC permission strings that were actually granted.
     */
    fun onPermissionResult(granted: Set<String>) {
        viewModelScope.launch {
            val requested = _permissionsToRequest.value
            val effectiveGranted = runCatching {
                healthConnectManager.getGrantedPermissionStrings()
            }.getOrDefault(granted)
            val grantedRequested = effectiveGranted.intersect(requested)
            _uiState.value = ConnectingUiState.PermissionResult(
                grantedCount = grantedRequested.size,
                totalRequested = requested.size,
            )
        }
    }

    /** Puts the state back to ReadyToRequest so the dialog can be re-launched. */
    fun retryPermissions() {
        _uiState.value = ConnectingUiState.ReadyToRequest
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun preparePermissions() {
        val permissions = consentRepository.getAll()
            .first()
            .filter { it.granted }
            .flatMap { record -> consentKeyToPermissions(record.dataType) }
            .toSet()
        _permissionsToRequest.value = permissions
        _uiState.value = if (permissions.isEmpty()) {
            // Nothing consented — skip HC dialog, proceed directly to dashboard
            ConnectingUiState.PermissionResult(grantedCount = 0, totalRequested = 0)
        } else {
            ConnectingUiState.ReadyToRequest
        }
    }

    companion object {
        /**
         * Maps a ConsentRecord.dataType string ("calories", "weight", "exercise")
         * to the corresponding HC permission strings.
         * Keeps HC SDK types inside :feature:health-connect via HealthConnectManager.
         */
        internal fun consentKeyToPermissions(key: String): Set<String> {
            val hdPermissions: Set<HealthDataPermission> = when (key) {
                "calories" -> setOf(
                    HealthDataPermission.CALORIES_BURNED,
                    HealthDataPermission.CALORIE_INTAKE,
                )
                "weight" -> setOf(HealthDataPermission.WEIGHT)
                "exercise" -> setOf(HealthDataPermission.EXERCISE)
                else -> emptySet()
            }
            return hdPermissions
                .flatMap { HealthConnectManager.permissionStringsFor(it) }
                .toSet()
        }
    }
}
