package com.healthinsights.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel scoped to [MainActivity].
 *
 * Responsibilities:
 * - Determine the initial navigation route from the `onboarding_complete` DataStore flag.
 * - Persist the flag when onboarding is finished.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    /**
     * Emits `null` while the DataStore is loading, then "welcome" or "dashboard".
     * The NavHost waits for a non-null value before rendering.
     */
    val startRoute: StateFlow<String?> = context.onboardingDataStore.data
        .map { prefs ->
            if (prefs[OnboardingPreferencesKeys.ONBOARDING_COMPLETE] == true) {
                ROUTE_DASHBOARD
            } else {
                ROUTE_WELCOME
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    /** Persists the onboarding_complete flag so the app starts on Dashboard next launch. */
    fun markOnboardingComplete() {
        viewModelScope.launch {
            context.onboardingDataStore.edit { prefs ->
                prefs[OnboardingPreferencesKeys.ONBOARDING_COMPLETE] = true
            }
        }
    }

    fun clearOnboardingComplete() {
        viewModelScope.launch {
            context.onboardingDataStore.edit { prefs ->
                prefs[OnboardingPreferencesKeys.ONBOARDING_COMPLETE] = false
            }
        }
    }
}

internal const val ROUTE_WELCOME = "welcome"
internal const val ROUTE_PROFILE = "profile"
internal const val ROUTE_GOAL = "goal"
internal const val ROUTE_CONSENT = "consent"
internal const val ROUTE_CONNECTING = "connecting"
internal const val ROUTE_DASHBOARD = "dashboard"
internal const val ROUTE_SETTINGS = "settings"
