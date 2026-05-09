package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import javax.inject.Inject

class DeleteLocalDataUseCase
    @Inject
    constructor(
        private val userProfileRepository: UserProfileRepository,
        private val consentRepository: ConsentRepository,
    ) {
        suspend operator fun invoke() {
            userProfileRepository.clear()
            consentRepository.clearAll()
        }
    }
