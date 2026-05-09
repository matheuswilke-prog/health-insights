package com.healthinsights.core.domain.usecase

import com.healthinsights.core.domain.model.ConsentRecord
import com.healthinsights.core.domain.model.UserProfile
import com.healthinsights.core.domain.repository.ConsentRepository
import com.healthinsights.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject

class ExportLocalDataUseCase
    @Inject
    constructor(
        private val userProfileRepository: UserProfileRepository,
        private val consentRepository: ConsentRepository,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(): String {
            val profile = userProfileRepository.get().first()
            val consents = consentRepository.getAll().first()

            return buildString {
                appendLine("{")
                appendLine("  \"schemaVersion\": 1,")
                appendLine("  \"exportedAt\": \"${escape(clock.instant().toString())}\",")
                appendLine("  \"source\": \"Health Insights local export\",")
                append("  \"userProfile\": ")
                appendProfile(profile)
                appendLine(",")
                appendConsents(consents)
                appendLine()
                append("}")
            }
        }

        private fun StringBuilder.appendProfile(profile: UserProfile?) {
            if (profile == null) {
                append("null")
                return
            }

            appendLine("{")
            appendLine("    \"weightKg\": ${profile.weightKg},")
            appendLine("    \"heightCm\": ${profile.heightCm},")
            appendLine("    \"ageYears\": ${profile.ageYears},")
            appendLine("    \"sex\": \"${escape(profile.sex.name)}\",")
            appendLine("    \"goal\": \"${escape(profile.goal.name)}\",")
            appendLine("    \"dailyCalorieTarget\": ${profile.dailyCalorieTarget}")
            append("  }")
        }

        private fun StringBuilder.appendConsents(consents: List<ConsentRecord>) {
            appendLine("  \"consents\": [")
            consents.forEachIndexed { index, consent ->
                appendLine("    {")
                appendLine("      \"dataType\": \"${escape(consent.dataType)}\",")
                appendLine("      \"granted\": ${consent.granted},")
                appendLine("      \"grantedAt\": ${consent.grantedAt},")
                appendLine("      \"policyVersion\": \"${escape(consent.policyVersion)}\"")
                append("    }")
                if (index < consents.lastIndex) {
                    appendLine(",")
                } else {
                    appendLine()
                }
            }
            append("  ]")
        }

        private fun escape(value: String): String =
            buildString {
                value.forEach { char ->
                    when (char) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\b' -> append("\\b")
                        '\u000C' -> append("\\f")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> {
                            if (char.code < CONTROL_CHARACTER_LIMIT) {
                                append("\\u")
                                append(char.code.toString(radix = 16).padStart(length = 4, padChar = '0'))
                            } else {
                                append(char)
                            }
                        }
                    }
                }
            }

        private companion object {
            const val CONTROL_CHARACTER_LIMIT = 0x20
        }
    }
