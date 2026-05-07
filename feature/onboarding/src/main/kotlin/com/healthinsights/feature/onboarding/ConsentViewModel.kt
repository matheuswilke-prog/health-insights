package com.healthinsights.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.model.UserGoal
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import com.healthinsights.core.domain.usecase.CalculateDailyTargetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Policy version tied to consent-copy-v1.1.md — bump on any material change to consent copy. */
private const val POLICY_VERSION = "consent-copy-v1.1"

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val consentRepository: ConsentRepository,
    private val calculateDailyTarget: CalculateDailyTargetUseCase,
) : ViewModel() {

    /**
     * Persists [profile] + [goal] as [UserProfile] and one [ConsentRecord] per
     * granted data type. Called only after the user taps "Concordo".
     *
     * LGPD requirement: [ConsentRecord] must exist before any health data is read.
     * This is the single persistence moment for all onboarding data collected in
     * ProfileScreen (S1.1) and GoalScreen (S1.2).
     *
     * @param grantedTypes Set of data-type identifiers the user toggled ON.
     * @param onComplete Invoked on the main thread after successful persistence.
     */
    fun confirmConsent(
        profileData: ProfileFormData,
        goal: UserGoal,
        grantedTypes: Set<ConsentDataType>,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            val profileWithTarget = buildProfile(profileData, goal)
            userProfileRepository.save(profileWithTarget)

            val grantedAt = System.currentTimeMillis()
            ConsentDataType.entries.forEach { dataType ->
                consentRepository.save(
                    ConsentRecord(
                        dataType = dataType.key,
                        granted = dataType in grantedTypes,
                        grantedAt = grantedAt,
                        policyVersion = POLICY_VERSION,
                    ),
                )
            }

            onComplete()
        }
    }

    private fun buildProfile(data: ProfileFormData, goal: UserGoal): UserProfile {
        val partial = UserProfile(
            weightKg = data.weightKg,
            heightCm = data.heightCm,
            ageYears = data.ageYears,
            sex = data.sex,
            goal = goal,
            dailyCalorieTarget = 0,
        )
        return partial.copy(dailyCalorieTarget = calculateDailyTarget(partial))
    }
}

/** Canonical data-type identifiers stored in [ConsentRecord.dataType]. */
enum class ConsentDataType(val key: String) {
    CALORIES("calories"),
    WEIGHT("weight"),
    EXERCISE("exercise"),
}
