package com.healthinsights.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** DataStore for non-sensitive onboarding flags. */
val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "onboarding",
)

object OnboardingPreferencesKeys {
    /** True once the user has completed the full onboarding flow. */
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
